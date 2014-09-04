package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.model.LldpLink.LldpPortIdSubType;
import org.opennms.netmgt.snmp.SnmpValue;

public abstract class LldpHelper {

    public static String decodeLldpChassisId(final SnmpValue lldpchassisid, Integer lldpLocChassisidSubType) {
    	String  lldpLocChassisId = lldpchassisid.toDisplayString();
    	if (lldpLocChassisidSubType.intValue() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS.getValue().intValue())
    		lldpLocChassisId = lldpchassisid.toHexString();
    	if (lldpLocChassisidSubType.intValue() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS.getValue().intValue())
    		lldpLocChassisId = InetAddressUtils.str(lldpchassisid.toInetAddress());
    	return lldpLocChassisId;
    }

	public static String decodeLldpLink(Integer lldpPortIdSubType,SnmpValue lldpportid) {
		String lldpPortId = lldpportid.toDisplayString();
    	if (lldpPortIdSubType.intValue() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS.getValue().intValue())
    		lldpPortId = lldpportid.toHexString();
    	if (lldpPortIdSubType.intValue() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_NETWORKADDRESS.getValue())
    		lldpPortId = InetAddressUtils.str(lldpportid.toInetAddress());
    	return lldpPortId;

	}

}
