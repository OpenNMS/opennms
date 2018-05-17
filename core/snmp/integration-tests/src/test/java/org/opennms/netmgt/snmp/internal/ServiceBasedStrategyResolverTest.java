/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.snmp.ClassBasedStrategyResolver;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.StrategyResolver;

public class ServiceBasedStrategyResolverTest {
    @After
    public void tearDown() {
        final Properties sysProps = System.getProperties();
        sysProps.remove("org.opennms.snmp.strategyClass");
    }

    @Test
    public void verifyThatClassBasedStrategyIsDefault() {
        StrategyResolver currentStrategyResolver = SnmpUtils.getStrategyResolver();
        assertTrue("ServiceBasedStrategyResolver should not be used by default.",
                !(currentStrategyResolver instanceof ServiceBasedStrategyResolver));

        ServiceBasedStrategyResolver.register();
        currentStrategyResolver = SnmpUtils.getStrategyResolver();
        assertTrue("ClassBasedStrategyResolver should be default when it is instantiable",
                currentStrategyResolver instanceof ClassBasedStrategyResolver);

        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertEquals("Should fall back to using the ClassBasedStrategyResolver when no strategies are registered",
                SnmpUtils.getStrategyClassName(), strategy.getClass().getCanonicalName());

    }

    /**
     * Validates all of the code paths in {@link ServiceBasedStrategyResolver#getStrategy}.
     */
    @Test
    public void canResolveAndFallback() {
          // Now create and bind a new mock strategy
        ServiceBasedStrategyResolver serviceBasedResolver = new ServiceBasedStrategyResolver();
        ServiceBasedStrategyResolver.register();
        SnmpStrategy strategy = serviceBasedResolver.getStrategy();
        SnmpStrategy mockStrategy = mock(SnmpStrategy.class);
        Map<String, String> props = new HashMap<>();
        props.put("implementation", "org.opennms.mock.MyMockStrategy");
        serviceBasedResolver.onBind(mockStrategy, props);

        try {
            // Grab the mock, as a fall-back
            strategy = serviceBasedResolver.getStrategy();
            assertEquals("Should fall back to using the first regitered strategy when the requested class is not registered"
                         , mockStrategy, strategy);

            // Now use the mock explicitly
            System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.mock.MyMockStrategy");
            strategy = serviceBasedResolver.getStrategy();
            assertEquals(mockStrategy, strategy);

            // Unbind the mock
            serviceBasedResolver.onUnbind(mockStrategy, props);

            // Grabbing the strategy should fail now, we fall-back to the ClassBasedResolver
            // but it won't be able to instantiate our mock strategy
            try {
                serviceBasedResolver.getStrategy();
                fail("Should not be able to instantiate org.opennms.mock.MyMockStrategy");
            } catch (RuntimeException e) { }
        } finally {
            serviceBasedResolver.onUnbind(mockStrategy, props);
        }
    }
}
