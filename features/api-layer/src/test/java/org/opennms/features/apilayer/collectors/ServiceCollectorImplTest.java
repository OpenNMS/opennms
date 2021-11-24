/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opennms.integration.api.v1.collectors.ServiceCollectorFactory;
import org.opennms.netmgt.rrd.RrdRepository;

public class ServiceCollectorImplTest {
    private ServiceCollectorFactory mockFactory;
    private ServiceCollectorImpl serviceCollector;
    private String baseDir = "/opt/opennms";
    private String[] rrsList = new String[]{
            "RRA:AVERAGE:0.5:1:2016",
            "RRA:AVERAGE:0.5:12:1488",
            "RRA:AVERAGE:0.5:288:366",
            "RRA:MAX:0.5:288:366",
            "RRA:MIN:0.5:288:366"
    };

    @Before
    public void setup(){
        System.setProperty("opennms.home", baseDir);
        mockFactory = mock(ServiceCollectorFactory.class);
        serviceCollector = new ServiceCollectorImpl(mockFactory);
    }

    @Test
    public void testGetRrdRepository() {
        RrdRepository repository = serviceCollector.getRrdRepository("test");
        assertThat(repository.getStep(), equalTo(300));
        assertThat(repository.getHeartBeat(), equalTo(600));
        assertThat(repository.getRraList(), hasSize(5));
        assertThat(repository.getRraList(), containsInAnyOrder(rrsList));
        assertThat(repository.getRrdBaseDir().getAbsolutePath(), equalTo(baseDir+"/share/rrd/test"));
    }

    @Test
    public void testGetCollectorClassName() {
        String testClass = "org.opennms.test.Collector";
        when(mockFactory.getCollectorClassName()).thenReturn(testClass);
        String className = serviceCollector.getCollectorClassName();
        assertThat(className, equalTo(testClass));
        verify(mockFactory).getCollectorClassName();
    }
}
