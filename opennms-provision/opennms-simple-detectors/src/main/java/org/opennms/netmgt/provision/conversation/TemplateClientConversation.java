/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.conversation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.provision.exchange.TemplateExchange;

/**
 * @author thedesloge
 *
 */
public class TemplateClientConversation extends Conversation {
    
    private List<TemplateExchange> m_conversation = new ArrayList<TemplateExchange>();
    
    public void addExchange(TemplateExchange exchange) {
        m_conversation.add(exchange); 
    }
    
    public boolean attemptClientConversation(Object...args) throws IOException {
        
        for(Iterator<TemplateExchange> it = m_conversation.iterator(); it.hasNext();) {
            TemplateExchange ex = it.next();
            
            if(!ex.processResponse(args)) {
               return false; 
            }
            System.out.println("processed response successfully");
            if(!ex.sendRequest(args)) {
                return false;
            }
            System.out.println("send request if there was a request");
        }
        
        return true;
        
    }
    
}
