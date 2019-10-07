/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.kvstore.json.shell;

import java.util.Objects;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.kvstore.api.JsonStore;

@Command(scope = "opennms-kv-json", name = "put", description = "Insert a record into the JSON store")
@Service
public class JsonStorePut implements Action {
    @Reference
    private JsonStore jsonStore;

    @Argument(index = 0, description = "The key", required = true)
    private String key;

    @Argument(index = 1, description = "The key's context", required = true)
    private String context;

    @Argument(index = 2, description = "The value to put", required = true)
    private String value;

    @Override
    public Object execute() {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);
        Objects.requireNonNull(value);

        jsonStore.put(key, value, context);

        return null;
    }
}
