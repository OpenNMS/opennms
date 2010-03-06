package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;


public class PollableInterface {

    private int m_nodeid;
    
    private String m_ipaddress;
    
    private PollableNetwork m_parent;
    
    private HashMap<String, PollableSnmpInterface> m_pollablesnmpinterface;

    private String m_packageName;
    
    private boolean polling = true;
    
    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */

    protected void initialize() {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (UnknownHostException ex) {
            throw new UndeclaredThrowableException(ex);
        } catch (MarshalException ex) {
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
        // Log4j category
        //
        // Get interface address from NetworkInterface
        //


    
    public int getNodeid() {
        return m_nodeid;
    }

    public void setNodeid(int nodeid) {
        m_nodeid = nodeid;
    }

    public String getIpaddress() {
        return m_ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        m_ipaddress = ipaddress;
    }

    public PollableInterface(PollableNetwork parent) {
        m_parent = parent;
        m_pollablesnmpinterface = new HashMap<String,PollableSnmpInterface>();
    }

//    public PollableSnmpInterface createPollableSnmpInterface(Interface pkgIface) {
  public PollableSnmpInterface createPollableSnmpInterface(String name, String criteria, boolean hasPort, 
          int port, boolean hasTimeout, int timeout, boolean hasRetries, int retries, 
          boolean hasMaxVarsPerPdu,int maxVarsPerPdu) {

        PollableSnmpInterface iface = new PollableSnmpInterface(this);
        iface.setName(name);
        iface.setCriteria(criteria);
        InetAddress ipAddr = null;
		try {
			ipAddr = InetAddress.getByName(getIpaddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipAddr);
        if (hasPort) agentConfig.setPort(port);
        if (hasTimeout) agentConfig.setTimeout(timeout);
        if (hasRetries) agentConfig.setRetries(retries);
        if (hasMaxVarsPerPdu) agentConfig.setMaxVarsPerPdu(maxVarsPerPdu);

        iface.setAgentConfig(agentConfig);
               
        m_pollablesnmpinterface.put(name,iface);
        return iface;
    }
    
    protected void refresh() {
        for ( PollableSnmpInterface node: getSnmpinterfacepollableNodes().values()){
            node = (getContext().refresh(node));
        }
    }
    
    protected void suspend() {
          polling = false;  
    }

    protected void activate() {
        polling = true;
    }

    public HashMap<String,PollableSnmpInterface> getSnmpinterfacepollableNodes() {
        return m_pollablesnmpinterface;
    }


    public boolean polling() {
        return polling;
    }
    

    protected void delete() {
        for ( PollableSnmpInterface node: getSnmpinterfacepollableNodes().values()){
            node.delete();
        }
    }

    public PollableNetwork getParent() {
        return m_parent;
    }

    public void setParent(PollableNetwork parent) {
        m_parent = parent;
    }
    
    public PollContext getContext() {
        return getParent().getContext();
    }

    public String getPackageName() {
        return m_packageName;
    }

    public void setPackageName(String packageName) {
        m_packageName = packageName;
    }

}
