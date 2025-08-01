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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

import java.util.HashMap;
import java.util.Map;

@Command(scope = "opennms", name = "scv-set", description="Sets and securely store the credentials for the given alias.")
@Service
public class ScvSetCommand implements Action {

    @Reference
    public SecureCredentialsVault secureCredentialsVault;

    @Argument(index = 0, name = "alias", description = "Alias used to retrieve the credentials.", required = true, multiValued = false)
    @Completion(AliasCompleter.class)
    public String alias = null;

    @Argument(index = 1, name = "username", description = "Username to store.", required = true, multiValued = false)
    public String username = null;

    @Argument(index = 2, name = "password", description = "Password to store.", required = true, multiValued = false)
    public String password = null;

    @Option(name="-a", description="Attributes to store with the credentials.", multiValued = true)
    public String[] attributes;

    @Override
    public Object execute() throws Exception {
        Map<String, String> properties = new HashMap<>();
        if (attributes != null) {
            for (String attributeKVPair : attributes) {
                try {
                    String[] tok = attributeKVPair.split("=");
                    properties.put(tok[0], tok[1]);
                }
                catch (Exception e) {
                    System.err.println("Invalid attribute specification: " + attributeKVPair);
                    e.printStackTrace();
                }
            }
        }
        final Credentials credentials = new Credentials(username, password, properties);
        secureCredentialsVault.setCredentials(alias, credentials);
        return null;
    }
}
