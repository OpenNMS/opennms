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

package org.opennms.core.ipc.sink.kafka.server;

import java.util.Objects;
import java.util.function.Supplier;

public class Utils {
    // HACK: When defining key.deserializer/value.deserializer classes, the kafka client library
    // tries to instantiate them by using the ClassLoader returned by Thread.currentThread().getContextClassLoader() if defined.
    // As that ClassLoader does not know anything about that classes a ClassNotFoundException is thrown
    // By setting the ClassLoader to null, the BundleContextClassLoader of the kafka client library is used instead,
    // which can instantiate those classes more likely (depending on Import/DynamicImport-Package definitions)
    public static <T> T runWithNullContextClassLoader(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
}
