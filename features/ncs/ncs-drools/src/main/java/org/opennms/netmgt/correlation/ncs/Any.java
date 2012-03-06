package org.opennms.netmgt.correlation.ncs;



public class Any {
	private Component m_component;
	
	
	public Any() {}
	
	public Any(Component component)
	{
		m_component = component;
		
	}

    public Component getComponent() {
        return m_component;
    }

    public void setComponent(Component component) {
        m_component = component;
    }

    @Override
    public String toString() {
        return "Any [component=" + m_component + "]";
    }

	
	
	
}
