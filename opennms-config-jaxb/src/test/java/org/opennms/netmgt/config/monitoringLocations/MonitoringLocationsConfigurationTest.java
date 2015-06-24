/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.monitoringLocations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.custommonkey.xmlunit.Difference;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringLocationsConfigurationTest extends XmlTestNoCastor<MonitoringLocationsConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationsConfigurationTest.class);

    public MonitoringLocationsConfigurationTest(final MonitoringLocationsConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Before
    public void setUp() {
        super.setUp();
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + getClass().getName(), "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbUtils", "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbClassObjectAdapter", "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.EmptyListAdapter", "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.XmlSchemaFilter", "TRACE");
        MockLogAppender.setupLogging(true, props);
    }

    @Override
    protected boolean ignoreNamespace(final String namespace) {
        LOG.debug("ignoreNamespace({})", namespace);
        return "http://xmlns.opennms.org/xsd/config/monitoring-locations".equals(namespace);
    }

    /*
     * 2014-01-23 11:15:32,958 WARN [main] org.opennms.core.test.xml.XmlTest - Found difference: presence of child nodes to be: Expected presence of child nodes to be 'true' but was 'false' - comparing <location-def...> at /monitoring-locations-configuration[1]/locations[1]/location-def[4] to <location-def...> at /monitoring-locations-configuration[1]/locations[1]/location-def[4]
     * 2014-01-23 11:15:32,958 WARN [main] org.opennms.core.test.xml.XmlTest - Found difference: number of child nodes: Expected number of child nodes '1' but was '0' - comparing <location-def...> at /monitoring-locations-configuration[1]/locations[1]/location-def[4] to <location-def...> at /monitoring-locations-configuration[1]/locations[1]/location-def[4]
     * 2014-01-23 11:15:32,958 WARN [main] org.opennms.core.test.xml.XmlTest - Found difference: presence of child node: Expected presence of child node 'ns1:tags' but was 'null' - comparing <ns1:tags...> at /monitoring-locations-configuration[1]/locations[1]/location-def[4]/tags[1] to  at null
     */
    @Override
    protected boolean ignoreDifference(final Difference d) {
        // we don't care if an empty <tags /> is added or not
        final String xpathLocation = d.getControlNodeDetail().getXpathLocation();
        LOG.debug("xpath location = {}", xpathLocation);
        if (xpathLocation != null && xpathLocation.contains("/monitoring-locations-configuration[1]/locations[1]/location-def[4]")) {
            return true;
        }
        return super.ignoreDifference(d);
    }

    protected String getSchemaFile() {
        return "target/classes/xsds/monitoring-locations.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getMinimalMonitoringLocationsConfig(),
                    MonitoringLocationsConfigurationTest.class.getResource("simple-monitoring-locations.xml")
                },
                {
                    getDemoMonitoringLocationsConfig(),
                    MonitoringLocationsConfigurationTest.class.getResource("demo-monitoring-locations.xml")
                }
        });
    }

    private static MonitoringLocationsConfiguration getMinimalMonitoringLocationsConfig() {
        final MonitoringLocationsConfiguration config = new MonitoringLocationsConfiguration();

        final LocationDef def = new LocationDef();
        def.setLocationName("RDU");
        def.setMonitoringArea("raleigh");
        def.setPollingPackageName("raleigh");
        def.setGeolocation("35.7174,-79.1619");
        config.addLocation(def);

        return config;
    }

    private static MonitoringLocationsConfiguration getDemoMonitoringLocationsConfig() {
        final MonitoringLocationsConfiguration config = new MonitoringLocationsConfiguration();

        final LocationDef ma = new LocationDef("MA", "USA", "usa", null, "BOS", "42.363143,-71.0072436", 100l, "foo");
        config.addLocation(ma);

        final LocationDef nc = new LocationDef("NC", "USA", "usa", null, "220 Chatham Business Drive, Pittsboro, NC 27312", "35.71736,-79.161814", 100l, "bar", "baz");
        config.addLocation(nc);

        config.addLocation(new LocationDef("GA", "USA", "usa", null, "ATL", "33.639975,-84.444032", 100l, "baz"));
        config.addLocation("OH", "USA", "usa", null, "Columbus, OH", "39.9611755,-82.9987942", 100l);
        config.addLocation("MN", "USA", "usa", null, "MSP", "44.881234,-93.203111", 100l);
        config.addLocation("CO", "USA", "usa", null, "Vail, CO", "39.6402638,-106.3741955", 100l);
        config.addLocation("CA", "USA", "usa", null, "LAX", "33.9434916,-118.4089705", 100l);
        config.addLocation("TX", "USA", "usa", null, "DFW", "32.8961644,-97.0427084", 100l);
        config.addLocation("FL", "USA", "usa", null, "MIA", "25.7889689,-80.2264393", 100l);
        config.addLocation("MT", "USA", "usa", null, "FCA", "48.311389,-114.255", 100l);
        config.addLocation("AZ", "USA", "usa", null, "PHX", "33.4483771,-112.0740373", 100l);

        return config;
    }
}
