package org.opennms.netmgt.correlation.ncs;

import java.util.List;



public class DependsOnAny {
	private Component m_component;
	private List<Component> m_subComponents;
	
	public DependsOnAny() {}
	
	public DependsOnAny(Component component, List<Component> subComponents)
	{
		m_component = component;
		m_subComponents = subComponents;
	}

    public Component getComponent() {
        return m_component;
    }

    public void setComponent(Component component) {
        m_component = component;
    }

    public List<Component> getSubComponents() {
        return m_subComponents;
    }

    public void setSubComponents(List<Component> subComponents) {
        m_subComponents = subComponents;
    }

	
	
	
	
	
}
