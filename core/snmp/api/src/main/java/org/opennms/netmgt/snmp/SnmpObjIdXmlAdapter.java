package org.opennms.netmgt.snmp;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SnmpObjIdXmlAdapter extends XmlAdapter<String, SnmpObjId> {

	@Override
	public String marshal(SnmpObjId snmpObjId) throws Exception {
		return snmpObjId.toString();
	}

	@Override
	public SnmpObjId unmarshal(String oid) throws Exception {
		return SnmpObjId.get(oid);
	}

}
