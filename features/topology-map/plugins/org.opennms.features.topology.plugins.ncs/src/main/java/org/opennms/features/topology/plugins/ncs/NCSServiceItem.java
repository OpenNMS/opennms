package org.opennms.features.topology.plugins.ncs;

import org.opennms.netmgt.model.ncs.NCSComponent;

public class NCSServiceItem {

    private Long m_id;
    private String m_name;
    private String m_foreignSource;
    private boolean m_isRoot = false;
    private boolean m_childrenAllowed = false;
    private String m_type;

    public NCSServiceItem(NCSComponent ncsComponent) {
        m_id = ncsComponent.getId();
        m_name = ncsComponent.getName();
        m_foreignSource = ncsComponent.getForeignSource();
        m_type = ncsComponent.getType();
    }
    
    public Long getId() {
        return m_id;
    }

    public void setId(Long id) {
        m_id = id;
    }

    public String getName() {
        if(m_name == null || m_name.equals("")) {
            return m_type + " Has No Name";
        }
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_foreignSource == null) ? 0 : m_foreignSource.hashCode());
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NCSServiceItem other = (NCSServiceItem) obj;
        if (m_foreignSource == null) {
            if (other.m_foreignSource != null)
                return false;
        } else if (!m_foreignSource.equals(other.m_foreignSource))
            return false;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        if (m_name == null) {
            if (other.m_name != null)
                return false;
        } else if (!m_name.equals(other.m_name))
            return false;
        return true;
    }

    public boolean getIsRoot() {
        return m_isRoot;
    }

    public void setRoot(boolean isRoot) {
        m_isRoot = isRoot;
    }

    public boolean isChildrenAllowed() {
        return m_childrenAllowed;
    }

    public void setChildrenAllowed(boolean childrenAllowed) {
        m_childrenAllowed = childrenAllowed;
    }

}
