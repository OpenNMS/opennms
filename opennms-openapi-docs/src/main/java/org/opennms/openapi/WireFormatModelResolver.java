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
package org.opennms.openapi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Resolves schemas the way the webapp serialises JSON.
 *
 * The ReST endpoints go through the Jackson 1 JacksonJaxbJsonProvider, so property names and
 * visibility follow the Jackson 1 ({@code org.codehaus}) and JAXB annotations: {@code meta-data}
 * rather than {@code metaData}, {@code XmlAccessorType} deciding which getters appear,
 * {@code XmlElementWrapper} naming the property, Jackson 1 {@code @JsonIgnore} hiding one.
 * Swagger's stock resolver reads only the Jackson 2 annotations, which the model classes do not
 * carry, and documents bean names.
 */
final class WireFormatModelResolver extends ModelResolver {

    private static final String REF_PREFIX = "#/components/schemas/";

    WireFormatModelResolver() {
        super(jaxbAwareMapper());
    }

    private static ObjectMapper jaxbAwareMapper() {
        final ObjectMapper mapper = Json.mapper().copy();
        // Jackson annotations before JAXB, as in the provider's default (JACKSON, JAXB) order.
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(
                AnnotationIntrospector.pair(
                        mapper.getSerializationConfig().getAnnotationIntrospector(),
                        new Jackson1AnnotationIntrospector()),
                new WrapperNamingJaxbIntrospector(mapper.getTypeFactory())));
        return mapper;
    }

    @Override
    public Schema resolve(final AnnotatedType type, final ModelConverterContext context,
                          final Iterator<ModelConverter> chain) {
        retypeThroughAdapter(type);
        final Schema<?> schema = super.resolve(type, context, chain);
        alignEnumDefaults(schema);
        // a named model is registered in the context and comes back as a $ref, so the properties
        // carrying the defaults are not on the schema returned here
        alignEnumDefaults(referencedModel(context, schema));
        return schema;
    }

    private static Schema<?> referencedModel(final ModelConverterContext context, final Schema<?> schema) {
        final String ref = schema == null ? null : schema.get$ref();
        if (ref == null || !ref.startsWith(REF_PREFIX)) {
            return null;
        }
        return context.getDefinedModels().get(ref.substring(REF_PREFIX.length()));
    }

    /**
     * An XmlJavaTypeAdapter decides what a property serialises to, so document the adapter's
     * value type rather than the declared one: OnmsSnmpInterface.node is an OnmsNode in Java but
     * an integer on the wire. Retypes in place, since swagger keys its caches off this instance.
     * Container properties are left alone, since JAXB adapts their elements and this does not
     * model that.
     */
    private void retypeThroughAdapter(final AnnotatedType type) {
        final XmlJavaTypeAdapter adapter = annotation(type.getCtxAnnotations(), XmlJavaTypeAdapter.class);
        if (adapter == null || type.getType() == null) {
            return;
        }
        final JavaType declared = _mapper.getTypeFactory().constructType(type.getType());
        if (declared.isContainerType()) {
            return;
        }
        final JavaType adapted = _mapper.getTypeFactory().constructType(adapter.value()).findSuperType(XmlAdapter.class);
        if (adapted == null) {
            return;
        }
        final JavaType value = adapted.containedTypeOrUnknown(0);
        if (!value.hasRawClass(Object.class) && !value.equals(declared)) {
            type.setType(value);
        }
    }

    /**
     * XmlElement.defaultValue holds the XML spelling, which for an enum is not what the JSON API
     * accepts: SyslogDestination.ip-protocol defaults to "udp" against values UDP and TCP. Match
     * the default to a documented value, and drop it when none corresponds.
     */
    private static void alignEnumDefaults(final Schema<?> schema) {
        if (schema == null || schema.getProperties() == null) {
            return;
        }
        for (final Object property : schema.getProperties().values()) {
            alignEnumDefault((Schema<?>) property);
        }
    }

    private static void alignEnumDefault(final Schema<?> property) {
        final List<?> values = property.getEnum();
        final Object configured = property.getDefault();
        if (values == null || values.isEmpty() || configured == null || values.contains(configured)) {
            return;
        }
        for (final Object candidate : values) {
            if (candidate != null && candidate.toString().equalsIgnoreCase(configured.toString())) {
                property.setDefault(candidate);
                return;
            }
        }
        property.setDefault(null);
    }

    private static <A extends Annotation> A annotation(final Annotation[] annotations, final Class<A> type) {
        if (annotations != null) {
            for (final Annotation candidate : annotations) {
                if (type.isInstance(candidate)) {
                    return type.cast(candidate);
                }
            }
        }
        return null;
    }

    /**
     * The stock implementation reaches findJsonValueAccessor reflectively and, if that throws,
     * falls back to BeanDescription.findJsonValueMethod, which Jackson 2.22 removed. Calling it
     * directly surfaces the real introspection error instead of a NoSuchMethodError.
     */
    @Override
    protected Type findJsonValueType(final BeanDescription beanDesc) {
        final AnnotatedMember accessor = beanDesc.findJsonValueAccessor();
        return accessor == null ? null : accessor.getType();
    }

    /**
     * The Jackson 1 annotations the provider honours at runtime, read through the Jackson 2
     * introspector API. Serialization-side only: the documents describe what the server emits.
     * Root names are left out on purpose, see {@link WrapperNamingJaxbIntrospector#findRootName}.
     */
    private static final class Jackson1AnnotationIntrospector extends NopAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        /** Back references are never written, and swagger does not consult findReferenceType. */
        @Override
        public boolean hasIgnoreMarker(final AnnotatedMember m) {
            final JsonIgnore ignore = m.getAnnotation(JsonIgnore.class);
            return (ignore != null && ignore.value()) || m.hasAnnotation(JsonBackReference.class);
        }

        @Override
        public JsonIgnoreProperties.Value findPropertyIgnoralByName(final MapperConfig<?> config, final Annotated a) {
            final org.codehaus.jackson.annotate.JsonIgnoreProperties ignored =
                    a.getAnnotation(org.codehaus.jackson.annotate.JsonIgnoreProperties.class);
            return ignored == null ? null : JsonIgnoreProperties.Value.forIgnoredProperties(ignored.value());
        }

        @Override
        public PropertyName findNameForSerialization(final Annotated a) {
            final JsonProperty property = a.getAnnotation(JsonProperty.class);
            if (property == null) {
                return null;
            }
            return property.value().isEmpty() ? PropertyName.USE_DEFAULT : PropertyName.construct(property.value());
        }

        @Override
        public Boolean hasAsValue(final Annotated a) {
            final JsonValue value = a.getAnnotation(JsonValue.class);
            return value == null ? null : value.value();
        }
    }

    /**
     * Jackson 1 took the XmlElementWrapper name as the property name outright. Jackson 2's
     * USE_WRAPPER_NAME_AS_PROPERTY_NAME renames after collection, so two wrapped lists sharing an
     * XmlElement name collide before the rename (AlarmStatistics has four "alarm" wrappers).
     */
    private static final class WrapperNamingJaxbIntrospector extends JaxbAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        WrapperNamingJaxbIntrospector(final TypeFactory typeFactory) {
            super(typeFactory);
        }

        @Override
        public PropertyName findNameForSerialization(final Annotated a) {
            final PropertyName wrapper = super.findWrapperName(a);
            return wrapper != null && wrapper.hasSimpleName() ? wrapper : super.findNameForSerialization(a);
        }

        @Override
        public PropertyName findNameForDeserialization(final Annotated a) {
            final PropertyName wrapper = super.findWrapperName(a);
            return wrapper != null && wrapper.hasSimpleName() ? wrapper : super.findNameForDeserialization(a);
        }

        @Override
        public PropertyName findWrapperName(final Annotated a) {
            return null;
        }

        /**
         * The runtime pair asks Jackson 1 first, and its introspector answers every enum with
         * Enum.name(), so XmlEnumValue never reaches the wire. Leave the Java names in place.
         */
        @Override
        public String[] findEnumValues(final Class<?> enumType, final Enum<?>[] enumValues, final String[] names) {
            return names;
        }

        @Override
        public String[] findEnumValues(final MapperConfig<?> config, final AnnotatedClass annotatedClass,
                                       final Enum<?>[] enumValues, final String[] names) {
            return names;
        }

        /**
         * Swagger names a schema after the root name when the introspector supplies one, and
         * XmlRootElement names repeat across classes ("node", "alarm"), so keep the class names.
         */
        @Override
        public PropertyName findRootName(final AnnotatedClass ac) {
            return null;
        }
    }
}
