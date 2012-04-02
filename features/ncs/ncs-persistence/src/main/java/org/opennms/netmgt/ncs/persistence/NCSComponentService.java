package org.opennms.netmgt.ncs.persistence;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.rest.NCSRestService.ComponentList;

public interface NCSComponentService {
	public EventIpcManager getEventIpcManager();
	public void setEventIpcManager(EventIpcManager eventIpcManager);

	public NCSComponent getComponent(String type, String foreignSource, String foreignId);
	public NCSComponent addOrUpdateComponents(NCSComponent component);
	public ComponentList findComponentsWithAttribute(String string, String string2);
	public void deleteComponent(String type, String foreignSource, String foreignId);
	public NCSComponent addSubcomponent(String type, String foreignSource, String foreignId, NCSComponent subComponent);

}
