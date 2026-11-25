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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.protocols.xml.config.Content;
import org.opennms.protocols.xml.config.Header;
import org.opennms.protocols.xml.config.Parameter;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlMapping;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlResourceKey;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.cm.XmlDataCollectionConfigDaoCm;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlCollectionSummaryDto;
import org.opennms.web.rest.v2.model.XmlGroupDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlGroupFileSummaryDto;
import org.opennms.web.rest.v2.model.XmlMappingDto;
import org.opennms.web.rest.v2.model.XmlObjectDto;
import org.opennms.web.rest.v2.model.XmlRequestDto;
import org.opennms.web.rest.v2.model.XmlRrdDto;
import org.opennms.web.rest.v2.model.XmlSourceDto;

/**
 * Maps the XML collector configuration model of {@code org.opennms.protocols.xml.config} to the DTOs of the REST
 * API and back.
 *
 * <p>Two normalizations happen on the way out: {@code import-groups} entries are reported as group file ids, also
 * when they are stored as the former file path ({@code xml-datacollection/foo.xml}), and lists are never null.
 * On the way in, strings are trimmed and blank optional values are dropped.</p>
 */
public final class XmlCollectionConfigMapper {

    private XmlCollectionConfigMapper() {
    }

    // ------------------------------------------------------------- to DTO

    public static XmlCollectionSummaryDto toSummaryDto(final XmlDataCollection collection) {
        final XmlCollectionSummaryDto dto = new XmlCollectionSummaryDto();
        dto.setName(collection.getName());
        dto.setStep(collection.getXmlRrd() == null ? null : collection.getXmlRrd().getStep());
        dto.setSourceCount(collection.getXmlSources().size());
        dto.setGroupCount(collection.getXmlSources().stream().mapToInt(source -> source.getXmlGroups().size()).sum());
        dto.setImportGroups(collection.getXmlSources().stream()
                .flatMap(source -> importGroupIds(source).stream())
                .distinct()
                .collect(Collectors.toList()));
        return dto;
    }

    public static XmlCollectionDto toDto(final XmlDataCollection collection) {
        final XmlCollectionDto dto = new XmlCollectionDto();
        dto.setName(collection.getName());
        dto.setRrd(toDto(collection.getXmlRrd()));
        dto.setSources(map(collection.getXmlSources(), XmlCollectionConfigMapper::toDto));
        return dto;
    }

    public static XmlGroupFileDto toGroupFileDto(final String id, final XmlGroups groups) {
        final XmlGroupFileDto dto = new XmlGroupFileDto();
        dto.setId(id);
        dto.setGroups(map(groups.getXmlGroups(), XmlCollectionConfigMapper::toDto));
        return dto;
    }

    public static XmlGroupFileSummaryDto toGroupFileSummaryDto(final String id, final XmlGroups groups, final List<String> usedBy) {
        final XmlGroupFileSummaryDto dto = new XmlGroupFileSummaryDto();
        dto.setId(id);
        dto.setGroupCount(groups.getXmlGroups().size());
        dto.setUsedBy(usedBy);
        return dto;
    }

    /**
     * @return the group file ids the {@code import-groups} entries of the source refer to, in order, without duplicates
     */
    public static List<String> importGroupIds(final XmlSource source) {
        return source.getImportGroupsList().stream()
                .filter(Objects::nonNull)
                .filter(entry -> !entry.isBlank())
                .map(XmlDataCollectionConfigDaoCm::toGroupsConfigId)
                .distinct()
                .collect(Collectors.toList());
    }

    private static XmlRrdDto toDto(final XmlRrd rrd) {
        if (rrd == null) {
            return null;
        }
        final XmlRrdDto dto = new XmlRrdDto();
        dto.setStep(rrd.getStep());
        dto.setRras(new ArrayList<>(rrd.getXmlRras()));
        return dto;
    }

    private static XmlSourceDto toDto(final XmlSource source) {
        final XmlSourceDto dto = new XmlSourceDto();
        dto.setUrl(source.getUrl());
        dto.setRequest(toDto(source.getRequest()));
        dto.setImportGroups(importGroupIds(source));
        dto.setGroups(map(source.getXmlGroups(), XmlCollectionConfigMapper::toDto));
        return dto;
    }

    private static XmlRequestDto toDto(final Request request) {
        if (request == null) {
            return null;
        }
        final XmlRequestDto dto = new XmlRequestDto();
        dto.setMethod(request.getMethod());
        dto.setParameters(map(request.getParameters(), p -> parameter(p.getName(), p.getValue())));
        dto.setHeaders(map(request.getHeaders(), h -> parameter(h.getName(), h.getValue())));
        if (request.getContent() != null) {
            dto.setContentType(request.getContent().getType());
            dto.setContent(request.getContent().getData());
        }
        return dto;
    }

    public static XmlGroupDto toDto(final XmlGroup group) {
        final XmlGroupDto dto = new XmlGroupDto();
        dto.setName(group.getName());
        dto.setResourceType(group.getResourceType());
        dto.setResourceXpath(group.getResourceXpath());
        dto.setKeyXpath(group.getKeyXpath());
        dto.setTimestampXpath(group.getTimestampXpath());
        dto.setTimestampFormat(group.getTimestampFormat());
        dto.setResourceKeys(group.getXmlResourceKey() == null
                ? new ArrayList<>()
                : new ArrayList<>(group.getXmlResourceKey().getKeyXpathList()));
        dto.setObjects(map(group.getXmlObjects(), XmlCollectionConfigMapper::toDto));
        return dto;
    }

    private static XmlObjectDto toDto(final XmlObject object) {
        final XmlObjectDto dto = new XmlObjectDto();
        dto.setName(object.getName());
        dto.setType(object.getDataType() == null ? null : object.getDataType().getName());
        dto.setXpath(object.getXpath());
        dto.setMappings(map(object.getXmlMappings(), mapping -> {
            final XmlMappingDto mappingDto = new XmlMappingDto();
            mappingDto.setFrom(mapping.getFrom());
            mappingDto.setTo(mapping.getTo());
            return mappingDto;
        }));
        return dto;
    }

    private static ParameterDto parameter(final String key, final String value) {
        final ParameterDto dto = new ParameterDto();
        dto.setKey(key);
        dto.setValue(value);
        return dto;
    }

    // ----------------------------------------------------------- to entity

    public static XmlDataCollection toEntity(final XmlCollectionDto dto) {
        final XmlDataCollection collection = new XmlDataCollection();
        collection.setName(trim(dto.getName()));
        collection.setXmlRrd(toEntity(dto.getRrd()));
        collection.setXmlSources(map(dto.getSources(), XmlCollectionConfigMapper::toEntity));
        return collection;
    }

    public static XmlGroups toEntity(final XmlGroupFileDto dto) {
        final XmlGroups groups = new XmlGroups();
        groups.setXmlGroups(map(dto.getGroups(), XmlCollectionConfigMapper::toEntity));
        return groups;
    }

    private static XmlRrd toEntity(final XmlRrdDto dto) {
        if (dto == null) {
            return null;
        }
        final XmlRrd rrd = new XmlRrd();
        rrd.setStep(dto.getStep());
        rrd.setXmlRras(trimmedNonBlank(dto.getRras()));
        return rrd;
    }

    private static XmlSource toEntity(final XmlSourceDto dto) {
        final XmlSource source = new XmlSource();
        source.setUrl(trim(dto.getUrl()));
        source.setRequest(toEntity(dto.getRequest()));
        source.setImportGroupsList(trimmedNonBlank(dto.getImportGroups()));
        source.setXmlGroups(map(dto.getGroups(), XmlCollectionConfigMapper::toEntity));
        return source;
    }

    private static Request toEntity(final XmlRequestDto dto) {
        if (dto == null) {
            return null;
        }
        final Request request = new Request();
        final String method = trimToNull(dto.getMethod());
        request.setMethod(method == null ? "GET" : method.toUpperCase());
        request.setParameters(map(dto.getParameters(), p -> new Parameter(trim(p.getKey()), p.getValue())));
        request.setHeaders(map(dto.getHeaders(), h -> new Header(trim(h.getKey()), h.getValue())));
        final String contentType = trimToNull(dto.getContentType());
        if (contentType != null || (dto.getContent() != null && !dto.getContent().isBlank())) {
            request.setContent(new Content(contentType, dto.getContent()));
        }
        return request;
    }

    public static XmlGroup toEntity(final XmlGroupDto dto) {
        final XmlGroup group = new XmlGroup();
        group.setName(trim(dto.getName()));
        group.setResourceType(trim(dto.getResourceType()));
        group.setResourceXpath(trim(dto.getResourceXpath()));
        group.setKeyXpath(trimToNull(dto.getKeyXpath()));
        group.setTimestampXpath(trimToNull(dto.getTimestampXpath()));
        group.setTimestampFormat(trimToNull(dto.getTimestampFormat()));
        final List<String> resourceKeys = trimmedNonBlank(dto.getResourceKeys());
        if (!resourceKeys.isEmpty()) {
            final XmlResourceKey resourceKey = new XmlResourceKey();
            resourceKey.setKeyXpathList(resourceKeys);
            group.setXmlResourceKey(resourceKey);
        }
        group.setXmlObjects(map(dto.getObjects(), XmlCollectionConfigMapper::toEntity));
        return group;
    }

    private static XmlObject toEntity(final XmlObjectDto dto) {
        final XmlObject object = new XmlObject();
        object.setName(trim(dto.getName()));
        object.setDataType(toAttributeType(dto.getType()));
        object.setXpath(trim(dto.getXpath()));
        object.setXmlMappings(map(dto.getMappings(), mappingDto -> {
            final XmlMapping mapping = new XmlMapping();
            mapping.setFrom(trimToNull(mappingDto.getFrom()));
            mapping.setTo(trim(mappingDto.getTo()));
            return mapping;
        }));
        return object;
    }

    /**
     * @return the attribute type of the given name, or null for null; invalid names are rejected by the validator
     * before this is reached
     */
    public static AttributeType toAttributeType(final String type) {
        final String trimmed = trimToNull(type);
        return trimmed == null ? null : AttributeType.parse(trimmed);
    }

    // ------------------------------------------------------------- helpers

    private static <S, T> List<T> map(final List<S> source, final Function<S, T> mapper) {
        if (source == null) {
            return new ArrayList<>();
        }
        return source.stream().filter(Objects::nonNull).map(mapper).collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<String> trimmedNonBlank(final List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .map(XmlCollectionConfigMapper::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
