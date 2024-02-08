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
package org.opennms.core.rpc.jms;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.camel.CamelRpcServerRouteManager;
import org.opennms.core.rpc.camel.MockMinionIdentity;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-echo.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class})
public class EchoRpcIT extends org.opennms.core.rpc.camel.EchoRpcIT {

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private ActiveMQComponent queuingservice;

    @Autowired
    @Qualifier("rpcClient")
    private CamelContext rpcClientContext;

    private TracerRegistry tracerRegistry = new TracerRegistry() {
        @Override
        public Tracer getTracer() {
            return GlobalTracer.get();
        }


        @Override
        public void init(String serviceName) {
        }
    };

    @Override
    public CamelContext getContext() {
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);
        return context;
    }

    @Override
    public CamelContext getClientContext() {
        return rpcClientContext;
    }

    @Override
    public CamelRpcServerRouteManager getRouteManager(CamelContext context) {
        return new JmsRpcServerRouteManager(context,
                new MockMinionIdentity(REMOTE_LOCATION_NAME), tracerRegistry);
    }
}
