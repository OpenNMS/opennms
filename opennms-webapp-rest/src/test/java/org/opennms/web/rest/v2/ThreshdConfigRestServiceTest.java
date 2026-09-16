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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.Thresholder;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.FakeThresholdingDaos.FakeThreshdDao;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdPackageSummaryDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;
import org.opennms.web.rest.v2.model.ThresholderDto;

public class ThreshdConfigRestServiceTest {

    private ThreshdConfigRestService restService;
    private FakeThreshdDao threshdDao;
    private EventProxy eventProxy;

    @Before
    public void setUp() throws Exception {
        restService = new ThreshdConfigRestService();
        threshdDao = new FakeThreshdDao(configWith(pkg("example1"), pkg("example2")));
        eventProxy = mock(EventProxy.class);

        setField(restService, "threshdDao", threshdDao);
        setField(restService, "eventProxy", eventProxy);
    }

    // ------------------------------------------------------------------- reads

    @Test
    public void returnsTheWholeConfigurationWithAnEntityTag() {
        try (Response response = restService.getThreshdConfiguration(adminContext())) {
            assertEquals(200, response.getStatus());
            final ThreshdConfigDto dto = (ThreshdConfigDto) response.getEntity();
            assertEquals(Integer.valueOf(5), dto.getThreads());
            assertEquals(2, dto.getPackages().size());
            assertEquals(1, dto.getThresholder().size());
            assertNotNull(response.getEntityTag());
        }
    }

    @Test
    public void reportsNotFoundWhenNoConfigurationIsStored() {
        setFieldQuietly(restService, "threshdDao", new FakeThreshdDao(null));

        try (Response response = restService.getThreshdConfiguration(adminContext())) {
            assertEquals(404, response.getStatus());
            assertEquals("Threshd configuration not found.", response.getEntity());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsPackagesSortedByName() {
        try (Response response = restService.getThreshdPackages(adminContext())) {
            assertEquals(200, response.getStatus());
            final List<ThreshdPackageSummaryDto> packages = (List<ThreshdPackageSummaryDto>) response.getEntity();
            assertEquals(List.of("example1", "example2"),
                    packages.stream().map(ThreshdPackageSummaryDto::getName).toList());
        }
    }

    @Test
    public void returnsNotFoundForAnUnknownPackage() {
        try (Response response = restService.getThreshdPackage("nope", adminContext())) {
            assertEquals(404, response.getStatus());
            assertEquals("Threshd package 'nope' does not exist.", response.getEntity());
        }
    }

    // ------------------------------------------------------------------ writes

    @Test
    public void createsAPackageAndPointsAtItWithLocation() {
        try (Response response = restService.createThreshdPackage(packageDto("example3"), adminContext())) {
            assertEquals(201, response.getStatus());
            assertEquals("threshd/packages/example3", response.getLocation().toString());
        }

        assertEquals(1, threshdDao.getSaveCount());
        assertTrue(threshdDao.getWriteableConfig().getPackage("example3").isPresent());
    }

    @Test
    public void refusesToCreateAPackageThatAlreadyExists() {
        try (Response response = restService.createThreshdPackage(packageDto("example1"), adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void requiresAFilterOnANewPackage() {
        final ThreshdPackageDto dto = packageDto("example3");
        dto.setFilter(null);

        try (Response response = restService.createThreshdPackage(dto, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("filter"));
        }
    }

    @Test
    public void renamesAPackage() {
        try (Response response = restService.updateThreshdPackage("example1", packageDto("renamed"), null,
                adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final ThreshdConfiguration config = threshdDao.getWriteableConfig();
        assertTrue(config.getPackage("renamed").isPresent());
        assertTrue(config.getPackage("example1").isEmpty());
        assertEquals(2, config.getPackages().size());
    }

    @Test
    public void refusesToRenameAPackageOntoAnExistingName() {
        try (Response response = restService.updateThreshdPackage("example1", packageDto("example2"), null,
                adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void rejectsAWriteWhoseIfMatchIsStale() {
        try (Response response = restService.updateThreshdPackage("example1", packageDto("example1"),
                "\"not-the-current-etag\"", adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void acceptsAWriteWhoseIfMatchIsCurrent() {
        final String etag;
        try (Response response = restService.getThreshdPackage("example1", adminContext())) {
            etag = response.getEntityTag().getValue();
        }

        try (Response response = restService.updateThreshdPackage("example1", packageDto("example1"),
                '"' + etag + '"', adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(1, threshdDao.getSaveCount());
    }

    @Test
    public void replacingAPackageAlsoReplacesItsOutageCalendars() {
        // Worth pinning: the scheduled outages API maintains the same list, so a client that writes a stale
        // package silently drops outage assignments made in between.
        threshdDao.getWriteableConfig().getPackage("example1").orElseThrow()
                .setOutageCalendars(List.of("maintenance"));

        try (Response response = restService.updateThreshdPackage("example1", packageDto("example1"), null,
                adminContext())) {
            assertEquals(204, response.getStatus());
        }

        assertTrue(threshdDao.getWriteableConfig().getPackage("example1").orElseThrow()
                .getOutageCalendars().isEmpty());
    }

    @Test
    public void deletesAPackage() {
        try (Response response = restService.deleteThreshdPackage("example1", null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        assertEquals(1, threshdDao.getSaveCount());
        assertEquals(List.of("example2"),
                threshdDao.getWriteableConfig().getPackages().stream().map(Package::getName).toList());
    }

    @Test
    public void refusesToDeleteTheLastPackage() {
        // thresholding.xsd declares package with minOccurs=1, so an empty list would not validate.
        setFieldQuietly(restService, "threshdDao", new FakeThreshdDao(configWith(pkg("only"))));

        try (Response response = restService.deleteThreshdPackage("only", null, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(),
                    response.getEntity().toString().contains("At least one threshd package is required"));
        }
    }

    @Test
    public void replacesTheWholeConfiguration() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setThreads(9);
        dto.setPackages(List.of(packageDto("only")));
        dto.setThresholder(List.of());

        try (Response response = restService.updateThreshdConfiguration(dto, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final ThreshdConfiguration config = threshdDao.getWriteableConfig();
        assertEquals(Integer.valueOf(9), config.getThreads());
        assertEquals(1, config.getPackages().size());
        assertTrue(config.getThresholder().isEmpty());
    }

    @Test
    public void rejectsAConfigurationWithoutPackages() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setThreads(5);
        dto.setPackages(List.of());

        try (Response response = restService.updateThreshdConfiguration(dto, null, adminContext())) {
            assertEquals(400, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    // ------------------------------------------------------------------ events

    @Test
    public void tellsTheDaemonToReloadTheRightFileAfterAWrite() {
        try (Response response = restService.createThreshdPackage(packageDto("example3"), adminContext())) {
            assertEquals(201, response.getStatus());
        }

        assertEquals("threshd-configuration.xml", reloadedConfigFile());
    }

    @Test
    public void reloadEndpointIsAdminOnly() {
        try (Response response = restService.reloadThreshdConfiguration(userContext())) {
            assertEquals(403, response.getStatus());
        }
        try (Response response = restService.reloadThreshdConfiguration(adminContext())) {
            assertEquals(202, response.getStatus());
        }
    }

    // ---------------------------------------------------------------- download

    @Test
    public void downloadIsAdminOnlyAndRejectsAnUnknownFormat() {
        try (Response response = restService.downloadThreshdConfiguration("json", userContext())) {
            assertEquals(403, response.getStatus());
        }
        try (Response response = restService.downloadThreshdConfiguration("yaml", adminContext())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void downloadsXmlAsAnAttachment() {
        try (Response response = restService.downloadThreshdConfiguration("xml", adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("attachment; filename=threshd-configuration.xml",
                    response.getHeaderString("Content-Disposition"));
            final String xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(xml, xml.contains("name=\"example1\""));
        }
    }

    @Test
    public void downloadsJsonUnderTheSingularThresholderKey() {
        try (Response response = restService.downloadThreshdConfiguration(null, adminContext())) {
            assertEquals(200, response.getStatus());
            final String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json, json.contains("\"thresholder\""));
            assertFalse(json, json.contains("\"thresholders\""));
        }
    }

    // ------------------------------------------------------------------ helpers

    private String reloadedConfigFile() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        try {
            verify(eventProxy).send(captor.capture());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }

        final Event event = captor.getValue();
        assertEquals(EventConstants.RELOAD_DAEMON_CONFIG_UEI, event.getUei());
        assertEquals("Threshd", parm(event, EventConstants.PARM_DAEMON_NAME));
        return parm(event, EventConstants.PARM_CONFIG_FILE_NAME);
    }

    private static String parm(final Event event, final String name) {
        for (final Parm parm : event.getParmCollection()) {
            if (name.equals(parm.getParmName())) {
                return parm.getValue().getContent();
            }
        }
        return null;
    }

    private static ThreshdConfiguration configWith(final Package... packages) {
        final ThreshdConfiguration config = new ThreshdConfiguration();
        config.setThreads(5);
        config.setPackages(List.of(packages));

        final Thresholder thresholder = new Thresholder();
        thresholder.setService("SNMP");
        thresholder.setClassName("org.opennms.netmgt.threshd.SnmpThresholder");
        config.setThresholder(List.of(thresholder));

        return config;
    }

    private static Package pkg(final String name) {
        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        return pkg;
    }

    private static ThreshdPackageDto packageDto(final String name) {
        final ParameterDto parameter = new ParameterDto();
        parameter.setKey("thresholding-group");
        parameter.setValue("mib2");

        final ThreshdServiceDto service = new ThreshdServiceDto();
        service.setName("SNMP");
        service.setInterval(300000L);
        service.setStatus("on");
        service.setParameters(List.of(parameter));

        final ThreshdPackageDto dto = new ThreshdPackageDto();
        dto.setName(name);
        dto.setFilter("IPADDR != '0.0.0.0'");
        dto.setServices(List.of(service));
        return dto;
    }

    private static SecurityContext adminContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return context;
    }

    private static SecurityContext userContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(false);
        return context;
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setFieldQuietly(final Object target, final String name, final Object value) {
        try {
            setField(target, name, value);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }
}
