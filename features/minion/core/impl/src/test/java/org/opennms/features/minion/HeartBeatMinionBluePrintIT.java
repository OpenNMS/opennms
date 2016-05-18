/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.minion;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.Component;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.seda.SedaComponent;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.minion.core.api.MinionIdentityDTO;
import org.opennms.minion.core.impl.MinionIdentityImpl;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/emptyContext.xml" })
public class HeartBeatMinionBluePrintIT extends CamelBlueprintTest {

    public static final String MINION_ID = "0001";
    public static final String MINION_LOCATION = "localhost";

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "file:blueprint-heartbeat.xml";
    }

    /**
     * Register a mock OSGi {@link SchedulerService} so that we can make sure
     * that the scheduler whiteboard is working properly.
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void addServicesOnStartup(
            Map<String, KeyValueHolder<Object, Dictionary>> services) {
        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(),
                     new KeyValueHolder<Object, Dictionary>(new SedaComponent(),
                                                            props));
        services.put(MinionIdentity.class.getName(),
                     new KeyValueHolder(new MinionIdentityImpl(MINION_LOCATION,
                                                               MINION_ID),
                                        null));

    }

    @Test
    public void testHeartBeat() throws Exception {

        MockEndpoint heartBeatqueue = getMockEndpoint("mock:queuingservice:heartBeat",
                                                      false);
        heartBeatqueue.setExpectedMessageCount(1);

        assertMockEndpointsSatisfied();
        String minionOutput = heartBeatqueue.getReceivedExchanges().get(0).getIn().getBody(String.class);
        MinionIdentityDTO minion = JaxbUtils.unmarshal(MinionIdentityDTO.class,
                                                       minionOutput);
        assertEquals(MINION_LOCATION, minion.getLocation());
        assertEquals(MINION_ID, minion.getId());
    }

}
