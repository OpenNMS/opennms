package org.opennms.netmgt.threshd;

import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class ThresholdGroup {

	private String m_name;
	private File m_rrdRepository;
	private ThresholdResourceType m_nodeResourceType;
	private ThresholdResourceType m_ifResourceType;

	public ThresholdResourceType getIfResourceType() {
		return m_ifResourceType;
	}

	public void setIfResourceType(ThresholdResourceType ifResourceType) {
		m_ifResourceType = ifResourceType;
	}

	public void setName(String name) {
		m_name = name;
	}

	public ThresholdGroup(String name) {
		m_name = name;
		
		
	}

	public String getName() {
		return m_name;
	}
	
	
	public void setRrdRepository(File rrdRepository) {
		m_rrdRepository = rrdRepository;
	}

	public File getRrdRepository() {
		return m_rrdRepository;
	}

	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public void setNodeResourceType(ThresholdResourceType nodeResourceType) {
		m_nodeResourceType = nodeResourceType;
		
	}

	public ThresholdResourceType getNodeResourceType() {
		return m_nodeResourceType;
	}

}
