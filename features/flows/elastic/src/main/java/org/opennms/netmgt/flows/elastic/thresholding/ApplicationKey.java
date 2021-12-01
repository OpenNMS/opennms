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

package org.opennms.netmgt.flows.elastic.thresholding;

import java.util.Objects;

import org.opennms.netmgt.flows.elastic.Direction;

public class ApplicationKey {
    public final int iface;
    public final Direction direction;
    public final String application;

    public ApplicationKey(final int iface,
                          final Direction direction,
                          final String application) {
        this.iface = iface;
        this.direction = direction;
        this.application = Objects.requireNonNull(application);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationKey)) {
            return false;
        }
        final ApplicationKey that = (ApplicationKey) o;
        return Objects.equals(this.iface, that.iface) &&
               Objects.equals(this.direction, that.direction) &&
               Objects.equals(this.application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.iface,
                            this.direction,
                            this.application);
    }
}
