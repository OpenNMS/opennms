package org.opennms.plugins.elasticsearch.rest;

import java.util.Map;

public interface NodeCache {

	public Map getEntry(Long key);

	public void refreshEntry(Long key);

}