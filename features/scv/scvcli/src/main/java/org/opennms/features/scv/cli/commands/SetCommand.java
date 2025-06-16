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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.cli.ScvCli;

public class SetCommand implements Function<ScvCli, Integer> {
    @Argument(required = true,
            index = 0,
            metaVar = "alias",
            usage = "the alias for this entry")
    private String alias = null;

    @Argument(required = true,
            index = 1,
            metaVar = "username",
            usage = "the username to be set")
    private String username = null;

    @Argument(required = true,
            index = 2,
            metaVar = "password",
            usage = "the password to be set")
    private String password = null;

    @Option(name="--attribute",
            aliases = {"-a"},
            handler = MapOptionHandler.class)
    Map<String,String> attributes = new HashMap<>();

    @Override
    public Integer apply(ScvCli scvCli) {
        scvCli.getSecureCredentialsVault().setCredentials(alias, new Credentials(username, password, attributes));
        return 0;
    }
}