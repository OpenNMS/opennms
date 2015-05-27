package org.opennms.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class OriginHeaderFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest req = (HttpServletRequest)request;
            final String header = req.getHeader("Origin");
            if (header != null && header.startsWith("file://")) {
                /* 
                 * file://* is technically an invalid Origin: for CORS, but it appears Cordova
                 * sometimes sends it so we need to filter it out.
                 */
                final List<String> headerNames = new ArrayList<>(Collections.list(req.getHeaderNames()));
                headerNames.remove("Origin");
                final HttpServletRequestWrapper newReq = new HttpServletRequestWrapper(req) {
                    @Override public Enumeration<String> getHeaderNames() {
                        return Collections.enumeration(headerNames);
                    }

                    @Override public Enumeration<String> getHeaders(final String name) {
                        if ("origin".equalsIgnoreCase(name)) {
                            return Collections.emptyEnumeration();
                        } else {
                            return super.getHeaders(name);
                        }
                    }

                    @Override public String getHeader(final String name) {
                        if ("origin".equalsIgnoreCase(name)) {
                            return null;
                        } else {
                            return super.getHeader(name);
                        }
                    }
                };
                chain.doFilter(newReq, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
