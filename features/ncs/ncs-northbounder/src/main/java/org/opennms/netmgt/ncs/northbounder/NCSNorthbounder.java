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

package org.opennms.netmgt.ncs.northbounder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.http.HttpResponseRange;
import org.opennms.core.web.HttpClientWrapper;
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

        final Map<String, String> alarmParms = alarm.getParameters();

        // in order to determine the service we need to have the following parameters set in the events
        if (!alarmParms.containsKey(COMPONENT_TYPE)) return false;
        if (!alarmParms.containsKey(COMPONENT_FOREIGN_SOURCE)) return false;
        if (!alarmParms.containsKey(COMPONENT_FOREIGN_ID)) return false;
        if (!alarmParms.containsKey(COMPONENT_NAME)) return false;

        // we only send events for "Service" components
        if (!"Service".equals(alarmParms.get(COMPONENT_TYPE))) return false;


        return true;

    }

    @Override
    public boolean isReady() {
        return getConfig().isEnabled();
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

        final Map<String, String> alarmParms = alarm.getParameters();

        String id = alarmParms.get(COMPONENT_FOREIGN_SOURCE)+":"+alarmParms.get(COMPONENT_FOREIGN_ID);
        String name = alarmParms.get(COMPONENT_NAME);

        return new ServiceAlarm(id, name, alarmType == AlarmType.PROBLEM ? "Down" : "Up");
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
        Integer retryCount = 3;

        HttpVersion httpVersion = determineHttpVersion(m_config.getHttpVersion());        

        URI uri = m_config.getURI();
        System.err.println("uri = " + uri);

        final HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .setSocketTimeout(socketTimeout)
                .setConnectionTimeout(connectionTimeout)
                .setRetries(retryCount)
                .useBrowserCompatibleCookies()
                .dontReuseConnections();

        if ("https".equals(uri.getScheme())) {
            try {
                clientWrapper.useRelaxedSSL("https");
            } catch (final GeneralSecurityException e) {
                throw new NorthbounderException("Failed to configure Relaxed SSL handling.", e);
            }
        }

        final HttpEntityEnclosingRequestBase method = m_config.getMethod().getRequestMethod(uri);

        if (m_config.getVirtualHost() != null && !m_config.getVirtualHost().trim().isEmpty()) {
            method.setHeader(HTTP.TARGET_HOST, m_config.getVirtualHost());
        }
        if (m_config.getUserAgent() != null && !m_config.getUserAgent().trim().isEmpty()) {
            method.setHeader(HTTP.USER_AGENT, m_config.getUserAgent());
        }
        method.setProtocolVersion(httpVersion);
        method.setEntity(entity);

        CloseableHttpResponse response = null;
        try {
            System.err.println("execute: " + method);
            response = clientWrapper.execute(method);
        } catch (ClientProtocolException e) {
            throw new NorthbounderException(e);
        } catch (IOException e) {
            throw new NorthbounderException(e);
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }

        if (response != null) {
            try {
                int code = response.getStatusLine().getStatusCode();
                final HttpResponseRange range = new HttpResponseRange("200-399");
                if (!range.contains(code)) {
                    LOG.warn("response code out of range for uri: {}.  Expected {} but received {}", uri, range, code);
                    throw new NorthbounderException("response code out of range for uri:" + uri + ".  Expected " + range + " but received " + code);
                }
            } finally {
                IOUtils.closeQuietly(clientWrapper);
            }
        }

        LOG.debug(response != null ? response.getStatusLine().getReasonPhrase() : "Response was null");
    }

    private HttpEntity createEntity(List<NorthboundAlarm> alarms) {
        ByteArrayOutputStream out = null;
        OutputStreamWriter writer = null;

        try {
            out = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(out);

            // marshall the output
            JaxbUtils.marshal(toServiceAlarms(alarms), writer);

            // verify its matches the expected results
            byte[] utf8 = out.toByteArray();

            ByteArrayEntity entity = new ByteArrayEntity(utf8);
            entity.setContentType("application/xml");
            return entity;

        } catch (Exception e) {
            throw new NorthbounderException("failed to convert alarms to xml", e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(out);
        }
    }


    private static HttpVersion determineHttpVersion(String version) {
        HttpVersion httpVersion = null;
        if ("1.0".equals(version)) {
            httpVersion = HttpVersion.HTTP_1_0;
        } else {
            httpVersion = HttpVersion.HTTP_1_1;
        }
        return httpVersion;
    }

    public NCSNorthbounderConfig getConfig() {
        return m_config;
    }

    public void setConfig(NCSNorthbounderConfig config) {
        m_config = config;
    }

}
