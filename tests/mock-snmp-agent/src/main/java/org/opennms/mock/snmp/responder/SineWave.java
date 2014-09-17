package org.opennms.mock.snmp.responder;

import java.text.DecimalFormat;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

public class SineWave implements DynamicVariable {
        @Override
	public Variable getVariableForOID(String oidStr) {
		String[] oids = oidStr.split("\\.");
		Integer instance = Integer.parseInt(oids[oids.length-1]);
		
		// Convert the instance from degrees to radians and calculate the sin
		double x = Math.sin(instance.doubleValue() * Math.PI / 180);
		
		// Round the value to two decimal places and multiply it by 100
		// Leaving us with an integer
		DecimalFormat epsilum = new DecimalFormat("#.##");
		x = Double.valueOf(epsilum.format(x)) * 100;
	
		// Convert the result back to an integer
		return new Integer32((int)x);
	}
}
