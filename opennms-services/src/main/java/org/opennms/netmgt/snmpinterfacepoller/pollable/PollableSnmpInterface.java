package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
    private boolean firstrun=true;
        
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
        if (snmpinterfaces != null && !snmpinterfaces.isEmpty()) { 
               Iterator<OnmsSnmpInterface> ite = snmpinterfaces.iterator();
               while (ite.hasNext()) {
                   OnmsSnmpInterface value = ite.next();
                   m_snmpinterfaces.put(value.getIfIndex(), value);
               }
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
            log().warn("run: polling snmp interfaces on package/interface " + getParent().getPackageName()+ "/" + getName() + "on primary address: " + getParent().getIpaddress());
            log().debug("run: polling snmp interfaces first run = " + firstrun);
            if (m_snmpinterfaces == null || m_snmpinterfaces.isEmpty()) {
                log().info("No Interface found. Doing nothing");
            } else {
                log().info(m_snmpinterfaces.size() + " Interfaces found. Getting Statutes....");
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
        
        if (mifaces != null) {
            log().info("doPoll: PollerMonitor return interfaces number: " + mifaces.size());
            Iterator<SnmpMinimalPollInterface> ite = mifaces.iterator();
            while (ite.hasNext()) {
                SnmpMinimalPollInterface miface = ite.next();
                log().debug("Working on interface with ifindex: " + miface.getIfindex());
                log().debug("Interface PollStatus is " + miface.getStatus().getStatusName());
                if (miface.getStatus().isUp()) {
                    OnmsSnmpInterface iface = m_snmpinterfaces.get(new Integer(miface.getIfindex()));
                    boolean changed = false;
                    log().debug("Previuos status Admin/Oper: " + iface.getIfAdminStatus() + "/" + iface.getIfOperStatus());
                    log().debug("Current status Admin/Oper: " + miface.getAdminstatus() + "/" + miface.getOperstatus());
                    
                    
                    // first run send all down events if suppressAdminDownEvent is false otherwise
                    // send all down events only for interface whose admin status is up
                    if (suppressAdminDownEvent()) {
                        if (firstrun && miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP && miface.getOperstatus() == SnmpMinimalPollInterface.IF_DOWN)
                    		sendOperDownEvent(iface);
                    } else {
                        if (firstrun && miface.getAdminstatus() == SnmpMinimalPollInterface.IF_DOWN) sendAdminDownEvent(iface);
                        if (firstrun && miface.getOperstatus() == SnmpMinimalPollInterface.IF_DOWN) sendOperDownEvent(iface);
                    }
                    // OperStatus management
                    if (iface.getIfOperStatus() != miface.getOperstatus()) {
                        changed = true;
                        iface.setIfOperStatus(new Integer(miface.getOperstatus()));
                        if (!firstrun) {
                            if (miface.getOperstatus() == SnmpMinimalPollInterface.IF_DOWN) {
                                sendOperDownEvent(iface);
                            }   
                            if (miface.getOperstatus() == SnmpMinimalPollInterface.IF_UP) {
                                sendOperUpEvent(iface);
                            }   
                        }
                    }
                    
                    //Admin status management
                    if (iface.getIfAdminStatus() != miface.getAdminstatus()) {
                        changed = true;
                        iface.setIfAdminStatus(new Integer(miface.getAdminstatus()));
                        if (!firstrun) {
                            if (miface.getAdminstatus() == SnmpMinimalPollInterface.IF_DOWN) {
                                sendAdminDownEvent(iface);
                            }   
                            if (miface.getAdminstatus() == SnmpMinimalPollInterface.IF_UP) {
                                sendAdminUpEvent(iface);
                            }   
                        }
                    }
                    log().debug("Interface changed = " + changed);
                    
                    // Save Data to Database
                    if (changed) {
                        update(iface);
                    }
                } else {
                    log().warn("run: " + getContext().getServiceName() + " not available, doing nothing.....");
                } //End if status OK
            } //end while on interface
            firstrun=false;
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
        return new SnmpMinimalPollInterface(iface.getIfIndex().intValue(),iface.getIfAdminStatus().intValue(), iface.getIfOperStatus().intValue());
    }
    
    private List<SnmpMinimalPollInterface> getSnmpMinimalPollInterface() {
        List<SnmpMinimalPollInterface> mifaces = new ArrayList<SnmpMinimalPollInterface>();
        Iterator<OnmsSnmpInterface> ite = getSnmpinterfaces().iterator();
        while (ite.hasNext()) {
            mifaces.add(getMinimalFromOnmsSnmpInterface(ite.next()));
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

	public boolean suppressAdminDownEvent() {
		return getContext().suppressAdminDownEvent();
	}

	public int getMaxInterfacePerPdu() {
		return maxInterfacePerPdu;
	}

	public void setMaxInterfacePerPdu(int maxInterfacePerPdu) {
		this.maxInterfacePerPdu = maxInterfacePerPdu;
	}


}

