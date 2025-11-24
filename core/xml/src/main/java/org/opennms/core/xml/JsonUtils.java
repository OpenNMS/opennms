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

public abstract class JsonUtils {

    private static final JsonExceptionTranslator EXCEPTION_TRANSLATOR = new JsonExceptionTranslator();

    // Shared ObjectMapper configured once. Safe for concurrent use after construction.
    private static final ObjectMapper MAPPER = createObjectMapper();

    private JsonUtils() {
    }

    private static class JsonExceptionTranslator {
        public RuntimeException translate(String operation, Exception e) {
            return new RuntimeException("JSON " + operation + " failed: " + e.getMessage(), e);
        }
    }

    // Core Marshaling Methods

    public static String marshal(final Object obj) {
        final StringWriter writer = new StringWriter();
        marshal(obj, writer);
        return writer.toString();
    }

    public static void marshal(final Object obj, final File file) throws IOException {
        String jsonString = marshal(obj);
        if (jsonString != null) {
            try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                fileWriter.write(jsonString);
                fileWriter.flush();
            }
        }
    }

    public static void marshal(final Object obj, final Writer writer) {
        try {
            getObjectMapper().writeValue(writer, obj);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("marshalling " + obj.getClass().getSimpleName(), e);
        }
    }

    // Core Unmarshaling Methods

    public static <T> T unmarshal(final Class<T> clazz, final String json) throws RuntimeException {
        try {
            return getObjectMapper().readValue(json, clazz);
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
        try {
            return getObjectMapper().readValue(reader, clazz);
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

    // Utility Methods

    public static <T> T duplicateObject(T obj, final Class<T> clazz) {
        return unmarshal(clazz, marshal(obj));
    }

    /**
     * Return the shared ObjectMapper instance. Callers may use writerWithDefaultPrettyPrinter()
     * on the returned mapper when they explicitly want pretty-printed output.
     */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Exclude nulls AND empty collections/arrays/strings
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        // More explicit: set default property inclusion to NON_EMPTY for both value and content
        mapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));

        // Ensure primitives behave same as before (non-default)
        mapper.configOverride(int.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, null));
        mapper.configOverride(Integer.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT, null));

        // Also explicitly tell mapper to avoid serializing empty java.util.List / Collection properties
        mapper.configOverride(java.util.List.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        mapper.configOverride(java.util.Collection.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));

        return mapper;
    }
}