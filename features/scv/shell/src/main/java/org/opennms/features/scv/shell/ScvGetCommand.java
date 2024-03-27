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
package org.opennms.features.scv.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

@Command(scope = "opennms", name = "scv-get", description="Retrieves the username and attributes for the given alias.")
@Service
public class ScvGetCommand implements Action {

    @Reference
    public SecureCredentialsVault secureCredentialsVault;

    @Argument(index = 0, name = "alias", description = "Alias used to retrieve the credentials.", required = true, multiValued = false)
    @Completion(AliasCompleter.class)
    public String alias = null;

    @Override
    public Object execute() throws Exception {
        final Credentials credentials = secureCredentialsVault.getCredentials(alias);
        if (credentials == null) {
            System.out.println("No credentials found for alias '" + alias + "'.");
        } else {
            System.out.printf("Credentials for %s:\n", alias);
            System.out.printf("\tUsername: %s\n", credentials.getUsername());
            System.out.printf("\tPassword: *********\n");
            for (String attributeKey : credentials.getAttributeKeys()) {
                System.out.printf("\t%s: %s\n", attributeKey, credentials.getAttribute(attributeKey));
            }
        }
        return null;
    }
}
