/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.configservice.api;

import java.util.Optional;


/**
 * We maintain and serve configurations from a centralized place in a uniform way.
 */
public interface ConfigurationService {

    /**
     * Loads the latest available configuration specified by the URI and transforms it into the given JaxB class.
     */
    <T> Optional<T> getConfigurationAsJaxb(final String uri, final Class<T> clazz);

    /**
     * Loads the latest available configuration specified by the URI as a raw String. It makes no assumptions about it's
     * format, e.g. XML, JSON or Properties
     */
    Optional<String> getConfigurationAsString(final String uri);

    void putConfiguration(final String uri, final String config);

    void registerForUpdates(final String uriOfConfig, final ConfigurationChangeListener listener);
}
