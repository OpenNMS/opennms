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
