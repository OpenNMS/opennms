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
package org.opennms.nrtg.commander.internal;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.nrtg.api.model.CollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

@Deprecated
/**
 * Do not use, it's just a basic sample for jms communication
 */
public class SimpleJobPublisher implements JobPublisher {

    private static final Logger logger = LoggerFactory.getLogger(JobPublisher.class);

    // URL of the JMS server. DEFAULT_BROKER_URL will just mean
    // that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    // Name of the queue we will be sending messages to
    // private static String subject = "TESTQUEUE";
    @Override
    public void publishJob(CollectionJob job, String site) {
        try {
            // Getting JMS connection from the server and starting it
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // JMS messages are sent and received using a Session. We will
            // create here a non-transactional session object. If you want
            // to use transactions you should set the first parameter to 'true'
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Destination represents here our queue 'TESTQUEUE' on the
            // JMS server. You don't have to do anything special on the
            // server to create it, it will be created automatically.
            Destination destination = session.createQueue(site);

            // MessageProducer is used for sending messages (as opposed
            // to MessageConsumer which is used for receiving them)
            MessageProducer producer = session.createProducer(destination);

            // We will send a small text message saying 'Hello' in Japanese
            ObjectMessage message = session.createObjectMessage(job);

            // Here we are sending the message!
            producer.send(message);
            logger.info("Sent message '{}'", ((CollectionJob) message.getObject()).toString());

            connection.close();
        } catch (Exception e) {
            logger.error("Exception during Job Publishing, sorry '{}'", e.getMessage());
        }
    }
}
