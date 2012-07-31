package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;

public final class LldpLocalGroup extends AggregateTracker {

    public final static String LLDP_LOC_CHASSISID_SUBTYPE_ALIAS = "lldpLocChassisIdSubtype";
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.3.1";
    
    public final static String LLDP_LOC_CHASSISID_ALIAS    = "lldpLocChassisId";
    public final static String LLDP_LOC_CHASSISID_OID    = ".1.0.8802.1.1.2.1.3.2";
    
    public final static String LLDP_LOC_SYSNAME_ALIAS = "lldpLocSysName";
    public final static String LLDP_LOC_SYSNAME_OID = ".1.0.8802.1.1.2.1.3.3";
    
    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[3];
        int ndx = 0;

        /**
         * <P>
         * "The type of encoding used to identify the chassis
         * associated with the local system."
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,LLDP_LOC_CHASSISID_SUBTYPE_ALIAS,LLDP_LOC_CHASSISID_SUBTYPE_OID);

        /**
         * <P>
         *  "The string value used to identify the chassis component
         *   associated with the local system."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_CHASSISID_ALIAS,LLDP_LOC_CHASSISID_OID);
        
        /**
         * <P>
         * "The string value used to identify the system name of the
         * local system.  If the local agent supports IETF RFC 3418,
         * lldpLocSysName object should have the same value of sysName
         * object."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_SYSNAME_ALIAS,LLDP_LOC_SYSNAME_OID);
    }
    
    public static final String LLDP_LOC_OID = ".1.0.8802.1.1.2.1.3";

    private SnmpStore m_store;
    private InetAddress m_address;
    
    public LldpLocalGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList);
    }
    
    public Integer getLldpLocChassisidSubType() {
        Integer type = m_store.getInt32(LLDP_LOC_CHASSISID_SUBTYPE_ALIAS);
        if (type == null) {
            return LldpMibConstants.LLDP_CHASSISID_SUBTYPE_LOCAL;
        }
        return type;
        
    }
    
    public String getLldpLocChassisid() {
        return m_store.getHexString(LLDP_LOC_CHASSISID_ALIAS);
    }
    
    public String getLldpLocSysname() {
        return m_store.getDisplayString(LLDP_LOC_SYSNAME_ALIAS);
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
