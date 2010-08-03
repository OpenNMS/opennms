package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.IncrementalCommand;

public interface CommandExecutor {
    
    public void schedule(IncrementalCommand command);
    
    public void schedule(Command command);
    
}
