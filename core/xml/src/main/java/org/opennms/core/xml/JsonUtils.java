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
package org.opennms.core.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class JsonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

    private static final JsonExceptionTranslator EXCEPTION_TRANSLATOR = new JsonExceptionTranslator();
    private static ThreadLocal<Map<Class<?>, ObjectMapper>> m_mappers = new ThreadLocal<>();

    private static class JsonExceptionTranslator {
        public RuntimeException translate(String operation, Exception e) {
            return new RuntimeException("JSON " + operation + " failed: " + e.getMessage(), e);
        }
    }

    private JsonUtils() {
    }

    // XML to JSON Conversion Methods

    /**
     * Convert XML string to JSON string using JaxbUtils unmarshalling
     */
    public static String xmlToJson(String xml, Class<?> clazz) {
        return xmlToJson(xml, clazz, false);
    }

    /**
     * Convert XML string to JSON string with pretty-printing option
     */
    public static String xmlToJson(String xml, Class<?> clazz, boolean prettyPrint) {
        try {
            LOG.trace("Converting XML to JSON for class: {}", clazz.getSimpleName());
            // Step 1: XML to Object using JaxbUtils
            Object obj = JaxbUtils.unmarshal(clazz, xml);
            // Step 2: Object to JSON using JsonUtils
            return marshal(obj, prettyPrint);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("XML to JSON conversion for " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert XML file to JSON string
     */
    public static String xmlToJson(File xmlFile, Class<?> clazz) {
        return xmlToJson(xmlFile, clazz, false);
    }

    /**
     * Convert XML file to JSON string with pretty-printing option
     */
    public static String xmlToJson(File xmlFile, Class<?> clazz, boolean prettyPrint) {
        try {
            LOG.trace("Converting XML file to JSON: {}", xmlFile.getName());
            // Step 1: XML file to Object using JaxbUtils
            Object obj = JaxbUtils.unmarshal(clazz, xmlFile);
            // Step 2: Object to JSON using JsonUtils
            return marshal(obj, prettyPrint);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("XML file to JSON conversion for " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert XML file to JSON file
     */
    public static void xmlToJson(File xmlFile, File jsonFile, Class<?> clazz) {
        xmlToJson(xmlFile, jsonFile, clazz, false);
    }

    /**
     * Convert XML file to JSON file with pretty-printing option
     */
    public static void xmlToJson(File xmlFile, File jsonFile, Class<?> clazz, boolean prettyPrint) {
        try {
            LOG.trace("Converting XML file {} to JSON file {}", xmlFile.getName(), jsonFile.getName());
            // Step 1: XML file to Object using JaxbUtils
            Object obj = JaxbUtils.unmarshal(clazz, xmlFile);
            // Step 2: Object to JSON file using JsonUtils
            marshal(obj, jsonFile, prettyPrint);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("XML file to JSON file conversion for " + clazz.getSimpleName(), e);
        }
    }

    // JSON to XML Conversion Methods

    /**
     * Convert JSON string to XML string using JsonUtils unmarshalling
     */
    public static String jsonToXml(String json, Class<?> clazz) {
        try {
            LOG.trace("Converting JSON to XML for class: {}", clazz.getSimpleName());
            // Step 1: JSON to Object using JsonUtils
            Object obj = unmarshal(clazz, json);
            // Step 2: Object to XML using JaxbUtils
            return JaxbUtils.marshal(obj);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("JSON to XML conversion for " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert JSON file to XML string
     */
    public static String jsonToXml(File jsonFile, Class<?> clazz) {
        try {
            LOG.trace("Converting JSON file to XML: {}", jsonFile.getName());
            // Step 1: JSON file to Object using JsonUtils
            Object obj = unmarshal(clazz, jsonFile);
            // Step 2: Object to XML using JaxbUtils
            return JaxbUtils.marshal(obj);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("JSON file to XML conversion for " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert JSON file to XML file
     */
    public static void jsonToXml(File jsonFile, File xmlFile, Class<?> clazz) {
        try {
            LOG.trace("Converting JSON file {} to XML file {}", jsonFile.getName(), xmlFile.getName());
            // Step 1: JSON file to Object using JsonUtils
            Object obj = unmarshal(clazz, jsonFile);
            // Step 2: Object to XML file using JaxbUtils
            JaxbUtils.marshal(obj, xmlFile);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("JSON file to XML file conversion for " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert JSON string to XML file
     */
    public static void jsonToXml(String json, File xmlFile, Class<?> clazz) {
        try {
            LOG.trace("Converting JSON string to XML file: {}", xmlFile.getName());
            // Step 1: JSON string to Object using JsonUtils
            Object obj = unmarshal(clazz, json);
            // Step 2: Object to XML file using JaxbUtils
            JaxbUtils.marshal(obj, xmlFile);
        } catch (Exception e) {
            throw EXCEPTION_TRANSLATOR.translate("JSON string to XML file conversion for " + clazz.getSimpleName(), e);
        }
    }

    // Core Marshaling Methods (Existing)

    public static String marshal(final Object obj) {
        final StringWriter writer = new StringWriter();
        marshal(obj, writer);
        return writer.toString();
    }

    public static String marshal(final Object obj, final boolean prettyPrint) {
        final StringWriter writer = new StringWriter();
        marshal(obj, writer, prettyPrint);
        return writer.toString();
    }

    public static void marshal(final Object obj, final File file) throws IOException {
        marshal(obj, file, false);
    }

    public static void marshal(final Object obj, final File file, final boolean prettyPrint) throws IOException {
        String jsonString = marshal(obj, prettyPrint);
        if (jsonString != null) {
            try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fileWriter.write(jsonString);
                fileWriter.flush();
            }
        }
    }

    public static void marshal(final Object obj, final Writer writer) {
        marshal(obj, writer, false);
    }

    public static void marshal(final Object obj, final Writer writer, final boolean prettyPrint) {
        final ObjectMapper mapper = getMapperFor(obj, prettyPrint);
        try {
            mapper.writeValue(writer, obj);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("marshalling " + obj.getClass().getSimpleName(), e);
        }
    }

    // Core Unmarshaling Methods (Existing)

    public static <T> T unmarshal(final Class<T> clazz, final String json) throws RuntimeException {
        final ObjectMapper mapper = getMapperFor(clazz, false);
        try {
            return mapper.readValue(json, clazz);
        } catch (final JsonProcessingException e) {
            throw EXCEPTION_TRANSLATOR.translate("unmarshalling " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final File file) {
        try (FileReader reader = new FileReader(file)) {
            return unmarshal(clazz, reader);
        } catch (final FileNotFoundException e) {
            throw EXCEPTION_TRANSLATOR.translate("reading " + file, e);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("closing file " + file, e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final Reader reader) throws RuntimeException {
        final ObjectMapper mapper = getMapperFor(clazz, false);
        try {
            return mapper.readValue(reader, clazz);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("unmarshalling " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputStream stream) {
        try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return unmarshal(clazz, reader);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("reading stream", e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return unmarshal(clazz, inputStream);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("getting a configuration resource from spring", e);
        }
    }

    // Utility Methods (Existing)

    public static <T> T duplicateObject(T obj, final Class<T> clazz) {
        return unmarshal(clazz, marshal(obj));
    }

    private static ObjectMapper getMapperFor(final Object obj, final boolean prettyPrint) {
        final Class<?> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();

        Map<Class<?>, ObjectMapper> mappers = m_mappers.get();
        if (mappers == null) {
            mappers = Collections.synchronizedMap(new WeakHashMap<Class<?>, ObjectMapper>());
            m_mappers.set(mappers);
        }

        String cacheKey = clazz.getName() + "_" + prettyPrint;

        if (mappers.containsKey(clazz)) {
            LOG.trace("found mapper for {}", cacheKey);
            return mappers.get(clazz);
        }

        LOG.trace("creating mapper for {}", cacheKey);
        final ObjectMapper mapper = createObjectMapper(prettyPrint);
        mappers.put(clazz, mapper);

        return mapper;
    }

    private static ObjectMapper createObjectMapper(final boolean prettyPrint) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(
                JsonInclude.Value.construct(JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.NON_DEFAULT));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configOverride(int.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, null));
        mapper.configOverride(Integer.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, null));

        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }


        return mapper;
    }

    public static ObjectMapper getObjectMapper(final Class<?> clazz) {
        return getMapperFor(clazz, false);
    }

    public static ObjectMapper getObjectMapper(final Class<?> clazz, final boolean prettyPrint) {
        return getMapperFor(clazz, prettyPrint);
    }
}