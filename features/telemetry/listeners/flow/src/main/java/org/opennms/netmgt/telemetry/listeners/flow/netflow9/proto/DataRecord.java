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

package org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.session.Field;
import org.opennms.netmgt.telemetry.listeners.flow.session.Template;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;

import com.google.common.base.MoreObjects;

public final class DataRecord implements Record {

    /*
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
      ...
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
    */

    public final DataSet set;  // Enclosing set

    public final Template template;
    public final List<FieldValue> fields;

    public DataRecord(final DataSet set,
                      final TemplateManager.TemplateResolver templateResolver,
                      final Template template,
                      final ByteBuffer buffer) throws InvalidPacketException {
        this.set = Objects.requireNonNull(set);

        this.template = Objects.requireNonNull(template);

        final List<FieldValue> values = new ArrayList<>(template.count());
        for (final Field templateField : template) {
            values.add(new FieldValue(templateResolver, templateField, buffer));
        }

        this.fields = Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fields", fields)
                .toString();
    }
}
