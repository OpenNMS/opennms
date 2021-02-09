/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.shell;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.config.configservice.api.ConfigurationService;


@Command(scope = "onmsconfig", name = "update", description = "updates the config")
@Service
public class UpdateConfigCommand implements Action {

    @Reference
    private ConfigurationService configService;

    @Option(name = "-p", aliases = "--pid", description = "PID to update", required = true)
    private String pid;

    @Option(name = "-k", aliases = "--key", description = "Key to update", required = true)
    private String key;

    @Option(name = "-v", aliases = "--value", description = "Value to update", required = true)
    private String value;

    @Override
    public Object execute() throws Exception {
        if (pid.isEmpty() || key.isEmpty()) {
            System.out.println("pid and key must not be empty.");
            return null;
        }

        // we only support property files currently.
        Dictionary dictionary = configService
                .getConfigurationAsDictionary(pid)
                .orElse(new Hashtable());
        dictionary.put(key, value);
        configService.putConfiguration(pid, dictionary);
        System.out.printf("Updated %s: %s=%s.", pid, key, value);
        return null;
    }
}
