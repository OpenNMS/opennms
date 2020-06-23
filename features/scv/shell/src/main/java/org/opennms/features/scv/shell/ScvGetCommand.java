/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.scv.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

@Command(scope = "scv", name = "get", description="Retrieves the username and attributes for the given alias.")
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
