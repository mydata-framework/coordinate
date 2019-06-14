package com.fd.listener;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fd.microSevice.helper.ReqInfo;

/**
 * 获取HTTP请求客户端数据
 * 
 * @author 符冬
 *
 */
@WebListener
public class WsServerListener implements ServletRequestListener {
	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public final static String REQ_INFO = "REQ_INFO";

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
		req.getSession().removeAttribute(REQ_INFO);
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
		ReqInfo reqinfo = new ReqInfo(getUserIp((HttpServletRequest) sre.getServletRequest()));
		req.getSession().setAttribute(REQ_INFO, reqinfo);
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}

		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		if (obj instanceof CharSequence) {
			return ((CharSequence) obj).length() == 0;
		}
		if (obj instanceof Collection) {
			return ((Collection) obj).isEmpty();
		}
		if (obj instanceof Map) {
			return ((Map) obj).isEmpty();
		}

		return false;
	}

	/**
	 * 判断对象是否不为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	public static String getloopBackAddress() {
		try {
			Enumeration<NetworkInterface> ccs = NetworkInterface.getNetworkInterfaces();
			while (ccs.hasMoreElements()) {
				NetworkInterface cc = ccs.nextElement();
				if (cc.isLoopback()&&cc.isUp()) {
					Enumeration<InetAddress> sss = cc.getInetAddresses();
					while (sss.hasMoreElements()) {
						InetAddress ia = sss.nextElement();
						log.info("服务注册来源地:{}", ia.getHostAddress());
						if (isIpv4(ia)) {
							return ia.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static boolean isIpv4(InetAddress ia) {
		return ia instanceof Inet4Address;
	}

	public static String getLanAddress() {
		try {
			Enumeration<NetworkInterface> ccs = NetworkInterface.getNetworkInterfaces();
			while (ccs.hasMoreElements()) {
				NetworkInterface cc = ccs.nextElement();
				if (!cc.isLoopback()&&!cc.isVirtual()&&cc.isUp()) {
					Enumeration<InetAddress> sss = cc.getInetAddresses();
					while (sss.hasMoreElements()) {
						InetAddress ia = sss.nextElement();
						if (isIpv4(ia)) {
							return ia.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 得到用户的IP地址
	 * 
	 * @param req
	 * @return
	 */
	public static String getUserIp(HttpServletRequest req) {
		String clientIp = req.getHeader("X-Real-IP");
		if (isEmpty(clientIp)) {
			clientIp = req.getHeader("X-Forwarded-For");
			if (isNotEmpty(clientIp)) {
				return clientIp;
			} else {
				return getrmip(req);
			}
		} else {
			return clientIp;
		}

	}

	private static String getrmip(HttpServletRequest req) {
		String remoteAddr = req.getRemoteAddr();
		if (remoteAddr.equals(getloopBackAddress())) {
			return getLanAddress();
		} else {
			return remoteAddr;
		}
	}
}
