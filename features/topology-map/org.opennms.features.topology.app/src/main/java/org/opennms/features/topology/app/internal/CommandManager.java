package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Window;


public class CommandManager  {
    
    public class DefaultOperationContext implements OperationContext{
        
        private Window m_mainWindow;
        private SimpleGraphContainer m_graphContainer;

        public DefaultOperationContext(Window mainWindow, SimpleGraphContainer graphContainer) {
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

    }
    
    private List<Command> m_commandList = new ArrayList<Command>();
    private List<Command> m_commandHistoryList = new ArrayList<Command>();
    private List<CommandUpdateListener> m_updateListeners = new ArrayList<CommandUpdateListener>();
    private List<String> m_topLevelMenuOrder = new ArrayList<String>();
    private Map<String, List<String>> m_subMenuGroupOrder = new HashMap<String, List<String>>();
    
    public CommandManager() {}
    
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
        for(CommandUpdateListener listener : m_updateListeners) {
            listener.menuBarUpdated(this);
        }
        
    }

    public void addCommandUpdateListener(CommandUpdateListener listener) {
        m_updateListeners.add(listener);
    }

    MenuBar getMenuBar(SimpleGraphContainer graphContainer, Window mainWindow) {
        OperationContext opContext = new DefaultOperationContext(mainWindow, graphContainer);
        MenuBarBuilder menuBarBuilder = new MenuBarBuilder();
        menuBarBuilder.setTopLevelMenuOrder(m_topLevelMenuOrder);
        menuBarBuilder.setSubMenuGroupOder(m_subMenuGroupOrder);
        
        for(Command command : getCommandList()) {
            String menuPosition = command.getMenuPosition();
            menuBarBuilder.addMenuCommand(menuCommand(command, graphContainer, mainWindow, opContext), menuPosition);
        }
        MenuBar menuBar = menuBarBuilder.get();
        return menuBar;
    }
    
    public MenuBar.Command menuCommand(final Command command, final SimpleGraphContainer graphContainer, final Window mainWindow, final OperationContext operationContext){
        
        return new MenuBar.Command() {
            
            public void menuSelected(MenuItem selectedItem) {
                List<Object> targets = new ArrayList<Object>();
                //Adding a check for selected vertices.
                for(Object vId : operationContext.getGraphContainer().getVertexIds()) {
                    Item vItem = operationContext.getGraphContainer().getVertexItem(vId);
                    boolean selected = (Boolean) vItem.getItemProperty("selected").getValue();
                    if(selected) {
                        targets.add(vItem.getItemProperty("key").getValue());
                    }
                }
                
                
                command.doCommand(targets, operationContext);
                m_commandHistoryList.add(command);
            }
        };
    }

    void addActionHandlers(TopologyComponent topologyComponent, SimpleGraphContainer graphContainer, Window mainWindow) {
        topologyComponent.addActionHandler(new ActionHandler(new DefaultOperationContext(mainWindow,graphContainer)));
    }

    
    
    public List<Command> getHistoryList(){
        return m_commandHistoryList;
    }

    public void updateMenuBar(SimpleGraphContainer graphContainer) {
        
    }
    
    private class ActionHandler implements Action.Handler{
       SimpleGraphContainer m_graphContainer;
       Window m_mainWindow;
       private DefaultOperationContext m_operationContext;
       
        public ActionHandler(DefaultOperationContext operationContext) {
            m_operationContext = operationContext;
        }

        public Action[] getActions(Object target, Object sender) {
            List<Action> actionList = new ArrayList<Action>();
            for(Command command : m_commandList) {
                if(command.isAction() && command.appliesToTarget(target, m_operationContext)) {
                    actionList.add(command.getAction());
                }
            }
            return actionList.toArray(new Action[actionList.size()]);
        }

        public void handleAction(Action action, Object sender, Object target) {
            if(action instanceof Command) {
                Command command = (Command) action;
                command.doCommand(target, m_operationContext);
                
                m_commandHistoryList.add(command);
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
        OperationCommand operCommand = new OperationCommand(null, operation, props);
        addCommand(operCommand);
    }
    
    public void onUnbind(Operation operation, Map<String, String> props) {
        removeCommand(operation);
    }

    private void removeCommand(Operation operation) {
        Iterator<Command> it = m_commandList.iterator();
        while(it.hasNext()) {
            Command command = it.next();
            if(command.getOperation() == operation) {
                it.remove();
            }
        }
        
        
    }

    private void removeCommand(Command command) {
        m_commandList.remove(command);
        updateCommandListeners();
    }

    public void setTopLevelMenuOrder(List<String> menuOrderList) {
        m_topLevelMenuOrder  = menuOrderList;
        
    }

    public void addGroupOrder(String key, List<String> orderSet) {
        if(!m_subMenuGroupOrder.containsKey(key)) {
            m_subMenuGroupOrder.put(key, orderSet);
        }
        
    }
    
}
