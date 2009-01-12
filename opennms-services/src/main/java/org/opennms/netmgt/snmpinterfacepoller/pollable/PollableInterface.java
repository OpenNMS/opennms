package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmpinterfacepoller.Interface;
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

    public PollableSnmpInterface createPollableSnmpInterface(Interface pkgIface) {
        PollableSnmpInterface iface = new PollableSnmpInterface(this);
        iface.setName(pkgIface.getName());
        iface.setCriteria(pkgIface.getCriteria());
        InetAddress ipAddr = null;
		try {
			ipAddr = InetAddress.getByName(getIpaddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipAddr);
        if (pkgIface.hasPort()) agentConfig.setPort(pkgIface.getPort());
        if (pkgIface.hasTimeout()) agentConfig.setTimeout(pkgIface.getTimeout());
        if (pkgIface.hasRetry()) agentConfig.setRetries(pkgIface.getRetry());
        if (pkgIface.hasMaxVarsPerPdu()) agentConfig.setMaxVarsPerPdu(pkgIface.getMaxVarsPerPdu());

        iface.setAgentConfig(agentConfig);
        
        if(pkgIface.hasMaxInterfacePerPdu()) iface.setMaxInterfacePerPdu(pkgIface.getMaxInterfacePerPdu());
        
        m_pollablesnmpinterface.put(pkgIface.getName(),iface);
        return iface;
    }
    
    protected void refresh() {
        Iterator<PollableSnmpInterface> ite = getSnmpinterfacepollableNodes().values().iterator();
        while (ite.hasNext()) {
            PollableSnmpInterface node = ite.next();
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
        Iterator<PollableSnmpInterface> ite = getSnmpinterfacepollableNodes().values().iterator();
        while (ite.hasNext()) {
            ite.next().delete();
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
