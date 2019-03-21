package com.fd.ws;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fd.cfg.RestServerConfigurator;
import com.fd.listener.WsServerListener;
import com.fd.microSevice.code.RestCode;
import com.fd.microSevice.helper.ApiInfo;
import com.fd.microSevice.helper.ClientApi;
import com.fd.microSevice.helper.ClientInfo;
import com.fd.microSevice.helper.CoordinateUtil;
import com.fd.microSevice.helper.HttpApiInfo;
import com.fd.microSevice.helper.ReqInfo;

/**
 * 分布式协调服务
 * 
 * @author 符冬
 *
 */
@ServerEndpoint(value = "/restcoordinate", configurator = RestServerConfigurator.class, decoders = {
		RestCode.class }, encoders = { RestCode.class })
public class RestServer {
	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private ReqInfo reqInfo;

	@OnOpen
	public void open(Session session, EndpointConfig config) {
		synchronized (CoordinateUtil.CLIENTS) {
			this.reqInfo = (ReqInfo) config.getUserProperties().get(WsServerListener.REQ_INFO);
			for (ClientInfo ci : CoordinateUtil.CLIENTS) {
				if (ci.getSession() != session && !ci.getSession().getId().equals(session.getId())
						&& session.isOpen()) {
					try {
						session.getBasicRemote().sendObject(ci.getClientApi());
					} catch (IOException | EncodeException e) {
						e.printStackTrace();
					}
				}
			}
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (session.isOpen()) {
							if (isAlive) {
								isAlive = false;
								session.getBasicRemote().sendPing(ByteBuffer.wrap(pings));
							} else {
								cancel();
								if (session.isOpen()) {
									log.error("关闭half-open连接");
									session.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "已经超时，请重新连接。"));
								}
							}
						} else {
							cancel();
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error("heartbeat", e);
					}
				}
			}, 50000, 50000);
		}
	}

	@OnError
	public void error(Throwable e, Session session) {

		try {
			if (session.isOpen()) {
				session.close();
			}
			HttpApiInfo ha = CoordinateUtil.getHttpApiInfo(session);
			if (ha != null) {
				log.error(String.format("客户端%s出错", ha.getBaseUrl()), e);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error("严重了。。。。。。。。。。。。。。", e);
		}
	}

	@OnClose
	public void close(Session session, CloseReason cr) {
		log.info("{},{},open={}", cr.getCloseCode(), cr.getReasonPhrase(), session.isOpen());
		synchronized (CoordinateUtil.CLIENTS) {
			HttpApiInfo ha = CoordinateUtil.getHttpApiInfo(session);
			if (ha != null) {
				log.error(String.format("客户端%s关闭连接..", ha.getBaseUrl()));
				if (!cr.getCloseCode().equals(CloseCodes.UNEXPECTED_CONDITION)
						&& !cr.getCloseCode().equals(CloseCodes.GOING_AWAY)) {
					Iterator<ClientInfo> ite = CoordinateUtil.CLIENTS.iterator();
					while (ite.hasNext()) {
						ClientInfo disapicl = ite.next();
						if (disapicl.getSession().getId().equals(session.getId())) {
							ite.remove();

							ClientApi clientApi = disapicl.getClientApi();
							clientApi.getHttpApiInfo().setIsOnline(false);
							sendapi(clientApi, session);
							log.error(String.format("%s客户端销毁成功..", session.getId()));
							log.error("还剩客户端总数量:{}", CoordinateUtil.CLIENTS.size());
						}
					}
				}
			} else {
				log.error("CoordinateUtil.CLIENTS:" + CoordinateUtil.CLIENTS.size());
			}
		}
	}

	@OnMessage
	public void handlerData(ClientApi api, Session session) {
		synchronized (CoordinateUtil.CLIENTS) {
			if (api.getSync()) {
				for (ApiInfo ai : api.getApis()) {
					ClientApi ca = CoordinateUtil.getClientApiByApiInfo(ai.getName(), ai.getMethod());
					if (ca != null && session.isOpen()) {
						try {
							session.getBasicRemote().sendObject(ca);
							log.info("主动同步成功。。。。:{}", ca);
						} catch (IOException | EncodeException e) {
							e.printStackTrace();
						}
					}
				}

			} else if (api.getHttpApiInfo() != null) {
				if (api.getHttpApiInfo().getContextPath() != null) {
					log.info(String.format("新增前，客户端总数量：%s", CoordinateUtil.CLIENTS.size()));
					if (api.getHttpApiInfo().getHost() == null || api.getHttpApiInfo().getHost().trim().length() < 4) {
						api.getHttpApiInfo().setHost(reqInfo.getRemoteAddr());
					}
					ClientInfo curClient = new ClientInfo(api, session);
					if (api.getHttpApiInfo().getIsOnline()) {
						CoordinateUtil.CLIENTS.add(curClient);
						log.info(String.format("服务器%s上线", api.getHttpApiInfo().getBaseUrl()));
					} else {
						log.info(String.format("服务器%s下线", api.getHttpApiInfo().getBaseUrl()));
						if (curClient.getSession().isOpen()) {
							try {
								curClient.getSession().close();
							} catch (Exception e) {
								e.printStackTrace();
								log.error("close:", e);
							}
						}
						CoordinateUtil.CLIENTS.remove(curClient);
					}
					sendapi(api, session);
					log.info(String.format("添加完毕，当前客户端总数量：%s", CoordinateUtil.CLIENTS.size()));
				} else {
					log.error(String.format("%s ContextPath is  null  ", api.getHttpApiInfo().getHost()));
				}
			} else {
				log.error("非法请求........................................");
			}
		}
	}

	private void sendapi(ClientApi api, Session session) {
		CoordinateUtil.CLIENTS.parallelStream().forEach(ci -> {
			Session se = ci.getSession();
			if (!se.getId().equals(session.getId()) && se.isOpen()) {
				try {
					HttpApiInfo ha = CoordinateUtil.getHttpApiInfo(se);
					if (ha != null) {
						log.info(String.format("发送给%s", ha.getBaseUrl()));
						se.getBasicRemote().sendObject(api);
					}
				} catch (Exception e) {
					log.error(String.format("出现 错误%s", se.getId()), e);
					e.printStackTrace();
				}
			}

		});

	}

	private static Timer timer = new Timer(true);
	// 判断连接是否有效
	private volatile boolean isAlive = true;

	@OnMessage
	public void onPong(PongMessage pm) {
		if (Arrays.equals(pm.getApplicationData().array(), pings)) {
			isAlive = true;
			log.info("{}心跳成功...", reqInfo.getRemoteAddr());
		} else {
			log.info("ping已断开连接");
			isAlive = false;
		}
	}

	protected byte[] pings = "1".getBytes(StandardCharsets.UTF_8);
}
