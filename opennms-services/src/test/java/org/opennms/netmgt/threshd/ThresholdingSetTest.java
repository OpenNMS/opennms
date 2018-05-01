/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.rrd.RrdRepository;

public class ThresholdingSetTest {

    @Test
    public void testBadThresholdingConfigInitialize() throws IOException {
        System.setProperty("opennms.home", getClass().getResource("testBadThresholdingConfig").getFile());
        PollOutagesConfigFactory.init();
        final ThresholdingSet set = new ThresholdingSet(1, "127.0.0.1", "ICMP", new RrdRepository());

        // there is no information about the node, so it should say it does not have an outage
        assertFalse(set.isNodeInOutage());

        // the config is empty
        assertFalse(set.hasThresholds());
    }

    @Test
    public void testBadThresholdingConfigReinitialize() throws IOException {
        System.setProperty("opennms.home", getClass().getResource("testBadThresholdingConfig").getFile());
        PollOutagesConfigFactory.init();
        final ThresholdingSet set = new ThresholdingSet(1, "127.0.0.1", "ICMP", new RrdRepository());
        set.reinitialize();
    }

}
