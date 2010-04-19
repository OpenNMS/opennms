package org.opennms.features.poller.remote.gwt;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CacheFilter implements Filter {
	private static final long ONE_DAY = 1000 * 60 * 60 * 24;

	public void init(final FilterConfig config) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
		if (!(req instanceof HttpServletRequest)) {
			chain.doFilter(req, resp);
			return;
		}
		if (!(resp instanceof HttpServletResponse)) {
			chain.doFilter(req, resp);
			return;
		}
		final HttpServletRequest request = (HttpServletRequest)req;
		final HttpServletResponse response = (HttpServletResponse)resp;
		
		final String requestURI = request.getRequestURI();
		if (requestURI.endsWith(".png") || requestURI.endsWith(".css")) {
			final long today = new Date().getTime();
			response.setDateHeader("Expires", today + ONE_DAY);
			chain.doFilter(request, response);
		} else {
			chain.doFilter(req, resp);
		}
	}

}
