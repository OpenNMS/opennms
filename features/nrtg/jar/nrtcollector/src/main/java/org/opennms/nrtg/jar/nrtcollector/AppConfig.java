/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.jar.nrtcollector;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.nrtg.nrtcollector.api.NrtCollector;
import org.opennms.nrtg.nrtcollector.internal.ProtocolCollectorRegistry;
import org.opennms.nrtg.nrtcollector.internal.ProtocolCollectorRegistryImpl;
import org.opennms.nrtg.nrtcollector.internal.jms.CollectionJobListener;
import org.opennms.nrtg.nrtcollector.internal.jms.NrtCollectorJMSDLMC;
import org.opennms.nrtg.nrtcollector.internal.jms.JmsExceptionListener;
import org.opennms.nrtg.protocolcollector.snmp.internal.SnmpProtocolCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;

/**
 * Java class as Spring configuration
 *
 * @author Markus Neumann
 */
@Configuration
public class AppConfig {

    @Bean(name = "connectionFactory")
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(amqConnectionFactory());
        cachingConnectionFactory.setSessionCacheSize(16);

        cachingConnectionFactory.setExceptionListener(jmsExceptionListener());
        return cachingConnectionFactory;
    }

    @Bean(name = "jmsExceptionListener")
    public ExceptionListener jmsExceptionListener() {
        return new JmsExceptionListener();
    }

    @Bean(name = "amqConnectionFactory")
    public ConnectionFactory amqConnectionFactory() {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
//        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://10.174.24.10:61616");

        return connectionFactory;
    }

    //
    // JMS Template
    //
    @Bean(name = "JmsTemplate")
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(connectionFactory());
    }

    //
    // JMS DLMC
    //
    @Bean(name = "listenerContainer")
    public AbstractMessageListenerContainer listenerContainer() {
        CollectionJobListener collectionJobListener = new CollectionJobListener(jmsTemplate());
        collectionJobListener.setProtocolCollectorRegistry(protocolCollectorRegistry());
        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory());
        listenerContainer.setConcurrentConsumers(16);
        listenerContainer.setMaxConcurrentConsumers(16);
        listenerContainer.setDestinationName("NrtCollectMe");
        listenerContainer.setMessageListener(collectionJobListener);
        return listenerContainer;
    }

    @Bean(name = "nrtCollector")
    public NrtCollector collectorJmsDLMC() {
        NrtCollectorJMSDLMC collector = new NrtCollectorJMSDLMC();
        collector.setListenerContainer(listenerContainer());
        return collector;
    }
    
    @Bean(name = "snmpStrategy")
    public SnmpStrategy snmpStrategy() {
    	return new Snmp4JStrategy();
    }
    
    @Bean(name = "snmpCollector")
    public SnmpProtocolCollector snmpCollector() {
    	SnmpProtocolCollector snmpCollector = new SnmpProtocolCollector();
    	return snmpCollector;
    }

    @Bean(name = "protocolCollectorRegistry")
    public ProtocolCollectorRegistry protocolCollectorRegistry() {
        ProtocolCollectorRegistryImpl registry = new ProtocolCollectorRegistryImpl();
        registry.getProtocolCollectors().add(snmpCollector());
        return registry;
    }
}
