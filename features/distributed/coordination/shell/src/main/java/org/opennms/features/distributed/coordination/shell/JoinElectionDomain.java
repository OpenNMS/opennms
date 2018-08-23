/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.distributed.coordination.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;
import org.opennms.features.distributed.coordination.api.RoleChangeHandler;

/**
 * A command to exercise HA redundancy leader election functionality.
 */
@Command(scope = "coordination", name = "join-election-domain", description = "Joins the specified election domain")
@Service
public class JoinElectionDomain implements Action, RoleChangeHandler {
    @Argument(index = 0, name = "domain", description = "The domain to join", required = true, multiValued = false)
    private String domain;
    @Reference
    private static DomainManagerFactory domainManagerFactory;
    private static final String testId = "test.id";

    @Override
    public Object execute() {
        DomainManager manager = domainManagerFactory.getManagerForDomain(domain);
        manager.register(testId, this);

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
                break;
            }
        }

        System.out.println("Leaving domain " + domain);
        manager.deregister(testId);

        return null;
    }

    @Override
    public void becomeActive(String domain) {
        System.out.println("Active for domain " + domain);
    }

    @Override
    public void becomeStandby(String domain) {
        System.out.println("Standby for domain " + domain);
    }
}
