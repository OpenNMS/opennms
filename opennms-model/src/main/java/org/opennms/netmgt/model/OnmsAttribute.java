package org.opennms.netmgt.model;

public class OnmsAttribute {

	private OnmsRepository m_repository;
	private OnmsResource m_resource;
	private OnmsAttributeDefinition m_definition;
	
	public OnmsAttributeDefinition getDefinition() {
		return m_definition;
	}
	public void setDefinition(OnmsAttributeDefinition definition) {
		m_definition = definition;
	}
	public OnmsRepository getRepository() {
		return m_repository;
	}
	public void setRepository(OnmsRepository repository) {
		m_repository = repository;
	}
	public OnmsResource getResource() {
		return m_resource;
	}
	public void setResource(OnmsResource resource) {
		m_resource = resource;
	}
}
