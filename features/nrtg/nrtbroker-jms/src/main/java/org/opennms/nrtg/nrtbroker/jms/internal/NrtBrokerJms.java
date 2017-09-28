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
