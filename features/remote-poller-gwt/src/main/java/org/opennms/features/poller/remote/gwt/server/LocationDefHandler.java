/**
 * 
 */
package org.opennms.features.poller.remote.gwt.server;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

interface LocationDefHandler {
	public void start(final int size);
	public void handleLocation(final OnmsMonitoringLocationDefinition def);
	public void finish();
}