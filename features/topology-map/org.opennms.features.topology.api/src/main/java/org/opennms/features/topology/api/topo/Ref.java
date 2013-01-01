package org.opennms.features.topology.api.topo;

public interface Ref {
	
	/**
	 * An identifier unique to the provider that 'owns' this reference
	 * @return
	 */
	public String getId();

	/**
	 * A string used to identify the provider this belongs to.
	 * 
	 * May only container characters that make for a reasonable java identifier
	 * such as letters digits and underscore (no colons, periods, commans etc.)
	 * 
	 */
	public String getNamespace();
	
}
