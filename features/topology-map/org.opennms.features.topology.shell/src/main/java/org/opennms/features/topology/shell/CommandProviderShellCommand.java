package org.opennms.features.topology.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.ValueMetadata;

@Command(scope = "onms", name = "listcommands", description="Lists the available shell commands and their providers.")
public class CommandProviderShellCommand extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
    	final ServiceReference[] services = this.bundleContext.getServiceReferences(BlueprintContainer.class.getName(), null);
    	for (final ServiceReference sr : services) {

    		final List<String> commands = new ArrayList<String>();
    		
    		final BlueprintContainer container = (BlueprintContainer)this.bundleContext.getService(sr);
    		@SuppressWarnings("unchecked")
			final Collection<ServiceMetadata> metadata = container.getMetadata(ServiceMetadata.class);
    		for (final ServiceMetadata data : metadata) {
    			String scope = null;
    			String function = null;

    			// Implements OSGi API
    			@SuppressWarnings("unchecked")
				final List properties = data.getServiceProperties();
    			for (final Object o : properties) {
    				final MapEntry entry = (MapEntry)o;
    				String key, value;
    				if (entry.getKey() instanceof ValueMetadata) {
    					final ValueMetadata vmKey = (ValueMetadata)entry.getKey();
    					key = vmKey.getStringValue();
    				} else {
    					key = entry.getKey().toString();
    				}
    				if (entry.getValue() instanceof ValueMetadata) {
    					final ValueMetadata vmValue = (ValueMetadata)entry.getValue();
    					value = vmValue.getStringValue();
    				} else {
    					value = entry.getValue().toString();
    				}
    				if (key.equals("osgi.command.scope")) {
    					scope = value;
    				} else if (key.equals("osgi.command.function")) {
    					function = value;
    				}
    			}
    			if (scope != null && function != null) {
    				commands.add(scope + ":" + function);
    			}
    		}
    		
    		if (commands.size() > 0) {
	    		System.out.println(sr.getBundle());
	    		
    			for (final String command : commands) {
    				System.out.println("    " + command);
    			}
	    		
	    		System.out.println();
    		}
    	}

    	return null;
    }
}
