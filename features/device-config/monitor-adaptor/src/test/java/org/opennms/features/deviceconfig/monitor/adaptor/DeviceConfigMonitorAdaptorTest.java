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
package org.opennms.features.deviceconfig.monitor.adaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigMonitorAdaptorTest {

    @Mock private DeviceConfigDao deviceConfigDao;
    @Mock private IpInterfaceDao ipInterfaceDao;
    @Mock private EventForwarder eventForwarder;
    @Mock private UsageAnalyticDao usageAnalyticDao;
    @Mock private NodeDao nodeDao;
    @Mock private SessionUtils sessionUtils;

    @InjectMocks
    private DeviceConfigMonitorAdaptor adaptor;

    private static final int    NODE_ID      = 1;
    private static final String IP_ADDR      = "192.168.1.1";
    private static final String SERVICE_NAME = "DeviceConfig-default";

    private MonitoredService  service;
    private OnmsIpInterface   ipInterface;

    @Before
    public void setUp() {
        service = mock(MonitoredService.class);
        when(service.getNodeId()).thenReturn(NODE_ID);
        when(service.getIpAddr()).thenReturn(IP_ADDR);
        when(service.getSvcName()).thenReturn(SERVICE_NAME);

        ipInterface = mock(OnmsIpInterface.class);
        when(ipInterfaceDao.findByNodeIdAndIpAddress(NODE_ID, IP_ADDR)).thenReturn(ipInterface);

        when(deviceConfigDao.updateDeviceConfigContent(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(1L));

        // lenient: not called when latestConfig is empty (noConfigChangedEventOnFirstBackup)
        Mockito.lenient()
               .when(deviceConfigDao.findStaleConfigs(any(), any(), any(), any()))
               .thenReturn(Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // configChanged event emission
    // -------------------------------------------------------------------------

    @Test
    public void configChangedEventFiredWhenContentDiffers() {
        stubPrevious("old config\n".getBytes(StandardCharsets.UTF_8));
        adaptor.handlePollResult(service, params(false),
                pollStatus("new config\n".getBytes(StandardCharsets.UTF_8)));

        List<String> ueis = capturedUeis(3);
        assertThat(ueis.get(0), is(EventConstants.DEVICE_CONFIG_BACKUP_STARTED_UEI));
        assertThat(ueis.get(1), is(EventConstants.DEVICE_CONFIG_BACKUP_SUCCEEDED_UEI));
        assertThat(ueis.get(2), is(EventConstants.DEVICE_CONFIG_CHANGED_UEI));
    }

    @Test
    public void noConfigChangedEventWhenContentUnchanged() {
        byte[] config = "same config\n".getBytes(StandardCharsets.UTF_8);
        stubPrevious(config);
        adaptor.handlePollResult(service, params(false), pollStatus(config));

        List<String> ueis = capturedUeis(2);
        assertThat(ueis, not(hasItem(EventConstants.DEVICE_CONFIG_CHANGED_UEI)));
    }

    @Test
    public void noConfigChangedEventOnFirstBackup() {
        // No stored config exists yet: previousContent is null, so configChanged cannot be true
        when(deviceConfigDao.getLatestConfigForInterface(any(), any())).thenReturn(Optional.empty());
        adaptor.handlePollResult(service, params(false),
                pollStatus("first config\n".getBytes(StandardCharsets.UTF_8)));

        List<String> ueis = capturedUeis(2);
        assertThat(ueis, not(hasItem(EventConstants.DEVICE_CONFIG_CHANGED_UEI)));
    }

    @Test
    public void noConfigChangedEventWhenOnlyCommentsDifferWithIgnoreComments() {
        // Configs differ only in a // timestamp comment; with ignore-comments=true they are equal
        stubPrevious("hostname router\n// timestamp: 2024-01-01\n".getBytes(StandardCharsets.UTF_8));
        adaptor.handlePollResult(service, params(true),
                pollStatus("hostname router\n// timestamp: 2024-01-02\n".getBytes(StandardCharsets.UTF_8)));

        List<String> ueis = capturedUeis(2);
        assertThat(ueis, not(hasItem(EventConstants.DEVICE_CONFIG_CHANGED_UEI)));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void stubPrevious(byte[] content) {
        DeviceConfig prev = mock(DeviceConfig.class);
        when(prev.getConfig()).thenReturn(content);
        when(deviceConfigDao.getLatestConfigForInterface(any(), any())).thenReturn(Optional.of(prev));
    }

    private PollStatus pollStatus(byte[] content) {
        var dc = new org.opennms.netmgt.poller.DeviceConfig(content, "config.txt");
        PollStatus ps = mock(PollStatus.class);
        when(ps.getDeviceConfig()).thenReturn(dc);
        when(ps.isUp()).thenReturn(true);
        return ps;
    }

    private Map<String, Object> params(boolean ignoreComments) {
        Map<String, Object> m = new HashMap<>();
        m.put("encoding", "UTF-8");
        m.put(DeviceConfigConstants.COMPARE_IGNORE_COMMENTS, String.valueOf(ignoreComments));
        return m;
    }

    private List<String> capturedUeis(int expectedCount) {
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder, times(expectedCount)).sendNowSync(captor.capture());
        return captor.getAllValues().stream().map(Event::getUei).collect(Collectors.toList());
    }
}
