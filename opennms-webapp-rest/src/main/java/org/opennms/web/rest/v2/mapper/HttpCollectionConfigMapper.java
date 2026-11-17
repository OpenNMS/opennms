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
package org.opennms.web.rest.v2.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.httpdatacollection.Attrib;
import org.opennms.netmgt.config.httpdatacollection.Header;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.httpdatacollection.Parameter;
import org.opennms.netmgt.config.httpdatacollection.Rrd;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.config.httpdatacollection.Url;
import org.opennms.web.rest.v2.model.HttpAttribDto;
import org.opennms.web.rest.v2.model.HttpCollectionConfigDto;
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpCollectionSummaryDto;
import org.opennms.web.rest.v2.model.HttpHeaderDto;
import org.opennms.web.rest.v2.model.HttpRrdDto;
import org.opennms.web.rest.v2.model.HttpUriDto;
import org.opennms.web.rest.v2.model.HttpUrlDto;
import org.opennms.web.rest.v2.model.ParameterDto;

/**
 * Converts between the JAXB HTTP collection config model and the REST DTOs.
 *
 * <p>URL attributes with a schema default are read through the model's getters, so a DTO always shows the value
 * the collector uses. On the way back a null DTO field, or one holding the schema default, leaves the model field unset,
 * so a save does not write the defaults the DTO shows into the stored URL.
 * Expects a DTO that passed {@code HttpCollectionConfigValidator}; the model setters reject empty required values.</p>
 */
public abstract class HttpCollectionConfigMapper {

    // The schema defaults, as the getters of Url return them for an unset field.
    static final String DEFAULT_METHOD = "GET";
    static final String DEFAULT_HTTP_VERSION = "1.1";
    static final String DEFAULT_SCHEME = "http";
    static final String DEFAULT_HOST = "${ipaddr}";
    static final Integer DEFAULT_PORT = 80;
    static final String DEFAULT_MATCHES = "(.*)";
    static final String DEFAULT_RESPONSE_RANGE = "100-399";

    private HttpCollectionConfigMapper() {
    }

    // ------------------------------------------------------------------ to DTO

    public static HttpCollectionConfigDto toDto(final HttpDatacollectionConfig config) {
        final HttpCollectionConfigDto dto = new HttpCollectionConfigDto();
        dto.setRrdRepository(config.getRrdRepository());
        dto.setCollections(map(config.getHttpCollection(), HttpCollectionConfigMapper::toDto));
        return dto;
    }

    public static HttpCollectionSummaryDto toSummaryDto(final HttpCollection collection) {
        final HttpCollectionSummaryDto dto = new HttpCollectionSummaryDto();
        dto.setName(collection.getName());
        dto.setRrdStep(collection.getRrd() == null ? null : collection.getRrd().getStep());
        dto.setUriCount(collection.getUris().size());
        dto.setAttributeCount(collection.getUris().stream().mapToInt(uri -> uri.getAttributes().size()).sum());
        return dto;
    }

    public static HttpCollectionDto toDto(final HttpCollection collection) {
        final HttpCollectionDto dto = new HttpCollectionDto();
        dto.setName(collection.getName());
        dto.setRrd(collection.getRrd() == null ? null : toDto(collection.getRrd()));
        dto.setUris(map(collection.getUris(), HttpCollectionConfigMapper::toDto));
        return dto;
    }

    public static HttpRrdDto toDto(final Rrd rrd) {
        final HttpRrdDto dto = new HttpRrdDto();
        dto.setStep(rrd.getStep());
        dto.setRras(new ArrayList<>(rrd.getRra()));
        return dto;
    }

    public static HttpUriDto toDto(final Uri uri) {
        final HttpUriDto dto = new HttpUriDto();
        dto.setName(uri.getName());
        dto.setUrl(uri.getUrl() == null ? null : toDto(uri.getUrl()));
        dto.setAttributes(map(uri.getAttributes(), HttpCollectionConfigMapper::toDto));
        return dto;
    }

    public static HttpUrlDto toDto(final Url url) {
        final HttpUrlDto dto = new HttpUrlDto();
        dto.setMethod(url.getMethod());
        dto.setHttpVersion(url.getHttpVersion());
        dto.setUserAgent(url.getUserAgent().orElse(null));
        dto.setVirtualHost(url.getVirtualHost().orElse(null));
        dto.setScheme(url.getScheme());
        dto.setUserInfo(url.getUserInfo().orElse(null));
        dto.setHost(url.getHost());
        dto.setPort(url.getPort());
        dto.setPath(url.getPath());
        dto.setQuery(url.getQuery().orElse(null));
        dto.setFragment(url.getFragment().orElse(null));
        dto.setMatches(url.getMatches());
        dto.setResponseRange(url.getResponseRange());
        dto.setCanonicalEquivalence(url.isCanonicalEquivalence());
        dto.setCaseInsensitive(url.isCaseInsensitive());
        dto.setComments(url.isComments());
        dto.setDotall(url.isDotall());
        dto.setLiteral(url.isLiteral());
        dto.setMultiline(url.isMultiline());
        dto.setUnicodeCase(url.isUnicodeCase());
        dto.setUnixLines(url.isUnixLines());
        dto.setParameters(map(url.getParameters(), parameter -> {
            final ParameterDto p = new ParameterDto();
            p.setKey(parameter.getKey());
            p.setValue(parameter.getValue());
            return p;
        }));
        dto.setHeaders(map(url.getHeaders(), header -> {
            final HttpHeaderDto h = new HttpHeaderDto();
            h.setName(header.getName());
            h.setValue(header.getValue());
            return h;
        }));
        return dto;
    }

    public static HttpAttribDto toDto(final Attrib attrib) {
        final HttpAttribDto dto = new HttpAttribDto();
        dto.setAlias(attrib.getAlias());
        dto.setMatchGroup(attrib.getMatchGroup());
        dto.setType(attrib.getType() == null ? null : attrib.getType().getName());
        return dto;
    }

    // --------------------------------------------------------------- to entity

    public static HttpDatacollectionConfig toEntity(final HttpCollectionConfigDto dto) {
        final HttpDatacollectionConfig config = new HttpDatacollectionConfig();
        config.setRrdRepository(dto.getRrdRepository().trim());
        config.getHttpCollection().addAll(map(dto.getCollections(), HttpCollectionConfigMapper::toEntity));
        return config;
    }

    public static HttpCollection toEntity(final HttpCollectionDto dto) {
        final HttpCollection collection = new HttpCollection();
        collection.setName(dto.getName().trim());
        collection.setRrd(toEntity(dto.getRrd()));
        collection.setUris(map(dto.getUris(), HttpCollectionConfigMapper::toEntity));
        return collection;
    }

    public static Rrd toEntity(final HttpRrdDto dto) {
        final Rrd rrd = new Rrd();
        rrd.setStep(dto.getStep());
        dto.getRras().forEach(rra -> rrd.getRra().add(rra.trim()));
        return rrd;
    }

    public static Uri toEntity(final HttpUriDto dto) {
        final Uri uri = new Uri();
        uri.setName(dto.getName().trim());
        uri.setUrl(toEntity(dto.getUrl()));
        final List<Attrib> attributes = map(dto.getAttributes(), HttpCollectionConfigMapper::toEntity);
        // An empty <attributes/> wrapper is invalid against the schema (attrib is minOccurs 1).
        uri.setAttributes(attributes.isEmpty() ? null : attributes);
        return uri;
    }

    public static Url toEntity(final HttpUrlDto dto) {
        final Url url = new Url();
        url.setMethod(unlessDefault(dto.getMethod(), DEFAULT_METHOD));
        url.setHttpVersion(unlessDefault(dto.getHttpVersion(), DEFAULT_HTTP_VERSION));
        url.setUserAgent(dto.getUserAgent());
        url.setVirtualHost(dto.getVirtualHost());
        url.setScheme(unlessDefault(dto.getScheme(), DEFAULT_SCHEME));
        url.setUserInfo(dto.getUserInfo());
        url.setHost(unlessDefault(dto.getHost(), DEFAULT_HOST));
        url.setPort(unlessDefault(dto.getPort(), DEFAULT_PORT));
        url.setPath(dto.getPath().trim());
        url.setQuery(dto.getQuery());
        url.setFragment(dto.getFragment());
        url.setMatches(unlessDefault(dto.getMatches(), DEFAULT_MATCHES));
        url.setResponseRange(unlessDefault(dto.getResponseRange(), DEFAULT_RESPONSE_RANGE));
        url.setCanonicalEquivalence(unlessDefault(dto.getCanonicalEquivalence(), Boolean.FALSE));
        url.setCaseInsensitive(unlessDefault(dto.getCaseInsensitive(), Boolean.FALSE));
        url.setComments(unlessDefault(dto.getComments(), Boolean.FALSE));
        url.setDotall(unlessDefault(dto.getDotall(), Boolean.FALSE));
        url.setLiteral(unlessDefault(dto.getLiteral(), Boolean.FALSE));
        url.setMultiline(unlessDefault(dto.getMultiline(), Boolean.FALSE));
        url.setUnicodeCase(unlessDefault(dto.getUnicodeCase(), Boolean.FALSE));
        url.setUnixLines(unlessDefault(dto.getUnixLines(), Boolean.FALSE));

        final List<Parameter> parameters = map(dto.getParameters(), p -> {
            final Parameter parameter = new Parameter();
            parameter.setKey(p.getKey().trim());
            parameter.setValue(p.getValue());
            return parameter;
        });
        url.setParameters(parameters.isEmpty() ? null : parameters);

        final List<Header> headers = map(dto.getHeaders(), h -> new Header(h.getName().trim(), h.getValue()));
        url.setHeaders(headers.isEmpty() ? null : headers);
        return url;
    }

    public static Attrib toEntity(final HttpAttribDto dto) {
        final Attrib attrib = new Attrib();
        attrib.setAlias(dto.getAlias().trim());
        attrib.setMatchGroup(dto.getMatchGroup());
        attrib.setType(AttributeType.parse(dto.getType().trim()));
        return attrib;
    }

    // ------------------------------------------------------------------ helpers

    private static <T> T unlessDefault(final T value, final T defaultValue) {
        return defaultValue.equals(value) ? null : value;
    }

    private static <S, T> List<T> map(final Collection<S> source, final Function<S, T> mapper) {
        if (source == null) {
            return new ArrayList<>();
        }
        return source.stream().map(mapper).collect(Collectors.toCollection(ArrayList::new));
    }
}
