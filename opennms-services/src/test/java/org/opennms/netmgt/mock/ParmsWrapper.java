package org.opennms.netmgt.mock;

import org.opennms.netmgt.capsd.EventUtils;
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
		Parms parms = m_parms;
        return EventUtils.toString(parms);
	}
}
