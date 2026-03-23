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

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.apitokens.ApiToken;
import org.opennms.features.apitokens.ApiTokenService;

@Command(scope = "opennms", name = "api-token-list", description = "List API tokens for a user")
@Service
public class ApiTokenListCommand implements Action {

    @Reference
    private ApiTokenService apiTokenService;

    @Argument(index = 0, name = "username", description = "Username to list tokens for", required = true)
    private String username;

    @Override
    public Object execute() throws Exception {
        List<ApiToken> tokens = apiTokenService.listTokens(username);
        if (tokens.isEmpty()) {
            System.out.println("No tokens found for user: " + username);
            return null;
        }
        System.out.printf("%-6s %-20s %-24s %-24s %-24s%n", "ID", "Description", "Created", "Expires", "Last Used");
        System.out.println("-".repeat(100));
        for (ApiToken token : tokens) {
            System.out.printf("%-6d %-20s %-24s %-24s %-24s%n",
                    token.getId(),
                    truncate(token.getDescription(), 20),
                    token.getCreatedAt(),
                    token.getExpiresAt(),
                    token.getLastUsedAt() != null ? token.getLastUsedAt() : "never");
        }
        return null;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
