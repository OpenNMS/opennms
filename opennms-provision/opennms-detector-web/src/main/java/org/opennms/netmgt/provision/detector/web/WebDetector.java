/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.web;

import org.opennms.netmgt.provision.detector.web.client.WebClient;
import org.opennms.netmgt.provision.detector.web.request.WebRequest;
import org.opennms.netmgt.provision.detector.web.response.WebResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;


/**
 * <p>WebDetector class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 */

public class WebDetector extends BasicDetector<WebRequest, WebResponse> {

    private static final String DEFAULT_SERVICE_NAME = "WEB";

    private static final int DEFAULT_PORT = 80;

    private String path = "/";

    private String userAgent = "OpenNMS WebMonitor";

    private String virtualHost;

    private boolean useHttpV1 = false;

    private String headers;

    private boolean authEnabled = false;

    private boolean authPreemtive = true;

    private String authUser = "admin";

    private String authPassword = "admin";

    private String responseText;

    private String responseRange = "100-399";
    
    private String schema = "http";

    private String queryString;

    private boolean useSSLFilter = false;

    /**
     * Default constructor
     */
    public WebDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public WebDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    @Override
    protected void onInit() {
        send(getRequest(), getWebValidator());
    }

    private WebRequest getRequest() {
        WebRequest request =  new WebRequest();
        request.parseHeaders(getHeaders());
        request.setResponseRange(getResponseRange());
        request.setResponseText(getResponseText());
        return request;
    }

    private static ResponseValidator<WebResponse> getWebValidator() {
        return new ResponseValidator<WebResponse>() {
            @Override
            public boolean validate(final WebResponse pack) {
                return pack.isValid();
            }
        };
    }

    @Override
    protected Client<WebRequest, WebResponse> getClient() {
        final WebClient client = new WebClient(isUseSSLFilter());

        client.setPath(getPath());
        client.setSchema(getSchema());
        client.setUserAgent(getUserAgent());
        client.setVirtualHost(getVirtualHost());
        client.setQueryString(getQueryString());
        client.setUseHttpV1(isUseHttpV1());
        if (isAuthEnabled()) {
            client.setAuth(getAuthUser(), getAuthPassword());
            client.setAuthPreemtive(isAuthPreemtive());
        }
        return client;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public boolean isUseSSLFilter() {
        return useSSLFilter;
    }

    public void setUseSSLFilter(boolean useSSLFilter) {
        this.useSSLFilter = useSSLFilter;
    }

    public boolean isUseHttpV1() {
        return useHttpV1;
    }

    public void setUseHttpV1(boolean useHttpV1) {
        this.useHttpV1 = useHttpV1;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public boolean isAuthPreemtive() {
        return authPreemtive;
    }

    public void setAuthPreemtive(boolean authPreemtive) {
        this.authPreemtive = authPreemtive;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseRange() {
        return responseRange;
    }

    public void setResponseRange(String responseRange) {
        this.responseRange = responseRange;
    }

}
