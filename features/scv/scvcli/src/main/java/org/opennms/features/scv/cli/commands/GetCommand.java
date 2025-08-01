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
package org.opennms.features.scv.cli.commands;

import java.util.function.Function;

import org.kohsuke.args4j.Argument;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.cli.ScvCli;

public class GetCommand implements Function<ScvCli, Integer> {
    @Argument(required = true,
            index = 0,
            metaVar = "alias",
            usage = "the alias for this entry")
    private String alias = null;

    @Override
    public Integer apply(ScvCli scvCli) {
        Credentials credentials = scvCli.getSecureCredentialsVault().getCredentials(alias);

        if (credentials == null) {
            System.err.println("No credentials found for alias '" + alias + "'.");
            return 1;
        } else {
            System.out.printf("Credentials for %s:\n", alias);
            System.out.printf("\tUsername: %s\n", credentials.getUsername());
            System.out.printf("\tPassword: *********\n");
            for (String attributeKey : credentials.getAttributeKeys()) {
                System.out.printf("\t%s: %s\n", attributeKey, credentials.getAttribute(attributeKey));
            }
        }
        return 0;
    }
}