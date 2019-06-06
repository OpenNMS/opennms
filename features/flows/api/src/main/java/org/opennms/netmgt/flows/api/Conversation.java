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

package org.opennms.netmgt.flows.api;

import java.util.Optional;

public class Conversation {
    public final String location;
    public final Integer protocol;
    public final String lowerIp;
    public final String upperIp;
    public final String lowerHostname;
    public final String upperHostname;
    public final String application;

    public Conversation(final String location,
                        final Integer protocol,
                        final String lowerIp,
                        final String upperIp,
                        final String lowerHostname,
                        final String upperHostname,
                        final String application) {
        this.location = location;
        this.protocol = protocol;
        this.lowerIp = lowerIp;
        this.upperIp = upperIp;
        this.lowerHostname = lowerHostname;
        this.upperHostname = upperHostname;
        this.application = application;
    }

    public Conversation withHostnames(final Optional<String> lowerHostname,
                                      final Optional<String> upperHostname) {
        return new Conversation(this.location,
                this.protocol,
                this.lowerIp,
                this.upperIp,
                lowerHostname.orElse(null),
                upperHostname.orElse(null),
                this.application);
    }

    public static Conversation from(final ConversationKey key) {
        return new Conversation(key.getLocation(),
                key.getProtocol(),
                key.getLowerIp(),
                key.getUpperIp(),
                null,
                null,
                key.getApplication());
    }
}
