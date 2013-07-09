/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rtc;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>
 * The DataUpdater is created for each event by the event receiver. Depending on
 * the event UEI, relevant information is read from the event and the
 * DataManager informed so that data maintained by the RTC is kept up-to-date
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
final class DataUpdater implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
    /**
     * The event from which data is to be read
     */
    private Event m_event;

    /**
     * If it is a nodeGainedService, create a new entry in the map
     */
    private void handleNodeGainedService(long nodeid, InetAddress ip, String svcName) {

        if (nodeid == -1 || ip == null || svcName == null) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeGainedService(nodeid, ip, svcName);

        LOG.debug("{} added {}: {}: {} to data store", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);

    }

    /**
     * If it is a nodeLostService, update downtime on the rtcnode
     */
    private void handleNodeLostService(long nodeid, InetAddress ip, String svcName, long eventTime) {

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc/eventtime: {}/{}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName, eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeLostService(nodeid, ip, svcName, eventTime);


        LOG.debug("Added nodeLostService to nodeid: {} ip: {} svcName: {}", svcName, nodeid, InetAddressUtils.str(ip));
    }

    /**
     * If it is an interfaceDown, update downtime on the appropriate rtcnodes
     */
    private void handleInterfaceDown(long nodeid, InetAddress ip, long eventTime) {

        if (nodeid == -1 || ip == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/eventtime: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.interfaceDown(nodeid, ip, eventTime);


        LOG.debug("Recorded interfaceDown for nodeid: {} ip: {}", InetAddressUtils.str(ip), nodeid);
    }

    /**
     * If it is an nodeDown, update downtime on the appropriate rtcnodes
     */
    private void handleNodeDown(long nodeid, long eventTime) {

        if (nodeid == -1 || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/eventtime: {}/{}", m_event.getUei(), nodeid, eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeDown(nodeid, eventTime);


        LOG.debug("Recorded nodeDown for nodeid: {}", nodeid);
    }

    /**
     * If it is a nodeUp, update regained time on the appropriate rtcnodes
     */
    private void handleNodeUp(long nodeid, long eventTime) {

        if (nodeid == -1 || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/eventtime: {}/{}", m_event.getUei(), nodeid, eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeUp(nodeid, eventTime);


        LOG.debug("Recorded nodeUp for nodeid: {}", nodeid);
    }

    /**
     * If it is an interfaceUp, update regained time on the appropriate rtcnodes
     */
    private void handleInterfaceUp(long nodeid, InetAddress ip, long eventTime) {

        if (nodeid == -1 || ip == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/eventtime: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.interfaceUp(nodeid, ip, eventTime);


        LOG.debug("Recorded interfaceUp for nodeid: {} ip: {}", InetAddressUtils.str(ip), nodeid);
    }

    /**
     * If it is a nodeRegainedService, update downtime on the rtcnode
     */
    private void handleNodeRegainedService(long nodeid, InetAddress ip, String svcName, long eventTime) {

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc/eventtime: {}/{}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName, eventTime);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.nodeRegainedService(nodeid, ip, svcName, eventTime);


        LOG.debug("Added nodeRegainedService to nodeid: {} ip: {} svcName: {}", svcName, nodeid, InetAddressUtils.str(ip));
    }

    /**
     * If it is a serviceDeleted, remove corresponding RTC nodes from the map
     */
    private void handleServiceDeleted(long nodeid, InetAddress ip, String svcName) {

        if (nodeid == -1 || ip == null || svcName == null) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);
            return;
        }

        DataManager dataMgr = RTCManager.getDataManager();
        dataMgr.serviceDeleted(nodeid, ip, svcName);


        LOG.debug("{} deleted {}: {}: {} from data store", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);

    }

    /**
     * Record the interfaceReparented info in the datastore
     */
    private void handleInterfaceReparented(InetAddress ip, List<Parm> list) {

        if (ip == null || list == null) {
            LOG.warn("{} ignored - info incomplete - ip/parms: {}/{}", m_event.getUei(), InetAddressUtils.str(ip), list);
            return;
        }

        // old node ID
        long oldNodeId = -1;

        // new node ID
        long newNodeId = -1;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : list) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            // old node ID
            if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
                String temp = parmContent;
                try {
                    oldNodeId = Long.valueOf(temp).longValue();
                } catch (NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_OLD_NODEID, nfe);
                    oldNodeId = -1;
                }
            }

            // new node ID
            else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
                String temp = parmContent;
                try {
                    newNodeId = Long.valueOf(temp).longValue();
                } catch (NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_NEW_NODEID, nfe);
                    newNodeId = -1;
                }
            }

        }

        if (oldNodeId == -1 || newNodeId == -1) {
            LOG.warn("{} did not have all required information for {} Values contained old nodeid: {} new nodeid: {}", m_event.getUei(), InetAddressUtils.str(ip), oldNodeId, newNodeId);
        } else {
            DataManager dataMgr = RTCManager.getDataManager();
            dataMgr.interfaceReparented(ip, oldNodeId, newNodeId);

            LOG.debug("{} reparented ip: {} from {} to {}", m_event.getUei(), InetAddressUtils.str(ip), oldNodeId, newNodeId);

        }

    }

    /**
     * Inform the data sender of the new listener
     */
    private void handleRtcSubscribe(List<Parm> list) {

        if (list == null) {
            LOG.warn("{} ignored - info incomplete (null event parms)", m_event.getUei());
            return;
        }

        String url = null;
        String clabel = null;
        String user = null;
        String passwd = null;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : list) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            if (parmName.equals(EventConstants.PARM_URL)) {
                url = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_CAT_LABEL)) {
                clabel = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_USER)) {
                user = parmContent;
            }

            else if (parmName.equals(EventConstants.PARM_PASSWD)) {
                passwd = parmContent;
            }

        }

        // check that we got all required parms
        if (url == null || clabel == null || user == null || passwd == null) {
            LOG.warn("{} did not have all required information. Values contained url: {} catlabel: {} user: {} passwd: {}", m_event.getUei(), url, clabel, user, passwd);

        } else {
            RTCManager.getInstance().getDataSender().subscribe(url, clabel, user, passwd);

            LOG.debug("{} subscribed {}: {}: {}", m_event.getUei(), url, clabel, user);

        }
    }

    /**
     * Inform the data sender of the listener unsubscribing
     */
    private void handleRtcUnsubscribe(List<Parm> list) {

        if (list == null) {
            LOG.warn("{} ignored - info incomplete (null event parms)", m_event.getUei());
            return;
        }

        String url = null;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : list) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            if (parmName.equals(EventConstants.PARM_URL)) {
                url = parmContent;
            }
        }

        // check that we got the required parameter
        if (url == null) {
            LOG.warn("{} did not have required information.  Value of url: {}", m_event.getUei(), url);
        } else {
            RTCManager.getInstance().getDataSender().unsubscribe(url);

            LOG.debug("{} unsubscribed {}", m_event.getUei(), url);
        }
    }

    /**
     * If it is a assetInfoChanged method, update RTC
     */
    private void handleAssetInfoChangedEvent(long nodeid) {

        DataManager dataMgr = RTCManager.getDataManager();

        dataMgr.assetInfoChanged(nodeid);


        LOG.debug("{} asset info changed for node {}", m_event.getUei(), nodeid);

    }
    
    /**
     * If a node's surveillance category membership changed,
     * update RTC since RTC categories may include surveillance
     * categories via "categoryName" or "catinc*" rules
     */
    private void handleNodeCategoryMembershipChanged(long nodeid) {

        DataManager dataMgr = RTCManager.getDataManager();

        dataMgr.nodeCategoryMembershipChanged(nodeid);


        LOG.debug("{} surveillance category membership changed for node {}", m_event.getUei(), nodeid);
    }

    /**
     * Read the event UEI, node ID, interface and service - depending on the UEI,
     * read event parms, if necessary, and call appropriate methods on the data
     * manager to update data
     */
    private void processEvent() {

        if (m_event == null) {

            LOG.debug("Event is null, nothing to process");
            return;
        }

        String eventUEI = m_event.getUei();
        if (eventUEI == null) {
            // huh? should only get registered events

            LOG.debug("Event received with null UEI, ignoring event");
            return;
        }

        long nodeid = -1;
        if (m_event.hasNodeid()) {
            nodeid = m_event.getNodeid();
        }

        InetAddress ip = m_event.getInterfaceAddress();

        String svcName = m_event.getService();

        long eventTime = -1;
        String eventTimeStr = m_event.getTime();
        try {
            java.util.Date date = EventConstants.parseToDate(eventTimeStr);
            eventTime = date.getTime();
        } catch (ParseException pe) {
            LOG.warn("Failed to convert time {} to java.util.Date, Setting current time instead", eventTime, pe);

            eventTime = (new java.util.Date()).getTime();
        }


        LOG.debug("Event UEI: {}\tnodeid: {}\tip: {}\tsvcName: {}\teventTime: {}", eventTimeStr, eventUEI, nodeid, InetAddressUtils.str(ip), svcName);

        //
        //
        // Check for any of the following UEIs:
        //
        // nodeGainedService
        // nodeLostService
        // interfaceDown
        // nodeDown
        // nodeUp
        // interfaceUp
        // nodeRegainedService
        // serviceDeleted
        // interfaceReparented
        // subscribe
        // unsubscribe
        //
        if (eventUEI.equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            handleNodeGainedService(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            handleNodeLostService(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI)) {
            handleInterfaceDown(nodeid, ip, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
            handleNodeDown(nodeid, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_UP_EVENT_UEI)) {
            handleNodeUp(nodeid, eventTime);
        } else if (eventUEI.equals(EventConstants.INTERFACE_UP_EVENT_UEI)) {
            handleInterfaceUp(nodeid, ip, eventTime);
        } else if (eventUEI.equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)) {
            handleNodeRegainedService(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.SERVICE_UNMANAGED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            handleInterfaceReparented(ip, m_event.getParmCollection());
        } else if (eventUEI.equals(EventConstants.RTC_SUBSCRIBE_EVENT_UEI)) {
            handleRtcSubscribe(m_event.getParmCollection());
        } else if (eventUEI.equals(EventConstants.RTC_UNSUBSCRIBE_EVENT_UEI)) {
            handleRtcUnsubscribe(m_event.getParmCollection());
        } else if (eventUEI.equals(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI)) {
            handleAssetInfoChangedEvent(nodeid);
        } else if (eventUEI.equals(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)) {
        	handleNodeCategoryMembershipChanged(nodeid);
        } else {

            LOG.debug("Event subscribed for not handled?!: {}", eventUEI);
        }

        RTCManager.getInstance().incrementCounter();
    }

    /**
     * Constructs the DataUpdater object
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public DataUpdater(Event event) {
        m_event = event;
    }

    /**
     * Process the event depending on the UEI and update date
     */
    @Override
    public void run() {

        try {
            processEvent();
        } catch (Throwable t) {
            LOG.warn("Unexpected exception processing event", t);
        }
    }
}
