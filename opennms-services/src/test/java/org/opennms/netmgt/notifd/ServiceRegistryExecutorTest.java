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
package org.opennms.netmgt.notifd;

import org.junit.Test;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServiceRegistryExecutorTest {

    @Test
    public void canFindServiceUsingFilter() {
        NullNotificationStrategy strategy = new NullNotificationStrategy();
        Map<String, String> props = new HashMap<>();
        props.put("type", NullNotificationStrategy.class.getCanonicalName());
        DefaultServiceRegistry.INSTANCE.register(strategy, props, NotificationStrategy.class);

        ServiceRegistryExecutor executor = new ServiceRegistryExecutor();
        int ret = executor.execute("(type=" + NullNotificationStrategy.class.getCanonicalName() + ")", Collections.emptyList());
        assertEquals(0, ret);
    }
}
