package org.opennms.features.topology.api.topo;

/**
 * The interface is extended by plugin developers to allow the setting of criteria for their Providers
 * 
 * @author brozow
 *
 */
public interface Criteria {
	
	
	enum ElementType { GRAPH, VERTEX, EDGE }
	
	/**
	 * This criteria applies to only providers of the indicated type
	 */
	public ElementType getType();
	
	/**
	 * This criteria only applies to providers for this namespace
	 */
	public String getNamespace();

}
