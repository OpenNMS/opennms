package org.opennms.netmgt.model;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OnmsResource implements Comparable<OnmsResource> {

    private String m_name;
    private Set<OnmsAttribute> m_attributes;
    private String m_label;
    private OnmsResourceType m_resourceType;
    private List<OnmsResource> m_resources;
    private OnmsResource m_parent = null;
    
    public OnmsResource(String name, String label,
            OnmsResourceType resourceType, Set<OnmsAttribute> attributes) {
        m_name = name;
        m_label = label;
        m_resourceType = resourceType;
        m_attributes = attributes;
        m_resources = new LinkedList<OnmsResource>();
    }
    
    public OnmsResource(String name, String label,
            OnmsResourceType resourceType, Set<OnmsAttribute> attributes,
            List<OnmsResource> resources) {
        m_name = name;
        m_label = label;
        m_resourceType = resourceType;
        m_attributes = attributes;
        m_resources = resources;
    }

    public String getName() {
        return m_name;
    }

    public String getLabel() {
        return m_label;
    }

    public OnmsResourceType getResourceType() {
        return m_resourceType;
    }

    public Set<OnmsAttribute> getAttributes() {
        return m_attributes;
    }
    
    public List<OnmsResource> getChildResources() {
        return m_resources;
    }

    public int compareTo(OnmsResource o) {
        return getLabel().compareTo(o.getLabel());
    }
    
    /**
     * Sorts the List of Resources and returns a new List of the
     * generic type Resource.
     * 
     * @param resources list of Resource objects.  This will be
     *          sorted using Collections.sort, and note that this will modify
     *          the provided list.
     * @return a sorted list
     */
    public static List<OnmsResource> sortIntoResourceList(List<OnmsResource> resources) {
        Collections.sort(resources);
        
        ArrayList<OnmsResource> outputResources =
            new ArrayList<OnmsResource>(resources.size());
        for (OnmsResource resource : resources) {
            outputResources.add(resource);
        }

        return outputResources;
    }

    public void setParent(OnmsResource parent) {
        m_parent = parent;
    }
    
    public OnmsResource getParent() {
        return m_parent;
    }
    
    public String getId() {
        String thisId = encode(m_resourceType.getName()) + "[" + encode(m_name) + "]";
        if (getParent() != null) {
            return getParent().getId() + "." + thisId;
        } else {
            return thisId;
        }
    }

    public String getLink() {
        return m_resourceType.getLinkForResource(this);
    }
    
    public static String createResourceId(String... resources) {
        if ((resources.length % 2) != 0) {
            throw new IllegalArgumentException("Values passed as resources parameter must be in pairs");
        }
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < (resources.length / 2); i++) {
            if (buf.length() > 0) {
                buf.append(".");
            }
            
            buf.append(resources[i * 2]);
            buf.append("[");
            buf.append(encode(resources[(i * 2) + 1]));
            buf.append("]");
        }
        return buf.toString();
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
    }

}
