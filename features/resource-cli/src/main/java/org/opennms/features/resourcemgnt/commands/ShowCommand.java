package org.opennms.features.resourcemgnt.commands;

import java.util.Map;

import org.kohsuke.args4j.Argument;
import org.opennms.features.resourcemgnt.ResourceCli;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.resource.ResourceDTO;

public class ShowCommand extends AbstractCommand {

    @Argument(required = true,
              metaVar = "resource",
              usage = "the resource to show")
    private String resource = "";

    @Override
    public void execute(final ResourceCli resourceCli) throws Exception {
        // Request and print the data
        final ResourceDTO resource = connect(resourceCli, this.resource)
                .header("Accept", "application/xml")
                .get(ResourceDTO.class);

        System.out.println("ID:         " + resource.getId());
        System.out.println("Name:       " + resource.getName());
        System.out.println("Label:      " + resource.getLabel());
        System.out.println("Type:       " + resource.getTypeLabel());
        System.out.println("Link:       " + resource.getLink());
        System.out.println("Parent ID:  " + resource.getParentId());

        System.out.println("Children:");
        if (resource.getChildren() != null) {
            for (final ResourceDTO childResource : resource.getChildren().getObjects()) {
                System.out.println("  " + childResource.getId());
            }
        }

        System.out.println("Attributes:");

        System.out.println("  External:");
        for (final Map.Entry<String, String> e : resource.getExternalValueAttributes().entrySet()) {
            System.out.println("    " + e.getKey() + " = '" + e.getValue() + "'");
        }

        System.out.println("  Metrics:");
        for (final Map.Entry<String, RrdGraphAttribute> e : resource.getRrdGraphAttributes().entrySet()) {
            System.out.println(
                    "    " + e.getKey() + " = '" + e.getValue().getRrdFile() + "'");
        }

        System.out.println("  Strings:");
        for (final Map.Entry<String, String> e : resource.getStringPropertyAttributes().entrySet()) {
            System.out.println("    " + e.getKey() + " = '" + e.getValue() + "'");
        }
    }
}
