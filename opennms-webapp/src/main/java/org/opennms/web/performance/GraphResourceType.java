package org.opennms.web.performance;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.PrefabGraph;

public interface GraphResourceType {
    /**
     * Provides a unique name for this resource type.
     * @return unique name
     */
    public String getName();
    
    /**
     * Provides a human-friendly label for this resource type.  It is
     * particularly used in the webUI to describe this resource type.
     * @return human-friendly label
     */
    public String getLabel();
    
    /**
     * Checks whether this resource type is on a specific node.  If possible,
     * this should have less overhead than calling #getResourcesForNode(int).
     * 
     * @param nodeId node ID to check
     * @return true if this resource type is on this node, false otherwise
     */
    public boolean isResourceTypeOnNode(int nodeId);
    
    /**
     * Gets a list of resources on a specific node.
     * 
     * @param nodeId node ID for which to get resources
     * @return list of resources
     */
    public List<GraphResource> getResourcesForNode(int nodeId);
    
    /**
     * Gets a relative path for an attribute on a resource of this resource
     * type.
     * 
     * @param resourceParent path to the parent of this resource
     * @param resource the resource on this resource type
     * @param attribute the attribute on the specific resource
     * @return relative path
     */
    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute);
    
    
    /**
     * Checks whether this resource type is on a specific domain.  If possible,
     * this should have less overhead than calling #getResourcesForDomain(String).
     * 
     * @param domain domain to check
     * @return true if this resource type is on this domain, false otherwise
     */
    public boolean isResourceTypeOnDomain(String domain);

    /**
     * Gets a list of resources on a specific domain.
     * 
     * @param domain domain for which to get resources
     * @return list of resources
     */
    public List<GraphResource> getResourcesForDomain(String domain);

    /**
     * Gets a list of available prefabricated graph definitions given a
     * set of attributes.
     * 
     * @param attributes set of attributes that are available for graphing
     * @return list of prefabricated graphs that can be generated with the
     *         provided attributes
     */
    public List<PrefabGraph> getAvailablePrefabGraphs(Set<GraphAttribute> attributes);
    
    public PrefabGraph getPrefabGraph(String name);
    
    public GraphModel getModel();

    public File getRrdDirectory();
}
