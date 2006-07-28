package org.opennms.netmgt.snmp.mock;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

public class PropsMockSnmpMOLoaderImpl implements MockSnmpMOLoader {

	private File m_moFile;
	
	public PropsMockSnmpMOLoaderImpl(File myMoFile) {
		m_moFile = myMoFile;
	}
	
	public ArrayList<ManagedObject> loadMOs() {
		Properties moProps = new Properties();
		ArrayList<ManagedObject> moList = new ArrayList<ManagedObject>();
		
		try {
			moProps.load( new FileInputStream(m_moFile) );
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		Enumeration moKeys = moProps.keys();
		while ( moKeys.hasMoreElements() ) {
			String oidStr = moKeys.nextElement().toString();
			ManagedObject newMo = getMOFromPropString(oidStr, moProps.getProperty(oidStr));
			moList.add(newMo);
		}
		return moList;
	}
	
	protected static ManagedObject getMOFromPropString(String oidStr, String valStr) {
		OID moOID = new OID(oidStr);
		String moTypeStr = valStr.substring(0, valStr.indexOf(":"));
		String moValStr = valStr.substring(valStr.indexOf(":") + 2);
		ManagedObject newMO;
		if (moTypeStr.equals("STRING")) {
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, new OctetString(moValStr));
		} else if (moTypeStr.equals("Hex-STRING")) {
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, OctetString.fromHexString(moValStr));
		} else if (moTypeStr.equals("INTEGER") || moTypeStr.equals("Gauge32") || moTypeStr.equals("Counter32")) {
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, new Integer32(Integer.parseInt(moValStr)));			
		} else if (moTypeStr.equals("TimeTicks")) {
			Integer ticksInt = Integer.parseInt( moValStr.substring( moValStr.indexOf("(") + 1, moValStr.indexOf(")") ) );
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, new Integer32(ticksInt));
		} else if (moTypeStr.equals("OID")) {
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, new OID(moValStr));
		} else {
			// Punt, assume it's a String
			newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, new OctetString(moValStr)); 
		}
		return newMO;
	}
	
}
