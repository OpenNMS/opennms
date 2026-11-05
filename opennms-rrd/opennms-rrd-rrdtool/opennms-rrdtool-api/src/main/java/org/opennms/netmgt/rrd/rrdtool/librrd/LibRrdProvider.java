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
package org.opennms.netmgt.rrd.rrdtool.librrd;

import java.lang.reflect.InvocationTargetException;

/**
 * Provides the {@link LibRrd} implementation.
 *
 * <p>The implementation calls the system librrd through the Java Foreign
 * Function &amp; Memory API. It lives in the {@code opennms-rrdtool-ffm}
 * module, which is compiled for a newer Java release than this one, so it is
 * only referenced reflectively.
 */
public final class LibRrdProvider {

    static final String FFM_CLASS = "org.opennms.netmgt.rrd.rrdtool.ffm.FfmLibRrd";

    private static volatile LibRrd instance;

    private LibRrdProvider() {}

    /**
     * @return the shared {@link LibRrd} instance
     * @throws IllegalStateException if the implementation is not on the class
     *         path, the JVM does not support FFM, or librrd cannot be loaded
     */
    public static LibRrd get() {
        LibRrd lib = instance;
        if (lib == null) {
            synchronized (LibRrdProvider.class) {
                lib = instance;
                if (lib == null) {
                    lib = load();
                    instance = lib;
                }
            }
        }
        return lib;
    }

    private static LibRrd load() {
        final Class<?> clazz;
        try {
            clazz = Class.forName(FFM_CLASS, true, LibRrdProvider.class.getClassLoader());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(FFM_CLASS + " is not on the class path (opennms-rrdtool-ffm).", e);
        } catch (final UnsupportedClassVersionError e) {
            throw new IllegalStateException("The librrd binding requires Java 22 or newer; running on Java "
                    + Runtime.version().feature() + ".", e);
        }
        try {
            return (LibRrd) clazz.getDeclaredConstructor().newInstance();
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            throw new IllegalStateException("Unable to initialize the librrd binding: " + cause.getMessage(), cause);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create " + FFM_CLASS, e);
        }
    }
}
