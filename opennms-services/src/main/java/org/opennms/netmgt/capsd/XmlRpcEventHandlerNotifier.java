/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.capsd;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.XmlrpcUtil;
import org.opennms.netmgt.xml.event.Event;

/**
 * XmlRpcEventHandlerNotifier
 *
 * @author brozow
 * @version $Id: $
 */

@Aspect
public class XmlRpcEventHandlerNotifier {

    /**
     * <p>capsdMethod</p>
     */
    @Pointcut("execution(* org.opennms.netmgt.capsd.BroadcastEventProcessor.*(..))")
    public void capsdMethod() {}
    
    /**
     * <p>eventHandler</p>
     */
    @Pointcut("@annotation(org.opennms.netmgt.model.events.annotations.EventHandler)")
    public void eventHandler() {}
    
    /**
     * <p>capsdEventHandler</p>
     */
    @Pointcut("capsdMethod() && eventHandler()")
    public void capsdEventHandler() {}
    
    /**
     * <p>onEvent</p>
     *
     * @param pjp a {@link org.aspectj.lang.ProceedingJoinPoint} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws java.lang.Throwable if any.
     */
    @Around("capsdEventHandler() && args(event)")
    public void onEvent(ProceedingJoinPoint pjp, Event event) throws Throwable {
        notifyEventReceived(event);
        
        try {
            pjp.proceed();
            
            notifyEventSuccess(event);
        } catch (InsufficientInformationException ex) {
            handleInsufficientInformationException(event, ex);
        } catch (FailedOperationException ex) {
            handleFailedOperationException(event, ex);
        } 
    }
    
    private Set<String> m_notifySet;
    private boolean m_xmlRpcEnabled;
    
    /**
     * <p>Constructor for XmlRpcEventHandlerNotifier.</p>
     */
    public XmlRpcEventHandlerNotifier() {
        m_notifySet = new HashSet<String>();
        
        m_notifySet.add(EventConstants.ADD_NODE_EVENT_UEI);
        m_notifySet.add(EventConstants.DELETE_NODE_EVENT_UEI);
        m_notifySet.add(EventConstants.ADD_INTERFACE_EVENT_UEI);
        m_notifySet.add(EventConstants.DELETE_INTERFACE_EVENT_UEI);
        m_notifySet.add(EventConstants.CHANGE_SERVICE_EVENT_UEI);
        m_notifySet.add(EventConstants.UPDATE_SERVER_EVENT_UEI);
        m_notifySet.add(EventConstants.UPDATE_SERVICE_EVENT_UEI);

    }
    
    /**
     * <p>isXmlRpcEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isXmlRpcEnabled() {
        return m_xmlRpcEnabled;
    }
    
    /**
     * <p>setXmlRpcEnabled</p>
     *
     * @param xmlRpcEnabled a boolean.
     */
    public void setXmlRpcEnabled(boolean xmlRpcEnabled) {
        m_xmlRpcEnabled = xmlRpcEnabled;
    }
    
    
    private void handleFailedOperationException(Event event, FailedOperationException ex) {
        log().error("BroadcastEventProcessor: operation failed for event: " + event.getUei() + ", exception: " + ex.getMessage());
        notifyEventError(event, "processing failed: ", ex);
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void handleInsufficientInformationException(Event event, InsufficientInformationException ex) {
        log().info("BroadcastEventProcessor: insufficient information in event, discarding it: " + ex.getMessage());
        notifyEventError(event, "Invalid parameters: ", ex);
    }

    private void notifyEventSuccess(Event event) {
        if (!isXmlRpcEnabled())
            return;

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        if ((txNo != -1) && m_notifySet.contains(event.getUei())) {
            StringBuffer message = new StringBuffer("Completed processing event: ");
            message.append(event.getUei());
            message.append(" : ");
            message.append(event);
            int status = EventConstants.XMLRPC_NOTIFY_SUCCESS;
            XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, event.getUei(), message.toString(), status, "OpenNMS.Capsd");
        }
    }

    private void notifyEventError(Event event, String msg, Exception ex) {
        if (!isXmlRpcEnabled())
            return;

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);
        if ((txNo != -1) && m_notifySet.contains(event.getUei())) {
            int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
            XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, event.getUei(), msg + ex.getMessage(), status, "OpenNMS.Capsd");
        }
    }

    private void notifyEventReceived(Event event) {
        if (!isXmlRpcEnabled())
            return;

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        if ((txNo != -1) && m_notifySet.contains(event.getUei())) {
            StringBuffer message = new StringBuffer("Received event: ");
            message.append(event.getUei());
            message.append(" : ");
            message.append(event);
            int status = EventConstants.XMLRPC_NOTIFY_RECEIVED;
            XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, event.getUei(), message.toString(), status, "OpenNMS.Capsd");
        }
    }


    
    
    
    

}
