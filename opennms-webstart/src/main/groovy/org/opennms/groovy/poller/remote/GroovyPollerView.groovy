package org.opennms.groovy.poller.remote;

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JFrame;
import groovy.swing.SwingBuilder;
import org.opennms.netmgt.poller.remote.PollerView;

class GroovyPollerView implements PollerView {

   def swing = new SwingBuilder();
   
   public void showView() {
	   
		        def frame = swing.frame(
		            title:'This is a Frame',
		            location:[100,100],
		            size:[800,400],
		            defaultCloseOperation:JFrame.EXIT_ON_CLOSE) {
		        	

		            menuBar {
		                menu(text:'File') {
		                    menuItem() {
		                        action(name:'New', closure:{ println("clicked on the new menu item!") })
		                    }
		                    menuItem() {
		                        action(name:'Open', closure:{ println("clicked on the open menu item!") })
		                    }
		                    separator()
		                    menuItem() {
		                        action(name:'Save', enabled:false, closure:{ println("clicked on the Save menu item!") })
		                    }
		                }
		                menu(text:'Demos') {
		                    menuItem() {
		                        action(name:'Simple TableModel Demo', closure:{ showGroovyTableDemo() })
		                    }
		                    menuItem() {
		                        action(name:'MVC Demo', closure:{ showMVCDemo() })
		                    }
		                    menuItem() {
		                        action(name:'TableLayout Demo', closure:{ showTableLayoutDemo() })
		                    }
		                }
		                menu(text:'Help') {
		                    menuItem() {
		                        action(name:'About', closure:{ showAbout() })
		                    }
		                }
		            }		
		            splitPane {
		                panel(layout:new BorderLayout(), border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'titled border')) {
		                    vbox(constraints:BorderLayout.NORTH) {
		                        panel(layout:new BorderLayout()) {
		                            label(text:'Name', constraints:BorderLayout.WEST, toolTipText:'This is the name field')
		                            textField(text:'James', constraints:BorderLayout.CENTER, toolTipText:'Enter the name into this field')
		                        }
		                        panel(layout:new BorderLayout()) {
		                            label(text:'Location', constraints:BorderLayout.WEST, toolTipText:'This is the location field')
		                            comboBox(items:['Atlanta', 'London', 'New York'], constraints:BorderLayout.CENTER, toolTipText:'Choose the location into this field')
		                        }
		                        button(text:'Click Me', actionPerformed:{event -> println("closure fired with event: " + event) })
		                    }
		                    scrollPane(constraints:BorderLayout.CENTER, border:BorderFactory.createRaisedBevelBorder()) {
		                        textArea(text:'Some text goes here', toolTipText:'This is a large text area to type in text')
		                    }
		                }
		                scrollPane {
		                    table()
		                }
		            }
		        }
		        frame.show()	
	}
	
}