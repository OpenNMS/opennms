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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.GroovyClassLoader;

/**
 * Loads the shipped openconfig-gnmi-mapping.groovy and gives tests access to its nested classes.
 */
final class GnmiMappingScript {

    private GnmiMappingScript() {
    }

    static File file() {
        try {
            return new File(GnmiMappingScript.class.getResource("/openconfig-gnmi-mapping.groovy").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Compiles the script into a fresh class loader, so static state never leaks between tests. */
    static GroovyClassLoader load() throws IOException {
        GroovyClassLoader gcl = new GroovyClassLoader(GnmiMappingScript.class.getClassLoader());
        gcl.parseClass(Files.readString(file().toPath()), "GnmiMappingScriptUnderTest.groovy");
        return gcl;
    }

    static Object call(GroovyClassLoader gcl, String className, String method, Object... args) throws ClassNotFoundException {
        return InvokerHelper.invokeStaticMethod(gcl.loadClass(className), method, args);
    }

    static Object newInstance(GroovyClassLoader gcl, String className, Object... args) throws ClassNotFoundException {
        return InvokerHelper.invokeConstructorOf(gcl.loadClass(className), args);
    }

    static Object prop(Object target, String name) {
        return InvokerHelper.getProperty(target, name);
    }

    static File writeFile(File dir, String name, String... lines) throws IOException {
        File file = new File(dir, name);
        Files.createDirectories(dir.toPath());
        Files.writeString(file.toPath(), String.join("\n", lines) + "\n");
        return file;
    }
}
