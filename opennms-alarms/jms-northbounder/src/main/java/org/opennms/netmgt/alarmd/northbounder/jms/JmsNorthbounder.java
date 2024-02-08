/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.alarmd.northbounder.jms;

import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsDestination.DestinationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Northbound Interface JMS Implementation
 * 
 * <p>Allows alarms to be automatically forwarded to a JMS destination. JMS
 * implementation neutral, defaults to ActiveMQ. To change,
 * add a Spring bean that implements javax.jms.ConnectionFactory
 * and change OpenNMS property opennms.alarms.northbound.jms.connectionFactoryImplRef
 * to that bean's ID. It will be wrapped in the Spring's CachingConnectionFactory
 * so your bean does not need to handle caching or pooling itself.</p>
 *
 * <p>Configuration is done in $ONMS_HOME/etc/jms-northbounder-configuration.xml
 * and is similar to the syslog NBI config file. See JmsNorthbounderConfig
 * or the appropriate schema file for details.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dschlenk@converge-one.com">David Schlenk</a>
 */
public class JmsNorthbounder extends AbstractNorthbounder implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JmsNorthbounder.class);

    /** The Constant NBI_NAME. */
    public static final String NBI_NAME = "JmsNBI";

    /** The JMX northbounder connection factory. */
    private ConnectionFactory m_jmsNorthbounderConnectionFactory;

    /** The JMS northbounder configuration. */
    private JmsNorthbounderConfig m_config;

    /** The JMS destination. */
    private JmsDestination m_jmsDestination;

    /** The JMS template. */
    private JmsTemplate m_template;

    /**
     * Instantiates a new JMS northbounder.
     *
     * @param config the configuration
     * @param jmsNorthbounderConnectionFactory the JMS northbounder connection factory
     * @param destination the destination
     */
    public JmsNorthbounder(JmsNorthbounderConfig config, ConnectionFactory jmsNorthbounderConnectionFactory, JmsDestination destination) {
        super(NBI_NAME + ":" + destination);
        m_config = config;
        m_jmsNorthbounderConnectionFactory = jmsNorthbounderConnectionFactory;
        m_jmsDestination = destination;
    }

    /**
     * Instantiates a new JMS northbounder.
     */
    protected JmsNorthbounder() {
        super(NBI_NAME);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#accepts(org.opennms.netmgt.alarmd.api.NorthboundAlarm)
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (!m_config.isEnabled()) {
            LOG.warn("JMS Northbounder {} is currently disabled, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }
        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());

        if (getConfig().getUeis() == null || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            return true;
        }

        LOG.debug("UEI: {}, rejected.", alarm.getUei());
        return false;
    }

    @Override
    public boolean isReady() {
        return getConfig().isEnabled();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        LOG.debug("creating new JmsTemplate with connection to {}",m_jmsDestination.getJmsDestination());
        m_template = new JmsTemplate(m_jmsNorthbounderConnectionFactory);
        if (m_jmsDestination.getDestinationType().equals(DestinationType.TOPIC)) {
            m_template.setPubSubDomain(true);
        }
        setNaglesDelay(m_config.getNaglesDelay());
        setMaxBatchSize(m_config.getBatchSize());
        setMaxPreservedAlarms(m_config.getQueueSize());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#forwardAlarms(java.util.List)
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        for (final NorthboundAlarm alarm : alarms) {
            final Integer count = alarm.getCount();
            LOG.debug("Does destination {} take only first occurances? {} Is new alarm? Has count of {}.", m_jmsDestination.getName(), m_jmsDestination.isFirstOccurrenceOnly(), count);
            if(count > 1 && m_jmsDestination.isFirstOccurrenceOnly()) {
                LOG.debug("Skipping because not new alarm.");
                continue;
            }
            LOG.debug("Attempting to send a message to {} of type {}", m_jmsDestination.getJmsDestination(), m_jmsDestination.getDestinationType());
            try {
                m_template.send(m_jmsDestination.getJmsDestination(), new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                          if (m_jmsDestination.isSendAsObjectMessageEnabled()) {
                              return session.createObjectMessage(alarm);
                          } else {
                              return session.createTextMessage(convertAlarmToText(alarm));
                          }
                    }
                });
                LOG.debug("Sent message");
            } catch (JmsException e) {
                LOG.error("Unable to send alarm to northbound JMS because {}", e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Convert alarm to text.
     *
     * @param alarm the alarm
     * @return the string
     */
    private String convertAlarmToText(NorthboundAlarm alarm) {
        String alarmXml = null;
        Map<String, Object> mapping = createMapping(alarm, null);
        LOG.debug("Making substitutions for tokens in message format for alarm: {}.", alarm.getId());
        if (m_jmsDestination.getMessageFormat() != null) {
            alarmXml = PropertiesUtils.substitute(m_jmsDestination.getMessageFormat(), mapping);
        } else {
            alarmXml = PropertiesUtils.substitute(m_config.getMessageFormat(), mapping);
        }
        return alarmXml;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public JmsDestination getDestination() {
        return m_jmsDestination;
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    public JmsNorthbounderConfig getConfig() {
        return m_config;
    }

    /**
     * Gets the JMS northbounder connection factory.
     *
     * @return the JMS northbounder connection factory
     */
    public ConnectionFactory getJmsNorthbounderConnectionFactory() {
        return m_jmsNorthbounderConnectionFactory;
    }

    /**
     * Sets the JMS northbounder connection factory.
     *
     * @param jmsNorthbounderConnectionFactory the new JMS northbounder connection factory
     */
    public void setJmsNorthbounderConnectionFactory(ConnectionFactory jmsNorthbounderConnectionFactory) {
        m_jmsNorthbounderConnectionFactory = jmsNorthbounderConnectionFactory;
    }

}
