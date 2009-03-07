/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.snmpinterfacepoller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class SnmpInterfacePollerEventProcessor implements EventListener {

    private final SnmpPoller m_poller;
	

    /**
     * Create message selector to set to the subscription
     */
    private void createMessageSelectorAndSubscribe() {
        
        List<String> ueiList = new ArrayList<String>();
        ueiList.add(EventConstants.NODE_DOWN_EVENT_UEI);
        ueiList.add(EventConstants.NODE_UP_EVENT_UEI);
        ueiList.add(EventConstants.INTERFACE_DOWN_EVENT_UEI);
        ueiList.add(EventConstants.INTERFACE_UP_EVENT_UEI);
        ueiList.add(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        ueiList.add(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI);
        ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);
        ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
        ueiList.add(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);
        ueiList.add(EventConstants.ADD_INTERFACE_EVENT_UEI);
        ueiList.add(EventConstants.DELETE_INTERFACE_EVENT_UEI);
        ueiList.add(EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI);
        ueiList.add(EventConstants.PROVISION_SCAN_COMPLETE_UEI);
        
        getEventManager().addEventListener(this, ueiList);
    }

     
    SnmpInterfacePollerEventProcessor(SnmpPoller poller) {

        m_poller = poller;

        createMessageSelectorAndSubscribe();

        Category log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled())
            log.debug("Subscribed to eventd");

    }

    /**
     * Unsubscribe from eventd
     */
    public void close() {
        getEventManager().removeEventListener(this);
    }

    /**
     * @return
     */
    private EventIpcManager getEventManager() {
        return getPoller().getEventManager();
    }

    /**
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     * 
     * @param event
     *            The event
     */
    public void onEvent(Event event) {
        if (event == null)
            return;

        Category log = ThreadCategory.getInstance(getClass());

        // print out the uei
        //
        log.debug("SnmpInterfacePollerEventProcessor: received event, uei = " + event.getUei());


        if(event.getUei().equals(EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI)) {
            reloadConfig();
        } else if(event.getUei().equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
    		nodeDownHandler(event);
    	} else if(event.getUei().equals(EventConstants.NODE_UP_EVENT_UEI)) {
            nodeUpHandler(event);
        } else if(event.getUei().equals(EventConstants.INTERFACE_DOWN_EVENT_UEI)) {
            interfaceDownHandler(event);
        } else if(event.getUei().equals(EventConstants.INTERFACE_UP_EVENT_UEI)) {
            interfaceUpHandler(event);
        } else if(event.getUei().equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            serviceDownHandler(event);
        } else if(event.getUei().equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI) ) {
            serviceUpHandler(event);
        } else if(event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            serviceGainedHandler(event);        
        } else if(event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI)) {
            nodeDeletedHandler(event);        
        } else if(event.getUei().equals(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)) {
            primarychangeHandler(event);        
        } else if(event.getUei().equals(EventConstants.DELETE_INTERFACE_EVENT_UEI) || 
                event.getUei().equals(EventConstants.ADD_INTERFACE_EVENT_UEI) ||
                event.getUei().equals(EventConstants.PROVISION_SCAN_COMPLETE_UEI) ) {
            refreshInterfaceHandler(event);        
        } // end single event process

    } // end onEvent()
    
    private void reloadConfig() {

        Category log = ThreadCategory.getInstance(getClass());

        try {
            getPollerConfig().update();
            
            getPoller().getNetwork().deleteAll();
            getPollerConfig().rebuildPackageIpListMap();
            getPoller().scheduleExistingSnmpInterface();
        } catch (MarshalException e) {
            log.error("Update SnmpPoller configuration file failed",e);
        } catch (ValidationException e) {
            log.error("Update SnmpPoller configuration file failed",e);
        } catch (IOException e) {
            log.error("Update SnmpPoller configuration file failed",e);
        }
    }


    private void primarychangeHandler(Event event) {
        nodeDeletedHandler(event);
        Parms parms = event.getParms();
        if (parms != null ) {
            Iterator<Parm> ite = parms.iterateParm();
            while (ite.hasNext()) {
                Parm parm = ite.next();
                if (parm.isValid() && parm.getParmName().equals("newPrimarySnmpAddress")) {
                    getPollerConfig().rebuildPackageIpListMap();
                    getPoller().scheduleNewSnmpInterface(parm.getValue().getContent());
                    return;
                }
            }
        }
    }


    private void refreshInterfaceHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        getPoller().getNetwork().refresh(nodeidlong.intValue());
        
    }


    private void nodeDeletedHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        getPoller().deleteSnmpInterface(nodeidlong.intValue());
    }


    private void serviceGainedHandler(Event event) {
        if (event.getService().equals(getPollerConfig().getService())) {
            getPollerConfig().rebuildPackageIpListMap();
            getPoller().scheduleNewSnmpInterface(event.getInterface());
        }
    }


    private void serviceDownHandler(Event event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
                getPoller().getNetwork().suspend(event.getInterface());
            }
        }
    }


    private void serviceUpHandler(Event event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
                getPoller().getNetwork().activate(event.getInterface());
            }
        }
        
    }


    private void interfaceUpHandler(Event event) {
        getPoller().getNetwork().activate(event.getInterface());
    }


    private void interfaceDownHandler(Event event) {
        getPoller().getNetwork().suspend(event.getInterface());
    }


    private void nodeUpHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        String ipprimary = getPoller().getNetwork().getIp(nodeidlong.intValue());
        if (ipprimary != null) getPoller().getNetwork().activate(ipprimary);
    }


    private void nodeDownHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        String ipprimary = getPoller().getNetwork().getIp(nodeidlong.intValue());
        if (ipprimary != null) getPoller().getNetwork().suspend(ipprimary);
    }


    /**
     * Return an id for this event listener
     */
    public String getName() {
        return "SnmpInterfacePoller:SnmpInterfacePollerEventProcessor";
    }

    /**
     * @return
     */
    private SnmpPoller getPoller() {
        return m_poller;
    }

    /**
     * @return
     */
    private SnmpInterfacePollerConfig getPollerConfig() {
        return getPoller().getPollerConfig();
    }


} // end class
