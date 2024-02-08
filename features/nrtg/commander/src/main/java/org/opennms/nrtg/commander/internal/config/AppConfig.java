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
