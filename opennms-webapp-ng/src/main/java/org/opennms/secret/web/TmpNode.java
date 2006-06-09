/**
 * 
 */
package org.opennms.secret.web;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TmpNode {
	
	static String[] nodeDSNames =  { "avgBusy5", "cpuLoad", "diskUtil" };
	static String[] nodeDSLabels = { "Averge Busy 5", "CPU Load", "Disk Utilization" };

	public String getNodeLabel() {
		return "Sample Node";
	}
	
	public Collection getInterfaces() {
		List list = new LinkedList();
		for(int i =0; i < 2; i++) {
			list.add(new TmpInterface("eth"+i));
		}
		return list;
	}
	
	public Collection getDataSources() {
		List list = new LinkedList();
		for (int i = 0; i < nodeDSNames.length; i++) {
			String name = nodeDSNames[i];
			String label = nodeDSLabels[i];
			list.add(new TmpDataSource(name, label));
		}
		return list;
	}

	public Long getNodeId() {
		return new Long(1);
	}
	
}