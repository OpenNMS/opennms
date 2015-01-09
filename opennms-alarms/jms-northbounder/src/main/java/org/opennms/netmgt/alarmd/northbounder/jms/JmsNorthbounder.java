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

package org.opennms.netmgt.alarmd.northbounder.jms;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;


/**
 * Northbound Interface JMS Implementation
 * 
 * FIXME: Needs LOTS of work.  Need to implement ActiveMQ client instead of Geronimo.
 * FIXME: Needs configuration DAO
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class JmsNorthbounder extends AbstractNorthbounder implements InitializingBean {
    
    protected JmsNorthbounder() {
        super("JmsNorthbounder");
    }

    @Autowired
    private JmsTemplate m_template;
    
    //Wire this so that we can have a single connection factory in OpenNMS
    @Autowired
    private ConnectionFactory m_connectionFactory;

    //TODO needs to be configured
    private Queue m_queue;
    
    //@Autowired
    //private JmsNorthbounderConfig m_config;
    
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        
        for (NorthboundAlarm alarm : alarms) {
            m_template.convertAndSend(alarm);
        }
        
        for (final NorthboundAlarm alarm : alarms) {
            m_template.send(m_queue, new MessageCreator() {

                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(convertAlarmToXml(alarm));
                }
                
            });
            
        }
        
    }
    

    protected String convertAlarmToXml(NorthboundAlarm alarm) {
        return "This is a test alarm.";
    }

    @Override
    public void onPreStart() throws NorthbounderException {
        m_template = new JmsTemplate(m_connectionFactory);
    }
    
}
