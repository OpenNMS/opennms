/**
 * 
 */
package org.opennms.secret.web;

public class TmpService {
	String m_name;
	public TmpService(String name) {
		m_name = name;
	}
	public String getServiceName() {
		return m_name;
	}
}