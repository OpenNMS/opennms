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

package org.opennms.netmgt.telemetry.listeners.flow.ie;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;

public class InformationElement {

    private final int id;

    private final String name;

    private final Value.Parser parser;
    private final Optional<Semantics> semantics;

    public InformationElement(final int id,
                              final String name,
                              final Value.Parser parser,
                              final Optional<Semantics> semantics) {
        this.id = id;
        this.name = name;
        this.parser = parser;
        this.semantics = semantics;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Optional<Semantics> getSemantics() {
        return this.semantics;
    }

    public Value parse(final TemplateManager.TemplateResolver templateResolver, final ByteBuffer buffer) throws InvalidPacketException {
        return this.parser.parse(templateResolver, buffer);
    }

    public int getMaximumFieldLength() {
        return this.parser.getMaximumFieldLength();
    }

    public int getMinimumFieldLength() {
        return this.parser.getMinimumFieldLength();
    }
}
