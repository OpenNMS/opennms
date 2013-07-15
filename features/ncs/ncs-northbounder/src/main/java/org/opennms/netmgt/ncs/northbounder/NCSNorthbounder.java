/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ncs.northbounder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.EmptyKeyRelaxedTrustSSLContext;
import org.opennms.core.utils.HttpResponseRange;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.AlarmType;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.ncs.northbounder.transfer.ServiceAlarm;
import org.opennms.netmgt.ncs.northbounder.transfer.ServiceAlarmNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards north bound alarms via HTTP.
 * FIXME: Needs lots of work still :(
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
public class NCSNorthbounder extends AbstractNorthbounder {
	
    private static final Logger LOG = LoggerFactory.getLogger(NCSNorthbounder.class);

    //FIXME: This should be wired with Spring but is implmented as was in the PSM
    // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
    // is available to JSSE
    static {
        
        //this is a safe call because the method returns -1 if it is already installed (by PageSequenceMonitor, etc.)
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }
    

    private static final String COMPONENT_NAME = "componentName";
	private static final String COMPONENT_FOREIGN_ID = "componentForeignId";
	private static final String COMPONENT_FOREIGN_SOURCE = "componentForeignSource";
	private static final String COMPONENT_TYPE = "componentType";
	private NCSNorthbounderConfig m_config;

    public NCSNorthbounder(NCSNorthbounderConfig config) {
        super("NCSNorthbounder");
        
		m_config = config;
		
		setNaglesDelay(m_config.getNaglesDelay());

    }

    
	@Override
    public boolean accepts(NorthboundAlarm alarm) {
    	if (!m_config.isEnabled()) return false;
    	
    	if (alarm.getAlarmType() == null) return false;
    	if (alarm.getAlarmType() == AlarmType.NOTIFICATION) return false;
    	
        if(m_config.getAcceptableUeis() != null && m_config.getAcceptableUeis().size() != 0 && !m_config.getAcceptableUeis().contains(alarm.getUei())) return false;

        Map<String, String> alarmParms = getParameterMap(alarm.getEventParms());

        // in order to determine the service we need to have the following parameters set in the events
        if (!alarmParms.containsKey(COMPONENT_TYPE)) return false;
        if (!alarmParms.containsKey(COMPONENT_FOREIGN_SOURCE)) return false;
        if (!alarmParms.containsKey(COMPONENT_FOREIGN_ID)) return false;
        if (!alarmParms.containsKey(COMPONENT_NAME)) return false;
        
        // we only send events for "Service" components
        if (!"Service".equals(alarmParms.get(COMPONENT_TYPE))) return false;
        

        return true;
        
    }
    
	private ServiceAlarmNotification toServiceAlarms(List<NorthboundAlarm> alarms) {
		
		List<ServiceAlarm> serviceAlarms = new ArrayList<ServiceAlarm>(alarms.size());
		for(NorthboundAlarm alarm : alarms) {
			serviceAlarms.add(toServiceAlarm(alarm));
		}
		
		return new ServiceAlarmNotification(serviceAlarms);

	}
	
    private ServiceAlarm toServiceAlarm(NorthboundAlarm alarm) {
    	AlarmType alarmType = alarm.getAlarmType();
    	
    	Map<String, String> alarmParms = getParameterMap(alarm.getEventParms());
    	
    	String id = alarmParms.get(COMPONENT_FOREIGN_SOURCE)+":"+alarmParms.get(COMPONENT_FOREIGN_ID);
    	String name = alarmParms.get(COMPONENT_NAME);
    	
    	return new ServiceAlarm(id, name, alarmType == AlarmType.PROBLEM ? "Down" : "Up");
	}
    
    Map<String, String> getParameterMap(String parmString) {
    	
    	Map<String, String> parmMap = new HashMap<String, String>();
    	
    	String[] parms = parmString.split(";");
    	
    	for(String parm : parms) {
    		if (parm.endsWith("(string,text)")) {
    			// we only include string valued keys in the map
    			parm = parm.substring(0, parm.length()-"(string,text)".length());
    			
    			int eq = parm.indexOf('=');
    			if (0 < eq && eq < parm.length()) {
    				String key = parm.substring(0, eq);
    				String val = parm.substring(eq+1);
    				parmMap.put(key, val);
    			}
    		}
    	}
    	
    	return parmMap;
    	
    }



    
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
    	
    	if (!m_config.isEnabled()) return;
    	
        LOG.info("Forwarding {} alarms", alarms.size());
  
        HttpEntity entity = createEntity(alarms);
        
        postAlarms(entity);
    }


	private void postAlarms(HttpEntity entity) {
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
        
        HttpEntityEnclosingRequestBase method = m_config.getMethod().getRequestMethod(uri);

        method.setEntity(entity);
            
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
        LOG.debug(response != null ? response.getStatusLine().getReasonPhrase() : "Response was null");
	}


	private HttpEntity createEntity(List<NorthboundAlarm> alarms) {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			// marshall the output
			JaxbUtils.marshal(toServiceAlarms(alarms), new OutputStreamWriter(out));

			// verify its matches the expected results
			byte[] utf8 = out.toByteArray();

			ByteArrayEntity entity = new ByteArrayEntity(utf8);
			entity.setContentType("application/xml");
            return entity;

		} catch (Exception e) {
			throw new NorthbounderException("failed to convert alarms to xml", e);
		}
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
        if (vHost != null) {
        	parms.setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(vHost, 8080));
        }
        return parms;
    }
    
    public NCSNorthbounderConfig getConfig() {
        return m_config;
    }

    public void setConfig(NCSNorthbounderConfig config) {
        m_config = config;
    }

}
