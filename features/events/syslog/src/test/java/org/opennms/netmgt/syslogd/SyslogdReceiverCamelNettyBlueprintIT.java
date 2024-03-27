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
package org.opennms.netmgt.syslogd;

import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.Identity;
import org.opennms.distributed.core.api.MinionIdentity;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogdReceiverCamelNettyBlueprintIT extends CamelBlueprintTest {

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        // Register any mock OSGi services here
        final MessageDispatcherFactory messageProducerFactory = mock(MessageDispatcherFactory.class);
        final MinionIdentity minionIdentity = mock(MinionIdentity.class);
        services.put(MessageDispatcherFactory.class.getName(),
                new KeyValueHolder<Object, Dictionary>(messageProducerFactory, new Properties()));
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(minionIdentity, new Properties()));
        services.put(Identity.class.getName(), new KeyValueHolder<>(minionIdentity, new Properties()));
    }

    // The location of our Blueprint XML files to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "file:blueprint-syslog-listener-camel-netty.xml,blueprint-empty-camel-context.xml";
    }

    @Test
    public void testSyslogd() throws Exception {
        // TODO: Perform integration testing
    }
}
