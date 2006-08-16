package org.opennms.netmgt.snmp.mock;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;

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
		Variable newVar;
		try {
		if (moTypeStr.equals("STRING")) {
			newVar = new OctetString(moValStr);
		} else if (moTypeStr.equals("Hex-STRING")) {
			newVar = OctetString.fromHexString(moValStr.trim());
		} else if (moTypeStr.equals("INTEGER")) {
			newVar = new Integer32(Integer.parseInt(moValStr));
		} else if (moTypeStr.equals("Gauge32")) {
			newVar = new Gauge32(Integer.parseInt(moValStr));
		} else if (moTypeStr.equals("Counter32")) {
			newVar = new Counter32(Long.parseLong(moValStr)); // a 32 bit counter can be > 2 ^ 31, which is > INTEGER_MAX
		} else if (moTypeStr.equals("Counter64")) {
			newVar = new Counter64(Long.parseLong(moValStr));
		} else if (moTypeStr.equals("TimeTicks")) {
			Integer ticksInt = Integer.parseInt( moValStr.substring( moValStr.indexOf("(") + 1, moValStr.indexOf(")") ) );
			newVar = new TimeTicks(ticksInt);
		} else if (moTypeStr.equals("OID")) {
			newVar = new OID(moValStr);
		} else {
			// Punt, assume it's a String
			newVar = new OctetString(moValStr);
		}
		} catch (Throwable t) {
			throw new UndeclaredThrowableException(t, "Could not convert value '" + moValStr + "' of type '" + moTypeStr + "' to SNMP object for OID " + oidStr);
		}
		newMO = new MOScalar(moOID, MOAccessImpl.ACCESS_READ_ONLY, newVar);
		return newMO;
	}
}
