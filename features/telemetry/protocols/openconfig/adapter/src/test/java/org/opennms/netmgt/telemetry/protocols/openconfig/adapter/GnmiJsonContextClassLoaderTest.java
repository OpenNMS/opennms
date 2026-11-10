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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.Test;

import groovy.json.JsonSlurper;
import groovy.lang.GroovyClassLoader;

/**
 * On a Core without Minion, the local sink runs the adapter on a thread whose context class loader
 * sees another copy of groovy-json. JsonSlurper looks up its FastStringService through that loader
 * and failed with "DefaultFastStringServiceFactory not a subtype", so no message was ever mapped.
 */
public class GnmiJsonContextClassLoaderTest {

    /** Loads groovy-json classes from its own copy of the jar, so every instance has fresh statics. */
    private static final class GroovyJsonCopy extends URLClassLoader {
        GroovyJsonCopy() {
            super(new URL[]{JsonSlurper.class.getProtectionDomain().getCodeSource().getLocation()},
                    GnmiJsonContextClassLoaderTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("groovy.json.") && !name.startsWith("org.apache.groovy.json.")) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(name);
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }

    @Test
    public void parsesJsonWhenTheContextClassLoaderSeesAnotherGroovyJson() throws Exception {
        GroovyClassLoader script = new GroovyClassLoader(new GroovyJsonCopy());
        script.parseClass(Files.readString(GnmiMappingScript.file().toPath()), "GnmiMappingScriptUnderTest.groovy");

        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        ClassLoader foreign = new GroovyJsonCopy();
        thread.setContextClassLoader(foreign);
        try {
            Object value = InvokerHelper.invokeStaticMethod(script.loadClass("Parsers"), "parseJsonValue", new Object[]{"\"5\""});
            assertThat(value, is("5"));
            assertThat(thread.getContextClassLoader(), sameInstance(foreign));
        } finally {
            thread.setContextClassLoader(original);
        }
    }
}
