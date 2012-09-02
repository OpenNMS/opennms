package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;

public final class OspfGeneralGroup extends AggregateTracker {

    public final static String OSPF_ROUTER_ID_ALIAS = "ospfRouterId";
    public final static String OSPF_ROUTER_ID_OID = ".1.3.6.1.2.1.14.1.1";
        
    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[1];
        int ndx = 0;

        /**
         * <P>
         * SYNTAX   RouterID
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         * "A  32-bit  integer  uniquely  identifying  the
         * router in the Autonomous System.
         * 
         * By  convention,  to  ensure  uniqueness,   this
         * should  default  to  the  value  of  one of the
         * router's IP interface addresses."
         * REFERENCE
         * "OSPF Version 2, C.1 Global parameters"
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,OSPF_ROUTER_ID_ALIAS,OSPF_ROUTER_ID_OID);

    }
    
    public static final String OSPF_GENERAL_GROUP_OID = ".1.3.6.1.2.1.14.1";

    private SnmpStore m_store;
    private InetAddress m_address;
    
    public OspfGeneralGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList);
    }
    
    public InetAddress getOspfRouterId() {
        return m_store.getIPAddress(OSPF_ROUTER_ID_ALIAS);        
    }
       
    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        log().warn("Error retrieving lldpLocalGroup from "+m_address+". "+msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving lldpLocalGroup from "+m_address+". "+msg);
    }

    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }


}
