/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.api.support.jms;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.opennms.netmgt.alarmd.api.AbstractNorthbounder;
import org.opennms.netmgt.alarmd.api.Alarm;
import org.opennms.netmgt.alarmd.api.support.NorthbounderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;


/**
 * Northbound Interface JMS Implementation
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class JmsNorthbounder extends AbstractNorthbounder {
    
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
    
    @Override
    public boolean accepts(Alarm alarm) {
        // TODO Auto-generated method stub
        return true;
    }



    @Override
    public void forwardAlarms(List<Alarm> alarms) throws NorthbounderException {
        
        for (final Alarm alarm : alarms) {
            m_template.send(m_queue, new MessageCreator() {

                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(convertAlarmToXml(alarm));
                }
                
            });
            
        }
        
    }
    

    protected String convertAlarmToXml(Alarm alarm) {
        return "This is a test alarm.";
    }

    @Override
    public void init() throws NorthbounderException {
        m_template = new JmsTemplate(m_connectionFactory);
    }
    
    /**
     * This method should be overridden in the implementation with a call to super.sync(alarm);
     */
    @Override
    public void sync(Alarm alarm) throws NorthbounderException {
    }
    
    /**
     * This method should be overridden in the implementation with a call to super.sync(alarm);
     */
    @Override
    public void fetch(String query) throws NorthbounderException {
    }
    
    /**
     * This method should be overridden in the implementation with a call to super.sync(alarm);
     */
    @Override
    public void syncAll() throws NorthbounderException {
        
    }


}
