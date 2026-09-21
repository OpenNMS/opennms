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
package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Since Hibernate 5, {@code IpInterfaceDao.update()} outside an active Spring transaction throws
 * {@code InvalidDataAccessApiUsageException} (non-transactional sessions are FlushMode.MANUAL).
 * This test pins {@link DefaultReverseDnsProvisioningAdapterService#update} to performing its DAO
 * write inside a transaction, which the adapter's own scheduling thread does not provide.
 */
public class DefaultReverseDnsProvisioningAdapterServiceTest {

    /** Tracks whether a transaction is currently active and whether it was committed. */
    private static class RecordingTransactionManager implements PlatformTransactionManager {
        final AtomicBoolean active = new AtomicBoolean(false);
        final AtomicBoolean committed = new AtomicBoolean(false);

        @Override
        public TransactionStatus getTransaction(final TransactionDefinition definition) {
            active.set(true);
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(final TransactionStatus status) {
            active.set(false);
            committed.set(true);
        }

        @Override
        public void rollback(final TransactionStatus status) {
            active.set(false);
        }
    }

    @Test
    public void updateWritesInterfaceInsideTransaction() throws Exception {
        final RecordingTransactionManager txManager = new RecordingTransactionManager();
        final OnmsIpInterface ipInterface = new OnmsIpInterface();
        final AtomicBoolean updatedInTransaction = new AtomicBoolean(false);

        final IpInterfaceDao dao = (IpInterfaceDao) Proxy.newProxyInstance(
                IpInterfaceDao.class.getClassLoader(),
                new Class<?>[] { IpInterfaceDao.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByNodeIdAndIpAddress":
                            return ipInterface;
                        case "update":
                            updatedInTransaction.set(txManager.active.get());
                            return null;
                        default:
                            throw new UnsupportedOperationException("unexpected DAO call: " + method.getName());
                    }
                });

        final DefaultReverseDnsProvisioningAdapterService service = new DefaultReverseDnsProvisioningAdapterService();
        service.setIpInterfaceDao(dao);
        service.setTemplate(new TransactionTemplate(txManager));

        final OnmsNode node = new OnmsNode();
        node.setLabel("host1");
        final OnmsIpInterface source = new OnmsIpInterface();
        source.setNode(node);
        source.setIpAddress(InetAddress.getByName("192.168.1.1"));
        final ReverseDnsRecord record = new ReverseDnsRecord(source, 3);
        service.update(1, record);

        assertTrue("DAO update must run inside an active transaction", updatedInTransaction.get());
        assertTrue("transaction must be committed", txManager.committed.get());
        assertEquals("host1.", ipInterface.getIpHostName());
    }
}
