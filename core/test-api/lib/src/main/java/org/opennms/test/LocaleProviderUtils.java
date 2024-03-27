/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"deprecation","rawtypes","unchecked","java:S3011"})
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
        final Field modifiersField = getModifiersField();
        modifiersField.setAccessible(true);
        modifiersField.setInt(adapterPreference, adapterPreference.getModifiers() & ~Modifier.FINAL);
    }

    public static Field getModifiersField() throws IllegalAccessException, NoSuchFieldException {
        // this is copied from https://github.com/powermock/powermock/pull/1010/files to
        // work around
        // JDK 12+
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                boolean accessibleBeforeSet = getDeclaredFields0.isAccessible();
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                getDeclaredFields0.setAccessible(accessibleBeforeSet);
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        modifiersField = field;
                        break;
                    }
                }
                if (modifiersField == null) {
                    throw e;
                }
            } catch (NoSuchMethodException | InvocationTargetException ex) {
                e.addSuppressed(ex);
                throw e;
            }
        }
        return modifiersField;
    }
}
