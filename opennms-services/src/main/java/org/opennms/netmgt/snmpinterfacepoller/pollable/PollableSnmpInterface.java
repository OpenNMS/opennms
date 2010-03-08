package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmpinterfacepoller.SnmpPollInterfaceMonitor;



public class PollableSnmpInterface implements ReadyRunnable {

    private volatile Schedule m_schedule;

    private HashMap<Integer,OnmsSnmpInterface> m_snmpinterfaces;
    
    private PollableSnmpInterfaceConfig m_snmppollableconfig;

    private PollableInterface m_parent;
    
    private String m_name;
        
    private String m_criteria;
        
    private SnmpAgentConfig m_agentConfig;
    
    private int maxInterfacePerPdu = 0;
        
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
      
    
    public Collection<OnmsSnmpInterface> getSnmpinterfaces() {
        return m_snmpinterfaces.values();
    }

    public void setSnmpinterfaces(List<OnmsSnmpInterface> snmpinterfaces) {
        for (OnmsSnmpInterface value: snmpinterfaces) {
            m_snmpinterfaces.put(value.getIfIndex(), value);            
        }
    }

    //Constructor
    public PollableSnmpInterface(
            PollableInterface parent) {
        m_parent = parent;
        m_snmpinterfaces = new HashMap<Integer,OnmsSnmpInterface>();

    }
    
    public Schedule getSchedule() {
        return m_schedule;
    }

    public void setSchedule(Schedule schedule) {
        m_schedule = schedule;
    }

    public PollableSnmpInterfaceConfig getSnmppollableconfig() {
        return m_snmppollableconfig;
    }

    public void setSnmppollableconfig(
            PollableSnmpInterfaceConfig snmppollableconfig) {
        m_snmppollableconfig = snmppollableconfig;
    }

    public PollableInterface getParent() {
        return m_parent;
    }
    
    public PollContext getContext() {
        return getParent().getContext();
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public boolean isReady() {
        return true;
    }

    public void run() {        
        if (getParent().polling()) {
            log().info("run: polling snmp interfaces on package/interface " + getParent().getPackageName()+ "/" + getName() + "on primary address: " + getParent().getIpaddress());
            if (m_snmpinterfaces == null || m_snmpinterfaces.isEmpty()) {
                log().debug("No Interface found. Doing nothing");
            } else {
                log().debug(m_snmpinterfaces.size() + " Interfaces found. Getting Statutes....");
            	SnmpPollInterfaceMonitor pollMonitor = new SnmpPollInterfaceMonitor();
        		int maxiface = getMaxInterfacePerPdu();
        		if (maxiface == 0) maxiface=m_snmpinterfaces.size();
        		log().debug("Max Interface Per Pdu is: " + maxiface);
        		List<SnmpMinimalPollInterface> mifaces = getSnmpMinimalPollInterface();
        		int start =0;
        		while (start + maxiface< m_snmpinterfaces.size()) {
            		doPoll(pollMonitor,mifaces.subList(start, start+maxiface));
            		start += maxiface;
        		}
        		doPoll(pollMonitor,mifaces.subList(start, m_snmpinterfaces.size()));
            }
            
        }  else {
            log().info("not polling: " + getParent().getIpaddress());
        } // End if polling
    } //end Run method
    
    private void doPoll(SnmpPollInterfaceMonitor pollMonitor, List<SnmpMinimalPollInterface> mifaces) {
        
        log().info("doPoll: input interfaces number: " + mifaces.size());
    	
        mifaces = pollMonitor.poll(getAgentConfig(), mifaces);
        
        boolean refresh = false;
        
//        Date now = new Date();
        
        if (mifaces != null) {
            log().info("doPoll: PollerMonitor return interfaces number: " + mifaces.size());
            for (SnmpMinimalPollInterface miface : mifaces) {
                log().debug("Working on interface with ifindex: " + miface.getIfindex());
                log().debug("Interface PollStatus is " + miface.getStatus().getStatusName());
                if (miface.getStatus().isUp()) {
                    OnmsSnmpInterface iface = m_snmpinterfaces.get(new Integer(miface.getIfindex()));

                    log().debug("Previuos status Admin/Oper: " + iface.getIfAdminStatus() + "/" + iface.getIfOperStatus());
                    log().debug("Current status Admin/Oper: " + miface.getAdminstatus() + "/" + miface.getOperstatus());
                    
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

                    iface.setIfAdminStatus(new Integer(miface.getAdminstatus()));
                    iface.setIfOperStatus(new Integer(miface.getOperstatus()));
                    //iface.setPoll("P");
                    //iface.setLastSnmpPoll(now);
                                    
                    
                    // Save Data to Database
                    try {
                        update(iface);
                    } catch (Exception e) {
                        log().warn("Error updating Interface" + iface.getIfName()+" " + e.getLocalizedMessage());
                        refresh = true;
                    }
                } else {
                    log().warn("run: " + getContext().getServiceName() + " not available, doing nothing.....");
                } //End if status OK
            } //end while on interface
            
            if (refresh) getParent().refresh();
        } else {
            log().error("the monitor return null object");
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
    
    public void schedule() {
        if (m_schedule == null)
            throw new IllegalStateException("Cannot schedule a service whose schedule is set to null");      
        m_schedule.schedule();
    }

    protected void delete() {
        m_schedule.unschedule();
    }
    
    private Category log() {
        return ThreadCategory.getInstance(PollableService.class);
    }

	public SnmpAgentConfig getAgentConfig() {
		return m_agentConfig;
	}

	public void setAgentConfig(SnmpAgentConfig config) {
		m_agentConfig = config;
	}

	public String getCriteria() {
		return m_criteria;
	}

	public void setCriteria(String m_criteria) {
		this.m_criteria = m_criteria;
	}

	public int getMaxInterfacePerPdu() {
		return maxInterfacePerPdu;
	}

	public void setMaxInterfacePerPdu(int maxInterfacePerPdu) {
		this.maxInterfacePerPdu = maxInterfacePerPdu;
	}

}

