package org.opennms.groovy.poller.remote;

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import groovy.swing.SwingBuilder;
import org.opennms.netmgt.poller.remote.PollerView;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

class PolledServicesTableModelFactoryBean implements FactoryBean, InitializingBean {
	
	def m_swing = new SwingBuilder();
	def m_tableModel;
	def m_frontEnd;
	
	public PollerFrontEnd getPollerFrontEnd() {
		return m_pollerFrontEnd;
	}
	
	public void setPollerFrontEnd(PollerFrontEnd frontEnd) {
		m_frontEnd = frontEnd;
	}
	
	Class getObjectType() {
		return (m_tableModel == null ? Object.class : m_tableModel.getClass());
	}
	
	Object getObject() {
		return m_tableModel;
	}
	
	boolean isSingleton() {
		return true;
	}
	
	void afterPropertiesSet() {
		
		def model = m_frontEnd.getPolledServices();
		
		m_tableModel = m_swing.tableModel(list:model) {
			closureColumn(header:'Node ID', read:{ polledService -> polledService.nodeId })
			closureColumn(header:'Node Label', read:{ polledService -> polledService.nodeLabel })
			closureColumn(header:'Interface', read:{ polledService -> polledService.ipAddr })
			closureColumn(header:'Service', read:{ polledService -> polledService.svcName })
			closureColumn(header:'Last Status', read: { polledService -> polledService.currentStatus })
			propertyColumn(header:'Last Changed', propertyName:'lastStatusChange')
			closureColumn(header:'Last Poll', read: { polledService -> polledService.lastPollTime })
		}
		
		m_frontEnd.polledServiceChanged = { m_tableModel.fireTableDataChanged() };
	}
	
}