/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
