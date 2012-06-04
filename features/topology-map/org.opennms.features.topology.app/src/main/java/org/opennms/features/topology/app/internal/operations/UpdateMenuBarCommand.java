package org.opennms.features.topology.app.internal.operations;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class UpdateMenuBarCommand extends Command implements Constants {

    private List<Command> m_commands;

    public UpdateMenuBarCommand(String caption, String menuLocation,String contextMenuLocation) {
        super(caption, menuLocation, contextMenuLocation);
        m_commands = new ArrayList<Command>();
        m_commands.add(new RedoLayout("Redo Layout", null, ""));
        
        m_commands.add(new OpenCommand("Open", "File", null));
        
        m_commands.add(new SaveCommand("Save", "File", null));
        
        m_commands.add(new AddVertexCommand("Add Vertex", "File", "", SERVER_ICON));
        
        m_commands.add(new AddVertexCommand("Add Switch Vertex", null, "", SWITCH_ICON));
        
        m_commands.add(new RemoveVertexCommand("Remove Vertex", "File", ""));
        
        m_commands.add(new ConnectCommand("Connect", "File", ""));
        m_commands.add(new CreateGroupCommand("Create Group", "Edit", ""));
        
        m_commands.add(new ManualLayoutCommand("Manual Layout", "Edit|Layout", null));

        m_commands.add(new CircleLayoutCommand("Circle Layout", "Edit|Layout|JUNG", null));

        m_commands.add(new SimpleLayoutCommand("Simple Layout", "Edit|Layout", null));

        m_commands.add(new SpringLayoutCommand("Spring Layout", "Edit|Layout|JUNG", null));
        
        m_commands.add(new KKLayoutCommand("KK Layout", "Edit|Layout|JUNG", null));
        m_commands.add(new ISOMLayoutCommand("ISOM Layout", "Edit|Layout|JUNG", null));
        m_commands.add(new FRLayoutCommand("FR Layout", "Edit|Layout|JUNG", null));
        
        m_commands.add(new OtherLayoutCommand("Other Layout", "Edit|Layout", null));
        
        m_commands.add(new ResetCommand("Reset", "Edit", null));
        
        m_commands.add(new HistoryCommand("History", "View", null));
        
        m_commands.add(new ShowMapCommand("Show Map", "View", null));
        
        m_commands.add(new GetInfoCommand("Get Info", "Device", null));
    }
    
    @Override
    public boolean appliesToTarget(Object target, SimpleGraphContainer graphContainer) {
        return true;
    }

    @Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
        if(m_commands.size() >= 1) {
            Command command = m_commands.remove(0);
            commandManager.addCommand(command);
        }
        
    }

}
