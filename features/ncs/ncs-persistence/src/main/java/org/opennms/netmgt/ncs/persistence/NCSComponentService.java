package org.opennms.netmgt.ncs.persistence;

import org.opennms.netmgt.model.ncs.NCSComponent;

public interface NCSComponentService {

	NCSComponent getComponent(String type, String foreignSource, String foreignId);
	void addOrUpdateComponents(NCSComponent component);

}
