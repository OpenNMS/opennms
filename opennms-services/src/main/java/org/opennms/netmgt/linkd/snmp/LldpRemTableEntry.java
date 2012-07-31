package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;

public class LldpRemTableEntry extends SnmpStore {
    
    public final static String LLDP_REM_CHASSISID_SUBTYPE_ALIAS= "lldpRemChassisIdSubtype";
    public final static String LLDP_REM_CHASSISID_ALIAS = "lldpRemChassisId";
    public final static String LLDP_REM_PORTID_SUBTYPE_ALIAS = "lldpRemPortIdSubtype";
    public final static String LLDP_REM_PORTID_ALIAS = "lldpRemPortId";
    public final static String LLDP_REM_SYSNAME_ALIAS = "lldpRemSysName";
    
    public final static String LLDP_REM_CHASSISID_SUBTYPE_OID= ".1.0.8802.1.1.2.1.4.1.1.4";
    public final static String LLDP_REM_CHASSISID_OID = ".1.0.8802.1.1.2.1.4.1.1.5";
    public final static String LLDP_REM_PORTID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.4.1.1.6";
    public final static String LLDP_REM_PORTID_OID = ".1.0.8802.1.1.2.1.4.1.1.7";
    public final static String LLDP_REM_SYSNAME_OID = ".1.0.8802.1.1.2.1.4.1.1.9";

    public static final NamedSnmpVar[] lldpremtable_elemList = new NamedSnmpVar[] {
        /**
         *  "The type of encoding used to identify the chassis associated
         *  with the remote system."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, LLDP_REM_CHASSISID_SUBTYPE_ALIAS, LLDP_REM_CHASSISID_SUBTYPE_OID, 1),
        
        /**
         * "The string value used to identify the chassis component
         * associated with the remote system."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, LLDP_REM_CHASSISID_ALIAS, LLDP_REM_CHASSISID_OID, 2),

        /**
         * "The type of port identifier encoding used in the associated
         * 'lldpRemPortId' object."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, LLDP_REM_PORTID_SUBTYPE_ALIAS, LLDP_REM_PORTID_SUBTYPE_OID, 3),

        /**
         * "The string value used to identify the port component
            associated with the remote system."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, LLDP_REM_PORTID_ALIAS, LLDP_REM_PORTID_OID, 4),
        
        /**
         * "The string value used to identify the port component
         * associated with the remote system."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, LLDP_REM_SYSNAME_ALIAS, LLDP_REM_SYSNAME_OID, 5)

    };
    
    public static final String TABLE_OID = ".1.0.8802.1.1.2.1.4.1.1"; // start of table (GETNEXT)
    
    public LldpRemTableEntry() {
        super(lldpremtable_elemList);
    }

    public Integer getLldpRemChassisidSubtype() {
        return getInt32(LLDP_REM_CHASSISID_SUBTYPE_ALIAS);
    }
    
    public String getLldpRemChassiid() {
        return getHexString(LLDP_REM_CHASSISID_ALIAS);
    }
    
    public Integer getLldpRemPortidSubtype() {
        return getInt32(LLDP_REM_PORTID_SUBTYPE_ALIAS);
    }

    public String getLldpRemPortid() {
        return getDisplayString(LLDP_REM_PORTID_ALIAS);
    }
    
    public String getLldpRemSysname() {
        return getDisplayString(LLDP_REM_SYSNAME_ALIAS);
    }

}
