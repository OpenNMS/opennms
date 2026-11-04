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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.FakeThresholdingDaos.FakeThreshdDao;
import org.opennms.web.rest.v2.FakeThresholdingDaos.FakeThresholdingDao;
import org.opennms.web.rest.v2.model.ExpressionDto;
import org.opennms.web.rest.v2.model.ResourceFilterDto;
import org.opennms.web.rest.v2.model.ThresholdDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;
import org.opennms.web.rest.v2.model.ThresholdGroupSummaryDto;
import org.opennms.web.rest.v2.model.ThresholdingMetadataDto;

public class ThresholdingConfigRestServiceTest {

    private ThresholdingConfigRestService restService;
    private FakeThresholdingDao thresholdingDao;
    private FakeThreshdDao threshdDao;
    private ThresholdUeiService thresholdUeiService;
    private EventConfPersistenceService eventConfPersistenceService;
    private EventProxy eventProxy;

    @Before
    public void setUp() throws Exception {
        restService = new ThresholdingConfigRestService();
        thresholdingDao = new FakeThresholdingDao(configWith(group("mib2"), group("cisco")));
        // example1 uses cisco; mib2 is unused unless a test says otherwise.
        threshdDao = new FakeThreshdDao(threshdWith(pkgUsing("example1", "cisco")));
        thresholdUeiService = mock(ThresholdUeiService.class);
        eventConfPersistenceService = mock(EventConfPersistenceService.class);
        eventProxy = mock(EventProxy.class);

        setField(restService, "thresholdingDao", thresholdingDao);
        setField(restService, "threshdDao", threshdDao);
        setField(restService, "thresholdUeiService", thresholdUeiService);
        setField(restService, "eventConfPersistenceService", eventConfPersistenceService);
        setField(restService, "eventProxy", eventProxy);
        setField(restService, "resourceDao", mock(ResourceDao.class));
    }

    // ------------------------------------------------------------------- reads

    @Test
    @SuppressWarnings("unchecked")
    public void listsGroupsSortedByName() {
        try (Response response = restService.getThresholdGroups(adminContext())) {
            assertEquals(200, response.getStatus());
            final List<ThresholdGroupSummaryDto> groups = (List<ThresholdGroupSummaryDto>) response.getEntity();
            assertEquals(List.of("cisco", "mib2"), groups.stream().map(ThresholdGroupSummaryDto::getName).toList());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void flagsGroupsThatOnlyExistInTheMergedViewAsReadOnly() {
        // A group contributed by an OSGi extension is visible to the daemon but is not stored, so the UI has
        // to be able to tell the user why it cannot be edited.
        thresholdingDao.setMergedConfig(configWith(group("mib2"), group("cisco"), group("fromExtension")));

        try (Response response = restService.getThresholdGroups(adminContext())) {
            final List<ThresholdGroupSummaryDto> groups = (List<ThresholdGroupSummaryDto>) response.getEntity();
            assertEquals(3, groups.size());
            assertEquals(Boolean.TRUE, byName(groups, "fromExtension").getReadOnly());
            assertEquals(Boolean.FALSE, byName(groups, "mib2").getReadOnly());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsNothingRatherThanFailingWhenNoConfigurationIsStored() {
        thresholdingDao = new FakeThresholdingDao(null);
        setFieldQuietly(restService, "thresholdingDao", thresholdingDao);

        try (Response response = restService.getThresholdGroups(adminContext())) {
            assertEquals(200, response.getStatus());
            assertTrue(((List<ThresholdGroupSummaryDto>) response.getEntity()).isEmpty());
        }
    }

    @Test
    public void returnsNotFoundRatherThanServerErrorForAnUnknownGroup() {
        // ThresholdingConfig.getGroup throws instead of returning null, so a naive lookup would 500 here.
        try (Response response = restService.getThresholdGroup("nope", adminContext())) {
            assertEquals(404, response.getStatus());
            assertEquals("Threshold group 'nope' does not exist.", response.getEntity());
        }
    }

    @Test
    public void returnsAGroupWithAnEntityTagThatAlsoAppearsInTheBody() {
        try (Response response = restService.getThresholdGroup("mib2", adminContext())) {
            assertEquals(200, response.getStatus());
            final ThresholdGroupDto dto = (ThresholdGroupDto) response.getEntity();
            assertEquals("mib2", dto.getName());
            assertNotNull(response.getEntityTag());
            assertEquals(response.getEntityTag().getValue(), dto.getVersion());
        }
    }

    @Test
    public void alwaysOffersTheTwoBuiltInDatasourceTypes() {
        try (Response response = restService.getThresholdingMetadata(adminContext())) {
            final ThresholdingMetadataDto dto = (ThresholdingMetadataDto) response.getEntity();
            assertEquals(List.of("node", "if"), dto.getDsTypes().stream().map(t -> t.getName()).toList());
            assertEquals(List.of("high", "low", "relativeChange", "absoluteChange", "rearmingAbsoluteChange"),
                    dto.getThresholdTypes());
            assertEquals(List.of("and", "or"), dto.getFilterOperators());
        }
    }

    // ------------------------------------------------------------------ create

    @Test
    public void createsAGroupAndPointsAtItWithLocation() {
        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(201, response.getStatus());
            assertEquals("thresholding/groups/newGroup", response.getLocation().toString());
        }

        assertEquals(1, thresholdingDao.getSaveCount());
        assertNotNull(thresholdingDao.getWriteableConfig().getGroup("newGroup"));
    }

    @Test
    public void refusesToCreateAGroupThatAlreadyExists() {
        try (Response response = restService.createThresholdGroup(groupDto("mib2"), adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void refusesToCreateAGroupThatAnExtensionAlreadyProvides() {
        // The merged view keeps whichever duplicate comes last, so one definition would silently win.
        thresholdingDao.setMergedConfig(configWith(group("mib2"), group("cisco"), group("fromExtension")));

        try (Response response = restService.createThresholdGroup(groupDto("fromExtension"), adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("extension"));
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void rejectsATriggerWithATrailingSuffix() {
        // Regression guard: XSD patterns are anchored, Matcher.find() is not, so "1abc" must not pass.
        final ThresholdGroupDto group = groupDto("newGroup");
        final ThresholdDto threshold = threshold();
        threshold.setTrigger("1abc");
        group.setThresholds(List.of(threshold));

        try (Response response = restService.createThresholdGroup(group, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("trigger"));
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void rejectsADatasourceNameRrdtoolCannotStore() {
        final ThresholdGroupDto group = groupDto("newGroup");
        final ThresholdDto threshold = threshold();
        threshold.setDsName("a".repeat(20));
        group.setThresholds(List.of(threshold));

        try (Response response = restService.createThresholdGroup(group, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("19"));
        }
    }

    @Test
    public void createsEventDefinitionsForCustomUeisBeforeSaving() {
        final ThresholdGroupDto group = groupDto("newGroup");
        final ThresholdDto threshold = threshold();
        threshold.setTriggeredUEI("uei.opennms.org/example/exceeded");
        group.setThresholds(List.of(threshold));

        try (Response response = restService.createThresholdGroup(group, adminContext())) {
            assertEquals(201, response.getStatus());
        }

        verify(thresholdUeiService).ensureUeisInEventConf(any());
    }

    @Test
    public void reloadsEventConfOncePerRequestWhenDefinitionsWereCreated() {
        when(thresholdUeiService.ensureUeisInEventConf(any())).thenReturn(true);

        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(201, response.getStatus());
        }
        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2"), null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        verify(eventConfPersistenceService, times(2)).reloadEventsIntoMemory();
    }

    @Test
    public void skipsTheEventConfReloadWhenNoDefinitionWasCreated() {
        when(thresholdUeiService.ensureUeisInEventConf(any())).thenReturn(false);

        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(201, response.getStatus());
        }

        verify(eventConfPersistenceService, never()).reloadEventsIntoMemory();
    }

    @Test
    public void stillReloadsEventConfWhenTheConfigSaveFailsAfterDefinitionsWereCreated() {
        // The definitions are committed before the config save; without the reload eventconf would not know
        // them, and the next save would create them again.
        when(thresholdUeiService.ensureUeisInEventConf(any())).thenReturn(true);
        thresholdingDao.failNextSaveWith(new RuntimeException("db down"));

        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(500, response.getStatus());
        }

        verify(eventConfPersistenceService, times(1)).reloadEventsIntoMemory();
    }

    @Test
    public void reportsAFailedSaveAsAServerErrorRatherThanPretendingItWorked() throws Exception {
        thresholdingDao.failNextSaveWith(new RuntimeException("db down"));

        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(500, response.getStatus());
        }
        verify(eventProxy, never()).send(any(Event.class));
    }

    // ------------------------------------------------------------------ update

    @Test
    public void renamingAGroupKeepsTheNameIndexConsistent() throws Exception {
        // ThresholdingConfig indexes groups by the name they had when they were added and setName does not
        // reindex, so a rename done in place leaves a group that getGroups() lists but getGroup() cannot find.
        final ThresholdGroupDto group = groupDto("renamed");

        try (Response response = restService.updateThresholdGroup("mib2", group, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final ThresholdingConfig config = thresholdingDao.getWriteableConfig();
        assertNotNull(config.getGroup("renamed"));
        try {
            config.getGroup("mib2");
            fail("the old name should no longer resolve");
        } catch (final IllegalArgumentException expected) {
            // the model signals an unknown group by throwing
        }
        assertEquals(2, config.getGroups().size());
    }

    @Test
    public void refusesToRenameAGroupOntoAnExistingName() {
        try (Response response = restService.updateThresholdGroup("mib2", groupDto("cisco"), null, adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void refusesToRenameAGroupOntoANameAnExtensionProvides() {
        thresholdingDao.setMergedConfig(configWith(group("mib2"), group("cisco"), group("fromExtension")));

        try (Response response = restService.updateThresholdGroup("mib2", groupDto("fromExtension"), null,
                adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void renamingAGroupPointsThePackagesThatUseItAtTheNewName() {
        try (Response response = restService.updateThresholdGroup("cisco", groupDto("cisco-new"), null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        assertEquals(1, thresholdingDao.getSaveCount());
        assertEquals(1, threshdDao.getSaveCount());
        assertEquals("cisco-new", groupOf(threshdDao.getWriteableConfig(), "example1"));
        // Both documents changed, but one notification covers them.
        assertSentOneConfigChangedEvent();
    }

    @Test
    public void renamingAnUnusedGroupLeavesTheThreshdConfigurationAlone() {
        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2-new"), null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        assertEquals(0, threshdDao.getSaveCount());
        assertEquals("cisco", groupOf(threshdDao.getWriteableConfig(), "example1"));
        assertSentOneConfigChangedEvent();
    }

    @Test
    public void refusesToRenameAGroupAPackageFromAnExtensionUses() {
        // That package cannot be rewritten, so it would be left pointing at nothing.
        threshdDao.setMergedConfig(threshdWith(pkgUsing("example1", "cisco"), pkgUsing("fromExtension", "mib2")));

        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2-new"), null, adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("fromExtension"));
        }
        assertEquals(0, thresholdingDao.getSaveCount());
        assertEquals(0, threshdDao.getSaveCount());
    }

    @Test
    public void restoresTheOldGroupNameWhenThePackagesCannotBeUpdated() throws Exception {
        threshdDao.failNextSaveWith(new RuntimeException("db down"));

        try (Response response = restService.updateThresholdGroup("cisco", groupDto("cisco-new"), null, adminContext())) {
            assertEquals(500, response.getStatus());
        }

        // Saved once with the new name, then once more to put the old one back.
        assertEquals(2, thresholdingDao.getSaveCount());
        final ThresholdingConfig config = thresholdingDao.getWriteableConfig();
        assertNotNull(config.getGroup("cisco"));
        assertEquals(List.of("mib2", "cisco"), config.getGroups().stream().map(Group::getName).toList());
        verify(eventProxy, never()).send(any(Event.class));
    }

    @Test
    public void rejectsAWriteWhoseIfMatchIsStale() {
        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2"), "\"not-the-current-etag\"",
                adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void acceptsAWriteWhoseIfMatchIsCurrent() {
        final String etag;
        try (Response response = restService.getThresholdGroup("mib2", adminContext())) {
            etag = response.getEntityTag().getValue();
        }

        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2"), '"' + etag + '"',
                adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(1, thresholdingDao.getSaveCount());
    }

    @Test
    public void acceptsAWriteWithNoIfMatchAtAll() {
        try (Response response = restService.updateThresholdGroup("mib2", groupDto("mib2"), null, adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(1, thresholdingDao.getSaveCount());
    }

    @Test
    public void replacingAGroupReplacesItsDefinitionsWholesale() {
        final ThresholdGroupDto group = groupDto("mib2");
        final ExpressionDto expression = new ExpressionDto();
        expression.setExpression("a + b");
        expression.setType("high");
        expression.setDsType("node");
        expression.setValue("1");
        expression.setRearm("2");
        expression.setTrigger("3");
        group.setThresholds(List.of());
        group.setExpressions(List.of(expression));

        try (Response response = restService.updateThresholdGroup("mib2", group, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final Group stored = thresholdingDao.getWriteableConfig().getGroup("mib2");
        assertTrue(stored.getThresholds().isEmpty());
        assertEquals(1, stored.getExpressions().size());
    }

    @Test
    public void preservesResourceFilterOrder() {
        final ThresholdGroupDto group = groupDto("mib2");
        final ThresholdDto threshold = threshold();
        threshold.setResourceFilters(List.of(filter("first"), filter("second"), filter("third")));
        group.setThresholds(List.of(threshold));

        try (Response response = restService.updateThresholdGroup("mib2", group, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        final var filters = thresholdingDao.getWriteableConfig().getGroup("mib2").getThresholds().get(0)
                .getResourceFilters();
        assertEquals(List.of("first", "second", "third"), filters.stream().map(f -> f.getField()).toList());
    }

    @Test
    public void refusesToWriteAGroupThatOnlyExistsInTheMergedView() {
        thresholdingDao.setMergedConfig(configWith(group("mib2"), group("cisco"), group("fromExtension")));

        try (Response response = restService.updateThresholdGroup("fromExtension", groupDto("fromExtension"), null,
                adminContext())) {
            assertEquals(403, response.getStatus());
        }
    }

    // ------------------------------------------------------------------ delete

    @Test
    public void deletesAGroup() {
        try (Response response = restService.deleteThresholdGroup("mib2", null, adminContext())) {
            assertEquals(204, response.getStatus());
        }

        assertEquals(1, thresholdingDao.getSaveCount());
        assertEquals(List.of("cisco"),
                thresholdingDao.getWriteableConfig().getGroups().stream().map(Group::getName).toList());
    }

    @Test
    public void refusesToDeleteAGroupAPackageStillUses() {
        try (Response response = restService.deleteThresholdGroup("cisco", null, adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("example1"));
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void refusesToDeleteAGroupAPackageFromAnExtensionUses() {
        threshdDao.setMergedConfig(threshdWith(pkgUsing("example1", "cisco"), pkgUsing("fromExtension", "mib2")));

        try (Response response = restService.deleteThresholdGroup("mib2", null, adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void returnsNotFoundWhenDeletingAGroupThatIsNotThere() {
        try (Response response = restService.deleteThresholdGroup("nope", null, adminContext())) {
            assertEquals(404, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    // ------------------------------------------------------------------ events

    @Test
    public void tellsTheThresholdingServiceTheConfigurationChangedAfterAWrite() {
        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(201, response.getStatus());
        }

        assertSentOneConfigChangedEvent();
    }

    @Test
    public void stillReportsSuccessWhenTheReloadEventCannotBeSent() throws Exception {
        // The configuration is stored by then; failing the request would tell the user nothing was saved.
        org.mockito.Mockito.doThrow(new RuntimeException("no event bus")).when(eventProxy).send(any(Event.class));

        try (Response response = restService.createThresholdGroup(groupDto("newGroup"), adminContext())) {
            assertEquals(201, response.getStatus());
        }
        assertEquals(1, thresholdingDao.getSaveCount());
    }

    // ---------------------------------------------------------------- download

    @Test
    public void downloadIsAdminOnlyAndRejectsAnUnknownFormat() {
        try (Response response = restService.downloadThresholdingConfiguration("json", userContext())) {
            assertEquals(403, response.getStatus());
        }
        try (Response response = restService.downloadThresholdingConfiguration("yaml", adminContext())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void downloadsXmlAsAnAttachment() {
        try (Response response = restService.downloadThresholdingConfiguration("xml", adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("attachment; filename=thresholds.xml", response.getHeaderString("Content-Disposition"));
            final String xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(xml, xml.contains("name=\"mib2\""));
        }
    }

    @Test
    public void downloadsJsonByDefault() {
        try (Response response = restService.downloadThresholdingConfiguration(null, adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("attachment; filename=thresholds.json", response.getHeaderString("Content-Disposition"));
            final String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json, json.contains("\"mib2\""));
        }
    }

    @Test
    public void downloadReportsNotFoundWhenNoConfigurationIsStored() {
        setFieldQuietly(restService, "thresholdingDao", new FakeThresholdingDao(null));

        try (Response response = restService.downloadThresholdingConfiguration("xml", adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    // ------------------------------------------------------------------ upload

    @Test
    public void uploadReplacesTheStoredGroups() {
        try (Response response = restService.uploadThresholdingConfigurationXml(
                attachment(xmlWith("cisco", "other")), adminContext())) {
            assertEquals(200, response.getStatus());
        }

        assertEquals(List.of("cisco", "other"),
                thresholdingDao.getWriteableConfig().getGroups().stream().map(Group::getName).toList());
    }

    @Test
    public void uploadRejectsTheSameGroupNameTwice() {
        try (Response response = restService.uploadThresholdingConfigurationXml(
                attachment(xmlWith("cisco", "cisco")), adminContext())) {
            assertEquals(400, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void uploadRejectsAGroupNameAnExtensionProvides() {
        thresholdingDao.setMergedConfig(configWith(group("mib2"), group("cisco"), group("fromExtension")));

        try (Response response = restService.uploadThresholdingConfigurationXml(
                attachment(xmlWith("cisco", "fromExtension")), adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void uploadRefusesToDropAGroupAPackageStillUses() {
        try (Response response = restService.uploadThresholdingConfigurationXml(
                attachment(xmlWith("mib2")), adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString(), response.getEntity().toString().contains("'cisco' (used by example1)"));
        }
        assertEquals(0, thresholdingDao.getSaveCount());
    }

    @Test
    public void uploadCreatesEventDefinitionsForCustomUeisLikeAWriteDoes() {
        when(thresholdUeiService.ensureUeisInEventConf(any())).thenReturn(true);

        try (Response response = restService.uploadThresholdingConfigurationXml(
                attachment(xmlWith("cisco")), adminContext())) {
            assertEquals(200, response.getStatus());
        }

        verify(thresholdUeiService).ensureUeisInEventConf(any());
        verify(eventConfPersistenceService).reloadEventsIntoMemory();
    }

    // ------------------------------------------------------------------ helpers

    private static String xmlWith(final String... groupNames) {
        final StringBuilder xml = new StringBuilder("<thresholding-config xmlns=\"http://xmlns.opennms.org/xsd/config/thresholding\">");
        for (final String name : groupNames) {
            xml.append("<group name=\"").append(name).append("\" rrdRepository=\"/opt/opennms/share/rrd/snmp/\">")
                    .append("<threshold type=\"high\" ds-name=\"cpu\" ds-type=\"node\" value=\"90\" rearm=\"70\" trigger=\"3\"/>")
                    .append("</group>");
        }
        return xml.append("</thresholding-config>").toString();
    }

    private static Attachment attachment(final String body) {
        final Attachment attachment = mock(Attachment.class);
        final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        when(attachment.getObject(InputStream.class)).thenReturn(stream);
        return attachment;
    }

    private static ThreshdConfiguration threshdWith(final Package... packages) {
        final ThreshdConfiguration config = new ThreshdConfiguration();
        config.setPackages(List.of(packages));
        return config;
    }

    private static Package pkgUsing(final String name, final String groupName) {
        final Parameter parameter = new Parameter();
        parameter.setKey("thresholding-group");
        parameter.setValue(groupName);

        final Service service = new Service();
        service.setName("SNMP");
        service.setInterval(300000L);
        service.setParameters(List.of(parameter));

        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        pkg.setServices(List.of(service));
        return pkg;
    }

    private static String groupOf(final ThreshdConfiguration config, final String packageName) {
        return config.getPackage(packageName).orElseThrow().getServices().get(0).getParameters().get(0).getValue();
    }

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

    private static ThresholdGroupSummaryDto byName(final List<ThresholdGroupSummaryDto> groups, final String name) {
        return groups.stream().filter(g -> name.equals(g.getName())).findFirst().orElseThrow();
    }

    private static ThresholdingConfig configWith(final Group... groups) {
        final ThresholdingConfig config = new ThresholdingConfig();
        config.setGroups(List.of(groups));
        return config;
    }

    private static Group group(final String name) {
        final Group group = new Group();
        group.setName(name);
        group.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        return group;
    }

    private static ThresholdGroupDto groupDto(final String name) {
        final ThresholdGroupDto dto = new ThresholdGroupDto();
        dto.setName(name);
        dto.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        dto.setThresholds(List.of(threshold()));
        return dto;
    }

    private static ThresholdDto threshold() {
        final ThresholdDto dto = new ThresholdDto();
        dto.setDsName("cpuUtilization");
        dto.setType("high");
        dto.setDsType("node");
        dto.setValue("90");
        dto.setRearm("70");
        dto.setTrigger("3");
        return dto;
    }

    private static ResourceFilterDto filter(final String field) {
        final ResourceFilterDto dto = new ResourceFilterDto();
        dto.setField(field);
        dto.setContent(".*");
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
