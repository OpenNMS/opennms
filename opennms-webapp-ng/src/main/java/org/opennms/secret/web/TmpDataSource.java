/**
 * 
 */
package org.opennms.secret.web;

public class TmpDataSource {
	String m_name;
	String m_label;
	public TmpDataSource(String name, String label) {
		m_name = name;
		m_label = label;
	}
	public String getName() {
		return m_name;
	}
	
	public String getLabel() {
		return m_label;
	}
}