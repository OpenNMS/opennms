/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.groovy.poller.remote;

import groovy.swing.SwingBuilder;

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.text.SimpleDateFormat

import javax.swing.BorderFactory
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition
import org.opennms.poller.remote.MonitoringLocationListCellRenderer;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */

class GroovyPollerView implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(GroovyPollerView.class);
    private static final String REGISTRATION = "registration";
    private static final String STATUS = "status";

   def m_frontEnd; 
    
   def swing = new SwingBuilder();
   def m_table;
   def m_frame;
   def m_cardPanel;
   def m_monLocation;
   def m_idLabel;
   def m_statusLabel;
   SimpleDateFormat m_dateFormat;
   
   public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
	   m_frontEnd = pollerFrontEnd;
   }

   public void afterPropertiesSet() {
       SwingUtilities.invokeLater( { createAndShowGui() } );
   }
   
   private void createAndShowGui() {
       
       m_dateFormat = new SimpleDateFormat("K:mm:ss a");
		
		m_table = swing.table(model:createTableModel())
		
		
		def frame = swing.frame(title:'OpenNMS Remote Poller', location:[100,100], size:[900,500], defaultCloseOperation:JFrame.EXIT_ON_CLOSE) {
		    m_cardPanel = panel(layout:new CardLayout(), constraints:BorderLayout.CENTER) {
		        panel(constraints:REGISTRATION) {
		            tableLayout(cellpadding:5) {
		                tr {
		                    td(colfill:true) {
		                    	label(text:'Current monitoring locations: ')
		                    }
		                    td {
		                        m_monLocation = comboBox(items:getCurrentMonitoringLocations(), renderer:new MonitoringLocationListCellRenderer())
		                    }
		                }
		                tr {
		                    td (colspan:2, align:"CENTER"){
		    	    		  button(text:'Register', constraints:BorderLayout.SOUTH, actionPerformed:{ doRegistration() })
		                    }
		                }
		            }
		        }
		        panel(constraints:STATUS, layout:new BorderLayout()) {
		            panel(constraints:BorderLayout.NORTH, layout:new BorderLayout()) {
		            	m_idLabel = label(constraints:BorderLayout.WEST, text:'Monitor: '+m_frontEnd.getMonitorName())
		            	m_statusLabel = label(constraints:BorderLayout.EAST, text:m_frontEnd.getStatus())
		            }
		    	    scrollPane(constraints:BorderLayout.CENTER, viewportView:m_table)
		        }
		    }
		    /*
		    panel(layout:new FlowLayout(), constraints:BorderLayout.SOUTH) {
		        button(text:"Show Registration", actionPerformed:{ setCurrentPanel(REGISTRATION) })
		        button(text:"Show Status", actionPerformed:{ setCurrentPanel(STATUS) })
		    }
		    */
		}
		
		updateCurrentPanel();
		
		m_frontEnd.pollStateChange = { updateTable() }
		m_frontEnd.propertyChange = { updateCurrentPanel(); m_idLabel.text = 'Monitor: '+m_frontEnd.getMonitorName(); m_statusLabel.text = m_frontEnd.getStatus() }
		m_frontEnd.configurationChanged = { updateTableModel(); m_idLabel.text = m_frontEnd.getMonitorName() }

		frame.show()	

   }
   
   private void doRegistration() {
       String loc = m_monLocation.selectedItem.name;
       System.err.println("Registering for location "+loc)
       m_frontEnd.register(loc);
   }
   
   private List getCurrentMonitoringLocations() {
	   try {
		   return m_frontEnd.getMonitoringLocations();
	   } catch (final Exception e) {
		   LOG.errorf("an error occurred getting the list of monitoring locations", e);
           System.exit(1);
	   }
   }
   
   private void updateCurrentPanel() {
		SwingUtilities.invokeLater({ setCurrentPanel(m_frontEnd.started ? STATUS : REGISTRATION) });
   }
   
   private void setCurrentPanel(String panelName) {
       m_cardPanel.layout.show(m_cardPanel, panelName);
   }
   
   private void updateTable() {
       //System.err.println("Updating Table (status Change)")
       SwingUtilities.invokeLater({ m_table.model.fireTableDataChanged() })
   }
   
   private void updateTableModel() {
       //System.err.println("Updating Table Model (config Change)")
       SwingUtilities.invokeLater({ m_table.model = createTableModel(); m_table.model.fireTableDataChanged() })
   }
   
   private TableModel createTableModel() {
       List rows = Collections.EMPTY_LIST;
       if (m_frontEnd.registered)
			rows = m_frontEnd.getPollerPollState();

       return swing.tableModel(list:rows) {
			//closureColumn(header:'Node ID', read:{ it.polledService.nodeId })
			closureColumn(header:'Node Label', read:{ pollState -> pollState.polledService.nodeLabel })
			closureColumn(header:'Interface', read:{ pollState -> pollState.polledService.ipAddr })
			closureColumn(header:'Service', read:{ pollState -> pollState.polledService.svcName })
			closureColumn(header:'Last Status', read: { pollState -> (pollState.lastPoll == null ? '-' : pollState.lastPoll.statusName) })
			closureColumn(header:'Reason/ResponseTime', read: { pollState -> reasonResponse(pollState.lastPoll)})
			closureColumn(header:'Last Poll', read: { pollState -> formatPollTime(pollState.lastPollTime) })
			closureColumn(header:'Next Poll', read: { pollState -> formatPollTime(pollState.nextPollTime) })
		}
		
		
       
   }
   
   private String reasonResponse(PollStatus lastPoll) {
       if (lastPoll == null)
           return '-';
       if (lastPoll.responseTime >= 0)
           return lastPoll.responseTime+" ms";
       if (lastPoll.reason != null)
           return lastPoll.reason
       return '-';
   }
   
   private String formatPollTime(Date pollTime) {
       if (pollTime == null) return "-";
       
       return m_dateFormat.format(pollTime);
   }
   
}
