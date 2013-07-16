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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmpinterfacepoller.SnmpPollInterfaceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableSnmpInterface
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableSnmpInterface implements ReadyRunnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(PollableSnmpInterface.class);

    private volatile Schedule m_schedule;

    private HashMap<Integer,OnmsSnmpInterface> m_snmpinterfaces;
    
    private PollableSnmpInterfaceConfig m_snmppollableconfig;

    private PollableInterface m_parent;
    
    private String m_name;
        
    private String m_criteria;
        
    private SnmpAgentConfig m_agentConfig;
    
    public class SnmpMinimalPollInterface {
        
        final static int IF_UP=1;
        final static int IF_DOWN=2;
        final static int IF_UNKNOWN=0;
        
        private final String[] s_statusNames = {"Unknown","InterfaceUp", "InterfaceDown"}; 
        
        int ifindex;
        int adminstatus;
        int operstatus;
        
        PollStatus m_status;
        
        public SnmpMinimalPollInterface(int ifindex, int adminstatus,
                int operstatus) {
            this.ifindex = ifindex;
            this.adminstatus = adminstatus;
            this.operstatus = operstatus;
            m_status = PollStatus.unknown();
        }

        public String decodeStatus(int status) {
            if (status >= 0 && status <= 2) {
                return s_statusNames[status];
            }
            return s_statusNames[0];
        }

        public int getIfindex() {
            return ifindex;
        }
        public void setIfindex(int ifindex) {
            this.ifindex = ifindex;
        }
        public int getAdminstatus() {
            return adminstatus;
        }
        
        public void setAdminstatus(int adminstatus) {
            if (adminstatus > 2 )
                this.adminstatus = IF_UNKNOWN;
            else this.adminstatus = adminstatus;
        }
        
        public int getOperstatus() {
            return operstatus;
        }
        
        public void setOperstatus(int operstatus) {
            if (operstatus > 2 )
                this.operstatus = IF_UNKNOWN;
            this.operstatus = operstatus;
        }

        public PollStatus getStatus() {
            return m_status;
        }

        public void setStatus(PollStatus status) {
            m_status = status;
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
    	if (snmpinterfaces == null) {
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
                        // Note: If OpenNMS is restarted, the event is going to be sent no matter if it was sent before, if the current status of the interface is down.        
                        m_snmpinterfaces.put(iface.getIfIndex(), iface);
        		if (iface.getIfAdminStatus() != null &&
        				iface.getIfAdminStatus().equals(SnmpMinimalPollInterface.IF_UP) && 
        				iface.getIfOperStatus() != null &&
        				iface.getIfOperStatus().equals(SnmpMinimalPollInterface.IF_DOWN) &&
        				(oldStatus == null || (iface.getIfOperStatus().intValue() != oldStatus.intValue()))) {
        			sendOperDownEvent(iface);
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
    public PollableSnmpInterface(
            PollableInterface parent) {
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
            LOG.info("run: polling SNMP interfaces on package/interface {}/{} on primary address: {}", getParent().getPackageName(), getName(), getParent().getIpaddress());
            if (m_snmpinterfaces == null || m_snmpinterfaces.isEmpty()) {
                LOG.debug("No Interface found. Doing nothing");
            } else {
                LOG.debug("{} Interfaces found. Getting Statutes....", m_snmpinterfaces.size());
            	SnmpPollInterfaceMonitor pollMonitor = new SnmpPollInterfaceMonitor();
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

                    LOG.debug("Previuos status Admin/Oper: {}/{}", iface.getIfAdminStatus(), iface.getIfOperStatus());
                    LOG.debug("Current status Admin/Oper: {}/{}", miface.getAdminstatus(), miface.getOperstatus());
                    
                    // If the interface is Admin Up, and the interface is Operational Down, we generate an alarm.
                    if ( miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP
                      && iface.getIfAdminStatus() == SnmpMinimalPollInterface.IF_UP
                      && miface.getOperstatus() == SnmpMinimalPollInterface.IF_DOWN 
                      && iface.getIfOperStatus() == SnmpMinimalPollInterface.IF_UP) {
                      sendOperDownEvent(iface);
                    } 
                    
                    // If the interface is Admin Up, and the interface is Operational Up, we generate a clean alarm
                    // if was previuos down in alarm table
                    if ( miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP
                        && iface.getIfAdminStatus() == SnmpMinimalPollInterface.IF_UP
                        && miface.getOperstatus() == SnmpMinimalPollInterface.IF_UP 
                        && iface.getIfOperStatus() == SnmpMinimalPollInterface.IF_DOWN ) {
                        sendOperUpEvent(iface);
                    } 
                                            
                    if ( miface.getAdminstatus() == SnmpMinimalPollInterface.IF_DOWN 
                            && iface.getIfAdminStatus() == SnmpMinimalPollInterface.IF_UP) {
                            sendAdminDownEvent(iface);
                    } 
                    
                    if ( miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP 
                            && iface.getIfAdminStatus() == SnmpMinimalPollInterface.IF_DOWN
                            && miface.getOperstatus() != SnmpMinimalPollInterface.IF_UP) {
                            sendAdminUpEvent(iface);
                    }

                    if ( miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP 
                            && iface.getIfAdminStatus() == SnmpMinimalPollInterface.IF_DOWN
                            && miface.getOperstatus() == SnmpMinimalPollInterface.IF_UP) {
                            sendAdminUpEvent(iface);
                            sendOperUpEvent(iface);
                    }

                    iface.setIfAdminStatus(Integer.valueOf(miface.getAdminstatus()));
                    iface.setIfOperStatus(Integer.valueOf(miface.getOperstatus()));
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
    
    private void sendAdminUpEvent(OnmsSnmpInterface iface) {
        getContext().sendEvent(getContext().createEvent(EventConstants.SNMP_INTERFACE_ADMIN_UP_EVENT_UEI, 
                                                        getParent().getNodeid(), getParent().getIpaddress(), getDate(), iface));       
    }
    
    private void sendAdminDownEvent(OnmsSnmpInterface iface) {
        getContext().sendEvent(getContext().createEvent(EventConstants.SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI, 
                                                        getParent().getNodeid(), getParent().getIpaddress(), getDate(), iface));
    }
    
    private void sendOperUpEvent(OnmsSnmpInterface iface) {
        getContext().sendEvent(getContext().createEvent(EventConstants.SNMP_INTERFACE_OPER_UP_EVENT_UEI, 
                                                        getParent().getNodeid(), getParent().getIpaddress(), getDate(), iface));
        
    }
    
    private void sendOperDownEvent(OnmsSnmpInterface iface) {
        getContext().sendEvent(getContext().createEvent(EventConstants.SNMP_INTERFACE_OPER_DOWN_EVENT_UEI, 
                                                        getParent().getNodeid(), getParent().getIpaddress(), getDate(), iface));
    }
    
    private Date getDate() {
        return new Date();
    }

    private SnmpMinimalPollInterface getMinimalFromOnmsSnmpInterface(OnmsSnmpInterface iface) {
        int adminStatus = SnmpMinimalPollInterface.IF_UP;
        int operStatus = SnmpMinimalPollInterface.IF_UP;
        if (iface.getIfAdminStatus() != null) {
            adminStatus = iface.getIfAdminStatus().intValue();
        } else {
            iface.setIfAdminStatus(SnmpMinimalPollInterface.IF_UNKNOWN);
        }
        if (iface.getIfOperStatus() != null) {
            operStatus = iface.getIfOperStatus().intValue();
        } else {
            iface.setIfOperStatus(SnmpMinimalPollInterface.IF_UNKNOWN);
        }
        
        return new SnmpMinimalPollInterface(iface.getIfIndex().intValue(),adminStatus, operStatus);
    }
    
    private List<SnmpMinimalPollInterface> getSnmpMinimalPollInterface() {
        
        List<SnmpMinimalPollInterface> mifaces = new ArrayList<SnmpMinimalPollInterface>();
        
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
}

