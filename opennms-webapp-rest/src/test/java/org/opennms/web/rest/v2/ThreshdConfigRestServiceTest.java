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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.FakeThresholdingDaos.FakeThreshdDao;
import org.opennms.web.rest.v2.model.IpRangeDto;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdPackageSummaryDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;

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
            assertEquals(2, dto.getPackages().size());
            assertNotNull(response.getEntityTag());
            assertEquals(response.getEntityTag().getValue(), dto.getVersion());
        }
    }

    @Test
    public void returnsAPackageWithAnEntityTagThatAlsoAppearsInTheBody() {
        try (Response response = restService.getThreshdPackage("example1", adminContext())) {
            assertEquals(200, response.getStatus());
            final ThreshdPackageDto dto = (ThreshdPackageDto) response.getEntity();
            assertNotNull(response.getEntityTag());
            assertEquals(response.getEntityTag().getValue(), dto.getVersion());
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
    public void rejectsAPackageReadBeforeAnOutageCalendarWasAssigned() {
        // The UI sends the body's version back as If-Match, so this is the round trip it relies on.
        final ThreshdPackageDto read;
        try (Response response = restService.getThreshdPackage("example1", adminContext())) {
            read = (ThreshdPackageDto) response.getEntity();
        }

        threshdDao.getWriteableConfig().getPackage("example1").orElseThrow()
                .setOutageCalendars(List.of("maintenance"));

        try (Response response = restService.updateThreshdPackage("example1", read,
                '"' + read.getVersion() + '"', adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
        assertEquals(List.of("maintenance"),
                threshdDao.getWriteableConfig().getPackage("example1").orElseThrow().getOutageCalendars());
    }

    @Test
    public void acceptsAPackageWrittenBackWithTheVersionItWasReadWith() {
        final ThreshdPackageDto read;
        try (Response response = restService.getThreshdPackage("example1", adminContext())) {
            read = (ThreshdPackageDto) response.getEntity();
        }

        try (Response response = restService.updateThreshdPackage("example1", read,
                '"' + read.getVersion() + '"', adminContext())) {
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
        dto.setPackages(List.of(packageDto("only")));

        try (Response response = restService.updateThreshdConfiguration(dto, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final ThreshdConfiguration config = threshdDao.getWriteableConfig();
        assertEquals(1, config.getPackages().size());
    }

    @Test
    public void rejectsAConfigurationWithoutPackages() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setPackages(List.of());

        try (Response response = restService.updateThreshdConfiguration(dto, null, adminContext())) {
            assertEquals(400, response.getStatus());
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void rejectsABlankParameterValueAsABadRequestRatherThanAServerError() {
        // The UI seeds thresholding-group with '' until a group is picked; Parameter.setValue rejects that, so
        // without the validator check this surfaced as a 500.
        final ThreshdPackageDto dto = packageDto("example3");
        dto.getServices().get(0).getParameters().get(0).setValue("");

        try (Response response = restService.createThreshdPackage(dto, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("thresholding-group"));
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void rejectsABlankAddressRangeAsABadRequestRatherThanAServerError() {
        final IpRangeDto range = new IpRangeDto();
        range.setBegin("10.0.0.1");
        range.setEnd(" ");
        final ThreshdPackageDto dto = packageDto("example3");
        dto.setIncludeRanges(List.of(range));

        try (Response response = restService.createThreshdPackage(dto, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("include range"));
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void rejectsAConfigurationWithDuplicatePackageNames() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setPackages(List.of(packageDto("same"), packageDto(" same ")));

        try (Response response = restService.updateThreshdConfiguration(dto, null, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("Duplicate package name"));
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void rejectsAnXmlUploadWithDuplicatePackageNames() {
        final String xml = "<threshd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/thresholding\">"
                + "<package name=\"same\"><filter>IPADDR != '0.0.0.0'</filter></package>"
                + "<package name=\"same\"><filter>IPADDR != '0.0.0.0'</filter></package>"
                + "</threshd-configuration>";

        try (Response response = restService.uploadThreshdConfigurationXml(attachment(xml), adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("Duplicate package name"));
        }
        assertEquals(0, threshdDao.getSaveCount());
    }

    // ------------------------------------------------------------------ events

    @Test
    public void tellsTheThresholdingServiceTheConfigurationChangedAfterAWrite() {
        try (Response response = restService.createThreshdPackage(packageDto("example3"), adminContext())) {
            assertEquals(201, response.getStatus());
        }

        assertSentOneConfigChangedEvent();
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
    public void downloadsJsonWithoutTheFieldsDroppedFromTheSchema() {
        try (Response response = restService.downloadThreshdConfiguration(null, adminContext())) {
            assertEquals(200, response.getStatus());
            final String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json, json.contains("\"packages\""));
            assertFalse(json, json.contains("threads"));
            assertFalse(json, json.contains("thresholder"));
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Exactly one event per write, and it is the config-changed notification rather than a daemon reload. */
    private void assertSentOneConfigChangedEvent() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        try {
            verify(eventProxy, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
        assertEquals(List.of(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI),
                captor.getAllValues().stream().map(Event::getUei).toList());
    }

    private static ThreshdConfiguration configWith(final Package... packages) {
        final ThreshdConfiguration config = new ThreshdConfiguration();
        config.setPackages(List.of(packages));
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

    private static Attachment attachment(final String body) {
        final Attachment attachment = mock(Attachment.class);
        final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        when(attachment.getObject(InputStream.class)).thenReturn(stream);
        return attachment;
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
