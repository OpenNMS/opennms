/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.shell;

import java.util.Map;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "opennms", name = "cm-set", description = "Set the value of a key")
@Service
public class SetCommand  extends ConfigCommandSupport {

    @Argument(index=0,name="key",required = true)
    @Completion(KeyCompleter.class)
    String key;

    @Argument(index=1,name="value",required = true)
    String value;

    @Override
    public Object execute() {
        Map<String,String> props = (Map<String,String>) this.session.get(CM_PROPS);
        if (props == null) {
            System.err.println("No configuration is being edited. Run the edit command first.");
            return null;
        }
        try {
            keyHandler.testValue(key, value);
            props.put(key, value);
        } catch (Exception e) {
            System.err.printf("Invalid value at key '%s': %s: %s\n", key, value, e);
        }
        return null;
    }
}