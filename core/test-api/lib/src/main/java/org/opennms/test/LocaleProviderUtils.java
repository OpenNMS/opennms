/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes","unchecked","java:S3011"})
public abstract class LocaleProviderUtils {
    private static Logger LOG = LoggerFactory.getLogger(LocaleProviderUtils.class);

    private static List defaultAdapters = null;

    private LocaleProviderUtils() {}

    public static void compat() throws ReflectiveOperationException, IllegalArgumentException {
        LOG.warn("The previously configured locale provider is being overridden to use the JRE (compatibility) provider.");
        final Class provider = Class.forName("sun.util.locale.provider.LocaleProviderAdapter");

        final Field adapterPreference = provider.getDeclaredField("adapterPreference");
        makeAccessible(adapterPreference);

        if (defaultAdapters == null) {
            defaultAdapters = (List)adapterPreference.get(provider);
        }

        final Class<Enum> type = (Class<Enum>) Class.forName("sun.util.locale.provider.LocaleProviderAdapter$Type");
        final Enum compat = Enum.valueOf(type, "JRE");

        final List adapters = new ArrayList();
        adapters.add(compat);
        adapterPreference.set(null, Collections.unmodifiableList(adapters));
    }

    public static void reset() throws ReflectiveOperationException, IllegalArgumentException {
        if (defaultAdapters == null) {
            throw new IllegalStateException("can't reset to defaults, because we have never stored the defaults!");
        }
        LOG.warn("Resetting the locale provider to {}", defaultAdapters.stream().map(Object::toString).collect(Collectors.joining(",")));
        final Class provider = Class.forName("sun.util.locale.provider.LocaleProviderAdapter");
        final Field adapterPreference = provider.getDeclaredField("adapterPreference");
        makeAccessible(adapterPreference);
        adapterPreference.set(null, defaultAdapters);
    }

    private static void makeAccessible(final Field adapterPreference) throws ReflectiveOperationException, IllegalArgumentException {
        adapterPreference.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(adapterPreference, adapterPreference.getModifiers() & ~Modifier.FINAL);
    }
}
