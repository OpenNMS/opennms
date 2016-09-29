package org.opennms.netmgt.poller.shell;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;

@Command(scope = "poller", name = "list-monitors", description = "Lists all of the available monitors ")
public class ListMonitors extends OsgiCommandSupport{
    
    private ServiceMonitorRegistry registry;

    @Override
    protected Object doExecute() throws Exception {
        registry.getMonitorClassNames().stream().forEachOrdered(e -> {
            System.out.printf("%s\n", e);
        });
        return null;
    }

    public void setRegistry(ServiceMonitorRegistry registry) {
        this.registry = registry;
    }

}
