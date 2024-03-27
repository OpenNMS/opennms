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
