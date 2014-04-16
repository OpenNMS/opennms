package org.opennms.netmgt.config.collector;

public interface Persistable {

	/**
	 * Determines whether the attribute should be persisted.
	 *
	 * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
	 * @return a boolean.
	 */
	boolean shouldPersist(ServiceParameters params);

}
