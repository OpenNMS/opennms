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
package org.opennms.core.ipc.sink.kafka.itests;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;

public class KafkaDispatcherBlueprintIT extends CamelBlueprintTest {

	@Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-ipc-client.xml,blueprint-empty-camel-context.xml";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MinionIdentity() {
                    @Override
                    public String getId() {
                        return "0";
                    }

                    @Override
                    public String getLocation() {
                        return "remote";
                    }

                    @Override
                    public String getType() {
                        return SystemType.Minion.name();
                    }
                }, new Properties()));

    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    protected String setConfigAdminInitialConfiguration(final Properties props) {
        props.put("bootstrap.servers", "127.0.0.1:9092");
        return KafkaSinkConstants.KAFKA_CONFIG_PID;
    }

    @Test
    public void canBlueprintLoadSuccesfully() throws Exception {
    }
}
