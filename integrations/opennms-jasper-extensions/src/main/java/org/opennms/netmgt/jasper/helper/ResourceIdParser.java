package org.opennms.netmgt.jasper.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceIdParser {
	
	Pattern m_nodePattern;
	Pattern m_resourcePattern;
	
	public ResourceIdParser() {
		m_nodePattern = Pattern.compile("node\\W(\\d.*?)\\W");
		m_resourcePattern = Pattern.compile("responseTime\\W(.*)\\W");
	}
	
	public String getNodeId(String resourceId) {
		return getMatch(m_nodePattern.matcher(resourceId));
	}

	public String getResource(String resourceId) {
		return getMatch(m_resourcePattern.matcher(resourceId));
	}
	
	private String getMatch(Matcher m) {
		m.find();
		return m.group(1);
	}
}
