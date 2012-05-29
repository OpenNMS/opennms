package org.opennms.features.topology.plugins.menu.internal;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.Application;

public class DefaultMenu extends Application{

	private HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
	
	public void init() {
		buildMainLayout();

	}
	
	private void buildMainLayout() {
		setMainWindow(new Window("Topology Maps"));
		getMainWindow().setImmediate(true);
		VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(createMenuBar());
        layout.addComponent(horizontalSplit);
       /* Allocate all available extra space to the horizontal split panel */
       layout.setExpandRatio(horizontalSplit, 1);

       getMainWindow().setContent(layout);
	}

	private MenuBar createMenuBar() {
		// TODO Auto-generated method stub
		final MenuBar menubar = new MenuBar();
		final Label selection = new Label("-");
		getMainWindow().addComponent(selection);
		MenuBar.Command select = new MenuBar.Command() {
			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				selection.setValue("Selected the " + selectedItem.getText() + " from the menu.");
				
			}
		};
		MenuBar.MenuItem file = menubar.addItem("File", select);
		MenuBar.MenuItem view = menubar.addItem("View", select);
		MenuBar.MenuItem node = menubar.addItem("Node Options", select);
		node.addItem("Events/Alarms", select);
		node.addItem("Ping", select);
		node.addItem("Resource Graphs", select);
		
		return menubar;
	}
	
}
