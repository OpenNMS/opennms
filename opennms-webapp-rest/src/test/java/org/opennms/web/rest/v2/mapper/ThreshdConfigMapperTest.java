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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.web.rest.v2.model.IpRangeDto;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;

public class ThreshdConfigMapperTest {

    @Test
    public void roundTripsAWholeConfiguration() {
        final ThreshdConfigDto dto = configDto();

        final ThreshdConfigDto roundTripped = ThreshdConfigMapper.toDto(ThreshdConfigMapper.toEntity(dto));

        assertEquals(1, roundTripped.getPackages().size());

        final ThreshdPackageDto pkg = roundTripped.getPackages().get(0);
        assertEquals("example1", pkg.getName());
        assertEquals("IPADDR != '0.0.0.0'", pkg.getFilter());
        assertEquals(List.of("10.0.0.1"), pkg.getSpecifics());
        assertEquals(1, pkg.getIncludeRanges().size());
        assertEquals("1.1.1.1", pkg.getIncludeRanges().get(0).getBegin());
        assertEquals("254.254.254.254", pkg.getIncludeRanges().get(0).getEnd());
        assertEquals(List.of("maintenance"), pkg.getOutageCalendars());

        final ThreshdServiceDto service = pkg.getServices().get(0);
        assertEquals("SNMP", service.getName());
        assertEquals(Long.valueOf(300000L), service.getInterval());
        assertEquals("on", service.getStatus());
        assertEquals("thresholding-group", service.getParameters().get(0).getKey());
        assertEquals("mib2", service.getParameters().get(0).getValue());
    }

    @Test
    public void carriesNoThreadCountOrThresholderBindings() {
        // Both were dropped from the schema in v1.2: nothing ever read them, and leaving them in the DTO
        // would keep writing dead keys into the stored configuration.
        final String json = writeJson(ThreshdConfigMapper.toDto(ThreshdConfigMapper.toEntity(configDto())));

        assertFalse(json, json.contains("threads"));
        assertFalse(json, json.contains("thresholder"));
    }

    @Test
    public void mapsTheFilterToAndFromItsBareStringForm() {
        final ThreshdPackageDto dto = packageDto();

        final Package entity = ThreshdConfigMapper.toEntity(dto);

        assertEquals("IPADDR != '0.0.0.0'", entity.getFilter().getContent().orElse(null));
        assertEquals("IPADDR != '0.0.0.0'", ThreshdConfigMapper.toDto(entity).getFilter());
    }

    @Test
    public void leavesTheFilterUnsetWhenItIsBlank() {
        // Package.setFilter rejects null, so the mapper has to skip the call entirely and let validation
        // produce the 400 rather than letting the model throw an IllegalArgumentException.
        final ThreshdPackageDto dto = packageDto();
        dto.setFilter("   ");

        final Package entity = ThreshdConfigMapper.toEntity(dto);

        assertNull(entity.getFilter());
        assertNull(ThreshdConfigMapper.toDto(entity).getFilter());
    }

    @Test
    public void parsesTheServiceStatusCaseInsensitivelyAndRejectsAnythingElse() {
        assertEquals(ServiceStatus.ON, ThreshdConfigMapper.toServiceStatus("ON"));
        assertEquals(ServiceStatus.OFF, ThreshdConfigMapper.toServiceStatus(" off "));
        assertNull(ThreshdConfigMapper.toServiceStatus("paused"));
        assertNull(ThreshdConfigMapper.toServiceStatus(null));
    }

    @Test
    public void summarizesAPackageByCountingItsServices() {
        final ThreshdConfiguration config = ThreshdConfigMapper.toEntity(configDto());

        final var summary = ThreshdConfigMapper.toSummaryDto(config.getPackages().get(0));

        assertEquals("example1", summary.getName());
        assertEquals(Integer.valueOf(1), summary.getServiceCount());
        assertEquals(List.of("maintenance"), summary.getOutageCalendars());
    }

    @Test
    public void dropsBlankEntriesFromStringCollections() {
        final ThreshdPackageDto dto = packageDto();
        dto.setSpecifics(java.util.Arrays.asList("10.0.0.1", "   ", null));

        assertEquals(List.of("10.0.0.1"), ThreshdConfigMapper.toEntity(dto).getSpecifics());
    }

    private static ThreshdConfigDto configDto() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setPackages(List.of(packageDto()));
        return dto;
    }

    private static ThreshdPackageDto packageDto() {
        final ThreshdPackageDto dto = new ThreshdPackageDto();
        dto.setName("example1");
        dto.setFilter("IPADDR != '0.0.0.0'");
        dto.setSpecifics(List.of("10.0.0.1"));

        final IpRangeDto range = new IpRangeDto();
        range.setBegin("1.1.1.1");
        range.setEnd("254.254.254.254");
        dto.setIncludeRanges(List.of(range));

        final ParameterDto parameter = new ParameterDto();
        parameter.setKey("thresholding-group");
        parameter.setValue("mib2");

        final ThreshdServiceDto service = new ThreshdServiceDto();
        service.setName("SNMP");
        service.setInterval(300000L);
        service.setUserDefined(Boolean.FALSE);
        service.setStatus("on");
        service.setParameters(List.of(parameter));
        dto.setServices(List.of(service));

        dto.setOutageCalendars(List.of("maintenance"));

        return dto;
    }

    private static String writeJson(final Object dto) {
        try {
            return new ObjectMapper().writeValueAsString(dto);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }
}
