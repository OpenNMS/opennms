/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller;


import org.apache.commons.lang.StringUtils;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * SnmpPoller daemon class
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */

@EventListener(name="snmpPoller", logPrefix="snmp-poller")
public class SnmpPoller extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpPoller.class);
    
    private static final SnmpPoller m_singleton = new SnmpPoller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private SnmpInterfacePollerConfig m_pollerConfig;
    
    private PollableNetwork m_network;
        
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return m_network;
    }

    /**
     * <p>setNetwork</p>
     *
     * @param pollableNetwork a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork} object.
     */
    public void setNetwork(
            PollableNetwork pollableNetwork) {
        m_network = pollableNetwork;
    }

    /**
     * <p>isInitialized</p>
     *
     * @return a boolean.
     */
    public boolean isInitialized() {
        return m_initialized;
    }

    /**
     * <p>getScheduler</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.opennms.netmgt.scheduler.LegacyScheduler} object.
     */
    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.SnmpInterfacePollerConfig} object.
     */
    public SnmpInterfacePollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * <p>setPollerConfig</p>
     *
     * @param snmpinterfacepollerConfig a {@link org.opennms.netmgt.config.SnmpInterfacePollerConfig} object.
     */
    public void setPollerConfig(
            SnmpInterfacePollerConfig snmpinterfacepollerConfig) {
        m_pollerConfig = snmpinterfacepollerConfig;
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        // get the category logger
        // start the scheduler
        //
        try {
            LOG.debug("onStart: Starting SNMP Interface Poller scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            LOG.error("onStart: Failed to start scheduler", e);
            throw e;
        }

    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        
        if(getScheduler()!=null) {
            LOG.debug("onStop: stopping scheduler");
            getScheduler().stop();
        }

        setScheduler(null);
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        getScheduler().pause();
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        getScheduler().resume();
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.SnmpPoller} object.
     */
    public static SnmpPoller getInstance() {
        return m_singleton;
    }
    
    /**
     * <p>Constructor for SnmpPoller.</p>
     */
    public SnmpPoller() {
        super("snmp-poller");
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        
        createScheduler();
        
        // Schedule the interfaces currently in the database
        //
        try {
            LOG.debug("onInit: Scheduling existing SNMP interfaces polling");
            scheduleExistingSnmpInterface();
        } catch (Throwable sqlE) {
            LOG.error("onInit: Failed to schedule existing interfaces", sqlE);
        }

        m_initialized = true;
        
    }
        
    /**
     * <p>scheduleNewSnmpInterface</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    protected void scheduleNewSnmpInterface(String ipaddr) {
 
    	for (OnmsIpInterface iface : getNetwork().getContext().getPollableNodesByIp(ipaddr)) {
            schedulePollableInterface(iface);    		
    	}
                
    }
    
    /**
     * <p>scheduleExistingSnmpInterface</p>
     */
    protected void scheduleExistingSnmpInterface() {
        
    	for (OnmsIpInterface iface : getNetwork().getContext().getPollableNodes()) {
            schedulePollableInterface(iface);    		
    	}
    }   

    /**
     * <p>schedulePollableInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    protected void schedulePollableInterface(OnmsIpInterface iface) {
        String ipaddress = iface.getIpAddress().getHostAddress();
        String netmask = null;
        // netmask is nullable
        if (iface.getNetMask() != null) {
            netmask = iface.getNetMask().getHostAddress();
        }
        Integer nodeid = iface.getNode().getId();
        String location = getNetwork().getContext().getLocation(nodeid);
        if (ipaddress != null && !ipaddress.equals("0.0.0.0")) {
            String pkgName = getPollerConfig().getPackageName(ipaddress);
            if (pkgName != null) {
                LOG.debug("Scheduling snmppolling for node: {} ip address: {} - Found package interface with name: {}", nodeid, ipaddress, pkgName);
                scheduleSnmpCollection(getNetwork().create(nodeid,ipaddress,netmask,pkgName), pkgName, location);
            } else if (!getPollerConfig().useCriteriaFilters()) {
                LOG.debug("No SNMP Poll Package found for node: {} ip address: {}. - Scheduling according with default interval", nodeid, ipaddress);
                scheduleSnmpCollection(getNetwork().create(nodeid, ipaddress,netmask, "null"), "null", location);
            }
        }
    }
    
    private void scheduleSnmpCollection(PollableInterface nodeGroup, String pkgName, String location) {
    	
    	String excludingCriteria = new String(" snmpifindex > 0 ");
        for (String pkgInterfaceName: getPollerConfig().getInterfaceOnPackage(pkgName)) {
            LOG.debug("found package interface with name: {}", pkgInterfaceName);
            if (getPollerConfig().getStatus(pkgName, pkgInterfaceName)){

                final String criteria = getPollerConfig().getCriteria(pkgName, pkgInterfaceName).orElse(null);
                if (getPollerConfig().getCriteria(pkgName, pkgInterfaceName).isPresent()) {
                    LOG.debug("package interface: criteria: {}", criteria);
                    excludingCriteria = excludingCriteria + " and not " + criteria;
                }
                
                long interval = getPollerConfig().getInterval(pkgName, pkgInterfaceName);
                LOG.debug("package interface: interval: {}", interval);

                int port = getPollerConfig().getPort(pkgName, pkgInterfaceName).orElse(-1);
                int timeout = getPollerConfig().getTimeout(pkgName, pkgInterfaceName).orElse(-1);
                int retries = getPollerConfig().getRetries(pkgName, pkgInterfaceName).orElse(-1);

                boolean hasMaxVarsPerPdu = getPollerConfig().hasMaxVarsPerPdu(pkgName, pkgInterfaceName);
                int maxVarsPerPdu = -1;
                if (hasMaxVarsPerPdu) maxVarsPerPdu = getPollerConfig().getMaxVarsPerPdu(pkgName, pkgInterfaceName);

                LOG.debug("package interface up-values: {}", getPollerConfig().getUpValues(pkgName, pkgInterfaceName));
                LOG.debug("package interface down-values: {}", getPollerConfig().getDownValues(pkgName, pkgInterfaceName));
                LOG.debug("package up-values: {}", getPollerConfig().getUpValues());
                LOG.debug("package down-values: {}", getPollerConfig().getDownValues());
                int[] upValues = statusValuesFromString(getPollerConfig().getUpValues(pkgName, pkgInterfaceName)
                        , new int[]{1});
                int[] downValues = statusValuesFromString(getPollerConfig().getDownValues(pkgName, pkgInterfaceName)
                        , new int[]{2});

                PollableSnmpInterface node = nodeGroup.createPollableSnmpInterface(location, pkgInterfaceName, 
                        criteria, port != -1, port, timeout != -1, timeout, retries != -1, retries, hasMaxVarsPerPdu, maxVarsPerPdu,
                        upValues, downValues);

                node.setSnmpinterfaces(getNetwork().getContext().get(node.getParent().getNodeid(), criteria));

                getNetwork().schedule(node,interval,getScheduler());
            } else {
                LOG.debug("package interface status: Off");
            }
        }
        if (!getPollerConfig().useCriteriaFilters()) {
            LOG.debug("excluding criteria used for default polling: {}", excludingCriteria);
            PollableSnmpInterface node = nodeGroup.createPollableSnmpInterface(location, "null",
                    excludingCriteria, false, -1, false, -1, false, -1, false, -1,
                    statusValuesFromString(getPollerConfig().getUpValues(), new int[]{1}),
                    statusValuesFromString(getPollerConfig().getDownValues(), new int[]{2}));

            node.setSnmpinterfaces(getNetwork().getContext().get(node.getParent().getNodeid(), excludingCriteria));

            getNetwork().schedule(node,getPollerConfig().getInterval(),getScheduler());
        }
    }

    private int[] statusValuesFromString(String str, int[] defValues) {
        Map map = new HashMap<String, String>(1);
        map.put("values", str);
        return ParameterMap.getKeyedIntegerArray(map, "values", defValues);
    }
    
    private void createScheduler() {

        // Create a scheduler
        //
        try {
            LOG.debug("init: Creating SNMP Interface Poller scheduler");

            setScheduler(new LegacyScheduler("Snmpinterfacepoller", getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create SNMP interface poller scheduler", e);
            throw e;
        }
    }

    /**
     * <p>reloadSnmpConfig</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.CONFIGURE_SNMP_EVENT_UEI)
    public void reloadSnmpConfig(IEvent event) {
        LOG.debug("reloadSnmpConfig: managing event: {}", event.getUei());
        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            LOG.debug("interrupted while waiting for reload", e);
            Thread.currentThread().interrupt();
        }
        
        SnmpEventInfo info = null;
        try {
            info = new SnmpEventInfo(event);
            
            if (StringUtils.isBlank(info.getFirstIPAddress())) {                
                LOG.error("configureSNMPHandler: event contained invalid firstIpAddress. {}", event);
                return;
            }
        } catch (final Throwable e) {
            LOG.error("reloadSnmpConfig: ", e);
            return;
        }
        
        final IPAddressRange range = new IPAddressRange(info.getFirstIPAddress(), info.getLastIPAddress());
        for (final IPAddress ipaddr : range) {
            LOG.debug("reloadSnmpConfig: found ipaddr: {}", ipaddr);
            if (getNetwork().hasPollableInterface(ipaddr.toDbString())) {
                LOG.debug("reloadSnmpConfig: recreating the Interface to poll: {}", ipaddr);
                getNetwork().delete(ipaddr.toDbString());
                scheduleNewSnmpInterface(ipaddr.toDbString());
            } else {
                LOG.debug("reloadSnmpConfig: no Interface found for ipaddr: {}", ipaddr);
            }
        }
    }
    
    /**
     * <p>reloadConfig</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI)
    public void reloadConfig(IEvent event) {
        LOG.debug("reloadConfig: managing event: {}", event.getUei());
        try {
            getPollerConfig().update();
            getNetwork().deleteAll();
            scheduleExistingSnmpInterface();
        } catch (Throwable e) {
            LOG.error("Update SnmpPoller configuration file failed",e);
        }
    }

    /**
     * <p>primarychangeHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)
    public void primarychangeHandler(IEvent event) {
        LOG.debug("primarychangeHandler: managing event: {}", event.getUei());

        getNetwork().delete(Long.valueOf(event.getNodeid()).intValue());
        
        for (IParm parm : event.getParmCollection()){
            if (parm.isValid() && parm.getParmName().equals("newPrimarySnmpAddress")) {
                scheduleNewSnmpInterface(parm.getValue().getContent());
                return;
            }
        }
    }
    
    /**
     * <p>deleteInterfaceHaldler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void deleteInterfaceHaldler(IEvent event){
        getNetwork().delete(event.getInterface());
    }

    /**
     * <p>scanCompletedHaldler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.PROVISION_SCAN_COMPLETE_UEI)
    public void scanCompletedHaldler(IEvent event){
        getNetwork().refresh(Long.valueOf(event.getNodeid()).intValue());
    }

    /**
     * <p>rescanCompletedHaldler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.RESCAN_COMPLETED_EVENT_UEI)
    public void rescanCompletedHaldler(IEvent event){
        getNetwork().refresh(Long.valueOf(event.getNodeid()).intValue());
    }

    /**
     * <p>nodeDeletedHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void nodeDeletedHandler(IEvent event) {
        getNetwork().delete(Long.valueOf(event.getNodeid()).intValue());
    }

    /**
     * <p>serviceGainedHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void serviceGainedHandler(IEvent event) {
        if (event.getService().equals(getPollerConfig().getService())) {
            getPollerConfig().rebuildPackageIpListMap();
            scheduleNewSnmpInterface(event.getInterface());
        }
    }

    /**
     * <p>serviceDownHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void serviceDownHandler(IEvent event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
		LOG.info("Critical Service Lost: suspending SNMP polling for primary interface: {}", event.getInterface());
                getNetwork().suspend(event.getInterface());
            }
        }
    }

    /**
     * <p>serviceUpHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void serviceUpHandler(IEvent event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
		LOG.info("Critical Service Regained: activate SNMP polling for primary interface: {}", event.getInterface());
                getNetwork().activate(event.getInterface());
            }
        }
        
    }

    /**
     * <p>interfaceUpHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.INTERFACE_UP_EVENT_UEI)
    public void interfaceUpHandler(IEvent event) {
        getNetwork().activate(event.getInterface());
    }

    /**
     * <p>interfaceDownHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.INTERFACE_DOWN_EVENT_UEI)
    public void interfaceDownHandler(IEvent event) {
        getNetwork().suspend(event.getInterface());
    }

    /**
     * <p>nodeUpHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_UP_EVENT_UEI)
    public void nodeUpHandler(IEvent event) {
        getNetwork().activate(Long.valueOf(event.getNodeid()).intValue());

    }

    /**
     * <p>nodeDownHandler</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_DOWN_EVENT_UEI)
    public void nodeDownHandler(IEvent event) {
        getNetwork().suspend(Long.valueOf(event.getNodeid()).intValue());
    }

}
