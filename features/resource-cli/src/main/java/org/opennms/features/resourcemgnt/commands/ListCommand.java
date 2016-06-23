package org.opennms.features.resourcemgnt.commands;

import com.google.common.base.Strings;
import org.opennms.features.resourcemgnt.ResourceCli;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.netmgt.model.resource.ResourceDTOCollection;

public class ListCommand extends AbstractCommand {

    private void print(final ResourceDTO resource, final int indent) {
        System.out.println(Strings.repeat("  ", indent) + resource.getId());

        if (resource.getChildren() != null) {
            for (final ResourceDTO childResource : resource.getChildren().getObjects()) {
                print(childResource, indent + 1);
            }
        }
    }

    @Override
    public void execute(final ResourceCli resourceCli) throws Exception {
        // Request and print the data
        final ResourceDTOCollection resourceDTOCollection = connect(resourceCli)
                .header("Accept", "application/xml")
                .get(ResourceDTOCollection.class);

        for (final ResourceDTO resource : resourceDTOCollection.getObjects()) {
            print(resource, 0);
        }
    }
}
