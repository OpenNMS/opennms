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
package org.opennms.features.deviceconfig.rest.impl;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-config-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
    "classpath:/META-INF/opennms/applicationContext-deviceconfig-persistence.xml",
    "classpath:/META-INF/opennms/applicationContext-deviceconfig-service.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-rpc-poller.xml",
    "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DefaultDeviceConfigRestServiceScheduleIT {
    private static final int RECORD_COUNT = 5;

    private static final List<String> CRON_SCHEDULES = List.of(
        "0 15 10 ? * *",
        "0 * 14 * * ?",
        "0 15 10 ? * 6#3",
        DeviceConfigConstants.NEVER,
        ""
    );

    private static final List<String> EXPECTED_CRON_SCHEDULE_DESCRIPTIONS = List.of(
        "At 10:15 am",
        "Every minute, at 2:00 pm",
        "At 10:15 am, on the third Saturday of the month",
        "Never",
        "Never"
    );

    private static final List<byte[]> CONFIG_BYTES = List.of(
        "one".getBytes(StandardCharsets.UTF_8),
        "two".getBytes(StandardCharsets.UTF_8),
        "three".getBytes(StandardCharsets.UTF_8),
        "four".getBytes(StandardCharsets.UTF_8),
        "five".getBytes(StandardCharsets.UTF_8)
    );

    // configuration for 3 devices used in test database
    private static final List<String> NODE_NAMES = List.of("dcb-1", "dcb-2", "dcb-3", "dcb-4", "dcb-5");
    private static final List<String> OPERATING_SYSTEMS = List.of("alpine", "centos", "redhat", "juniper", "cisco");
    private static final List<String> FOREIGN_IDS = List.of("21", "22", "23", "24", "25");
    private static final List<String> IP_ADDRESSES = List.of("192.168.3.1", "192.168.3.2", "192.168.3.3", "192.168.3.4", "192.168.3.5");
    private static final List<String> CONFIG_STRINGS = List.of("one", "two", "three", "four", "five");
    private static final List<String> CONFIG_TYPES = List.of("default", "running", "wurstblinker", "pommes-frites", "rotweiss");
    private static final List<String> CONFIG_NAMES = List.of("Startup Configuration", "Running Configuration", "Wurstblinker Configuration", "Pommes Frites Configuration", "Rotweiss Configuration");
    private static final List<String> SERVICE_NAMES = List.of("DeviceConfig-default", "DeviceConfig-running", "DeviceConfig-wurstblinker", "DeviceConfig-pommes-frites", "DeviceConfig-rotweiss");
    private static final List<String> LOCATIONS = List.of("Default", "Default", "Wurstblinker Location", "Default", "Rotweiss Location");
    // alternate service names that may not match config type, to test we are using serviceName field
    private static final List<String> SUBSTITUTE_SERVICE_NAMES = List.of("DeviceConfig", "DeviceConfig-running", "DeviceConfig-wurstblinker", "DeviceConfig-pommes-frites", "DeviceConfig-rotweiss");

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private DeviceConfigService deviceConfigService;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private SessionUtils sessionUtils;

    private DeviceConfigRestService deviceConfigRestService;

    @Autowired
    private TransactionOperations operations;

    @Before
    public void before() throws IOException {
        deviceConfigRestService = new DefaultDeviceConfigRestService(deviceConfigDao, deviceConfigService, operations);
    }

    @After
    public void after() {
    }

    @Test
    public void testGetLatestDeviceConfigsForDeviceAndConfigType() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // sanity check that nodes and interfaces were created correctly
            List<Integer> ipInterfaceIds = ipInterfaces.stream().map(OnmsIpInterface::getId).collect(Collectors.toList());

            List<DeviceConfigService.RetrievalDefinition> services = ipInterfaces.stream()
                .flatMap(iface -> deviceConfigService.getRetrievalDefinitions(InetAddressUtils.str(iface.getIpAddress()), iface.getNode().getLocation().getLocationName()).stream())
                .collect(Collectors.toList());
            assertThat(services.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            IntStream.range(0, RECORD_COUNT).forEach(idx -> {
                deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                    SERVICE_NAMES.get(idx), dates.get(idx), CONFIG_BYTES.get(idx)));
            });

            // Older item, this one should NOT show up as it is an older item for same ipInterfaceId and configType as index 1 above
            Date olderDate = Date.from(dates.get(1).toInstant().minusSeconds(1));
            byte[] olderConfig = "older".getBytes(StandardCharsets.UTF_8);
            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1),
                SERVICE_NAMES.get(1), olderDate, olderConfig));

            final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(10, 0, "lastUpdated", "asc", null, null, false);
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(true));

            final var responseHeaders = response.getHeaders();
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            assertThat(responseHeaders.containsKey("Content-Range"), is(true));

            final String contentRange = getHeaderAsString(responseHeaders, "Content-Range");
            final String expectedContentRange = getContentRange(0, RECORD_COUNT - 1, RECORD_COUNT);
            assertThat(contentRange, equalTo(expectedContentRange));

            final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
            assertThat(responseList.size(), equalTo(RECORD_COUNT));
            final List<String> expectedOperatingSystems = List.of("alpine", "centos", "redhat", "juniper", "cisco");

            IntStream.range(0, RECORD_COUNT).forEach(i -> {
                final DeviceConfigDTO dto = responseList.get(i);

                assertDtoWith(dto, ipInterfaces.get(i).getId(), CONFIG_TYPES.get(i),
                    dates.get(i), dates.get(i), dates.get(i), null, DeviceConfigStatus.SUCCESS);
                assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(i)));
                assertThat(dto.getServiceName(), equalTo(SERVICE_NAMES.get(i)));
                assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.DEFAULT_ENCODING));
                assertThat(dto.getFailureReason(), nullValue());
                assertThat(dto.getConfig(), equalTo(CONFIG_STRINGS.get(i)));
                assertThat(dto.getOperatingSystem(), equalTo(expectedOperatingSystems.get(i)));
                assertThat(dto.getScheduledInterval().get(SERVICE_NAMES.get(i)),
                    equalTo(EXPECTED_CRON_SCHEDULE_DESCRIPTIONS.get(i)));

                if (i >= 3) {
                    assertThat(dto.getNextScheduledBackupDate(), is(nullValue()));
                } else {
                    assertThat(dto.getNextScheduledBackupDate().after(currentDate), is(true));
                }
            });

            // Now do a 'history' search which should return 2 items for index 1 "dcb-2", having 2 different backup dates
            final var historyResponse = deviceConfigRestService.getDeviceConfigsByInterface(ipInterfaceIds.get(1), null);
            assertThat(historyResponse, notNullValue());
            assertThat(historyResponse.hasEntity(), is(true));

            final var historyResponseHeaders = historyResponse.getHeaders();
            assertThat(historyResponse.getStatus(), is(Response.Status.OK.getStatusCode()));
            assertThat(historyResponseHeaders.containsKey("Content-Range"), is(true));

            final String historyContentRange = historyResponseHeaders.get("Content-Range").get(0).toString();
            final String expectedHistoryContentRange = getContentRange(0, 1, 2);
            assertThat(historyContentRange, equalTo(expectedHistoryContentRange));

            final List<DeviceConfigDTO> historyResponseList = (List<DeviceConfigDTO>) historyResponse.getEntity();
            assertThat(historyResponseList.size(), equalTo(2));

            // check for common values
            IntStream.range(0, 2).forEach(i -> {
                DeviceConfigDTO dto = historyResponseList.get(i);

                assertThat(dto.getServiceName(), equalTo("DeviceConfig-" + CONFIG_TYPES.get(1)));
                assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(1)));
                assertThat(dto.getIpInterfaceId(), equalTo(ipInterfaceIds.get(1)));
                assertThat(CONFIG_TYPES.get(1).equalsIgnoreCase(dto.getConfigType()), is(true));
                assertThat(dto.getServiceName(), equalTo(SERVICE_NAMES.get(1)));
                assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.DEFAULT_ENCODING));
                assertThat(dto.getFailureReason(), nullValue());
                assertThat(dto.getOperatingSystem(), equalTo(expectedOperatingSystems.get(1)));
                assertThat(dto.getScheduledInterval().get(SERVICE_NAMES.get(1)),
                    equalTo(EXPECTED_CRON_SCHEDULE_DESCRIPTIONS.get(1)));
                assertThat(dto.getNextScheduledBackupDate().after(currentDate), is(true));
            });

            // check for different values
            final DeviceConfigDTO newerDto = historyResponseList.get(0);
            final DeviceConfigDTO olderDto = historyResponseList.get(1);

            assertDtoWith(newerDto, ipInterfaceIds.get(1), CONFIG_TYPES.get(1),
                dates.get(1), dates.get(1), dates.get(1), null, DeviceConfigStatus.SUCCESS);
            assertThat(newerDto.getConfigName(), equalTo(CONFIG_NAMES.get(1)));
            assertThat(newerDto.getConfig(), equalTo(CONFIG_STRINGS.get(1)));

            assertDtoWith(olderDto, ipInterfaceIds.get(1), CONFIG_TYPES.get(1),
                olderDate, olderDate, olderDate, null, DeviceConfigStatus.SUCCESS);
            assertThat(olderDto.getConfigName(), equalTo(CONFIG_NAMES.get(1)));
            assertThat(olderDto.getConfig(), equalTo("older"));
        });
    }

    @Test
    public void testGetLatestDeviceConfigsForDeviceAndConfigTypeWithStatus() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            // Add 3 items having status: NONE, SUCCESS, FAILED
            DeviceConfig dcNone = createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0),
                    SERVICE_NAMES.get(0), null, null);
            dcNone.setStatus(DeviceConfigStatus.NONE);
            deviceConfigDao.saveOrUpdate(dcNone);

            DeviceConfig dcSuccess = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1),
                SERVICE_NAMES.get(1), dates.get(1), CONFIG_BYTES.get(1));
            dcSuccess.setStatus(DeviceConfigStatus.SUCCESS);
            deviceConfigDao.saveOrUpdate(dcSuccess);

            DeviceConfig dcFailed = createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2),
                SERVICE_NAMES.get(2), dates.get(2), CONFIG_BYTES.get(2));
            dcFailed.setLastFailed(dates.get(2));
            dcFailed.setLastSucceeded(Date.from(dates.get(2).toInstant().minusSeconds(10)));
            dcFailed.setStatus(DeviceConfigStatus.FAILED);
            deviceConfigDao.saveOrUpdate(dcFailed);

            {
                // SUCCESS or FAILED
                final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(
                    10, 0, "lastUpdated", "asc", null,
                    Set.of(DeviceConfigStatus.SUCCESS, DeviceConfigStatus.FAILED), false);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(2));

                final var dtoSuccess =
                    responseList.stream().filter(dto -> dto.getIpInterfaceId().equals(ipInterfaces.get(1).getId())).findFirst();
                assertDtoWith(dtoSuccess.orElse(null), ipInterfaces.get(1).getId(), CONFIG_TYPES.get(1),
                    dates.get(1), dates.get(1), dates.get(1), null, DeviceConfigStatus.SUCCESS);
                dtoSuccess.ifPresent(dto -> assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(1))));

                final var dtoFailed =
                    responseList.stream().filter(dto -> dto.getIpInterfaceId().equals(ipInterfaces.get(2).getId())).findFirst();
                assertDtoWith(dtoFailed.orElse(null), ipInterfaces.get(2).getId(), CONFIG_TYPES.get(2),
                    dates.get(2), dates.get(2), dcFailed.getLastSucceeded(), dates.get(2), DeviceConfigStatus.FAILED);
                dtoFailed.ifPresent(dto -> assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(2))));
            }

            {
                // SUCCESS only
                final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(
                    10, 0, "lastUpdated", "asc", null,
                    Set.of(DeviceConfigStatus.SUCCESS), false);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(1));

                final var dtoSuccess =
                    responseList.stream().filter(dto -> dto.getIpInterfaceId().equals(ipInterfaces.get(1).getId())).findFirst();
                assertDtoWith(dtoSuccess.orElse(null), ipInterfaces.get(1).getId(), CONFIG_TYPES.get(1),
                    dates.get(1), dates.get(1), dates.get(1), null, DeviceConfigStatus.SUCCESS);
                dtoSuccess.ifPresent(dto -> assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(1))));
            }

            {
                // FAILED only
                final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(
                    10, 0, "lastUpdated", "asc", null,
                    Set.of(DeviceConfigStatus.FAILED), false);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(1));

                final var dtoFailed =
                    responseList.stream().filter(dto -> dto.getIpInterfaceId().equals(ipInterfaces.get(2).getId())).findFirst();
                assertDtoWith(dtoFailed.orElse(null), ipInterfaces.get(2).getId(), CONFIG_TYPES.get(2),
                    dates.get(2), dates.get(2), dcFailed.getLastSucceeded(), dates.get(2), DeviceConfigStatus.FAILED);
                dtoFailed.ifPresent(dto -> assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(2))));
            }

            {
                // NONE only
                final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(
                    10, 0, "lastUpdated", "asc", null,
                    Set.of(DeviceConfigStatus.NONE), false);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(1));

                final var dtoNone =
                    responseList.stream().filter(dto -> dto.getIpInterfaceId().equals(ipInterfaces.get(0).getId())).findFirst();
                assertDtoWith(dtoNone.orElse(null), ipInterfaces.get(0).getId(), CONFIG_TYPES.get(0),
                    null, null, null, null, DeviceConfigStatus.NONE);
                dtoNone.ifPresent(dto -> assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(0))));
            }
        });
    }

    @Test
    public void testGetLatestDeviceConfigsWithServiceNameNotMatchingConfigType() {
        populateDeviceConfigServiceInfo(true);

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            List<Integer> ipInterfaceIds = ipInterfaces.stream().map(OnmsIpInterface::getId).collect(Collectors.toList());

            List<DeviceConfigService.RetrievalDefinition> services = ipInterfaces.stream()
                .flatMap(iface -> deviceConfigService.getRetrievalDefinitions(InetAddressUtils.str(iface.getIpAddress()), iface.getNode().getLocation().getLocationName()).stream())
                .collect(Collectors.toList());
            assertThat(services.size(), equalTo(RECORD_COUNT));
            assertThat(services.get(0).getServiceName(), Matchers.equalToIgnoringCase(SUBSTITUTE_SERVICE_NAMES.get(0)));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0),
                SUBSTITUTE_SERVICE_NAMES.get(0), dates.get(0), CONFIG_BYTES.get(0)));

            final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(10, 0, "lastUpdated", "asc", null, null, false);
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(true));

            final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
            assertThat(responseList.size(), equalTo(1));

            final DeviceConfigDTO dto = responseList.get(0);

            final var actualMonitoredService = ipInterfaces.get(0).getMonitoredServiceByServiceType(SUBSTITUTE_SERVICE_NAMES.get(0));
            assertThat(dto.getServiceName(), equalTo(actualMonitoredService.getServiceName()));
            assertThat(CONFIG_TYPES.get(0).equalsIgnoreCase(dto.getConfigType()), is(true));
            assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(0)));
            assertThat(dto.getServiceName(), equalTo(SUBSTITUTE_SERVICE_NAMES.get(0)));
            assertThat(dto.getLastUpdatedDate().getTime(), equalTo(dates.get(0).getTime()));
            assertThat(dto.getConfig(), equalTo(CONFIG_STRINGS.get(0)));
        });
    }

    @Test
    public void testGetLatestDeviceConfigsWithSearchTerm() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            List<Integer> ipInterfaceIds = ipInterfaces.stream().map(OnmsIpInterface::getId).collect(Collectors.toList());

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            IntStream.range(0, RECORD_COUNT).forEach(idx -> {
                var deviceConfig = createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                    SERVICE_NAMES.get(idx), dates.get(idx), CONFIG_BYTES.get(idx));

                // Add older entry for same ipinterfaceid, these should not show up in results or total count
                Date olderDate = Date.from(dates.get(idx).toInstant().minusSeconds(10));

                var olderDeviceConfig = createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                    SERVICE_NAMES.get(idx), olderDate, CONFIG_BYTES.get(idx));

                deviceConfigDao.saveOrUpdate(deviceConfig);
                deviceConfigDao.saveOrUpdate(olderDeviceConfig);
            });

            // Search for "dcb-2" by device name and ip address, should only retrieve the one record, index '1'
            final var searchTerms = List.of("dcb-2", "192.168.3.2");

            searchTerms.forEach(searchTerm -> {
                var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(10, 0, null, null, searchTerm, null, false);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseHeaders = response.getHeaders();
                assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
                assertThat(responseHeaders.containsKey("Content-Range"), is(true));

                String contentRange = getHeaderAsString(responseHeaders, "Content-Range");
                String expectedContentRange = getContentRange(0, 0, 1);
                assertThat(contentRange, equalTo(expectedContentRange));

                List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(1));
                List<String> expectedOperatingSystems = List.of("alpine", "centos", "redhat");

                DeviceConfigDTO dto = responseList.get(0);

                final var actualMonitoredService = ipInterfaces.get(1).getMonitoredServiceByServiceType(SERVICE_NAMES.get(1));
                assertThat(dto.getIpInterfaceId(), equalTo(ipInterfaceIds.get(1)));
                assertThat(dto.getServiceName(), equalTo("DeviceConfig-" + CONFIG_TYPES.get(1)));
                assertThat(CONFIG_TYPES.get(1).equalsIgnoreCase(dto.getConfigType()), is(true));
                assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(1)));
                assertThat(dto.getServiceName(), equalTo(SERVICE_NAMES.get(1)));
                assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.DEFAULT_ENCODING));
                assertThat(dto.getLastBackupDate().getTime(), equalTo(dates.get(1).getTime()));
                assertThat(dto.getLastUpdatedDate().getTime(), equalTo(dates.get(1).getTime()));
                assertThat(dto.getLastSucceededDate().getTime(), equalTo(dates.get(1).getTime()));
                assertThat(dto.getLastFailedDate(), nullValue());
                assertThat(dto.getFailureReason(), nullValue());
                assertThat(dto.getConfig(), equalTo(CONFIG_STRINGS.get(1)));
                assertThat(dto.getOperatingSystem(), equalTo(expectedOperatingSystems.get(1)));
                assertThat(dto.getBackupStatus(), equalTo(DeviceConfigStatus.SUCCESS.name().toLowerCase(Locale.ROOT)));
                assertThat(dto.getScheduledInterval().get(SERVICE_NAMES.get(1)),
                    equalTo(EXPECTED_CRON_SCHEDULE_DESCRIPTIONS.get(1)));
                assertThat(dto.getNextScheduledBackupDate().after(currentDate), is(true));
            });
        });
    }

    @Test
    public void testGetLatestDeviceConfigsSorted() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            IntStream.range(0, RECORD_COUNT).forEach(idx -> {
                deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                    SERVICE_NAMES.get(idx), dates.get(idx), CONFIG_BYTES.get(idx)));
            });

            final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(null, null, "lastUpdated", "asc", null, null, false);
            final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
            assertThat(responseList.size(), equalTo(RECORD_COUNT));

            // sort by devicename desc
            {
                final String[] expectedSortedItems = responseList.stream()
                    .map(DeviceConfigDTO::getDeviceName).sorted(Comparator.reverseOrder()).toArray(String[]::new);

                final var sortedResponse = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(null, null, "deviceName", "desc", null, null, false);
                final List<DeviceConfigDTO> sortedResponseList = (List<DeviceConfigDTO>) sortedResponse.getEntity();
                final String[] actualSortedItems = sortedResponseList.stream()
                    .map(DeviceConfigDTO::getDeviceName).toArray(String[]::new);

                assertThat(sortedResponseList.size(), equalTo(responseList.size()));
                assertArrayEquals(expectedSortedItems, actualSortedItems);
            }

            // sort by location
            {
                final String[] expectedSortedItems = responseList.stream()
                    .map(DeviceConfigDTO::getLocation).sorted().toArray(String[]::new);

                final var sortedResponse = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(null, null, "location", "asc", null, null, false);
                final List<DeviceConfigDTO> sortedResponseList = (List<DeviceConfigDTO>) sortedResponse.getEntity();
                final String[] actualSortedItems = sortedResponseList.stream()
                    .map(DeviceConfigDTO::getLocation).toArray(String[]::new);

                assertThat(sortedResponseList.size(), equalTo(responseList.size()));
                assertArrayEquals(expectedSortedItems, actualSortedItems);
            }
        });
    }

    @Test
    public void testGetLatestDeviceConfigsSortedByStatus() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, RECORD_COUNT);

            // this must be RECORD_COUNT in size
            final List<DeviceConfigStatus> statuses = List.of(DeviceConfigStatus.SUCCESS, DeviceConfigStatus.FAILED,
                DeviceConfigStatus.NONE, DeviceConfigStatus.SUCCESS, DeviceConfigStatus.FAILED);

            IntStream.range(0, RECORD_COUNT).forEach(idx -> {
                DeviceConfig dc = createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                        SERVICE_NAMES.get(idx), dates.get(idx), CONFIG_BYTES.get(idx));
                dc.setStatus(statuses.get(idx));
                deviceConfigDao.saveOrUpdate(dc);
            });

            final var response = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(null, null, "lastUpdated", "asc", null, null, false);
            final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
            assertThat(responseList.size(), equalTo(RECORD_COUNT));

            final String[] expectedSortedItems = responseList.stream()
                .map(DeviceConfigDTO::getBackupStatus).sorted().toArray(String[]::new);

            final var sortedResponse = deviceConfigRestService.getLatestDeviceConfigsForDeviceAndConfigType(null, null, "status", "asc", null, null, false);
            final List<DeviceConfigDTO> sortedResponseList = (List<DeviceConfigDTO>) sortedResponse.getEntity();
            final String[] actualSortedItems = sortedResponseList.stream()
                .map(DeviceConfigDTO::getBackupStatus).toArray(String[]::new);

            assertThat(sortedResponseList.size(), equalTo(responseList.size()));
            assertArrayEquals(expectedSortedItems, actualSortedItems);
        });
    }

    @Test
    public void testGetEmptyDeviceConfigs() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            List<Date> dates = getTestDates(new Date(), RECORD_COUNT);

            IntStream.range(0, RECORD_COUNT).forEach(idx -> {
                deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(idx), CONFIG_TYPES.get(idx),
                    SERVICE_NAMES.get(idx), dates.get(idx), CONFIG_BYTES.get(idx)));
            });

            final int nonExistingIpInterfaceId = ipInterfaces.stream().mapToInt(OnmsIpInterface::getId).max().orElse(9999) + 1;

            final var response = deviceConfigRestService.getDeviceConfigsByInterface(nonExistingIpInterfaceId, null);
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(false));
            assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
            assertThat(response.getHeaders().containsKey("Content-Range"), is(false));
        });
    }

    @Test
    public void testGetDeviceConfigsWithBinaryConfig() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            List<Date> dates = getTestDates(new Date(), RECORD_COUNT);

            final byte[] configBytes = new byte[] { 0, 1, 2, 3, 11, 25, 127 };
            final String expectedConfig = "000102030B197F";
            DeviceConfig dc = createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0),
                SERVICE_NAMES.get(0), dates.get(0), configBytes);
            dc.setEncoding(DefaultDeviceConfigRestService.BINARY_ENCODING);
            deviceConfigDao.saveOrUpdate(dc);

            var response = deviceConfigRestService.getDeviceConfig(dc.getId());
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(true));

            DeviceConfigDTO dto = (DeviceConfigDTO) response.getEntity();
            assertThat(dc.getId(), equalTo(dto.getId()));
            assertThat(CONFIG_TYPES.get(0).equalsIgnoreCase(dto.getConfigType()), is(true));
            assertThat(dto.getConfigName(), equalTo(CONFIG_NAMES.get(0)));
            assertThat(dto.getServiceName(), equalTo(SERVICE_NAMES.get(0)));
            assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.BINARY_ENCODING));
            assertThat(dto.getConfig(), equalTo(expectedConfig));
        });
    }

    @Test
    public void testGetDeviceConfigsByInterface() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            List<Date> dates = getTestDates(new Date(), RECORD_COUNT);

            final var dc0 = createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0),
                SERVICE_NAMES.get(0), dates.get(0), CONFIG_BYTES.get(0));
            final var dc1a = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1),
                SERVICE_NAMES.get(1), dates.get(1), CONFIG_BYTES.get(1));
            final var dc1b = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(0),
                SERVICE_NAMES.get(0),
                Date.from(dates.get(1).toInstant().minusSeconds(1)), "older".getBytes(StandardCharsets.UTF_8));
            final var dc2 = createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2),
                SERVICE_NAMES.get(2), dates.get(2), CONFIG_BYTES.get(2));
            final var dc3 = createDeviceConfig(ipInterfaces.get(3), CONFIG_TYPES.get(3),
                    SERVICE_NAMES.get(3), dates.get(3), CONFIG_BYTES.get(3));
            final var dc4 = createDeviceConfig(ipInterfaces.get(4), CONFIG_TYPES.get(4),
                    SERVICE_NAMES.get(4), dates.get(4), CONFIG_BYTES.get(4));

            List.of(dc0, dc1a, dc1b, dc2, dc3, dc4).forEach(deviceConfigDao::saveOrUpdate);

            final var expectedDeviceConfigs = List.of(List.of(dc0), List.of(dc1a, dc1b), List.of(dc2), List.of(dc3), List.of(dc4));

            IntStream.range(0, RECORD_COUNT).forEach(i -> {
                final var response = deviceConfigRestService.getDeviceConfigsByInterface(ipInterfaces.get(i).getId(), null);
                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final var responseHeaders = response.getHeaders();
                assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
                assertThat(responseHeaders.containsKey("Content-Range"), is(true));

                final int expectedCount = expectedDeviceConfigs.get(i).size();
                final String contentRange = getHeaderAsString(responseHeaders, "Content-Range");
                final String expectedContentRange = getContentRange(0, expectedCount - 1, expectedCount);
                assertThat(contentRange, equalTo(expectedContentRange));

                final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(expectedCount));

                IntStream.range(0, expectedCount).forEach(j -> {
                    DeviceConfigDTO dto = responseList.get(j);
                    DeviceConfig expectedDc = expectedDeviceConfigs.get(i).get(j);

                    assertThat(dto.getId(), equalTo(expectedDc.getId()));
                    assertThat(dto.getLastUpdatedDate(), equalTo(expectedDc.getLastUpdated()));
                    assertThat(dto.getIpInterfaceId(), equalTo(expectedDc.getIpInterface().getId()));
                    assertThat(dto.getDeviceName(), equalTo(expectedDc.getIpInterface().getNode().getLabel()));
                });
            });

            // Test filtering by config type
            IntStream.range(0, 2).forEach(i -> {
                final var response = deviceConfigRestService.getDeviceConfigsByInterface(ipInterfaces.get(1).getId(), CONFIG_TYPES.get(i));

                assertThat(response, notNullValue());
                assertThat(response.hasEntity(), is(true));

                final List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
                assertThat(responseList.size(), equalTo(1));

                DeviceConfigDTO dto = responseList.get(0);
                assertThat(dto.getIpInterfaceId(), equalTo(ipInterfaces.get(1).getId()));
                assertThat(dto.getConfigType(), equalTo(CONFIG_TYPES.get(i)));
            });
        });
    }

    @Test
    public void testDownloadNoDeviceConfig() {
        List<String> idParams = new ArrayList<>();
        idParams.add(null);
        idParams.add("");

        idParams.forEach(id -> {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            assertThat(response, notNullValue());
            assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
        });
    }

    @Test
    public void testDownloadInvalidRequest() {
        final List<String> idParams = List.of("abc", "a,b,c", ",,,,,0a", ";", "123,,", "123,,,456", ",123");

        idParams.forEach(id -> {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            assertThat(response, notNullValue());
            assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        });
    }

    @Test
    @Transactional
    public void testDownloadSingleDeviceConfig() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

        // Add DeviceConfig entries mapped to ipInterfaces and services
        List<Date> dates = getTestDates(new Date(), RECORD_COUNT);

        deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0),
            SERVICE_NAMES.get(0), dates.get(0), CONFIG_BYTES.get(0)));
        DeviceConfig dc = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1),
            SERVICE_NAMES.get(1), dates.get(1), CONFIG_BYTES.get(1));
        deviceConfigDao.saveOrUpdate(dc);
        deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2),
            SERVICE_NAMES.get(2), dates.get(2), CONFIG_BYTES.get(2)));

        final var response = deviceConfigRestService.downloadDeviceConfig(dc.getId().toString());
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        var responseHeaders = response.getHeaders();
        assertThat(responseHeaders.containsKey("Content-Type"), is(true));
        assertThat(responseHeaders.containsKey("Content-Disposition"), is(true));

        assertThat(getHeaderAsString(responseHeaders, "Content-Type"),
            equalTo("text/plain;charset=" + DefaultDeviceConfigRestService.DEFAULT_ENCODING));

        String expectedFileName = DefaultDeviceConfigRestService.createDownloadFileName(
            "dcb-2", "192.168.3.2", CONFIG_TYPES.get(1), dc.getCreatedTime());
        String expectedContentDisposition = "attachment; filename=" + expectedFileName;
        String actualContentDisposition = getHeaderAsString(responseHeaders, "Content-Disposition");
        assertThat(actualContentDisposition, equalTo(expectedContentDisposition));

        Object responseObj = response.getEntity();
        byte[] responseBytes = (byte[]) response.getEntity();
        assertArrayEquals(dc.getConfig(), responseBytes);
    }

    @Test
    @Transactional
    public void testDownloadMultipleDeviceConfigs() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

        final List<Date> dates = getTestDates(new Date(), RECORD_COUNT);

        var dcs = IntStream.range(0, RECORD_COUNT).boxed().map(i ->
            createDeviceConfig(ipInterfaces.get(i), CONFIG_TYPES.get(i),
                SERVICE_NAMES.get(i), dates.get(i), CONFIG_BYTES.get(i))
        ).collect(Collectors.toList());

        dcs.forEach(deviceConfigDao::saveOrUpdate);

        List<Long> ids = dcs.stream().map(DeviceConfig::getId).collect(Collectors.toList());
        String idParam = String.join(",", ids.stream().map(id -> id.toString()).collect(Collectors.toList()));

        final var response = deviceConfigRestService.downloadDeviceConfig(idParam);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        final var responseHeaders = response.getHeaders();
        assertThat(responseHeaders.containsKey("Content-Type"), is(true));
        assertThat(responseHeaders.containsKey("Content-Disposition"), is(true));
        assertThat(getHeaderAsString(responseHeaders, "Content-Type"), equalTo("application/gzip"));

        String actualContentDisposition = getHeaderAsString(responseHeaders, "Content-Disposition");
        assertThat(actualContentDisposition, startsWith("attachment; filename="));
        assertThat(actualContentDisposition, endsWith(".tar.gz"));

        var pattern = Pattern.compile(".*?filename=(.+)$");
        var matcher = pattern.matcher(actualContentDisposition);

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.groupCount(), equalTo(1));
        final String actualFileName = matcher.group(1);
        assertThat(actualFileName, startsWith("device-configs-"));

        assertThat(response.getEntity(), notNullValue());
        assertThat(response.getEntity(), instanceOf(byte[].class));

        Map<String,byte[]> fileMap = null;

        try {
            byte[] responseBytes = (byte[]) response.getEntity();
            fileMap = CompressionUtils.unTarGzipMultipleFiles(responseBytes);
        } catch (IOException e) {
            Assert.fail("IOException calling CompressionUtils.unTarGzipMultipleFiles");
        }

        assertThat(fileMap, notNullValue());
        assertThat(fileMap.size(), equalTo(RECORD_COUNT));
        final Map<String,byte[]> finalFileMap = fileMap;

        final Set<String> fileKeys = fileMap.keySet();
        List<String> sortedFileNames = fileMap.keySet().stream().sorted().collect(Collectors.toList());
        assertThat(sortedFileNames.size(), equalTo(RECORD_COUNT));

        IntStream.range(0, RECORD_COUNT).forEach(idx -> {
            final String fileName = sortedFileNames.get(idx);
            assertThat(fileName, startsWith(dcs.get(idx).getIpInterface().getNode().getLabel()));
            assertArrayEquals(CONFIG_BYTES.get(idx), finalFileMap.get(fileName));
        });
    }
    private List<OnmsIpInterface> populateDeviceConfigServiceInfo() {
        return populateDeviceConfigServiceInfo(false);
    }

    private List<OnmsIpInterface> populateDeviceConfigServiceInfo(boolean useSubstituteServiceNames) {
        final var result = this.sessionUtils.withTransaction(() -> {
            List<OnmsIpInterface> ipInterfaces = new ArrayList<>();
            NetworkBuilder builder = new NetworkBuilder();

            IntStream.range(0, RECORD_COUNT).forEach(i -> {
                final String serviceNameToUse = useSubstituteServiceNames ? SUBSTITUTE_SERVICE_NAMES.get(i) : SERVICE_NAMES.get(i);

                builder.addNode(NODE_NAMES.get(i)).setForeignSource("imported:").setForeignId(FOREIGN_IDS.get(i)).setType(OnmsNode.NodeType.ACTIVE);
                builder.getCurrentNode().setOperatingSystem(OPERATING_SYSTEMS.get(i));
                builder.getCurrentNode().setLocation(addOrGetLocation(LOCATIONS.get(i)));

                builder.addInterface(IP_ADDRESSES.get(i)).setIsManaged("M").setIsSnmpPrimary("P");
                builder.addService(addOrGetServiceType(serviceNameToUse));
                builder.setServiceMetaDataEntry("requisition", "dcb:schedule", CRON_SCHEDULES.get(i));
                nodeDao.saveOrUpdate(builder.getCurrentNode());

                OnmsIpInterface ipInterface = builder.getCurrentNode().getIpInterfaceByIpAddress(IP_ADDRESSES.get(i));
                ipInterfaces.add(ipInterface);
            });

            nodeDao.flush();

            return ipInterfaces;
        });

        PollerConfigFactory.getInstance().rebuildPackageIpListMap();
        return result;
    }

    private OnmsMonitoringLocation addOrGetLocation(final String locationName) {
        OnmsMonitoringLocation location = monitoringLocationDao.get(locationName);

        if (location == null) {
            location = new OnmsMonitoringLocation(locationName, locationName);
            monitoringLocationDao.save(location);
            monitoringLocationDao.flush();
        }

        return location;
    }

    private OnmsServiceType addOrGetServiceType(final String serviceName) {
        OnmsServiceType serviceType = serviceTypeDao.findByName(serviceName);

        if (serviceType == null) {
            serviceType = new OnmsServiceType(serviceName);
            serviceTypeDao.save(serviceType);
            serviceTypeDao.flush();
        }

        return serviceType;
    }

    private void assertDtoWith(DeviceConfigDTO dto, Integer ipInterfaceId, String configType,
                               Date lastBackup, Date lastUpdated, Date lastSucceeded, Date lastFailed, DeviceConfigStatus status) {
        assertNotNull(dto);
        assertThat(dto.getIpInterfaceId(), equalTo(ipInterfaceId));
        assertThat(dto.getConfigType(), equalTo(configType));
        assertThat(dto.getLastBackupDate(), equalTo(lastBackup));
        assertThat(dto.getLastUpdatedDate(), equalTo(lastUpdated));
        assertThat(dto.getLastSucceededDate(), equalTo(lastSucceeded));
        assertThat(dto.getLastFailedDate(), equalTo(lastFailed));
        assertThat(dto.getBackupStatus(), equalTo(status.name().toLowerCase(Locale.ROOT)));
    }

    private static DeviceConfig createDeviceConfig(OnmsIpInterface ipInterface1, String configType,
        String serviceName, Date date, byte[] config) {
        var dc = new DeviceConfig();
        dc.setConfig(config);
        dc.setLastUpdated(date);
        dc.setLastSucceeded(date);
        dc.setCreatedTime(date);
        dc.setEncoding(DefaultDeviceConfigRestService.DEFAULT_ENCODING);
        dc.setIpInterface(ipInterface1);
        dc.setServiceName(serviceName);
        dc.setConfigType(configType);
        dc.setStatus(DeviceConfigStatus.SUCCESS);

        return dc;
    }

    private static List<Date> getTestDates(Date currentDate, int count) {
        return IntStream.range(1, count + 1).boxed()
            .sorted(Collections.reverseOrder())
            .map(seconds -> Date.from(currentDate.toInstant().minusSeconds(seconds)))
            .collect(Collectors.toList());
    }

    private static String getHeaderAsString(MultivaluedMap<String,Object> headers, String key) {
        return headers.getFirst(key).toString();
    }

    private static String getContentRange(int startIndex, int endIndex, int count) {
        return String.format("items %d-%d/%d", startIndex, endIndex, count);
    }
}
