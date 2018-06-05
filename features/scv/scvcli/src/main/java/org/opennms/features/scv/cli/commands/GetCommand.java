/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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