//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 Blast Internet Services, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 * 
 */
@Distributable
public class HttpMonitor2 extends IPv4Monitor {

    protected static final String DEFAULT_VERSION = "1.1";
    private static final int DEFAULT_PORT = 80;
    private static final String DEFAULT_URL = "/";
    private static final String DEFAULT_TIMEOUT = "3000";
    private static final String DEFAULT_VIRTUAL_HOST = null;
    private static final int DEFAULT_RETRY = 0;
    private static final String DEFAULT_METHOD = "GET";
    private static final String DEFAULT_SCHEME = "http";
    private static final String DEFAULT_USER_INFO = null;
    private static final String DEFAULT_PATH = "/";
    private static final String DEFAULT_QUERY = null;
    private static final String DEFAULT_FRAGMENT = null;
    private static final boolean DEFAULT_FORM_BASED_LOGIN = false;
    private static final String DEFAULT_FORM_LOGIN_PATH = "/";
    
    

    public PollStatus poll(MonitoredService svc, Map parameters) {
    	
    	
        HttpClient client = null;
        HttpMethod method = null;
        
        try {
            client = new HttpClient(buildParams(svc, parameters));
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            if (ParameterMap.getKeyedBoolean(parameters, "form-based-login", DEFAULT_FORM_BASED_LOGIN)) {
                login(client, parameters);
            }

            method = buildHttpMethod(svc, parameters);
            
            //going to need this
            HttpState state = client.getState();
            client.executeMethod(method);
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
        
//        return PollStatus.get(serviceStatus, reason, responseTime);
        return null;
    }

    void login(HttpClient client, Map<String, String> map) {
        String formLoginPath = ParameterMap.getKeyedString(map, "login-form-cookie-path", DEFAULT_FORM_LOGIN_PATH);
        
        GetMethod authGet = new GetMethod(formLoginPath);
        try {
            client.executeMethod(authGet);
        } catch (Exception e) {
            throw new HttpMonitorException("problem processing from login", new RuntimeException(e));
        } finally {
            if (authGet != null) authGet.releaseConnection();
        }
        
        String cookieSite = ParameterMap.getKeyedString(map, "login-form-cookie-site", client.getHostConfiguration().getHost());
        String cookiePath = ParameterMap.getKeyedString(map, "login-form-cookie-path", "/");
        int cookiePort = client.getHostConfiguration().getPort();
        
        CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] initcookies = cookiespec.match(cookieSite, cookiePort, "/", false, client.getState().getCookies());
        
        
        //FIXME: Come up with way to define form parameters as value pairs
        
        PostMethod authPost = new PostMethod(formLoginPath);
//        NameValuePair[]valuePairs = new NameValuePair[7];
        List<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
        String action = ParameterMap.getKeyedString(map, "login-form-action", "login");
        valuePairs.add(new NameValuePair("action", action));
        
        String url = ParameterMap.getKeyedString(map, "login-form-path", "/index.html");
        valuePairs.add(new NameValuePair("url", url));
        
        String userId = ParameterMap.getKeyedString(map, "login-form-user", "user");
        valuePairs.add(new NameValuePair("userid", userId));
        
        String password = ParameterMap.getKeyedString(map, "login-form-password", "password");
        valuePairs.add(new NameValuePair("password", password));
        authPost.setRequestBody((NameValuePair[])valuePairs.toArray());
        
        String versionString = ParameterMap.getKeyedString(map, "login-form-httpVersion", "1.1");
        authPost.getParams().setVersion(new HttpVersion(determineMajorVersion(versionString), determineMinorVersion(versionString)));
        
        //somethings to consider in configuration
        authPost.setFollowRedirects(true);
        
    }


    private HttpMethod buildHttpMethod(MonitoredService svc, Map parameters) {
        HttpMethod method;
        String methodString = determineMethod(parameters);
        if ("GET".equals(methodString)) {
            method = new GetMethod();
        } else {
            method = new PostMethod();
        }
        try {
            method.setURI(buildUri(svc, parameters));
        } catch (URIException e) {
            throw new HttpMonitorException("Error creating URI", new RuntimeException(e));
        }
        return method;
    }

    private URI buildUri(MonitoredService svc, Map map) throws URIException {
        String scheme;
        String userInfo;
        String host;
        int port;
        String path;
        String query;
        String fragment;
        
        scheme = ParameterMap.getKeyedString(map, "scheme", DEFAULT_SCHEME);
        userInfo = ParameterMap.getKeyedString(map, "user-info", DEFAULT_USER_INFO);
        host = svc.getIpAddr();
        port = ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT);
        path = ParameterMap.getKeyedString(map, "path", DEFAULT_PATH);
        query = ParameterMap.getKeyedString(map, "query", DEFAULT_QUERY);
        fragment = ParameterMap.getKeyedString(map, "fragment", DEFAULT_FRAGMENT);
        return new URI(scheme, userInfo, host, port, path, query, fragment);
    }

    private String determineMethod(Map parameters) {
        String method = ParameterMap.getKeyedString(parameters, "method", DEFAULT_METHOD);
        if (isValidMethod(method)) {
            return method;
        } else {
            throw new HttpMonitorException("Invalid method specified: ["+method+"]", new RuntimeException());
        }
    }

    private boolean isValidMethod(String method) {
        return ("GET".equals(method) || "POST".equals(method));
    }

    private HttpClientParams buildParams(MonitoredService svc, Map<String, String> parameters) {
        HttpClientParams params = new HttpClientParams();
        params.setVersion(computeVersion(parameters));
        params.setSoTimeout(Integer.parseInt(ParameterMap.getKeyedString(parameters, "timeout", DEFAULT_TIMEOUT)));
        params.setVirtualHost(ParameterMap.getKeyedString(parameters, "virtual-host", DEFAULT_VIRTUAL_HOST));
        int retryCount = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));
        return params;
    }

    private HttpVersion computeVersion(Map<String, String> parameters) {
        String versionString = determineVersion(parameters);
        return new HttpVersion(determineMajorVersion(versionString), determineMinorVersion(versionString));
    }

    public int determineMinorVersion(String versionString) {
        if (isValidVersion(versionString)) {
            return Integer.parseInt(versionString.substring(3));
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("Incorrect version specified: [");
            sb.append(versionString);
            sb.append("]");
            throw new HttpMonitorException(sb.toString(), new IllegalArgumentException("Bad HTTP version paramenter: "+versionString));
        }
    }
    
    public int determineMajorVersion(String versionString) {
        if (isValidVersion(versionString)) {
            return Integer.parseInt(versionString.substring(1, 1));
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("Incorrect version specified: [");
            sb.append(versionString);
            sb.append("]");
            throw new HttpMonitorException(sb.toString(), new IllegalArgumentException("Bad HTTP version paramenter: "+versionString));
        }
    }

    public String determineVersion(Map<String, String> parameters) {
        String version = ParameterMap.getKeyedString(parameters, "version", DEFAULT_VERSION);
        if (isValidVersion(version)) {
            return version;
        } else {
            throw new HttpMonitorException("Incorrect version specified: ("+version+")", new IllegalArgumentException("Bad HTTP version paramenter: "+version));
        }
    }
    
    public boolean isValidVersion(String version) {
        return ("1.0".equals(version) || "1.1".equals(version));
    }


    class HttpMonitorException extends RuntimeException {
        private static final long serialVersionUID = -2453812259707223488L;
        private String m_msg;
        protected HttpMonitorException(String msg, RuntimeException e) {
            m_msg = msg;
        }

        @Override
        public String getMessage() {
            StringBuffer sb = new StringBuffer();
            sb.append(m_msg);
            sb.append(": ");
            sb.append(super.getMessage());
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(m_msg);
            sb.append(": ");
            sb.append(super.toString());
            return sb.toString();
        }
        
    }
    

}

