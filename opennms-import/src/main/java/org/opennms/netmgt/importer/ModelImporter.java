package org.opennms.netmgt.importer;

import org.opennms.netmgt.eventd.EventIpcManager;




/**
 * Model Importer
 *
 */
public class ModelImporter extends BaseImporter {

    private EventIpcManager m_eventManager;

	public ModelImporter() {
    }

	public EventIpcManager getEventManager() {
	    return m_eventManager;
	}

	public void setEventManager(EventIpcManager eventManager) {
		m_eventManager = eventManager;
	}
    
}
