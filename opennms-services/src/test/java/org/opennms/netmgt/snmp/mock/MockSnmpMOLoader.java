package org.opennms.netmgt.snmp.mock;

import java.util.ArrayList;

import org.snmp4j.agent.ManagedObject;

/*
 * @author Jeff Gehlbach <jeffg jeffg org>
 */
public interface MockSnmpMOLoader {
	public ArrayList<ManagedObject> loadMOs();
}
