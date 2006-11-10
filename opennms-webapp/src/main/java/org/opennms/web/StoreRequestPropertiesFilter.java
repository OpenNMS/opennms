package org.opennms.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class StoreRequestPropertiesFilter implements Filter {

    private String m_servletPathAttribute;
    private String m_relativeServletPathAttribute;

    public void init(FilterConfig config) throws ServletException {
        m_servletPathAttribute = config.getInitParameter("servletPathAttribute");
        m_relativeServletPathAttribute = config.getInitParameter("relativeServletPathAttribute");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (m_servletPathAttribute != null) {
            request.setAttribute(m_servletPathAttribute,
                                 httpRequest.getServletPath());
        }
        if (m_relativeServletPathAttribute != null) {
            String servletPath = httpRequest.getServletPath();
            if (servletPath != null && servletPath.length() > 0 && servletPath.charAt(0) == '/') {
                servletPath = servletPath.substring(1);
            }
            request.setAttribute(m_relativeServletPathAttribute,
                                 servletPath);
        }
        
        chain.doFilter(request, response);
    }

    public void destroy() {
        // Nothing to destroy that a GC won't take care of. :-)
    }

}
