/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.config.service.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.microblog.MicroblogConfiguration;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.config.wmi.agent.WmiConfig;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@RunWith(Parameterized.class)
public class BeanFieldCopyUtilTest<T> {

    private PodamFactory factory = new PodamFactoryImpl();

    private final Class<T> clazz;

    public BeanFieldCopyUtilTest(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Parameters
    public static List<Class<?>> testClasses() {
        // Config class have specific setter checking cannot put in here. e.g. ProvisiondConfiguration
        // It is due to random POJO filler class will throw exception.
        return Arrays.asList(new Class<?>[]
                {
                        DiscoveryConfiguration.class,
                        SnmpConfig.class,
                        WmiConfig.class,
                        PollerConfiguration.class,
                        EventdConfiguration.class,
                        NotifdConfiguration.class,
                        VacuumdConfiguration.class,
                        JmxDatacollectionConfig.class,
                        MicroblogConfiguration.class,
                        ReadmailConfig.class,
                        HwInventoryAdapterConfiguration.class,
                        TrapdConfiguration.class
                });
    }

    @Test
    public void testCopy() throws Exception {
        T config = factory.manufacturePojoWithFullData(clazz);
        T dest = clazz.getDeclaredConstructor().newInstance();
        BeanFieldCopyUtil.copyFields(config, dest);
        Assert.assertEquals("Content should be the same", config, dest);
        Assert.assertNotSame("Object reference should be different", config, dest);
    }
}
