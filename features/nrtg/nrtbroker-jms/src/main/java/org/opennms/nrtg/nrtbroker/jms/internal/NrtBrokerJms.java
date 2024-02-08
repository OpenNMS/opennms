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
package org.opennms.nrtg.nrtbroker.jms.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.nrtg.api.NrtBroker;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Markus Neumann
 * @author Christian Pape
 */
public class NrtBrokerJms implements NrtBroker {
    
    private static Logger logger = LoggerFactory.getLogger(NrtBrokerJms.class);

    private JmsTemplate m_jmsTemplate;
    private final SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();
    Map<String, List<String>> m_messageStore = new HashMap<String, List<String>>();
    Map<String, Date> m_lastMessagePolled = new HashMap<String, Date>();

    @Override
    public void publishCollectionJob(CollectionJob collectionJob) {
        logger.debug("JmsTemplate '{}'", m_jmsTemplate);
        m_jmsTemplate.convertAndSend("NrtCollectMe", collectionJob);
    }
    

    @Override
    public List<MeasurementSet> receiveMeasurementSets(String nrtCollectionTaskId) {
        List<MeasurementSet> result = new ArrayList<MeasurementSet>();

        m_jmsTemplate.setReceiveTimeout(125);

        Message message = m_jmsTemplate.receive(nrtCollectionTaskId);

        while (message != null) {
            MeasurementSet measurementSet;
            try {
                measurementSet = (MeasurementSet) simpleMessageConverter.fromMessage(message);
    
                result.add(measurementSet);
            } catch (JMSException ex) {
                logger.error("Error receiving messages", ex);

                return result;
            } catch (MessageConversionException ex) {
                logger.error("Error converting messages", ex);
                
                return result;
            }
            
            message = m_jmsTemplate.receive(nrtCollectionTaskId);
        }

        return result;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        m_jmsTemplate = jmsTemplate;
    }
}
