/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Window;

public class CommandManager {

	private class DefaultOperationContext implements OperationContext {

		private final Window m_mainWindow;
		private final GraphContainer m_graphContainer;
		private final DisplayLocation m_displayLocation;
		private boolean m_checked = false;

		public DefaultOperationContext(Window mainWindow, GraphContainer graphContainer, DisplayLocation displayLocation) {
			m_mainWindow = mainWindow;
			m_graphContainer = graphContainer;
			m_displayLocation = displayLocation;
		}

		@Override
		public Window getMainWindow() {
			return m_mainWindow;
		}

		@Override
		public GraphContainer getGraphContainer() {
			return m_graphContainer;
		}
		
                @Override
		public DisplayLocation getDisplayLocation() {
			return m_displayLocation;
		}

		public void setChecked(boolean checked) {
			m_checked = checked;
		}

		@Override
		public boolean isChecked() {
			return m_checked;
		}

	}
	
	private class ContextMenuListener implements ContextMenu.ClickListener {
		
		private final OperationContext m_opContext;
		
		public ContextMenuListener(OperationContext opContext) {
			m_opContext = opContext;
		}

		@Override
		public void contextItemClick(ClickEvent event) {
			Operation operation = m_contextMenuItemsToOperationMap.get(event.getClickedItem());
			//TODO: Do some implementation here for execute
			if (operation != null) {
				TopoContextMenu source = (TopoContextMenu)event.getSource();
				operation.execute(asVertexList(source.getTarget()), m_opContext);
			}
		}
		
	}

	private final List<Command> m_commandList = new CopyOnWriteArrayList<Command>();
	private final List<Command> m_commandHistoryList = new ArrayList<Command>();
	private final List<CommandUpdateListener> m_updateListeners = new ArrayList<CommandUpdateListener>();
	private final List<MenuItemUpdateListener> m_menuItemUpdateListeners = new ArrayList<MenuItemUpdateListener>();
	private final List<String> m_topLevelMenuOrder = new ArrayList<String>();
	private final Map<String, List<String>> m_subMenuGroupOrder = new HashMap<String, List<String>>();
	private final Map<MenuBar.Command, Operation> m_commandToOperationMap = new HashMap<MenuBar.Command, Operation>();
	private final Map<ContextMenuItem, Operation> m_contextMenuItemsToOperationMap = new HashMap<ContextMenuItem, Operation>();
	public CommandManager() {
	}

	public void addCommand(Command command) {
		m_commandList.add(command);
		updateCommandListeners();
	}

	private void updateCommandListeners() {
		for (CommandUpdateListener listener : m_updateListeners) {
			listener.menuBarUpdated(this);
		}

	}

	public void addCommandUpdateListener(CommandUpdateListener listener) {
		m_updateListeners.add(listener);
	}

	public void addMenuItemUpdateListener(MenuItemUpdateListener listener) {
		m_menuItemUpdateListeners.add(listener);
	}

	public void removeMenuItemUpdateListener(MenuItemUpdateListener listener) {
		m_menuItemUpdateListeners.remove(listener);
	}

	MenuBar getMenuBar(GraphContainer graphContainer, Window mainWindow) {
		OperationContext opContext = new DefaultOperationContext(mainWindow, graphContainer, DisplayLocation.MENUBAR);
		MenuBarBuilder menuBarBuilder = new MenuBarBuilder();
		menuBarBuilder.setTopLevelMenuOrder(m_topLevelMenuOrder);
		menuBarBuilder.setSubMenuGroupOder(m_subMenuGroupOrder);
		
		for (Command command : m_commandList) {
			String menuPosition = command.getMenuPosition();
			MenuBar.Command menuCommand = menuCommand(command, graphContainer, mainWindow, opContext);
			updateCommandToOperationMap(command, menuCommand);
			menuBarBuilder.addMenuCommand(menuCommand, menuPosition);
		}
		MenuBar menuBar = menuBarBuilder.get();
		return menuBar;
	}

	/**
	 * Gets the ContextMenu addon for the app based on OSGi Operations
	 * @param graphContainer
	 * @param mainWindow
	 * @return
	 */
	public TopoContextMenu getContextMenu(GraphContainer graphContainer, Window mainWindow) {
		OperationContext opContext = new DefaultOperationContext(mainWindow, graphContainer, DisplayLocation.CONTEXTMENU);
		ContextMenuBuilder contextMenuBuilder = new ContextMenuBuilder();
		Map<String, Operation> operationMap = new HashMap<String, Operation>(); 
		for (Command command : m_commandList) {
			if (command.isAction()) {
				String contextPosition = command.getContextMenuPosition();
				contextMenuBuilder.addMenuCommand(command, contextPosition);
				operationMap.put(command.toString(), command.getOperation());
			}
		}
		TopoContextMenu contextMenu = contextMenuBuilder.get();
		contextMenu.addListener(new ContextMenuListener(opContext));
		
		updateContextCommandToOperationMap(contextMenu.getItems());
		return contextMenu;
	}
	
	private void updateContextCommandToOperationMap(List<TopoContextMenuItem> items) {
	    for(TopoContextMenuItem item : items) {
	        if(item.hasChildren() && !item.hasOperation()) {
	            updateContextCommandToOperationMap(item.getChildren());
	        }else {
	            m_contextMenuItemsToOperationMap.put(item.getItem(), item.getOperation());
	        }
	    }
	}

	private void updateCommandToOperationMap(Command command, MenuBar.Command menuCommand) {
		m_commandToOperationMap.put(menuCommand, command.getOperation());
	}

	public MenuBar.Command menuCommand(final Command command,
			final GraphContainer graphContainer, final Window mainWindow,
			final OperationContext operationContext) {

		return new MenuBar.Command() {

                        @Override
			public void menuSelected(MenuItem selectedItem) {
				List<VertexRef> targets = new ArrayList<VertexRef>(graphContainer.getSelectionManager().getSelectedVertexRefs());

				DefaultOperationContext context = (DefaultOperationContext) operationContext;
				context.setChecked(selectedItem.isChecked());

				command.doCommand(targets, operationContext);
				m_commandHistoryList.add(command);
				updateMenuItemListeners();
			}
		};
	}

	protected void updateMenuItemListeners() {
		for(MenuItemUpdateListener listener : m_menuItemUpdateListeners) {
			listener.updateMenuItems();
		}
	}

	public List<Command> getHistoryList() {
		return m_commandHistoryList;
	}

	public Operation getOperationByMenuItemCommand(MenuBar.Command command) {
		return m_commandToOperationMap.get(command);
	}

	public synchronized void onBind(Command command) {
		try {
			addCommand(command);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
		}
	}

	public synchronized void onUnbind(Command command) {
		try {
			removeCommand(command);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
		}
	}

	public void onBind(Operation operation, Map<String, String> props) {
		OperationCommand operCommand = new OperationCommand(null, operation, props);
		addCommand(operCommand);
	}

	public void onUnbind(Operation operation, Map<String, String> props) {
		removeCommand(operation);
	}

	private void removeCommand(Operation operation) {
		for (Command command : m_commandList) {
			if (command.getOperation() == operation) {
				removeCommand(command); 
			}
		}
	}

	private void removeCommand(Command command) {
		m_commandList.remove(command);
		updateCommandListeners();
	}

	public void setTopLevelMenuOrder(List<String> menuOrderList) {
		if (m_topLevelMenuOrder == menuOrderList) return;
		m_topLevelMenuOrder.clear();
		m_topLevelMenuOrder.addAll(menuOrderList);

	}

    public void updateMenuConfig(Dictionary<Object,Object> props) {
        List<String> topLevelOrder = Arrays.asList(props
                .get("toplevelMenuOrder").toString().split(","));
        setTopLevelMenuOrder(topLevelOrder);

		for (String topLevelItem : topLevelOrder) {
			if (!topLevelItem.equals("Additions")) {
				String key = "submenu." + topLevelItem + ".groups";
				addOrUpdateGroupOrder(topLevelItem,
						Arrays.asList(props.get(key).toString().split(",")));
			}
		}
		addOrUpdateGroupOrder(
				"Default",
				Arrays.asList(props.get("submenu.Default.groups").toString()
						.split(",")));

		updateCommandListeners();

	}

	public void addOrUpdateGroupOrder(String key, List<String> orderSet) {
		if (!m_subMenuGroupOrder.containsKey(key)) {
			m_subMenuGroupOrder.put(key, orderSet);
		} else {
			m_subMenuGroupOrder.remove(key);
			m_subMenuGroupOrder.put(key, orderSet);
		}

	}

	public Map<String, List<String>> getMenuOrderConfig() {
		return m_subMenuGroupOrder;
	}

	public void updateMenuItem(MenuItem menuItem, GraphContainer graphContainer, Window mainWindow) {
		DefaultOperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, DisplayLocation.MENUBAR);
		Operation operation = getOperationByMenuItemCommand(menuItem.getCommand());
		
		//Check for null because separators have no Operation
		if(operation != null) {
    		List<VertexRef> selectedVertices = new ArrayList<VertexRef>(graphContainer.getSelectionManager().getSelectedVertexRefs());
			boolean visibility = operation.display(selectedVertices, operationContext);
    		menuItem.setVisible(visibility);
    		boolean enabled = operation.enabled(selectedVertices, operationContext);
    		menuItem.setEnabled(enabled);
    
    		if (operation instanceof CheckedOperation) {
    			if (!menuItem.isCheckable()) {
    				menuItem.setCheckable(true);
    			}
    
    			menuItem.setChecked(((CheckedOperation) operation).isChecked(selectedVertices, operationContext));
    		}
		}
	}

    public void updateContextMenuItem(Object target, TopoContextMenuItem contextItem, GraphContainer graphContainer, Window mainWindow) {
        DefaultOperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, DisplayLocation.CONTEXTMENU);
        
        ContextMenuItem ctxMenuItem = contextItem.getItem();
        Operation operation = m_contextMenuItemsToOperationMap.get(ctxMenuItem);
     
        List<VertexRef> targets = asVertexList(target);
        ctxMenuItem.setVisible(operation.display(targets, operationContext));
        ctxMenuItem.setEnabled(operation.enabled(targets, operationContext));   
    }

	private List<VertexRef> asVertexList(Object target) {
		return (target != null && target instanceof VertexRef) ? Arrays.asList((VertexRef)target) : Collections.<VertexRef>emptyList();
	}

}
