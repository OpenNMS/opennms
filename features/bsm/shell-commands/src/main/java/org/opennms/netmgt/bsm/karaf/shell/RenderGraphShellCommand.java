/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.karaf.shell;

import java.io.File;
import java.util.Objects;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;

@Command(scope = "bsm", name = "render-graph", description="Renders the current state machine graph to a .png file.")
public class RenderGraphShellCommand extends OsgiCommandSupport {

    private BusinessServiceStateMachine businessServiceStateMachine;

    @Override
    protected Object doExecute() throws Exception {
        File tempFile = File.createTempFile("bsm-state-machine", ".png");
        businessServiceStateMachine.renderGraphToPng(tempFile);
        System.out.println("Succesfully rendered state machine graph to " + tempFile.getAbsolutePath());
        return null;
    }

    public void setBusinessServiceStateMachine(BusinessServiceStateMachine businessServiceStateMachine) {
        this.businessServiceStateMachine = Objects.requireNonNull(businessServiceStateMachine);
    }
}
