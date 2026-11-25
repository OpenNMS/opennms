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

package org.opennms.web.rest.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.web.rest.v2.mapper.XmlCollectionConfigMapper;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlGroupDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlMappingDto;
import org.opennms.web.rest.v2.model.XmlObjectDto;
import org.opennms.web.rest.v2.model.XmlRequestDto;
import org.opennms.web.rest.v2.model.XmlRrdDto;
import org.opennms.web.rest.v2.model.XmlSourceDto;

/**
 * Checks XML collector configuration DTOs before they are stored.
 *
 * <p>Covers what {@code xml-datacollection-config.xsd} requires and what the collector needs to work at all. The
 * configuration manager validates the stored JSON against the schema as well, but its messages name JSON paths
 * rather than the collection, source and group the user edited. The UI mirrors these rules in
 * {@code ui/src/lib/xmlCollectionValidator.ts}; keep the two in step.</p>
 *
 * <p>Every method returns a message describing the first problem found, or null when the DTO is valid.</p>
 */
final class XmlCollectionConfigValidator {

    /** Group file ids become configuration ids, and used to be file names. */
    static final Pattern GROUP_FILE_ID = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]*$");

    /** The form RrdUtils accepts: {@code RRA:<cf>:<xff>:<steps>:<rows>}. */
    static final Pattern RRA = Pattern.compile("^RRA:(AVERAGE|MIN|MAX|LAST):(0(\\.\\d+)?|1(\\.0+)?|\\.\\d+):[1-9]\\d*:[1-9]\\d*$");

    /** An HTTP method token. */
    private static final Pattern METHOD = Pattern.compile("^[A-Za-z]+$");

    private XmlCollectionConfigValidator() {
    }

    static String validateRrdRepository(final String rrdRepository) {
        return isBlank(rrdRepository) ? "The RRD repository is required." : null;
    }

    /**
     * @param groupFileIds ids of the group files that exist, {@code import-groups} entries must name one of them
     */
    static String validateCollection(final XmlCollectionDto collection, final Set<String> groupFileIds) {
        if (collection == null) {
            return "The XML data collection is required.";
        }
        if (isBlank(collection.getName())) {
            return "The collection name is required.";
        }
        final String name = collection.getName().trim();

        final String rrdMessage = validateRrd(collection.getRrd());
        if (rrdMessage != null) {
            return "Collection '" + name + "': " + rrdMessage;
        }

        final List<XmlSourceDto> sources = collection.getSources();
        if (sources != null) {
            final Set<String> urls = new HashSet<>();
            for (int i = 0; i < sources.size(); i++) {
                final XmlSourceDto source = sources.get(i);
                final String sourceMessage = validateSource(source, groupFileIds);
                if (sourceMessage != null) {
                    return "Collection '" + name + "', source " + (i + 1) + ": " + sourceMessage;
                }
                if (!urls.add(source.getUrl().trim())) {
                    return "Collection '" + name + "': source URL '" + source.getUrl().trim() + "' is used more than once.";
                }
            }
        }
        return null;
    }

    static String validateGroupFile(final XmlGroupFileDto groupFile) {
        if (groupFile == null) {
            return "The group file is required.";
        }
        final String idMessage = validateGroupFileId(groupFile.getId());
        if (idMessage != null) {
            return idMessage;
        }
        final String groupsMessage = validateGroups(groupFile.getGroups());
        return groupsMessage == null ? null : "Group file '" + groupFile.getId().trim() + "': " + groupsMessage;
    }

    static String validateGroupFileId(final String id) {
        if (isBlank(id)) {
            return "The group file id is required.";
        }
        if (!GROUP_FILE_ID.matcher(id.trim()).matches()) {
            return "Group file id '" + id.trim() + "' may only contain letters, digits, '.', '_' and '-', and must start with a letter or digit.";
        }
        return null;
    }

    private static String validateRrd(final XmlRrdDto rrd) {
        if (rrd == null) {
            return "the RRD settings are required.";
        }
        if (rrd.getStep() == null || rrd.getStep() <= 0) {
            return "the RRD step must be a positive number of seconds.";
        }
        if (rrd.getRras() == null || rrd.getRras().stream().allMatch(XmlCollectionConfigValidator::isBlank)) {
            return "at least one RRA is required.";
        }
        for (final String rra : rrd.getRras()) {
            if (isBlank(rra)) {
                continue;
            }
            if (!RRA.matcher(rra.trim()).matches()) {
                return "RRA '" + rra.trim() + "' is invalid, expected RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>, e.g. RRA:AVERAGE:0.5:1:2016.";
            }
        }
        return null;
    }

    private static String validateSource(final XmlSourceDto source, final Set<String> groupFileIds) {
        if (source == null) {
            return "the source is empty.";
        }
        if (isBlank(source.getUrl())) {
            return "the URL is required.";
        }

        final String requestMessage = validateRequest(source.getRequest());
        if (requestMessage != null) {
            return requestMessage;
        }

        final boolean hasImports = source.getImportGroups() != null
                && source.getImportGroups().stream().anyMatch(id -> !isBlank(id));
        final boolean hasGroups = source.getGroups() != null && !source.getGroups().isEmpty();
        if (!hasImports && !hasGroups) {
            return "define at least one group or import a group file.";
        }

        if (source.getImportGroups() != null) {
            for (final String importGroup : source.getImportGroups()) {
                if (isBlank(importGroup)) {
                    continue;
                }
                if (groupFileIds != null && !groupFileIds.contains(importGroup.trim())) {
                    return "imported group file '" + importGroup.trim() + "' does not exist.";
                }
            }
        }
        return validateGroups(source.getGroups());
    }

    private static String validateRequest(final XmlRequestDto request) {
        if (request == null) {
            return null;
        }
        if (!isBlank(request.getMethod()) && !METHOD.matcher(request.getMethod().trim()).matches()) {
            return "request method '" + request.getMethod().trim() + "' is invalid.";
        }
        final String parameterMessage = validateKeyValues(request.getParameters(), "request parameter");
        if (parameterMessage != null) {
            return parameterMessage;
        }
        final String headerMessage = validateKeyValues(request.getHeaders(), "request header");
        if (headerMessage != null) {
            return headerMessage;
        }
        if (!isBlank(request.getContent()) && isBlank(request.getContentType())) {
            return "the request content needs a content type.";
        }
        return null;
    }

    private static String validateKeyValues(final List<ParameterDto> entries, final String what) {
        if (entries == null) {
            return null;
        }
        final Set<String> keys = new HashSet<>();
        for (final ParameterDto entry : entries) {
            if (entry == null || isBlank(entry.getKey())) {
                return "every " + what + " needs a name.";
            }
            if (entry.getValue() == null) {
                return what + " '" + entry.getKey().trim() + "' needs a value.";
            }
            if (!keys.add(entry.getKey().trim())) {
                return what + " '" + entry.getKey().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    private static String validateGroups(final List<XmlGroupDto> groups) {
        if (groups == null) {
            return null;
        }
        final Set<String> names = new HashSet<>();
        for (final XmlGroupDto group : groups) {
            final String message = validateGroup(group);
            if (message != null) {
                return message;
            }
            if (!names.add(group.getName().trim())) {
                return "group '" + group.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    static String validateGroup(final XmlGroupDto group) {
        if (group == null || isBlank(group.getName())) {
            return "every group needs a name.";
        }
        final String prefix = "group '" + group.getName().trim() + "': ";
        if (isBlank(group.getResourceType())) {
            return prefix + "the resource type is required.";
        }
        if (isBlank(group.getResourceXpath())) {
            return prefix + "the resource XPath is required.";
        }
        if (group.getObjects() == null || group.getObjects().isEmpty()) {
            return prefix + "at least one object is required.";
        }
        final Set<String> names = new HashSet<>();
        for (final XmlObjectDto object : group.getObjects()) {
            final String message = validateObject(object);
            if (message != null) {
                return prefix + message;
            }
            if (!names.add(object.getName().trim())) {
                return prefix + "object '" + object.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    private static String validateObject(final XmlObjectDto object) {
        if (object == null || isBlank(object.getName())) {
            return "every object needs a name.";
        }
        final String prefix = "object '" + object.getName().trim() + "': ";
        if (isBlank(object.getType())) {
            return prefix + "the type is required.";
        }
        if (XmlCollectionConfigMapper.toAttributeType(object.getType()) == null) {
            return prefix + "type '" + object.getType().trim() + "' is invalid, expected gauge, counter or string.";
        }
        if (isBlank(object.getXpath())) {
            return prefix + "the XPath is required.";
        }
        if (object.getMappings() != null) {
            final Set<String> froms = new HashSet<>();
            for (final XmlMappingDto mapping : object.getMappings()) {
                if (mapping == null || mapping.getTo() == null || mapping.getTo().isBlank()) {
                    return prefix + "every value mapping needs a value to map to.";
                }
                final String from = mapping.getFrom() == null ? "" : mapping.getFrom().trim();
                if (!froms.add(from)) {
                    return prefix + (from.isEmpty()
                            ? "only one value mapping may leave 'from' empty."
                            : "value '" + from + "' is mapped more than once.");
                }
            }
        }
        return null;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
