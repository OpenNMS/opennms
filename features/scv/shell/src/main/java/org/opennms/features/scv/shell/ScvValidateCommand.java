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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

@Command(scope = "opennms", name = "scv-validate", description = "validate credentials for the given alias.")
@Service
public class ScvValidateCommand implements Action {

    @Reference
    public SecureCredentialsVault secureCredentialsVault;

    @Option(name = "-u", aliases = "--username", description = "Username to validate.", required = true, multiValued = false)
    public String username = null;

    @Option(name = "-p", aliases = "--password", description = "Password to validate.", required = true, multiValued = false, censor = true, mask = '*')
    public String password = null;

    @Argument(index = 0, name = "alias", description = "Alias used to retrieve the credentials.", required = true, multiValued = false)
    @Completion(AliasCompleter.class)
    public String alias = null;

    @Override
    public Object execute() throws Exception {
        final Credentials credentials = secureCredentialsVault.getCredentials(alias);
        if (credentials == null) {
            System.out.println("No credentials found for Alias '" + alias + "'.");
        } else if (credentials.getUsername() != null && credentials.getPassword() != null &&
                credentials.getUsername().equals(username) && credentials.getPassword().equals(password)) {
            System.out.printf("Found valid credentials for Alias %s, Username: %s, Password: ******\n", alias, username);
        } else if (credentials.getAttribute(username) != null && credentials.getAttribute(username).equals(password)) {
            System.out.printf("Found valid credentials for Alias %s, Attribute Key: %s, Attribute Value: ******\n", alias, username);
        } else {
            System.out.printf("No valid credentials found for Alias %s, Username: %s and Password: ****** \n", alias, username);
        }
        return null;
    }
}
