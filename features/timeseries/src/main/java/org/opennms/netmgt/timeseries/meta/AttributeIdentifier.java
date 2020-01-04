/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.timeseries.meta;

import org.opennms.netmgt.collection.support.builder.Attribute;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;

/** Unique identifier for a Attribute. There can't be more than one Attribute with the same identifier.
 *  // TODO: Patrick: discuss with Jesse if this implementation is correct
 */
public class AttributeIdentifier {

    // TODO: Patrick: it would also be nice if we could simplify this composite key. Maybe just convert it to a path, similar to an URL?
    private final String resourceId;
    private final String group;
    private final String metricName;
    private final String type;
    private final String attributeName;


    private AttributeIdentifier(
            final String resourceId,
            final String group,
            final String metricName,
            final String type,
            final String attributeName) {
        this.resourceId = resourceId;
        this.group = group;
        this.metricName = metricName;
        this.type = type;
        this.attributeName = attributeName;
    }

    public static AttributeIdentifier of(final Attribute attribute) {
        return of(attribute.getIdentifier(), attribute.getGroup(), attribute.getName(), attribute.getType().name());
    }

    public static AttributeIdentifier of(final Resource resource, final String group, final String metricName,
                                         final MetricType type) {
        return of(resource.getId(), group, metricName, type.name());
    }

    public static AttributeIdentifier of(final String resourceId, final String group, final String metricName,
        final String type) {
        return new AttributeIdentifier(resourceId, group, metricName, type, null);
    }

    public static AttributeIdentifier of(final String resourceId, final String group, final String metricName,
                                         final String type, final String attributeName) {
        return new AttributeIdentifier(resourceId, group, metricName, type, null);
    }

    public AttributeIdentifier withAttributeName(final String attributeName) {
        return of(this.resourceId, this.group, this.metricName, this.type, attributeName);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        append(b, "identifier", resourceId);
        append(b, "group", group);
        append(b, "metricName", metricName);
        append(b, "type", type);
        append(b, "attributeName", attributeName);
        return b.toString();
    }

    private void append(final StringBuilder b, String name, String element ) {
        if(element != null) {
            if(b.length() > 0) {
                b.append("_");
            }
            b.append(name).append(":").append(element);
        }
    }
}
