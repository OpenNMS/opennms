package org.opennms.netmgt.correlation.ncs;


public class ImpactEventSent {

    private Component m_component;
    private ComponentDownEvent m_cause;
    
    
    public ImpactEventSent(Component component, ComponentDownEvent cause) {
        m_component = component;
        m_cause = cause;
    }


    public Component getComponent() {
        return m_component;
    }


    public void setComponent(Component component) {
        m_component = component;
    }


    public ComponentDownEvent getCause() {
        return m_cause;
    }


    public void setCause(ComponentDownEvent cause) {
        m_cause = cause;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_cause == null) ? 0 : m_cause.hashCode());
        result = prime * result
                + ((m_component == null) ? 0 : m_component.hashCode());
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
        ImpactEventSent other = (ImpactEventSent) obj;
        if (m_cause == null) {
            if (other.m_cause != null)
                return false;
        } else if (!m_cause.equals(other.m_cause))
            return false;
        if (m_component == null) {
            if (other.m_component != null)
                return false;
        } else if (!m_component.equals(other.m_component))
            return false;
        return true;
    }
    
    
    
    

}
