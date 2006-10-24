package org.opennms.web.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DefaultGraphResource implements GraphResource, Comparable<DefaultGraphResource> {

    private String m_name;
    private Set<GraphAttribute> m_attributes;
    private String m_label;
    
    public DefaultGraphResource(String name, String label,
            Set<GraphAttribute> attributes) {
        m_name = name;
        m_label = label;
        m_attributes = attributes;
    }

    public String getName() {
        return m_name;
    }

    public String getLabel() {
        return m_label;
    }

    public Set<GraphAttribute> getAttributes() {
        return m_attributes;
    }

    public int compareTo(DefaultGraphResource o) {
        return getLabel().compareTo(o.getLabel());
    }
    
    /**
     * Sorts the List of DefaultGraphResources and returns a new List of the
     * generic type GraphResource.
     * 
     * @param resources list of DefaultGraphResource objects.  This will be
     *          sorted using Collections.sort, and note that this will modify
     *          the provided list.
     * @return a sorted list
     */
    public static List<GraphResource> sortIntoGraphResourceList(List<DefaultGraphResource> resources) {
        Collections.sort(resources);
        
        ArrayList<GraphResource> outputResources =
            new ArrayList<GraphResource>(resources.size());
        for (DefaultGraphResource resource : resources) {
            outputResources.add(resource);
        }

        return outputResources;
    }


}
