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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.web.rest.v2.XmlCollectionConfigRestServiceTest.collection;
import static org.opennms.web.rest.v2.XmlCollectionConfigRestServiceTest.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlGroupDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlMappingDto;
import org.opennms.web.rest.v2.model.XmlRequestDto;

public class XmlCollectionConfigValidatorTest {

    private static final Set<String> GROUP_FILES = Set.of("elasticsearch-cluster-stats");

    @Test
    public void acceptsAValidCollection() {
        assertNull(XmlCollectionConfigValidator.validateCollection(collection("c"), GROUP_FILES));
    }

    @Test
    public void acceptsACollectionWithoutSources() {
        final XmlCollectionDto dto = collection("c");
        dto.setSources(List.of());
        assertNull(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES));
    }

    @Test
    public void requiresAName() {
        assertEquals("The collection name is required.", XmlCollectionConfigValidator.validateCollection(collection(" "), GROUP_FILES));
    }

    @Test
    public void checksTheRrd() {
        final XmlCollectionDto dto = collection("c");
        dto.getRrd().setStep(0);
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("step"));

        dto.getRrd().setStep(300);
        dto.getRrd().setRras(List.of());
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("at least one RRA"));

        for (final String rra : List.of("RRA:AVERAGE:0.5:1:2016", "RRA:MAX:0:288:366", "RRA:LAST:1:1:1", "RRA:MIN:.5:12:1488")) {
            dto.getRrd().setRras(List.of(rra));
            assertNull(rra, XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES));
        }
        for (final String rra : List.of("RRA:SUM:0.5:1:2016", "RRA:AVERAGE:1.5:1:2016", "RRA:AVERAGE:0.5:0:2016", "AVERAGE:0.5:1:2016")) {
            dto.getRrd().setRras(List.of(rra));
            assertTrue(rra, XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("is invalid"));
        }
    }

    @Test
    public void requiresGroupsOrImportsInASource() {
        final XmlCollectionDto dto = collection("c");
        dto.getSources().get(0).setGroups(List.of());
        dto.getSources().get(0).setImportGroups(List.of());
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("at least one group"));
    }

    @Test
    public void refusesDuplicateSourceUrls() {
        final XmlCollectionDto dto = collection("c");
        dto.setSources(List.of(dto.getSources().get(0), dto.getSources().get(0)));
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("more than once"));
    }

    @Test
    public void checksTheRequest() {
        final XmlCollectionDto dto = collection("c");
        final XmlRequestDto request = dto.getSources().get(0).getRequest();
        request.setContentType(null);
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("content type"));

        request.setContentType("text/plain");
        request.setMethod("GE T");
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("method"));

        request.setMethod("GET");
        final ParameterDto unnamed = new ParameterDto();
        unnamed.setValue("x");
        request.setHeaders(List.of(unnamed));
        assertTrue(XmlCollectionConfigValidator.validateCollection(dto, GROUP_FILES).contains("needs a name"));
    }

    @Test
    public void checksGroupsAndObjects() {
        final XmlGroupDto group = group("g");
        assertNull(XmlCollectionConfigValidator.validateGroup(group));

        group.setResourceXpath("");
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("resource XPath"));
        group.setResourceXpath("/");

        group.getObjects().get(0).setType("float");
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("type 'float' is invalid"));
        group.getObjects().get(0).setType("counter32");
        assertNull("aliases are accepted", XmlCollectionConfigValidator.validateGroup(group));

        group.setObjects(List.of(group.getObjects().get(0), group.getObjects().get(0)));
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("defined more than once"));

        group.setObjects(List.of());
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("at least one object"));
    }

    @Test
    public void allowsOneDefaultMappingOnly() {
        final XmlGroupDto group = group("g");
        final List<XmlMappingDto> mappings = new ArrayList<>();
        mappings.add(mapping(null, "0"));
        mappings.add(mapping("green", "1"));
        group.getObjects().get(0).setMappings(mappings);
        assertNull(XmlCollectionConfigValidator.validateGroup(group));

        mappings.add(mapping(" ", "2"));
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("only one value mapping"));

        mappings.set(2, mapping("green", "2"));
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("'green' is mapped more than once"));

        mappings.set(2, mapping("red", ""));
        assertTrue(XmlCollectionConfigValidator.validateGroup(group).contains("needs a value"));
    }

    @Test
    public void checksGroupFileIds() {
        for (final String id : List.of("elasticsearch-cluster-stats", "3gpp.full", "a_b")) {
            assertNull(id, XmlCollectionConfigValidator.validateGroupFileId(id));
        }
        for (final String id : List.of("", "../x", "a/b", ".hidden", "with space")) {
            assertTrue(id, XmlCollectionConfigValidator.validateGroupFileId(id) != null);
        }
    }

    @Test
    public void refusesDuplicateGroupsInAGroupFile() {
        final XmlGroupFileDto dto = new XmlGroupFileDto();
        dto.setId("f");
        dto.setGroups(List.of(group("g"), group("g")));
        assertTrue(XmlCollectionConfigValidator.validateGroupFile(dto).contains("group 'g' is defined more than once"));
    }

    private static XmlMappingDto mapping(final String from, final String to) {
        final XmlMappingDto mapping = new XmlMappingDto();
        mapping.setFrom(from);
        mapping.setTo(to);
        return mapping;
    }
}
