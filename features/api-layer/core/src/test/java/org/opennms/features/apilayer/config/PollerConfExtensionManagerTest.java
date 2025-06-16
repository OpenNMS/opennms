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
package org.opennms.features.apilayer.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.opennms.integration.api.v1.config.poller.AddressRange;
import org.opennms.integration.api.v1.config.poller.Downtime;
import org.opennms.integration.api.v1.config.poller.Monitor;
import org.opennms.integration.api.v1.config.poller.Package;
import org.opennms.integration.api.v1.config.poller.Parameter;
import org.opennms.integration.api.v1.config.poller.PollerConfigurationExtension;
import org.opennms.integration.api.v1.config.poller.Service;
import org.opennms.integration.api.xml.ClasspathPollerConfigurationLoader;
import org.opennms.netmgt.config.PollerConfigManager;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.events.api.EventForwarder;

public class PollerConfExtensionManagerTest {

    static PollerConfigurationExtension pollerConfiguration1;
    static PollerConfigurationExtension pollerConfiguration2;

    @Before
    public void init() {
        pollerConfiguration1 = new ClasspathPollerConfigurationLoader(PollerConfExtensionManagerTest.class, "poller",
                "poller-configuration1.xml").getPollerConfiguration();
        pollerConfiguration2 = new ClasspathPollerConfigurationLoader(PollerConfExtensionManagerTest.class, "poller",
                "poller-configuration2.xml").getPollerConfiguration();
    }

    @Test
    public void callbackCalled() {
        PollerConfigManager objectToNotify = mock(PollerConfigManager.class);
        PollerConfExtensionManager instance = new PollerConfExtensionManager(objectToNotify, mock(EventForwarder.class));
        instance.onBind(pollerConfiguration1, null);
        verify(objectToNotify, times(1)).setExternalData(any(), any());
        instance.onBind(pollerConfiguration2, null);
        verify(objectToNotify, times(2)).setExternalData(any(), any());
    }

    @Test
    public void dataMatches() {
        PollerConfigManager objectToNotify = mock(PollerConfigManager.class);
        PollerConfExtensionManager instance = new PollerConfExtensionManager(objectToNotify, mock(EventForwarder.class));
        instance.onBind(pollerConfiguration1, null);
        instance.onBind(pollerConfiguration2, null);

        final PollerConfExtensionManager.PollerConfigurationPart dataToTest = instance.getObject();

        assertEquals(pollerConfiguration1.getPackages().size() + pollerConfiguration2.getPackages().size(),
                dataToTest.getPackages().size());
        assertEquals(pollerConfiguration1.getMonitors().size() + pollerConfiguration2.getMonitors().size(),
                dataToTest.getMonitors().size());

        //first Package of pollerConfiguration1 matches to the first of dataToTest
        assertMatches(
                pollerConfiguration1.getPackages().get(0),
                dataToTest.getPackages().get(0)
        );

        //lasts Package of pollerConfiguration2 matches to the last of dataToTest
        assertMatches(
                pollerConfiguration2.getPackages().get(pollerConfiguration2.getPackages().size() - 1),
                dataToTest.getPackages().get(dataToTest.getPackages().size() - 1)
        );

        //first Monitor of pollerConfiguration1 matches to the first of dataToTest
        assertMatches(
                pollerConfiguration1.getMonitors().get(0),
                dataToTest.getMonitors().get(0)
        );

        //lasts Monitor of pollerConfiguration2 matches to the last of dataToTest
        assertMatches(
                pollerConfiguration2.getMonitors().get(pollerConfiguration2.getMonitors().size() - 1),
                dataToTest.getMonitors().get(dataToTest.getMonitors().size() - 1)
        );
    }

    private static void assertMatches(Monitor source, org.opennms.netmgt.config.poller.Monitor result) {
        assertEquals(source.getClassName(), result.getClassName());
        assertEquals(source.getService(), result.getService());
        assertEquals(source.getParameters().size(), result.getParameters().size());

        for (int i = 0; i < source.getParameters().size(); i++) {
            assertEquals(source.getParameters().get(i).getValue(), result.getParameters().get(i).getValue());
            assertEquals(source.getParameters().get(i).getKey(), result.getParameters().get(i).getKey());
            assertNull(result.getParameters().get(i).getAnyObject());
        }
    }

    private static void assertMatches(Package source, org.opennms.netmgt.config.poller.Package result) {
        assertEquals(source.getName(), result.getName());
        assertEquals(source.getFilter(), result.getFilter().getContent());
        assertEquals(source.getDowntimes().size(), result.getDowntimes().size());
        assertEquals(source.getOutageCalendars().size(), result.getOutageCalendars().size());
        assertEquals(source.getRrd().getStep(), result.getRrd().getStep().intValue());
        assertEquals(source.getRrd().getRras(), result.getRrd().getRras());
        assertEquals(source.getServices().size(), result.getServices().size());
        assertEquals(source.getExcludeRanges().size(), result.getExcludeRanges().size());
        assertEquals(source.getIncludeRanges().size(), result.getIncludeRanges().size());
        assertEquals(source.getSpecifics(), result.getSpecifics());

        final Downtime firstSourceDowntime = source.getDowntimes().get(0);
        final org.opennms.netmgt.config.poller.Downtime firstResultDowntime = result.getDowntimes().get(0);
        assertEquals(firstSourceDowntime.getDelete().map(it -> it.toString().toLowerCase()).orElse(null),
                firstResultDowntime.getDelete());
        assertEquals(firstSourceDowntime.getBegin().getSeconds(), firstResultDowntime.getBegin().longValue());
        assertEquals(firstSourceDowntime.getEnd().map(Duration::getSeconds).orElse(null), firstResultDowntime.getEnd());
        assertEquals(firstSourceDowntime.getInterval().orElse(null), firstResultDowntime.getInterval());

        final Service firstSourceService = source.getServices().get(0);
        final org.opennms.netmgt.config.poller.Service firstResultService = result.getServices().get(0);
        assertEquals(firstSourceService.getInterval(), firstResultService.getInterval().longValue());
        assertEquals(firstSourceService.getName(), firstResultService.getName());
        assertEquals(firstSourceService.getParameters().size(), firstResultService.getParameters().size());
        assertEquals(firstSourceService.getPattern().orElse(null), firstResultService.getPattern());

        if (firstSourceService.getParameters().size() > 0) {
            final Parameter firstSourceParameter = firstSourceService.getParameters().get(0);
            final org.opennms.netmgt.config.poller.Parameter firstResultParameter =
                    firstResultService.getParameters().get(0);
            assertEquals(firstSourceParameter.getKey(), firstResultParameter.getKey());
            assertEquals(firstSourceParameter.getValue(), firstResultParameter.getValue());
        }

        if (source.getExcludeRanges().size() > 0) {
            final AddressRange firstSourceExcludeRange = source.getExcludeRanges().get(0);
            final ExcludeRange firstResultExcludeRange = result.getExcludeRanges().get(0);
            assertEquals(firstSourceExcludeRange.getBegin(), firstResultExcludeRange.getBegin());
            assertEquals(firstSourceExcludeRange.getEnd(), firstResultExcludeRange.getEnd());
        }
    }

}
