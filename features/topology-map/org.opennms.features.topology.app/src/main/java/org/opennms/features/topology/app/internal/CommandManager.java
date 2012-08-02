package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Window;

public class CommandManager {

    public class DefaultOperationContext implements OperationContext {

        private Window m_mainWindow;
        private SimpleGraphContainer m_graphContainer;
        private boolean m_checked = false;

        public DefaultOperationContext(Window mainWindow,
                SimpleGraphContainer graphContainer) {
            m_mainWindow = mainWindow;
            m_graphContainer = graphContainer;
        }

        @Override
        public Window getMainWindow() {
            return m_mainWindow;
        }

        @Override
        public SimpleGraphContainer getGraphContainer() {
            return m_graphContainer;
        }

        public void setChecked(boolean checked) {
            m_checked = checked;
        }

        @Override
        public boolean isChecked() {
            return m_checked;
        }

    }

    private List<Command> m_commandList = new ArrayList<Command>();
    private List<Command> m_commandHistoryList = new ArrayList<Command>();
    private List<CommandUpdateListener> m_updateListeners = new ArrayList<CommandUpdateListener>();
    private List<MenuItemUpdateListener> m_menuItemUpdateListeners = new ArrayList<MenuItemUpdateListener>();
    private List<String> m_topLevelMenuOrder = new ArrayList<String>();
    private Map<String, List<String>> m_subMenuGroupOrder = new HashMap<String, List<String>>();
    private Map<MenuBar.Command, Operation> m_commandToOperationMap = new HashMap<MenuBar.Command, Operation>();

    public CommandManager() {
    }

    public List<Command> getCommandList() {
        return m_commandList;
    }

    public void setCommandList(List<Command> commandList) {
        m_commandList = commandList;
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

    MenuBar getMenuBar(SimpleGraphContainer graphContainer, Window mainWindow) {
        OperationContext opContext = new DefaultOperationContext(mainWindow,
                graphContainer);
        MenuBarBuilder menuBarBuilder = new MenuBarBuilder();
        menuBarBuilder.setTopLevelMenuOrder(m_topLevelMenuOrder);
        menuBarBuilder.setSubMenuGroupOder(m_subMenuGroupOrder);

        for (Command command : getCommandList()) {
            String menuPosition = command.getMenuPosition();
            MenuBar.Command menuCommand = menuCommand(command, graphContainer,
                    mainWindow, opContext);
            updateCommandToOperationMap(command, menuCommand);
            menuBarBuilder.addMenuCommand(menuCommand, menuPosition);
        }
        MenuBar menuBar = menuBarBuilder.get();
        return menuBar;
    }

    private void updateCommandToOperationMap(Command command,
            MenuBar.Command menuCommand) {
        m_commandToOperationMap.put(menuCommand, command.getOperation());
    }

    public MenuBar.Command menuCommand(final Command command,
            final SimpleGraphContainer graphContainer, final Window mainWindow,
            final OperationContext operationContext) {

        return new MenuBar.Command() {

            public void menuSelected(MenuItem selectedItem) {
                List<Object> targets = getSelectedVertices(operationContext);

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

    void addActionHandlers(TopologyComponent topologyComponent,
            SimpleGraphContainer graphContainer, Window mainWindow) {
        topologyComponent.addActionHandler(new ActionHandler(
                new DefaultOperationContext(mainWindow, graphContainer)));
    }

    public List<Command> getHistoryList() {
        return m_commandHistoryList;
    }

    public Operation getOperationByCommand(MenuBar.Command command) {
        return m_commandToOperationMap.get(command);
    }

    private class ActionHandler implements Action.Handler {
        SimpleGraphContainer m_graphContainer;
        Window m_mainWindow;
        private DefaultOperationContext m_operationContext;

        public ActionHandler(DefaultOperationContext operationContext) {
            m_operationContext = operationContext;
        }

        public Action[] getActions(Object target, Object sender) {
            List<Action> actionList = new ArrayList<Action>();
            for (Command command : m_commandList) {
                if (command.isAction()
                        && command.appliesToTarget(target, m_operationContext)) {
                    actionList.add(command.getAction());
                }
            }
            return actionList.toArray(new Action[actionList.size()]);
        }

        public void handleAction(Action action, Object sender, Object target) {
            if (action instanceof Command) {
                Command command = (Command) action;
                command.doCommand(target, m_operationContext);

                m_commandHistoryList.add(command);
                updateMenuItemListeners();
            }
        }
    }

    public void onBind(Command command) {
        addCommand(command);
    }

    public void onUnbind(Command command) {
        removeCommand(command);
    }

    public void onBind(Operation operation, Map<String, String> props) {
        OperationCommand operCommand = new OperationCommand(null, operation,
                props);
        addCommand(operCommand);
    }

    public void onUnbind(Operation operation, Map<String, String> props) {
        removeCommand(operation);
    }

    private void removeCommand(Operation operation) {
        Iterator<Command> it = m_commandList.iterator();
        while (it.hasNext()) {
            Command command = it.next();
            if (command.getOperation() == operation) {
                it.remove();
            }
        }

    }

    private void removeCommand(Command command) {
        m_commandList.remove(command);
        updateCommandListeners();
    }

    public void setTopLevelMenuOrder(List<String> menuOrderList) {
        m_topLevelMenuOrder = menuOrderList;

    }

    public void updateMenuConfig(Dictionary props) {
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

    private List<Object> getSelectedVertices(
            final OperationContext operationContext) {
        List<Object> targets = new ArrayList<Object>();
        for (Object vId : operationContext.getGraphContainer().getVertexIds()) {
            Item vItem = operationContext.getGraphContainer()
                    .getVertexItem(vId);
            boolean selected = (Boolean) vItem.getItemProperty("selected")
                    .getValue();
            if (selected) {
                targets.add(vItem.getItemProperty("key").getValue());
            }
        }
        return targets;
    }

    public void updateMenuItem(MenuItem menuItem,
            SimpleGraphContainer graphContainer, Window mainWindow) {
        DefaultOperationContext operationContext = new DefaultOperationContext(
                mainWindow, graphContainer);
        Operation operation = getOperationByCommand(menuItem.getCommand());

        boolean visibility = operation.display(
                getSelectedVertices(operationContext), operationContext);
        menuItem.setVisible(visibility);
        boolean enabled = operation.enabled(
                getSelectedVertices(operationContext), operationContext);
        menuItem.setEnabled(enabled);

        if (operation instanceof CheckedOperation) {
            if (!menuItem.isCheckable()) {
                menuItem.setCheckable(true);
            }

            menuItem.setChecked(((CheckedOperation) operation).isChecked(
                    getSelectedVertices(operationContext), operationContext));
        }
    }

}
