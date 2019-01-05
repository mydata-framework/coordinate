package com.fd.web.controller;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fd.microSevice.helper.ApiInfo;
import com.fd.microSevice.helper.ClientInfo;
import com.fd.microSevice.helper.CoordinateUtil;
import com.fd.web.controller.base.BaseController;
import com.fd.web.controller.vo.ApiInfoVo;

@Controller
@RequestMapping("/home")
public class HomeController extends BaseController {
	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@RequestMapping
	public String index(HttpServletRequest req) {
		return "index";
	}

	/**
	 * 主机列表
	 * 
	 * @return
	 */
	@RequestMapping("/hostlist")
	public String hostlist(HttpServletRequest req, String sname) {
		List<ApiInfoVo> aivs = new ArrayList<>();
		for (ClientInfo c : CoordinateUtil.CLIENTS) {
			int count = Long.valueOf(c.getClientApi().getApis().stream().map(ApiInfo::getName).distinct().count())
					.intValue();
			if (c.getClientApi().getApis().size() > 0) {
				a: for (ApiInfo api : c.getClientApi().getApis()) {
					ApiInfoVo av = new ApiInfoVo(c.getClientApi().getHttpApiInfo().getHost(),
							c.getClientApi().getHttpApiInfo().getPort(), count, api.getName(),
							api.getMethod().toString(), c.getClientApi().getHttpApiInfo().getContextPath());
					for (ApiInfoVo a : aivs) {
						if (a.equals(av)) {
							a.setMethods(a.getMethods() + "," + av.getMethods());
							continue a;
						}
					}
					aivs.add(av);
				}
			} else {
				ApiInfoVo av = new ApiInfoVo(c.getClientApi().getHttpApiInfo().getHost(),
						c.getClientApi().getHttpApiInfo().getPort(), count, "", "",
						c.getClientApi().getHttpApiInfo().getContextPath());
				aivs.add(av);
			}
		}
		log.info("name:{}", sname);
		if (sname != null && sname.trim().length() > 1) {
			List<ApiInfoVo> collect = aivs.stream().filter(a -> a.getName().contains(sname.trim()))
					.collect(Collectors.toList());
			log.info("name:{},cosize:{}", sname, collect.size());
			req.setAttribute("cas", collect);
			req.setAttribute("sname", sname);
		} else {
			req.setAttribute("cas", aivs);
		}
		return "host";
	}

}
