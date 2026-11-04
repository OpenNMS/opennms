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
package org.opennms.netmgt.rrd.rrdtool;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.opennms.netmgt.rrd.jrrd2.api.JRrd2;
import org.opennms.netmgt.rrd.jrrd2.impl.JRrd2Jni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chooses the {@link JRrd2} implementation.
 *
 * <p>Controlled by the {@value #IMPL_PROPERTY} system property:
 * <dl>
 *   <dt>{@code auto} (default)</dt><dd>use the FFM binding to the system librrd if it loads, otherwise JNI (libjrrd2)</dd>
 *   <dt>{@code ffm}</dt><dd>FFM only; fail if it cannot be loaded</dd>
 *   <dt>{@code jni}</dt><dd>libjrrd2 JNI only (previous behaviour)</dd>
 * </dl>
 *
 * <p>The FFM implementation is compiled for a newer Java release than this
 * module, so it is only referenced reflectively. On an older JVM it fails to
 * load and {@code auto} falls back to JNI.
 */
public final class JRrd2Provider {
    private static final Logger LOG = LoggerFactory.getLogger(JRrd2Provider.class);

    public static final String IMPL_PROPERTY = "org.opennms.rrd.jrrd2.impl";
    static final String FFM_CLASS = "org.opennms.netmgt.rrd.rrdtool.ffm.JRrd2Ffm";

    private JRrd2Provider() {}

    public static JRrd2 newInstance() {
        final String mode = System.getProperty(IMPL_PROPERTY, "auto").trim().toLowerCase(Locale.ROOT);
        switch (mode) {
            case "ffm":
                return newFfm();
            case "jni":
                return newJni();
            case "auto":
            default:
                try {
                    final JRrd2 ffm = newFfm();
                    LOG.info("Using FFM librrd binding ({}).", FFM_CLASS);
                    return ffm;
                } catch (final Throwable ffmFailure) {
                    LOG.info("FFM librrd binding unavailable ({}); falling back to JNI (libjrrd2).", ffmFailure.toString());
                    try {
                        return newJni();
                    } catch (final Throwable jniFailure) {
                        jniFailure.addSuppressed(ffmFailure);
                        throw jniFailure;
                    }
                }
        }
    }

    private static JRrd2 newJni() {
        return new JRrd2Jni();
    }

    private static JRrd2 newFfm() {
        try {
            final Class<?> clazz = Class.forName(FFM_CLASS, true, JRrd2Provider.class.getClassLoader());
            return (JRrd2) clazz.getDeclaredConstructor().newInstance();
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Failed to create " + FFM_CLASS, cause);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create " + FFM_CLASS, e);
        }
    }
}
