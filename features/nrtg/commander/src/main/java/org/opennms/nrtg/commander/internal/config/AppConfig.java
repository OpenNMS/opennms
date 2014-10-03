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

package org.opennms.nrtg.commander.internal.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.nrtg.commander.internal.JmsExceptionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;

/**
 * @author Markus Neumann
 */
@Configuration
public class AppConfig {

//    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean(name = "connectionFactory")
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(amqConnectionFactory());
        cachingConnectionFactory.setSessionCacheSize(8);

        cachingConnectionFactory.setExceptionListener(jmsExceptionListener());
        return cachingConnectionFactory;
    }

    @Bean(name = "amqConnectionFactory")
    public ConnectionFactory amqConnectionFactory() {
        return new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
    }

    @Bean(name = "JmsTemplate")
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setDeliveryPersistent(false);
        jmsTemplate.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setTimeToLive(120000);
        return jmsTemplate;
    }

    @Bean(name = "JmsExceptionListener")
    public JmsExceptionListener jmsExceptionListener() {
        return new JmsExceptionListener();
    }
}
