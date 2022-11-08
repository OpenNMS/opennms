/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.*;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmpinterfacepoller.SnmpPollInterfaceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opennms.netmgt.snmpinterfacepoller.pollable.SnmpInterfaceStatus.*;

/**
 * Represents a PollableSnmpInterface
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableSnmpInterface implements ReadyRunnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(PollableSnmpInterface.class);

    private volatile Schedule m_schedule;

    private Map<Integer,OnmsSnmpInterface> m_snmpinterfaces;
    
    private PollableSnmpInterfaceConfig m_snmppollableconfig;

    private PollableInterface m_parent;
    
    private String m_name;
        
    private String m_criteria;

    private Set<SnmpInterfaceStatus> m_upValues;

    private Set<SnmpInterfaceStatus> m_downValues;
        
    private SnmpAgentConfig m_agentConfig;
    
    public static class SnmpMinimalPollInterface {

        int ifindex;
        SnmpInterfaceStatus adminstatus;
        SnmpInterfaceStatus operstatus;

        PollStatus m_status;

        PollStatus m_operPollStatus;
        PollStatus m_adminPollStatus;

        public SnmpMinimalPollInterface(int ifindex, SnmpInterfaceStatus adminstatus,
                SnmpInterfaceStatus operstatus) {
            this.ifindex = ifindex;
            this.adminstatus = adminstatus;
            this.operstatus = operstatus;
            m_status = PollStatus.unknown();
        }

        public int getIfindex() {
            return ifindex;
        }
        public void setIfindex(int ifindex) {
            this.ifindex = ifindex;
        }
        public SnmpInterfaceStatus getAdminstatus() {
            return adminstatus;
        }

        /**
         *
         * @param adminstatus valid values are up(1), down(2), testing(3) according to RFC 2863. Value will be set to
         *                    invalid(0) in the case where an improper value is attempted to be set.
         */
        public void setAdminstatus(SnmpInterfaceStatus adminstatus) {
            if (adminstatus.getMibValue() > 3 ) {
                this.adminstatus = INVALID;
            } else {
                this.adminstatus = adminstatus;
            }
        }

        public SnmpInterfaceStatus getOperstatus() {
            return operstatus;
        }

        /**
         *
         * @param operstatus valid values are up(1), down(2), testing(3), unknown(4), dormant(5), notPresent(6),
         *                  lowerLayerDown(7) according to RFC 2863
         */
        public void setOperstatus(SnmpInterfaceStatus operstatus) {
                this.operstatus = operstatus;
        }

        /**
         *
         * @return PollStatus indicating whether getting ifAdminStatus and ifOperStatus succeeded
         */
        public PollStatus getStatus() {
            return m_status;
        }

        public void setStatus(PollStatus status) {
            m_status = status;
        }

        /**
         *
         * @return PollStatus indicating the status based on config and values of ifOperStatus and ifAdminStatus
         */
        public PollStatus getOperPollStatus() {
            return m_operPollStatus;
        }

        public void setOperPollStatus(PollStatus operPollStatus) {
            m_operPollStatus = operPollStatus;
        }

        /**
         *
         * @return PollStatus indicating current admin status based on config and values retrieved
         */
        public PollStatus getAdminPollStatus() {
            return m_adminPollStatus;
        }

        public void setAdminPollStatus(PollStatus adminPollStatus) {
            m_adminPollStatus = adminPollStatus;
        }
    }


    /**
     * <p>getSnmpinterfaces</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsSnmpInterface> getSnmpinterfaces() {
        return m_snmpinterfaces.values();
    }

    /**
     * <p>setSnmpinterfaces</p>
     *
     * @param snmpinterfaces a {@link java.util.List} object.
     */
    public void setSnmpinterfaces(List<OnmsSnmpInterface> snmpinterfaces) {
        if (snmpinterfaces == null || snmpinterfaces.isEmpty()) {
            LOG.debug("setting snmpinterfaces: got null, thread instantiated but at moment no interface found");
            return;
        }
    	// ifIndex -> operstatus
    	final Map<Integer, Integer> oldStatuses = new HashMap<Integer, Integer>();
    	for (final Integer ifIndex : m_snmpinterfaces.keySet()) {
    		final OnmsSnmpInterface iface = m_snmpinterfaces.get(ifIndex);
    		if (iface != null && iface.getIfOperStatus() != null) {
    			oldStatuses.put(ifIndex, iface.getIfOperStatus());
    		}
    	}
    	
    	m_snmpinterfaces.clear();
        for (OnmsSnmpInterface iface: snmpinterfaces) {
		LOG.debug("setting snmpinterface:", iface.toString());
        	if (iface != null && iface.getIfIndex() != null && iface.getIfIndex() > 0) {
        		final Integer oldStatus = oldStatuses.get(iface.getIfIndex());
                        LOG.debug("setting snmpinterface (oldStatus={}):{}", oldStatus, iface.toString());
                        // Note: If OpenNMS is restarted, the event is going to be sent no matter if it was sent before,
                        // if the current status of the interface is not up.
                        m_snmpinterfaces.put(iface.getIfIndex(), iface);
        		if (iface.getIfAdminStatus() != null &&
        				m_upValues.contains(iface.getIfAdminStatus()) &&
        				iface.getIfOperStatus() != null) {
        		    if (m_downValues.contains(iface.getIfOperStatus()) &&
        				(oldStatus == null || (iface.getIfOperStatus() != oldStatus))) {
                        sendOperDownEvent(iface);
                    }
        		    // if not literally down, also send the more detailed status event
        		    if (iface.getIfOperStatus() != SnmpInterfaceStatus.DOWN.getMibValue()) {
                        sendOperDownishEvent(SnmpInterfaceStatus.statusFromMibValue(iface.getIfOperStatus()), iface);
                    }
        		}
        	}
        }
    }

    //Constructor
    /**
     * <p>Constructor for PollableSnmpInterface.</p>
     *
     * @param parent a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface} object.
     */
    public PollableSnmpInterface(PollableInterface parent) {
        m_parent = parent;
        m_snmpinterfaces = new HashMap<Integer,OnmsSnmpInterface>();
    }

    /**
     * <p>getSchedule</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.Schedule} object.
     */
    public Schedule getSchedule() {
        return m_schedule;
    }

    /**
     * <p>setSchedule</p>
     *
     * @param schedule a {@link org.opennms.netmgt.scheduler.Schedule} object.
     */
    public void setSchedule(Schedule schedule) {
        m_schedule = schedule;
    }

    /**
     * <p>getSnmppollableconfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterfaceConfig} object.
     */
    public PollableSnmpInterfaceConfig getSnmppollableconfig() {
        return m_snmppollableconfig;
    }

    /**
     * <p>setSnmppollableconfig</p>
     *
     * @param snmppollableconfig a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterfaceConfig} object.
     */
    public void setSnmppollableconfig(
            PollableSnmpInterfaceConfig snmppollableconfig) {
        m_snmppollableconfig = snmppollableconfig;
    }

    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface} object.
     */
    public PollableInterface getParent() {
        return m_parent;
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext} object.
     */
    public PollContext getContext() {
        return getParent().getContext();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>isReady</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * <p>run</p>
     */
    @Override
    public void run() {
            if (getParent().polling()) {
                String location = getContext().getLocation(getParent().getNodeid());
                LOG.info("run: polling SNMP interfaces on package/interface {}/{} on primary address: {} at location {}",
                        getParent().getPackageName(), getName(), getParent().getIpaddress(), location);
                if (m_snmpinterfaces == null || m_snmpinterfaces.isEmpty()) {
                    LOG.debug("No Interface found. Doing nothing");
                } else {
                    LOG.debug("{} Interfaces found. Getting Statutes....", m_snmpinterfaces.size());
                    SnmpPollInterfaceMonitor pollMonitor = new SnmpPollInterfaceMonitor(getContext().getLocationAwareSnmpClient());
                    pollMonitor.setLocation(location);
                    pollMonitor.setInterval(getSnmppollableconfig().getInterval());
                    int maxiface = getMaxInterfacePerPdu();
                    if (maxiface == 0) maxiface=m_snmpinterfaces.size();
                    LOG.debug("Max Interface Per Pdu is: {}", maxiface);
                    List<SnmpMinimalPollInterface> mifaces = getSnmpMinimalPollInterface();
                    int start =0;
                    while (start + maxiface< m_snmpinterfaces.size()) {
                        doPoll(pollMonitor,mifaces.subList(start, start+maxiface));
                        start += maxiface;
                    }
                    doPoll(pollMonitor,mifaces.subList(start, m_snmpinterfaces.size()));
                }

            }  else {
                LOG.info("not polling: {}", getParent().getIpaddress());
            } // End if polling
    } //end Run method
        
    private void doPoll(SnmpPollInterfaceMonitor pollMonitor, List<SnmpMinimalPollInterface> mifaces) {
        
        LOG.info("doPoll: input interfaces number: {}", mifaces.size());
    	
        mifaces = pollMonitor.poll(getAgentConfig(), mifaces);
        
        boolean refresh = false;
        
        Date now = getDate();
        
        if (mifaces != null) {
            LOG.info("doPoll: PollerMonitor return interfaces number: {}", mifaces.size());
            for (SnmpMinimalPollInterface miface : mifaces) {
                LOG.debug("Working on interface with ifindex: {}", miface.getIfindex());
                LOG.debug("Interface PollStatus is {}", miface.getStatus().getStatusName());
                if (miface.getStatus().isUp()) {
                    OnmsSnmpInterface iface = m_snmpinterfaces.get(Integer.valueOf(miface.getIfindex()));

                    LOG.debug("Previous status Admin/Oper: {}/{}", iface.getIfAdminStatus(), iface.getIfOperStatus());
                    LOG.debug("Current status Admin/Oper: {}/{}", miface.getAdminstatus(), miface.getOperstatus());

                    // If the interface is Admin Up and the oper status is newly down, we generate an alarm.
                    if ( m_upValues.contains(miface.getAdminstatus())
                         && m_downValues.contains(miface.getOperstatus())
                         && !m_downValues.contains(SnmpInterfaceStatus.statusFromMibValue(iface.getIfOperStatus()))) {
                        sendOperDownEvent(iface);
                        if (miface.getOperstatus() != SnmpInterfaceStatus.DOWN) {
                            sendOperDownishEvent(miface.getOperstatus(), iface);
                        }
                        miface.setOperPollStatus(PollStatus.unavailable("ifOperStatus is " + miface.getOperstatus().getLabel()));
                        miface.setAdminPollStatus(PollStatus.available());
                    }
                    
                    // If the interface is Admin Up and Operational Up and previously not operational up
                    // we send the operational up event
                    if ( m_upValues.contains(miface.getAdminstatus())
                         && m_upValues.contains(miface.getOperstatus())
                         && !m_upValues.contains(SnmpInterfaceStatus.statusFromMibValue(iface.getIfOperStatus()))) {
                        sendOperUpEvent(iface);
                        miface.setOperPollStatus(PollStatus.available());
                        miface.setAdminPollStatus(PollStatus.available());
                    }
                                            
                    // if the interface is now admin down but was not previously, send the admin down event
                    if ( m_downValues.contains(miface.getAdminstatus())
                         && !m_downValues.contains(SnmpInterfaceStatus.statusFromMibValue(iface.getIfAdminStatus()))) {
                        sendAdminDownEvent(iface);
                        miface.setAdminPollStatus(PollStatus.unavailable("ifAdminStatus is " + miface.getAdminstatus().getLabel()));
                    }
                    
                    // if the interface is now admin up and was previously in a non-up state
                    // send the admin up event
                    if ( m_upValues.contains(miface.getAdminstatus())
                         && !m_upValues.contains(SnmpInterfaceStatus.statusFromMibValue(iface.getIfAdminStatus()))) {
                        sendAdminUpEvent(iface);
                        miface.setAdminPollStatus(PollStatus.available());
                    }

                    iface.setIfAdminStatus(miface.getAdminstatus().getMibValue());
                    iface.setIfOperStatus(miface.getOperstatus().getMibValue());
                    iface.setLastSnmpPoll(now);

                    // Save Data to Database
                    try {
                        update(iface);
                    } catch (Throwable e) {
                        LOG.warn("Failing updating Interface {} {}", iface.getIfName(), e.getLocalizedMessage());
                        refresh = true;
                    }
                } else {
                    LOG.debug("No {} data available for interface.", getContext().getServiceName());
                } //End if status OK
            } //end while on interface
            
            if (refresh) 
                getParent().getParent().refresh(getParent().getNodeid());
        } else {
            LOG.error("the monitor return null object");
        } //end If not null

    }
    
    private void update(OnmsSnmpInterface iface) {
        getContext().update(iface);
    }

    private void sendInterfaceEvent(final String uei, final OnmsSnmpInterface iface) {
        getContext().sendEvent(getContext().createEvent(uei, getParent().getNodeid(),
                getParent().getIpaddress(), getParent().getNetMask(), getDate(), iface));
    }

    private void sendAdminUpEvent(OnmsSnmpInterface iface) {
        sendInterfaceEvent(EventConstants.SNMP_INTERFACE_ADMIN_UP_EVENT_UEI, iface);
    }
    
    private void sendAdminDownEvent(OnmsSnmpInterface iface) {
        sendInterfaceEvent(EventConstants.SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI, iface);
    }

    private void sendOperUpEvent(OnmsSnmpInterface iface) {
        sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_UP_EVENT_UEI, iface);
    }
    
    private void sendOperDownEvent(OnmsSnmpInterface iface) {
        sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_DOWN_EVENT_UEI, iface);
    }

    private void sendOperDownishEvent(SnmpInterfaceStatus downishStatus, OnmsSnmpInterface iface) {
        switch (downishStatus) {
            case TESTING:
                sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_TESTING_EVENT_UEI, iface);
                break;
            case UNKNOWN:
                sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_UNKNOWN_EVENT_UEI, iface);
                break;
            case DORMANT:
                sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_DORMANT_EVENT_UEI, iface);
                break;
            case NOT_PRESENT:
                sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_NOT_PRESENT_EVENT_UEI, iface);
                break;
            case LOWER_LAYER_DOWN:
                sendInterfaceEvent(EventConstants.SNMP_INTERFACE_OPER_LOWER_LAYER_DOWN_EVENT_UEI, iface);
        }
    }

    private Date getDate() {
        return new Date();
    }

    private SnmpMinimalPollInterface getMinimalFromOnmsSnmpInterface(OnmsSnmpInterface iface) {
        SnmpInterfaceStatus adminStatus = SnmpInterfaceStatus.UP;
        SnmpInterfaceStatus operStatus = SnmpInterfaceStatus.UP;
        if (iface.getIfAdminStatus() != null) {
            adminStatus = SnmpInterfaceStatus.statusFromMibValue(iface.getIfAdminStatus().intValue());
        } else {
            iface.setIfAdminStatus(INVALID.getMibValue());
        }
        if (iface.getIfOperStatus() != null) {
            operStatus = SnmpInterfaceStatus.statusFromMibValue(iface.getIfOperStatus().intValue());
        } else {
            iface.setIfOperStatus(INVALID.getMibValue());
        }
        
        return new SnmpMinimalPollInterface(iface.getIfIndex().intValue(),adminStatus, operStatus);
    }
    
    private List<SnmpMinimalPollInterface> getSnmpMinimalPollInterface() {
        
        List<SnmpMinimalPollInterface> mifaces = new ArrayList<>();
        
        for (OnmsSnmpInterface iface: getSnmpinterfaces()) {
            mifaces.add(getMinimalFromOnmsSnmpInterface(iface));
        }
        return mifaces;
    }
    
    /**
     * <p>schedule</p>
     */
    public void schedule() {
        if (m_schedule == null)
            throw new IllegalStateException("Cannot schedule a service whose schedule is set to null");      
        m_schedule.schedule();
    }

    /**
     * <p>delete</p>
     */
    protected void delete() {
        m_schedule.unschedule();
    }
    
    /**
	 * <p>getAgentConfig</p>
	 *
	 * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
	 */
	public SnmpAgentConfig getAgentConfig() {
		return m_agentConfig;
	}

	/**
	 * <p>setAgentConfig</p>
	 *
	 * @param config a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
	 */
	public void setAgentConfig(SnmpAgentConfig config) {
		m_agentConfig = config;
	}

	/**
	 * <p>getCriteria</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCriteria() {
		return m_criteria;
	}

	/**
	 * <p>setCriteria</p>
	 *
	 * @param m_criteria a {@link java.lang.String} object.
	 */
	public void setCriteria(String m_criteria) {
		this.m_criteria = m_criteria;
	}

	/**
	 * <p>Getter for the field <code>maxInterfacePerPdu</code>.</p>
	 *
	 * @return a int.
	 */
	public int getMaxInterfacePerPdu() {
		return getAgentConfig().getMaxVarsPerPdu();
	}

    /**
     * <p>getUpValues</p>
     *
     * @return a {@link java.util.Set} object of {@link SnmpInterfaceStatus} objects.
     */
    public Set<SnmpInterfaceStatus> getUpValues() { return m_upValues; }

    /**
     * <p>setUpValues</p>
     *
     * @param upValues a {@link java.util.Set} object of {@link SnmpInterfaceStatus} objects.
     */
    public void setUpValues(Set<SnmpInterfaceStatus> upValues) { m_upValues = upValues; }

    /**
     * <p>getDownValues</p>
     *
     * @return a {@link java.util.Set} object of {@link SnmpInterfaceStatus} objects.
     */
    public Set<SnmpInterfaceStatus> getDownValues() { return m_downValues; }

    /**
     * <p>setDownValues</p>
     *
     * @param downValues a {@link java.util.Set} object of {@link SnmpInterfaceStatus} objects.
     */
    public void setDownValues(Set<SnmpInterfaceStatus> downValues) { m_downValues = downValues; }
}

