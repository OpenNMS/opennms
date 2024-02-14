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
package org.opennms.protocols.nsclient.collector;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.protocols.nsclient.AbstractNsclientTest;
import org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory;
import org.opennms.protocols.nsclient.config.NSClientPeerFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <p>JUnit Test Class for NsclientCollector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientCollectorTest extends AbstractNsclientTest {

    private PlatformTransactionManager m_transactionManager;

    private IpInterfaceDao m_ipInterfaceDao;

    private CollectionAgent m_collectionAgent;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final class CountResourcesVisitor extends AbstractCollectionSetVisitor {

        private int count = 0;

        public int getCount() {
            return count;
        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            count++;
            Assert.assertEquals(10d, attribute.getNumericValue());
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        startServer("None&8&", "10");

        // Initialize Mocks
        m_transactionManager = new MockPlatformTransactionManager();
        m_ipInterfaceDao = mock(IpInterfaceDao.class);
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("winsrv");
        builder.addSnmpInterface(1).setCollectionEnabled(true).addIpInterface(getServer().getInetAddress().getHostAddress());
        builder.getCurrentNode().setId(1);
        OnmsIpInterface iface = builder.getCurrentNode().getIpInterfaces().iterator().next();
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        iface.setId(1);
        when(m_ipInterfaceDao.load(1)).thenReturn(iface);

        // Initialize NSClient Configuration
        String nsclient_config = "<nsclient-config port=\"" + getServer().getLocalPort() + "\" retry=\"1\" timeout=\"3000\" />";
        NSClientPeerFactory.setInstance(new NSClientPeerFactory(new ByteArrayInputStream(nsclient_config.getBytes())));
        NSClientDataCollectionConfigFactory.setInstance(new NSClientDataCollectionConfigFactory("src/test/resources/nsclient-datacollection-config.xml"));

        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        NSClientPeerFactory.getInstance().setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));

        // Initialize Collection Agent
        m_collectionAgent = DefaultCollectionAgent.create(1, m_ipInterfaceDao, m_transactionManager);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        stopServer();
        verify(m_ipInterfaceDao, atLeastOnce()).load(1);
        super.tearDown();
    }

    @Test
    public void testCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        NSClientCollector collector = getCollector(parameters);
        parameters.putAll(collector.getRuntimeAttributes(m_collectionAgent, parameters));
        CollectionSet collectionSet = collector.collect(m_collectionAgent, parameters);
        Assert.assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CountResourcesVisitor visitor = new CountResourcesVisitor();
        collectionSet.visit(visitor);
        Assert.assertEquals(42, visitor.getCount());
    }

    private NSClientCollector getCollector(Map<String, Object> parameters) {
        NSClientCollector collector = new NSClientCollector();
        return collector;
    }

}
