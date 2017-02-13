/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.camel;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.echo.EchoClient;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThreadLocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * Used to verify and validate the thread profiles of the RPC
 * server via the EchoRpcModule.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-camel.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-echo.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.ipc.rpc.threads=1",
        "org.opennms.ipc.rpc.queue.max=-1" ,
        "org.opennms.ipc.rpc.threads.max=" + EchoRpcThreadIT.NTHREADS,
        })
public class EchoRpcThreadIT extends CamelBlueprintTest {

    protected static final int NTHREADS = 100;

    private static final String REMOTE_LOCATION_NAME = "remote";

    @Rule
    public ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private EchoClient echoClient;

    private ThreadLockingEchoRpcModule lockingRpcModule = new ThreadLockingEchoRpcModule();

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MockMinionIdentity(REMOTE_LOCATION_NAME),
                new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queuingservice, props));
        services.put(RpcModule.class.getName(), new KeyValueHolder<Object, Dictionary>(lockingRpcModule, new Properties()));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-rpc-server.xml";
    }

    @Override
    public boolean isCreateCamelContextPerClass() {
        return true;
    }

    @Test(timeout=60000)
    public void canProcessManyRequestsAsynchronously() throws Exception {
        // Execute a request via a remote location
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        // Lock the run method in our RPC module, we want to validate
        // the number of threads that are "running" the module
        CompletableFuture<Integer> runLockedFuture = lockingRpcModule.getRunLocker().waitForThreads(NTHREADS);
 
        // Fire off NTHREADS request
        List<CompletableFuture<EchoResponse>> futures = new ArrayList<>();
        for (int i = 0; i < NTHREADS; i++) {
            EchoRequest request = new EchoRequest("ping");
            request.setTimeToLiveMs(30000L);
            request.setLocation(REMOTE_LOCATION_NAME);
            futures.add(echoClient.execute(request));
        }

        // Wait for all the threads calling run() to be locked
        runLockedFuture.get();

        // Release and verify that all the futures return
        lockingRpcModule.getRunLocker().release();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[NTHREADS])).get();
    }

    public static class ThreadLockingEchoRpcModule extends EchoRpcModule {

        private final ThreadLocker runLocker = new ThreadLocker();

        @Override
        public void beforeRun() {
            runLocker.park();
        }

        public ThreadLocker getRunLocker() {
            return runLocker;
        }
    }
}
