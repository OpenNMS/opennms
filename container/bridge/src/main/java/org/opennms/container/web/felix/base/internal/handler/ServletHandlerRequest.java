/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web.felix.base.internal.handler;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.osgi.service.http.HttpContext;

final class ServletHandlerRequest extends HttpServletRequestWrapper {
    private final String m_alias;

    private String m_contextPath;
    private String m_servletPath;
    private String m_pathInfo;

    private boolean m_pathInfoCalculated = false;

    public ServletHandlerRequest(final HttpServletRequest req, final String alias) {
        super(req);

        m_alias = alias;

        updatePathInfo();
    }

    @Override
    public String getContextPath() {
        updatePathInfo();
        return m_contextPath;
    }

    @Override
    public String getServletPath() {
        updatePathInfo();
        return m_servletPath;
    }

    @Override
    public String getPathInfo() {
        updatePathInfo();
        return m_pathInfo;
    }

    @Override
    public String getPathTranslated() {
        final String info = getPathInfo();
        if (info == null) return null;
        
        return getRealPath(info);
    }

    @Override
    public String getAuthType() {
        String authType = (String) getAttribute(HttpContext.AUTHENTICATION_TYPE);

        if (authType == null) {
            authType = super.getAuthType();
        }

        return authType;
    }

    @Override
    public String getRemoteUser() {
        String remoteUser = (String) getAttribute(HttpContext.REMOTE_USER);

        if (remoteUser == null) {
            remoteUser = super.getRemoteUser();
        }

        return remoteUser;
    }

    @Override
    public void setRequest(ServletRequest request) {
        super.setRequest(request);
        m_pathInfoCalculated = false;
    }

    private void updatePathInfo() {
        if (m_pathInfoCalculated) return;

        final HttpServletRequest req = (HttpServletRequest)this.getRequest();

        final String requestContextPath = req.getContextPath();
        final String requestServletPath = req.getServletPath();
        final String requestPathInfo    = req.getPathInfo();

        m_servletPath = m_alias;
        m_contextPath = requestContextPath;
        if ("/".equals(m_servletPath)) {
            m_servletPath = "";
        }

        if (requestPathInfo == null) {
            if (!"/".equals(m_alias) && requestServletPath.startsWith(m_alias)) {
                m_pathInfo = requestServletPath.substring(m_alias.length());
            }
        } else {
            if (!"/".equals(m_alias) && requestPathInfo.startsWith(m_alias)) {
                m_pathInfo = requestPathInfo.substring(m_alias.length());
            }
        }

        // ensure empty string is coerced to null
        if (m_pathInfo != null && m_pathInfo.length() == 0) {
            m_pathInfo = null;
        }

        m_pathInfoCalculated = true;
    }
}
