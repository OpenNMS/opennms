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
package org.opennms.netmgt.config;

import junit.framework.AssertionFailedError;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This is an integration test checking if all provided example XML files can be
 * unmarshalled.
 *
 * For each file to test, an entry in the {@link #files()} list must exist.
 * During test run, all tests methods are executed for each test.
 *
 * @see WillItUnmarshalIT
 */
@RunWith(value = Parameterized.class)
public class CmWillItUnmarshalIT {
    /**
     * Possible implementations for resource loading.
     */
    public enum Source {
        CONFIG,
        EXAMPLE,
        SPRING,
        ABSOLUTE,
        CLASSPATH
    }

    /**
     * A list of test parameters to execute.
     *
     * See {@link #files()} for detailed information.
     */
    public static final ArrayList<Object[]> FILES = new ArrayList<>();

    /**
     * Add a file to the list of XML files to test.
     * @param source The {@link Source} type
     * @param file the file to unmarshal
     * @param clazz the class it unmarshals to
     * @param schemaFile
     * @param topLevelElement
     * @param checkFormat
     * @param exceptionMessage
     */
    private static void addFile(final Source source, final String file, final Class<?> clazz, String schemaFile, String topLevelElement, boolean checkFormat, final String exceptionMessage) {
        FILES.add(new Object[] {source, file, clazz, schemaFile, topLevelElement, checkFormat, exceptionMessage});
    }

    static {
        addFile(Source.CONFIG, "provisiond-configuration.xml", ProvisiondConfiguration.class, "provisiond-configuration.xsd", "provisiond-configuration", false, null);
    }

    /**
     * The list of files to test.
     *
     * For each XML file to test, this method must return an entry in the list.
     * Each entry consists of the following parts:
     * <ul>
     *   <li>The source to load the resource from</li>
     *   <li>The file to test</li>
     *   <li>The class used for unmarshaling</li>
     *   <li>The xsd used for unmarshaling</li>
     *   <li>The top-element</li>
     *   <li>Whether to check if the file is in JAXB's default marshal format</li>
     *   <li>An expected exception message</li>
     * </ul>
     *
     * The returned file list is stored in {@link #FILES} which is filled in the
     * static constructor.
     *
     * @return list of parameters for the test
     */
    @Parameterized.Parameters
    public static Collection<Object[]> files() {
        return FILES;
    }

    private final Source source;
    private final String file;
    private final Class<?> clazz;
    private final boolean checkFormat;
    private final String exception;
    private final String schemaFile;
    private final String topLevelElement;

    public CmWillItUnmarshalIT(final Source source,
                               final String file,
                               final Class<?> clazz,
                               final String schemaFile,
                               final String topLevelElement,
                               final boolean checkFormat,
                               final String exception) {
        this.source = source;
        this.file = file;
        this.clazz = clazz;
        this.checkFormat = checkFormat;
        this.exception = exception;
        this.schemaFile  = schemaFile;
        this.topLevelElement = topLevelElement;
    }

    @Test
    public void testUnmarshalling() {
        final Resource resource = this.createResource();
        assertNotNull("Resource must not be null", resource);

        // Unmarshall the config file
        Object result = null;
        try {
            String xmlStr = readResource(resource);
            ConfigDefinition configDefinition = XsdHelper.buildConfigDefinition(this.getClass().getName(), schemaFile, topLevelElement, "/cm");
            ConfigConverter converter = XsdHelper.getConverter(configDefinition);
            String json = converter.xmlToJson(xmlStr);
            result = ConfigConvertUtil.jsonToObject(json, this.clazz);

            // Assert that unmarshalling returned a valid result
            Assert.assertNotNull(json);
            Assert.assertNotEquals(json.length(), 0);
            Assert.assertNotNull("Unmarshalled instance must not be null", result);

        } catch (final AssertionFailedError ex) {
            throw ex;

        } catch (final Exception ex) {
            // If we have an expected exception, the returned exception must
            // match - if not the test failed
            if (this.exception != null) {
                assertEquals(this.exception, exception.toString());

            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void testJaxbFormat() throws IOException, JSONException {
        if (!checkFormat) {
            return;
        }
        final Resource resource = this.createResource();
        assertNotNull("Resource must not be null", resource);

        String xmlStr = readResource(resource);
        ConfigDefinition configDefinition = XsdHelper.buildConfigDefinition(this.getClass().getName(), schemaFile, topLevelElement, "/cm");
        ConfigConverter converter = XsdHelper.getConverter(configDefinition);
        String onDisk = converter.xmlToJson(xmlStr);
        Object result = ConfigConvertUtil.jsonToObject(onDisk, this.clazz);
        String finalJson = ConfigConvertUtil.objectToJson(result);

        JSONAssert.assertEquals(finalJson, onDisk, true);
    }

    private static final String readResource(final Resource resource) {
        try (final InputStream is = resource.getInputStream(); final InputStreamReader isr = new InputStreamReader(is)) {
            return IOUtils.toString(isr);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a resource for the config file to unmarshall using the configured
     * source.
     *
     * @return the Resource
     */
    public final Resource createResource() {
        // Create a resource for the config file to unmarshall using the
        // configured source
        switch (this.source) {
        case CONFIG:
            return new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile(file));

        case EXAMPLE:
            return new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("examples/" + file));

        case SPRING:
            return ConfigurationTestUtils.getSpringResourceForResource(this, this.file);

        case ABSOLUTE:
            return new FileSystemResource(this.file);

        case CLASSPATH:
            return new ClassPathResource(file, clazz);

        default:
            throw new RuntimeException("Source unknown: " + this.source);
        }
    }
}