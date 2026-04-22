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
package org.opennms.features.apitokens.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.apitokens.ApiTokenCreateResponse;
import org.opennms.features.apitokens.ApiTokenService;

@Command(scope = "opennms", name = "api-token-generate", description = "Generate an API token for a user")
@Service
public class ApiTokenGenerateCommand implements Action {

    @Reference
    private ApiTokenService apiTokenService;

    @Argument(index = 0, name = "username", description = "Username to generate token for", required = true)
    private String username;

    @Option(name = "--description", aliases = {"-d"}, description = "Token description")
    private String description;

    @Option(name = "--expiry-days", aliases = {"-e"}, description = "Token expiry in days")
    private Integer expiryDays;

    @Override
    public Object execute() throws Exception {
        ApiTokenCreateResponse response = apiTokenService.createToken(username, description, expiryDays);
        System.out.println("Token generated successfully.");
        System.out.println("Token:       " + response.getToken());
        System.out.println("ID:          " + response.getId());
        System.out.println("Description: " + (response.getDescription() != null ? response.getDescription() : ""));
        System.out.println("Created:     " + response.getCreatedAt());
        System.out.println("Expires:     " + response.getExpiresAt());
        System.out.println();
        System.out.println("WARNING: This token will not be shown again. Copy it now.");
        return null;
    }
}
