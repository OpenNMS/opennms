package org.opennms.features.resourcemgnt.commands;

import org.opennms.features.resourcemgnt.ResourceCli;

public interface Command {
    void execute(final ResourceCli resourceCli) throws Exception;
}
