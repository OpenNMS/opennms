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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.mockito.Mockito;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;

import groovy.lang.GroovyClassLoader;

/**
 * Loads the shipped openconfig-gnmi-mapping.groovy and gives tests access to its nested classes.
 */
final class GnmiMappingScript {

    /** Where the script looks for mapping files, relative to opennms.home. */
    static final String MAPPING_DIR = "etc/gnmi-datacollection-config.d";

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

    /** Runs the whole script, as the adapter does, for node id 1. */
    static CollectionSet run(Object message) throws Exception {
        CollectionAgent agent = Mockito.mock(CollectionAgent.class);
        Mockito.when(agent.getNodeId()).thenReturn(1);
        // On a Core the mapper knows the configured resource types; building resolves generic types through it
        ResourceTypeMapper.getInstance().setResourceTypeMapper(GnmiMappingScript::resourceType);
        return new ScriptedCollectionSetBuilder(file()).build(agent, message, 0L);
    }

    private static ResourceType resourceType(String name) {
        ResourceType type = Mockito.mock(ResourceType.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(type.getName()).thenReturn(name);
        // Same strategies as the shipped ocComponent type in resource-types.d
        Mockito.when(type.getStorageStrategy().getClazz()).thenReturn(IndexStorageStrategy.class.getName());
        Mockito.when(type.getPersistenceSelectorStrategy().getClazz()).thenReturn(PersistAllSelectorStrategy.class.getName());
        return type;
    }

    /** One sorted line per attribute: resource-type|instance|group|name|type|value */
    static String render(CollectionSet set) {
        List<String> lines = new ArrayList<>();
        set.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                String value = attribute.getType() == AttributeType.STRING
                        ? attribute.getStringValue()
                        : String.valueOf(attribute.getNumericValue());
                lines.add(String.join("|",
                        attribute.getResource().getResourceTypeName(),
                        attribute.getResource().getInstance(),
                        attribute.getAttributeType().getGroupType().getName(),
                        attribute.getName(),
                        attribute.getType().getName(),
                        value));
            }
        });
        Collections.sort(lines);
        return String.join("\n", lines);
    }

    /** Copies mapping.yaml or mapping.json from a fixture directory to home/etc/gnmi-datacollection-config.d/. */
    static void installMapping(File home, Path caseDir) throws IOException {
        Path target = home.toPath().resolve(MAPPING_DIR);
        Files.createDirectories(target);
        Path yaml = caseDir.resolve("mapping.yaml");
        if (Files.exists(yaml)) {
            Files.copy(yaml, target.resolve("mapping.yaml"));
        } else {
            Files.copy(caseDir.resolve("mapping.json"), target.resolve("mapping.json"));
        }
    }
}
