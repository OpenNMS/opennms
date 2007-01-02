/**
 * 
 */
package org.opennms.web.graph;

import java.util.Date;

import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.OnmsResource;

public class KscResultSet {
    private String m_title;
    private Date m_start;
    private Date m_end;
    private OnmsResource m_resource;
    private PrefabGraph m_prefabGraph;
    
    public KscResultSet(String title, Date start, Date end, OnmsResource resource, PrefabGraph prefabGraph) {
        m_title = title;
        m_start = start;
        m_end = end;
        m_resource = resource;
        m_prefabGraph = prefabGraph;
    }
    
    public String getTitle() {
        return m_title;
    }
    
    public Date getStart() {
        return m_start;
    }
    
    public Date getEnd() {
        return m_end;
    }
    
    public OnmsResource getResource() {
        return m_resource;
    }
    
    public PrefabGraph getPrefabGraph() {
        return m_prefabGraph;
    }
}