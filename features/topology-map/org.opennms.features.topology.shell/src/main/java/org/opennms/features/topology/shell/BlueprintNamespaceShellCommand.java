package org.opennms.features.topology.shell;

import java.util.ArrayList;

import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

@Command(scope = "onms", name = "listnamespaces", description="Lists the available blueprint namespaces and their providers.")
public class BlueprintNamespaceShellCommand extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {

        final ServiceReference[] services = this.bundleContext.getServiceReferences(NamespaceHandler.class.getName(), null);
    	for (final ServiceReference sr : services) {
    		final Bundle bundle = sr.getBundle();
    		final Object rawNamespaces = sr.getProperty("osgi.service.blueprint.namespace");

    		final ArrayList<String> namespaces = new ArrayList<String>();
    		if (rawNamespaces instanceof String) {
    			namespaces.add((String)rawNamespaces);
    		} else if (rawNamespaces instanceof Object[]) {
    			for (final Object namespace : (Object[])rawNamespaces) {
    				namespaces.add(namespace.toString());
    			}
    		} else if (rawNamespaces instanceof String[]) {
    			for (final String namespace : (String[])rawNamespaces) {
    				namespaces.add(namespace);
    			}
    		} else {
    			System.err.println("Hmm, not sure how to interpret: " + rawNamespaces);
    		}

    		System.out.println(bundle.toString());
    		for (final Object namespace : namespaces) {
    			System.out.println("    " + namespace);
    		}
    		System.out.println();
    	}
        return null;
    }
}
