/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.config.api;

import java.util.List;

/**
 * {@link TelemetryBeanDefinition} to define listeners.
 * Besides the common attributes, a listener may define one or more parsers.
 *
 *  @author mvrueden
 */
public interface ListenerDefinition extends TelemetryBeanDefinition {
    /**
     * The {@link ParserDefinition} to create.
     * Should not be null or empty.
     * If empty, the listener will not be created.
     *
     * @return The list of parsers to create. Should neither be null or empty. However if empty, the listener will not be created
     */
    List<? extends ParserDefinition> getParsers();

}
