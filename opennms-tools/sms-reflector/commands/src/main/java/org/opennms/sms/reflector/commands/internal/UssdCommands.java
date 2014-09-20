/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.reflector.commands.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.USSDRequest;

/**
 * Public API representing an example OSGi service
 *
 * @author ranger
 * @version $Id: $
 */
public class UssdCommands implements CommandProvider
{
    private SmsService m_service;
    // private USSDNotification m_ussdNotification;
    
    /**
     * <p>setService</p>
     *
     * @param svc a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
     */
    public void setService(SmsService svc) {
        m_service = svc;
    }
    
    /**
     * <p>_ussdSend</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     */
    public void _ussdSend(CommandInterpreter intp) {
        String data = intp.nextArgument();
        String gwId = intp.nextArgument();
        
        if (data == null || gwId == null) {
            intp.println("usage: ussdSend <data> <gatewayID>");
        }
        intp.println("Data is : " + data);
        intp.println("Gateway ID is : " + gwId);
        
        USSDRequest req = new USSDRequest(data);
        req.setGatewayId(gwId);
        intp.println("USSD raw request: " + req.toString());

        try {
            m_service.sendUSSDRequest(req, gwId);
        } catch (Throwable e) {
            intp.println("Exception sending USSD request: " + e.getMessage());
            intp.printStackTrace(e);
        }
    }
    
    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---USSD Commands---");
        buffer.append("\n\t").append("ussdSend <data> <gatewayID>");
        buffer.append("\n");
        return buffer.toString(); 
    } 

}

