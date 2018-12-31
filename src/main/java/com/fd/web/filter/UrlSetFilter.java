package com.fd.web.filter;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class UrlSetFilter
 */
@WebFilter("/initcontextpathFilter")
public class UrlSetFilter implements Filter {

	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Default constructor.
	 */
	public UrlSetFilter() {
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		String contextPath = fConfig.getServletContext().getContextPath();
		log.info("contextPath----------------:{}", contextPath);
		fConfig.getServletContext().setAttribute("MYCONTEXTPATH", contextPath);
	}

}
