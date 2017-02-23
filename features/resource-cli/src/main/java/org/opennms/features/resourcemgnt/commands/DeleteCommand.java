package org.opennms.features.resourcemgnt.commands;

import org.kohsuke.args4j.Argument;
import org.opennms.features.resourcemgnt.ResourceCli;

public class DeleteCommand extends AbstractCommand {

    @Argument(required = true,
              metaVar = "resource",
              usage = "the resource to delete")
    private String resource = "";

    @Override
    public void execute(final ResourceCli resourceCli) throws Exception {
        // Delete the data
        connect(resourceCli, this.resource)
                .delete();
    }
}
