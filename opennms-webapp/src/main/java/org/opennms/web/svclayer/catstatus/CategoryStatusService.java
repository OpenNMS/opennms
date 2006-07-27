package org.opennms.web.svclayer.catstatus;

import java.util.Collection;

import org.opennms.web.svclayer.catstatus.model.StatusSection;

public interface CategoryStatusService {

	
	public Collection<StatusSection> getCategoriesStatus();
	
	
}
