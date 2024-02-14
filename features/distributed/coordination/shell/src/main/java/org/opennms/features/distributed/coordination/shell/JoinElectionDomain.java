/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.distributed.coordination.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;
import org.opennms.features.distributed.coordination.api.Role;
import org.opennms.features.distributed.coordination.api.RoleChangeHandler;

/**
 * A command to exercise HA redundancy leader election functionality.
 */
@Command(scope = "opennms", name = "join-election-domain", description = "Joins the specified election domain")
@Service
public class JoinElectionDomain implements Action, RoleChangeHandler {
    @Argument(index = 0, name = "domain", description = "The domain to join", required = true, multiValued = false)
    private String domain;
    @Reference
    private DomainManagerFactory domainManagerFactory;
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
    public void handleRoleChange(Role role, String domain) {
        System.out.println(role + " for domain " + domain);
    }
}
