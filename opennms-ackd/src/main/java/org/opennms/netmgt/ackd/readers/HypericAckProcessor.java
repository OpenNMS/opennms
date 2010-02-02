/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 28, 2010
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ackd.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.acknowledgments.AckService;

public class HypericAckProcessor implements AckProcessor {

    private static final String HYPERIC_IP_ADDRESS = "127.0.0.1";
    private static final int HYPERIC_PORT = 7081;
    private static final String HYPERIC_USER = "hqadmin";
    private static final String HYPERIC_PASSWORD = "hqadmin";

    private AckdConfigurationDao m_ackdDao;
    private AlarmDao m_alarmDao;
    private AckService m_ackService;

    @XmlRootElement(name="hyperic-alert-statuses")
    static class HypericAlertStatuses {
        private List<HypericAlertStatus> statusList;

        @XmlElement
        public List<HypericAlertStatus> getStatusList() {
            return statusList;
        }

        public void setStatusList(List<HypericAlertStatus> statusList) {
            this.statusList = statusList;
        }

    }

    @XmlRootElement(name="alert")
    static class HypericAlertStatus {
        private int alertId;
        private boolean isAcknowledged;
        private boolean isFixed;

        @XmlAttribute(name="id", required=true)
        public int getAlertId() {
            return alertId;
        }
        public void setAlertId(int alertId) {
            this.alertId = alertId;
        }

        @XmlAttribute(name="ack", required=true)
        public boolean isAcknowledged() {
            return isAcknowledged;
        }
        public void setAcknowledged(boolean isAcknowledged) {
            this.isAcknowledged = isAcknowledged;
        }

        @XmlAttribute(name="fixed", required=true)
        public boolean isFixed() {
            return isFixed;
        }
        public void setFixed(boolean isFixed) {
            this.isFixed = isFixed;
        }
    }

    private static Logger log() {
        return ThreadCategory.getInstance(HypericAckProcessor.class);
    }

    public void reloadConfigs() {
        log().debug("reloadConfigs: reloading configuration...");
        m_ackdDao.reloadConfiguration();
        log().debug("reloadConfigs: configuration reloaded");
    }

    public void run() {
        int count = 0;

        // Parse Hyperic alert states
        try {
            log().info("run: Processing Hyperic acknowledgments..." );

            OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class, "alarm");
            criteria.add(Restrictions.isNull("alarmAckUser"));
            // Restrict to Hyperic alerts
            criteria.add(Restrictions.eq("uei", "uei.opennms.org/external/hyperic/alert"));
            // TODO Figure out how to query by parameters

            Map<String,List<OnmsAlarm>> organizedAlarms = new TreeMap<String,List<OnmsAlarm>>();

            // Query list of outstanding alerts with remote platform identifiers
            List<OnmsAlarm> unAckdAlarms = m_alarmDao.findMatching(criteria);

            // Organize the alarms according to the Hyperic system where they originated
            for (OnmsAlarm alarm : unAckdAlarms) {
                String hypericSystem = alarm.getEventParms();
                // Figure out how to parse the event parms into a list, get the platform.agent.address or platform.id
                String key = "blah";
                List<OnmsAlarm> targetList = organizedAlarms.get(key);
                if (targetList == null) {
                    targetList = new ArrayList<OnmsAlarm>();
                    organizedAlarms.put(key, targetList);
                }
                targetList.add(alarm);
            }

            // Connect to each Hyperic system and query for the status of corresponding alerts 
            for (Map.Entry<String, List<OnmsAlarm>> alarmList : organizedAlarms.entrySet()) {
                String hypericSystem = alarmList.getKey();
                List<OnmsAlarm> alarmsForSystem = alarmList.getValue();
                for (OnmsAlarm alarmForSystem : alarmList.getValue()) {
                    // Construct a sane query for the Hyperic system
                }

                // Call fetchHypericAlerts() for each system
                List<HypericAlertStatus> alertsForSystem = new ArrayList<HypericAlertStatus>();

                // Iterate and update any acknowledged or fixed alerts
                List<OnmsAcknowledgment> acks = new ArrayList<OnmsAcknowledgment>();
                for (HypericAlertStatus alert : alertsForSystem) {
                    OnmsAlarm alarm = findAlarmForHypericAlert(alarmsForSystem, hypericSystem, alert);
                    // If the Hyperic alert has been ack'd and the local alarm is not yet ack'd, then ack it
                    if (alert.isAcknowledged() && alarm.getAckTime() == null) {
                        // TODO Get the ack time from Hyperic??
                        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, "Ackd.HypericAckProcessor", new Date());
                        ack.setAckAction(AckAction.ACKNOWLEDGE);
                        ack.setLog("Acknowledged by Ackd.HypericAckProcessor");
                        acks.add(ack);
                    }

                    // If the Hyperic alert has been fixed and the local alarm is not yet marked as CLEARED, then clear it
                    if (alert.isFixed() && !OnmsSeverity.CLEARED.equals(alarm.getSeverity())) {
                        // TODO Get the ack time from Hyperic??
                        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, "Ackd.HypericAckProcessor", new Date());
                        ack.setAckAction(AckAction.CLEAR);
                        ack.setLog("Cleared by Ackd.HypericAckProcessor");
                        acks.add(ack);
                    }
                }

                if (acks.size() > 0) {
                    m_ackService.processAcks(acks);
                }
            }

            log().info("run: Finished processing Hyperic acknowledgments (" + count + " acks processed)" );
        } catch (Throwable e) {
            log().warn("run: threw exception: "+e);
        }
    }

    public static OnmsAlarm findAlarmForHypericAlert(List<OnmsAlarm> alarms, String platformId, HypericAlertStatus alert) {
        String targetPlatformId = "platform.id=" + platformId + "(string,text)";
        String targetAlertId = "alert.id="+ String.valueOf(alert.getAlertId()) + "(string,text)";
        for (OnmsAlarm alarm : alarms) {
            String parmString = alarm.getEventParms();
            String[] parms = parmString.split(";");
            for (String parm : parms) {
                if (targetPlatformId.equals(parm)) {
                    for (String alertparm : parms) {
                        if (targetAlertId.equals(alertparm)) {
                            return alarm;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<HypericAlertStatus> fetchHypericAlerts() {
        HttpClient httpClient = new HttpClient();
        HostConfiguration hostConfig = new HostConfiguration();
        GetMethod  getMethod = new GetMethod("/hqu/opennms/alertStatus/list.hqu");
        httpClient.getParams().setParameter(HttpClientParams.SO_TIMEOUT, 3000);
        httpClient.getParams().setParameter(HttpClientParams.USER_AGENT, "OpenNMS Ackd.HypericAckProcessor");
        // Change these parameters to be configurable
        hostConfig.setHost(HYPERIC_IP_ADDRESS, HYPERIC_PORT);
        // hostConfig.getParams().setParameter(HttpClientParams.VIRTUAL_HOST, "localhost");
        // if(ParameterMap.getKeyedBoolean(map, "http-1.0", false))
        // httpClient.getParams().setParameter(HttpClientParams.PROTOCOL_VERSION,HttpVersion.HTTP_1_0);

        if (HYPERIC_USER != null && !"".equals(HYPERIC_USER) && HYPERIC_PASSWORD != null && !"".equals(HYPERIC_PASSWORD)) {
            httpClient.getParams().setAuthenticationPreemptive(true);
            httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(HYPERIC_USER, HYPERIC_PASSWORD));
        }

        List<HypericAlertStatus> retval = new ArrayList<HypericAlertStatus>();
        try {
            log().debug("httpClient request with the following parameters: " + httpClient);
            log().debug("hostConfig parameters: " + hostConfig);
            log().debug("getMethod parameters: " + getMethod);
            httpClient.executeMethod(hostConfig, getMethod);

            Integer statusCode = getMethod.getStatusCode();
            String statusText = getMethod.getStatusText();
            InputStream responseText = getMethod.getResponseBodyAsStream();

            retval = parseHypericAlerts(new InputStreamReader(responseText));
        } catch (HttpException e) {
            log().warn(e);
        } catch (IOException e) {
            log().warn(e);
        } catch (JAXBException e) {
            log().warn(e);
        } catch (XMLStreamException e) {
            log().warn(e);
        } finally{
            getMethod.releaseConnection();
        }
        return retval;
    }

    public static List<HypericAlertStatus> parseHypericAlerts(Reader reader) throws JAXBException, XMLStreamException {
        List<HypericAlertStatus> retval = new ArrayList<HypericAlertStatus>();

        // Instantiate a JAXB context to parse the alert status
        JAXBContext context = JAXBContext.newInstance(new Class[] { HypericAlertStatuses.class, HypericAlertStatus.class });
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLEventReader xmler = xmlif.createXMLEventReader(reader);
        EventFilter filter = new EventFilter() {
            public boolean accept(XMLEvent event) {
                return event.isStartElement();
            }
        };
        XMLEventReader xmlfer = xmlif.createFilteredReader(xmler, filter);
        // Read up until the beginning of the root element
        StartElement startElement = (StartElement)xmlfer.nextEvent();
        // Fetch the root element name for {@link HypericAlertStatus} objects
        String rootElementName = context.createJAXBIntrospector().getElementName(new HypericAlertStatuses()).getLocalPart();
        if (rootElementName.equals(startElement.getName().getLocalPart())) {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // Use StAX to pull parse the incoming alert statuses
            while (xmlfer.peek() != null) {
                Object object = unmarshaller.unmarshal(xmler);
                if (object instanceof HypericAlertStatus) {
                    HypericAlertStatus alertStatus = (HypericAlertStatus)object;
                    retval.add(alertStatus);
                }
            }
        } else {
            System.err.println("*** ERROR *** Wrong root element: " + rootElementName + " != " + startElement.getName().getLocalPart());
        }
        return retval;
    }


    public synchronized void setAckdConfigDao(final AckdConfigurationDao configDao) {
        m_ackdDao = configDao;
    }

    public synchronized void setAckService(final AckService ackService) {
        m_ackService = ackService;
    }

    public void afterPropertiesSet() throws Exception {
    }

    public synchronized void setAlertDao(final AlarmDao dao) {
        m_alarmDao = dao;
    }
}
