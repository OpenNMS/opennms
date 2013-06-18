/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.northbounder.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.EmptyKeyRelaxedTrustSSLContext;
import org.opennms.core.utils.HttpResponseRange;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.http.HttpNorthbounderConfig.HttpMethod;

/**
 * Forwards north bound alarms via HTTP.
 * FIXME: Needs lots of work still :(
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
public class HttpNorthbounder extends AbstractNorthbounder {
    private HttpNorthbounderConfig m_config;

    protected HttpNorthbounder() {
        super("HttpNorthbounder");
    }

    //FIXME: This should be wired with Spring but is implmented as was in the PSM
    // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
    // is available to JSSE
    static {
        
        //this is a safe call because the method returns -1 if it is already installed (by PageSequenceMonitor, etc.)
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }
    

    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (m_config.getAcceptableUeis() == null || m_config.getAcceptableUeis().contains(alarm.getUei())) {
            return true;
        }
        return false;
    }

    
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        
        LogUtils.infof(this, "Forwarding %i alarms", alarms.size());
        
        //Need a configuration bean for these
        
        int connectionTimeout = 3000;
        int socketTimeout = 3000;
        Integer retryCount = Integer.valueOf(3);
        
        HttpVersion httpVersion = determineHttpVersion(m_config.getHttpVersion());        
        String policy = CookiePolicy.BROWSER_COMPATIBILITY;
        
        URI uri = m_config.getURI();
        
        DefaultHttpClient client = new DefaultHttpClient(buildParams(httpVersion, connectionTimeout,
                socketTimeout, policy, m_config.getVirtualHost()));
        
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
        
        if ("https".equals(uri.getScheme())) {
            final SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();
            final Scheme https = registry.getScheme("https");

            // Override the trust validation with a lenient implementation
            SSLSocketFactory factory = null;
            
            try {
                factory = new SSLSocketFactory(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (Throwable e) {
                throw new NorthbounderException(e);
            }

            final Scheme lenient = new Scheme(https.getName(), https.getDefaultPort(), factory);
            // This will replace the existing "https" schema
            registry.register(lenient);
        }
        
        HttpUriRequest method = null;
        
        if (HttpMethod.POST == (m_config.getMethod())) {
            HttpPost postMethod = new HttpPost(uri);
            
            //TODO: need to configure these
            List<NameValuePair> postParms = new ArrayList<NameValuePair>();
            
            //FIXME:do this for now
            NameValuePair p = new BasicNameValuePair("foo", "bar");
            postParms.add(p);
            
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParms, "UTF-8");
                postMethod.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                throw new NorthbounderException(e);
            }
            
            HttpEntity entity = null;
            try {
                //I have no idea what I'm doing here ;)
                entity = new StringEntity("XML HERE");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            postMethod.setEntity(entity);
            
            method = postMethod;
        } else if (HttpMethod.GET == m_config.getMethod()) {
            
            //TODO: need to configure these
            //List<NameValuePair> getParms = null;
            method = new HttpGet(uri);
        }
        
        method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, m_config.getUserAgent());

        HttpResponse response = null;
        try {
            response = client.execute(method);
        } catch (ClientProtocolException e) {
            throw new NorthbounderException(e);
        } catch (IOException e) {
            throw new NorthbounderException(e);
        }
        
        if (response != null) {
            int code = response.getStatusLine().getStatusCode();
            HttpResponseRange range = new HttpResponseRange("200-399");
            if (!range.contains(code)) {
                System.err.println("response code out of range for uri:" + uri + ".  Expected " + range + " but received " + code);
                throw new NorthbounderException("response code out of range for uri:" + uri + ".  Expected " + range + " but received " + code);
            }
        }
        
        System.err.println(response != null ? response.getStatusLine().getReasonPhrase() : "Response was null");
        LogUtils.debugf(this, response != null ? response.getStatusLine().getReasonPhrase() : "Response was null");
    }


    private HttpVersion determineHttpVersion(String version) {
        HttpVersion httpVersion = null;
        if (version != "1.0") {
            httpVersion = HttpVersion.HTTP_1_1;
        } else {
            httpVersion = HttpVersion.HTTP_1_0;
        }
        return httpVersion;
    }

    private HttpParams buildParams(HttpVersion protocolVersion,
            int connectionTimeout, int socketTimeout, String policy,
            String vHost) {
        HttpParams parms = new BasicHttpParams();
        parms.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, protocolVersion);
        parms.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        parms.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
        parms.setParameter(ClientPNames.COOKIE_POLICY, policy);
        parms.setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(vHost, 8080));
        return parms;
    }
    
    public HttpNorthbounderConfig getConfig() {
        return m_config;
    }

    public void setConfig(HttpNorthbounderConfig config) {
        m_config = config;
    }

}
