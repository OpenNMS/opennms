/**
 * 
 */
package org.opennms.secret.web;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TmpInterface {
	
	static String[] ifDSNames =  { "ifInOctets", "ifOutOctets", "ifInErrors", "ifInDiscards" };
	static String[] ifDSLabels = { "In Octets", "Out Octets", "In Errors", "In Discards" };
	static String[] svcNames = { "ICMP", "HTTP", "DNS", "SSH" };

	String m_label;
	public TmpInterface(String label) {
		m_label = label;
	}
	public String getIfDescr() {
		return m_label;
	}
	public Collection getServices() {
		List list = new LinkedList();
		for (int i = 0; i < svcNames.length; i++) {
			String svcName = svcNames[i];
			list.add(new TmpService(svcName));
		}
		return list;
	}
	public Collection getDataSources() {
		List list = new LinkedList();
		for (int i = 0; i < ifDSNames.length; i++) {
			String name = ifDSNames[i];
			String label = ifDSLabels[i];
			list.add(new TmpDataSource(name, label));
		}
		return list;
	}
	
}