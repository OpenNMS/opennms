package org.opennms.features.topology.netutils.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.operations.EventsAlarmsOperation;
import org.opennms.features.topology.netutils.internal.operations.NodeInfoOperation;
import org.opennms.features.topology.netutils.internal.operations.PingOperation;
import org.opennms.features.topology.netutils.internal.operations.ResourceGraphsOperation;
import org.opennms.features.topology.netutils.internal.operations.TracerouteOperation;
//import org.vaadin.peter.contextmenu.ContextMenu;
//import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
//import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.Application;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The DefaultMen class is a simple Vaadin Application that builds a Topology map
 * proof-of-concept environment with context menus and Node management windows.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class DefaultMenu extends Application{
    
	private Window main;
	private HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel(); //Creates two panels to represent two Nodes.
	private VerticalLayout mainLayout = null; //Master container for all components on the main window.
	private VerticalLayout leftLayout = new VerticalLayout(); //Left side of Horizontal split panel which represents the Cartman node.
	private VerticalLayout rightLayout = new VerticalLayout(); //Right side of Horizontal split panel which represents the Butters node.
	private EventsAlarmsOperation eaOperation = null; //Sub-window which contains an embedded browser that shows the Events and Alarms for a node.
	private ResourceGraphsOperation rgOperation = null; //Sub-window which contains an embedded browser that shows the Resource Graphs for a node.
	private PingOperation pingOperation = null; //Sub-window which contains the functionality for Pinging a node.
	private TracerouteOperation traceOperation = null; //Sub-window which contains the functionality for Tracerouting a node.
	private NodeInfoOperation infoOperation = null; //Sub-window which contains the functionality for getting node information
	private Label cartman = new Label("<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">Cartman</div>"); //Name of the node which is displayed at the top of the Left side of the split panel.
	private Label butters = new Label("<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">Butters</div>"); //Name of the node which is displayed at the top of the right side of the split panel.
//	private ContextMenu cartmanMenu = new ContextMenu(); //Context Menu that appears when right clicking on the left side of the split panel.
//	private ContextMenu buttersMenu = new ContextMenu(); //Context Menu that appears when right clicking on the right side of the split panel.

	/*Test Data*/
	private Node testNode1 = new Node(9,"172.20.1.10","Cartman");
	private Node testNode2 = new Node(43, "172.20.1.14", "Butters");
	private List<Object> testTargets1 = new ArrayList<Object>();
	private List<Object> testTargets2 = new ArrayList<Object>();
	private OperationContext opContext;

	/**
	 * The init method initializes the DefaultMenu Application and sets up the layouts and windows.
	 */
	public void init() {
		buildMainLayout();
	}

	/**
	 * The buildMainLayout method sets up all of the layouts and windows and adds all of 
	 * the visible and invisible components of the application to the main window.
	 */
	private void buildMainLayout() {
		main = new Window("Topology Maps");
		setMainWindow(main);
		getMainWindow().setImmediate(true);

		testTargets1.add(testNode1);
		testTargets2.add(testNode2);
		eaOperation = new EventsAlarmsOperation();
		rgOperation = new ResourceGraphsOperation();
		pingOperation = new PingOperation();
		traceOperation = new TracerouteOperation();
		infoOperation = new NodeInfoOperation();
		opContext = new OperationContext() {

			public Window getMainWindow() {
				return main;
			}

			public GraphContainer getGraphContainer() {
				return null;
			}
		};
		
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.addComponent(createMenuBar());
		mainLayout.addComponent(horizontalSplit);

		leftLayout.setSizeFull();
		VerticalLayout cartmanBox = new VerticalLayout();
		Embedded cartmanImage = new Embedded("", new ClassResource("cartmanIcon.jpg", this));
		cartmanBox.addComponent(cartmanImage);
		cartmanBox.setComponentAlignment(cartmanImage, Alignment.MIDDLE_CENTER);
		cartmanBox.setWidth("" + (int)cartmanImage.getWidth() + "px"); //Sets layout box to width of image
		cartman.setContentMode(Label.CONTENT_XHTML);
		cartmanBox.addComponent(cartman);
		leftLayout.addComponent(cartmanBox);
		leftLayout.setComponentAlignment(cartmanBox, Alignment.MIDDLE_CENTER);

		rightLayout.setSizeFull();
		VerticalLayout buttersBox = new VerticalLayout();
		Embedded buttersImage = new Embedded("", new ClassResource("buttersIcon.jpg", this));
		buttersBox.addComponent(buttersImage);
		buttersBox.setComponentAlignment(buttersImage, Alignment.MIDDLE_CENTER);
		buttersBox.setWidth("" + (int)buttersImage.getWidth() + "px"); //Sets layout box to width of image
		butters.setContentMode(Label.CONTENT_XHTML);
		buttersBox.addComponent(butters);
		rightLayout.addComponent(buttersBox);
		rightLayout.setComponentAlignment(buttersBox, Alignment.MIDDLE_CENTER);

		horizontalSplit.setFirstComponent(leftLayout);
		horizontalSplit.setSecondComponent(rightLayout);

		buildCartmanMenu(); //Left side of split panel
		buildButtersMenu(); //Right side of split panel

		/*Sets up a right click listener which brings up the Cartman Context menu*/
		cartmanBox.addListener(new LayoutClickListener() {

			public void layoutClick(LayoutClickEvent event) {
				if (LayoutClickEvent.BUTTON_RIGHT == event.getButton()) {
//					cartmanMenu.show(event.getClientX(), event.getClientY());
				}else if(LayoutClickEvent.BUTTON_LEFT == event.getButton()) {
					if(event.isCtrlKey()){
//						cartmanMenu.show(event.getClientX(), event.getClientY());
					}
				}
			}

		});

		/*Sets up a right click listener which brings up the Butters Context menu*/
		buttersBox.addListener(new LayoutClickListener() {

			public void layoutClick(LayoutClickEvent event) {
				if (LayoutClickEvent.BUTTON_RIGHT == event.getButton()) {
//					buttersMenu.show(event.getClientX(), event.getClientY());
				}else if(LayoutClickEvent.BUTTON_LEFT == event.getButton()) {
					if(event.isCtrlKey()){
//						buttersMenu.show(event.getClientX(), event.getClientY());
					}
				}
			}

		});

		/* Allocate all available extra space to the horizontal split panel */
		mainLayout.setExpandRatio(horizontalSplit, 1);

		/*Adds all of the layouts and components to the main window for the Application.*/
		getMainWindow().setContent(mainLayout);
//		getMainWindow().addComponent(cartmanMenu);
//		getMainWindow().addComponent(buttersMenu);
	}

	/**
	 * The createMenuBar method builds a simple Vaadin menu bar with File, View, and Node Options.
	 * Listeners are also added for each option selected so that the corresponding window opens when clicked.
	 * @return MenuBar component
	 */
	private MenuBar createMenuBar() {
		final MenuBar menubar = new MenuBar();

		/*Sets up command for clicking on Node -> Events/Alarms option*/
		MenuBar.Command EA_Select = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				try {
					eaOperation.execute(testTargets1, opContext);
				} catch (Exception e) { e.printStackTrace(); }
			}
		};

		/*Sets up command for clicking on Node -> Resource Graphs option*/
		MenuBar.Command RG_Select = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				try {
					rgOperation.execute(testTargets1, opContext);
				} catch (Exception e) { e.printStackTrace(); }
			}
		};

		/*Sets up command for clicking on Node -> Ping option*/
		MenuBar.Command Ping_Select = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				pingOperation.execute(testTargets1, opContext);
			}
		};

		/*Sets up command for clicking on Node -> Traceroute option*/
		MenuBar.Command Trace_Select = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				traceOperation.execute(testTargets1, opContext);
			}
		};

		/*Sets up command for clicking on Node -> Node Info option*/
		MenuBar.Command NodeInfo_Select = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				try {
					infoOperation.execute(testTargets1, opContext);
				} catch (Exception e) { e.printStackTrace(); }
			}
		};

		/*Creates menu item dropdowns*/
		MenuBar.MenuItem file = menubar.addItem("File", null);
		MenuBar.MenuItem view = menubar.addItem("View", null);
		MenuBar.MenuItem node = menubar.addItem("Node Options", null);

		/* Add options to the main Menu bar along with commands for each*/
		file.addItem("Open", null);
		file.addItem("Close", null);
		view.addItem("Layouts", null);
		node.addItem("Node Info", NodeInfo_Select);
		node.addItem("Events/Alarms", EA_Select);
		node.addItem("Ping", Ping_Select);
		node.addItem("Traceroute", Trace_Select);
		node.addItem("Resource Graphs", RG_Select);
		menubar.setWidth("100%");

		return menubar;
	}

	/**
	 * The buildCartmanMenu method creates a Vaadin Context menu and adds it to the
	 * left side of the split panel
	 */
	private void buildCartmanMenu() {
//		final ContextMenuItem nodeInfo = cartmanMenu.addItem("Node Info");
//		final ContextMenuItem ping = cartmanMenu.addItem("Ping");
//		final ContextMenuItem traceroute = cartmanMenu.addItem("Traceroute");
//		final ContextMenuItem eventsAlarms = cartmanMenu.addItem("Events/Alarms");
//		final ContextMenuItem resourceGraphs = cartmanMenu.addItem("Resource Graphs");
//		cartmanMenu.addListener(new ContextMenu.ClickListener() {
//
//			public void contextItemClick(ClickEvent event) {
//				try {
//					if (eventsAlarms == event.getClickedItem()){
//						eaOperation.execute(testTargets1, opContext);
//					} else if (resourceGraphs == event.getClickedItem()){
//						rgOperation.execute(testTargets1, opContext);
//					} else if (ping == event.getClickedItem()) {
//						pingOperation.execute(testTargets1, opContext);
//					} else if (traceroute == event.getClickedItem()) {
//						traceOperation.execute(testTargets1, opContext);
//					} else if (nodeInfo == event.getClickedItem()) {
//						infoOperation.execute(testTargets1, opContext);
//					}
//				} catch (Exception e) { e.printStackTrace(); }
//			}
//		});
	}

	/**
	 * The buildButtersMenu method creates a Vaadin Context menu and adds it to the
	 * right side of the split panel
	 */
	private void buildButtersMenu() {
//		final ContextMenuItem nodeInfo = buttersMenu.addItem("Node Info");
//		final ContextMenuItem eventsAlarms = buttersMenu.addItem("Events/Alarms");
//		final ContextMenuItem resourceGraphs = buttersMenu.addItem("Resource Graphs");
//		buttersMenu.addListener(new ContextMenu.ClickListener() {
//
//			public void contextItemClick(ClickEvent event) {
//				try {
//					if (eventsAlarms == event.getClickedItem()){
//						eaOperation.execute(testTargets2, opContext);
//					} else if (resourceGraphs == event.getClickedItem()){
//						rgOperation.execute(testTargets2, opContext);
//					} else if (nodeInfo == event.getClickedItem()) {
//						infoOperation.execute(testTargets2, opContext);
//					}
//				} catch (Exception e) { e.printStackTrace(); }
//			}
//		});
	}

}
