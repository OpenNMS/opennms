package org.opennms.netmgt.mock;

import java.util.Enumeration;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

public class ParmsWrapper {
	Parms m_parms;
	
	public ParmsWrapper(Parms parms) {
		m_parms = parms;
	}
	
	public Parms getParms() {
		return m_parms;
	}
	
	public String toString() {
		if (m_parms.getParmCount() == 0) {
			return "Parms: (none)\n";
		}
		
		StringBuffer b = new StringBuffer();
		b.append("Parms:\n");
		for (Enumeration e = m_parms.enumerateParm(); e.hasMoreElements(); ) {
			Parm p = (Parm) e.nextElement();
			b.append(" ");
			b.append(p.getParmName());
			b.append(" = ");
			b.append(new ValueWrapper(p.getValue()));
			b.append("\n");
		}
		b.append("End Parms\n");
		return b.toString();
	}
}
