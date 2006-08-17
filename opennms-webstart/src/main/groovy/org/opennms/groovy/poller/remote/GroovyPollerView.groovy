package org.opennms.groovy.poller.remote;

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import groovy.swing.SwingBuilder;
import org.opennms.netmgt.poller.remote.PollerView;
import org.springframework.beans.factory.InitializingBean;

class GroovyPollerView implements PollerView, InitializingBean {

   def swing = new SwingBuilder();
   def tableModel;
   
   public void setTableModel(TableModel tm) {
	   tableModel = tm;
   }
   
   public TableModel getTableModel() {
	   return tableModel;
   }
   
   public TableModel createDefaultTableModel() {
	   def model = [['name':'Matt', 'location':'Durham'], ['name':'Bob', 'location':'Wake Forest'], ['name':'Tarus', 'location':'Pittsboro'], ['name':'David', 'location':'Cary']]
	   tableModel = swing.tableModel(list:model) {
		   propertyColumn(header:'Name', propertyName:'name')
		   propertyColumn(header:'Location', propertyName:'location')
	   }
   }
   
   public void afterPropertiesSet() {
	   if (tableModel == null)
		   tableModel = createDefaultTableModel();
   }
   
   
   
   public void showView() {
	   
	   	
	  def frame = swing.frame(title:'OpenNMS Remote Poller', location:[100,100], size:[800,500], defaultCloseOperation:JFrame.EXIT_ON_CLOSE) {
		         scrollPane {
		            table(model:tableModel)
		         }
		  }
		  frame.show()	
	}
	
}