package org.opennms.features.topology.plugins.ncs;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.osgi.framework.ServiceReference;

@Command(scope = "ncs", name = "listcomponents", description="Lists the available NCS components.")
public class ComponentCommand extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        final ServiceReference<NCSComponentRepository> sr = this.bundleContext.getServiceReference(NCSComponentRepository.class);
        if (sr == null) return null;

        final NCSComponentRepository repository = this.bundleContext.getService(sr);
        for (final NCSComponent component : repository.findAll()) {
            System.out.println("    " + component.toString());
        }
        System.out.println();

        return null;
    }

}
