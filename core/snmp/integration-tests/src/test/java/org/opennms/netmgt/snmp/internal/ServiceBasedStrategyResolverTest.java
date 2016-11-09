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

import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.StrategyResolver;

public class ServiceBasedStrategyResolverTest {

    /**
     * Validates all of the code paths in {@link ServiceBasedStrategyResolver#getStrategy}.
     */
    @Test
    public void canResolveAndFallback() {
        StrategyResolver currentStrategyResolver = SnmpUtils.getStrategyResolver();
        assertTrue("ServiceBasedStrategyResolver should not be used by default.",
                !(currentStrategyResolver instanceof ServiceBasedStrategyResolver));

        ServiceBasedStrategyResolver.register();
        currentStrategyResolver = SnmpUtils.getStrategyResolver();
        assertTrue("Calling register() should set the strategy resolver.",
                currentStrategyResolver instanceof ServiceBasedStrategyResolver);

        final ServiceBasedStrategyResolver serviceBasedResolver = (ServiceBasedStrategyResolver)currentStrategyResolver;
        assertEquals("No services should be registered by default.",
                0, serviceBasedResolver.getStrategies().size());

        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertEquals("Should fall back to using the ClassBasedStrategyResolver when no strategies are registered",
                SnmpUtils.getStrategyClassName(), strategy.getClass().getCanonicalName());

        // Now create and bind a new mock strategy
        SnmpStrategy mockStrategy = mock(SnmpStrategy.class);
        Map<String, String> props = new HashMap<>();
        props.put("implementation", "org.opennms.mock.MyMockStrategy");
        serviceBasedResolver.onBind(mockStrategy, props);

        // Grab the mock, as a fall-back
        strategy = SnmpUtils.getStrategy();
        assertEquals("Should fall back to using the first regitered strategy when the requested class is not registered"
                , mockStrategy, strategy);

        // Now use the mock explicitly
        System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.mock.MyMockStrategy");
        strategy = SnmpUtils.getStrategy();
        assertEquals(mockStrategy, strategy);

        // Unbind the mock
        serviceBasedResolver.onUnbind(mockStrategy, props);

        // Grabbing the strategy should fail now, we fall-back to the ClassBasedResolver
        // but it won't be able to instantiate our mock strategy
        try {
            SnmpUtils.getStrategy();
            fail("Should not be able to instantiate org.opennms.mock.MyMockStrategy");
        } catch (RuntimeException e) { }
    }
}
