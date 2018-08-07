/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.distributed.minion;

import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.opennms.netmgt.telemetry.listeners.api.ListenerDefinition;

public class ListenerHealthCheck implements HealthCheck {

    private final String listenerName;
    private final String listenerType;
    private Response response = new Response(Status.Starting);

    public ListenerHealthCheck(ListenerDefinition listenerDefinition) {
        this(listenerDefinition.getName(), listenerDefinition.getClassName());
    }

    private ListenerHealthCheck(String listenerName, String listenerType) {
        this.listenerName = listenerName;
        this.listenerType = listenerType;
    }

    @Override
    public String getDescription() {
        return "Verifying Listener " + listenerName + " (" + listenerType + ")";
    }

    @Override
    public Response perform() throws Exception {
        return response;
    }

    public void markSucess() {
        response = new Response(Status.Success);
    }

    public void markError(Exception e) {
        response = new Response(e);
    }

}


