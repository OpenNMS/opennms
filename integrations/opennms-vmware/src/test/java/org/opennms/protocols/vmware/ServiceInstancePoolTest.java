/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.protocols.vmware;

import static com.jayway.awaitility.Awaitility.await;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.net.ssl.TrustManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.SessionManager;

public class ServiceInstancePoolTest {
    private ServiceInstancePool serviceInstancePool;
    private int instanceCounter;

    private class DummyServiceInstance extends ServiceInstance {
        private boolean valid = true;
        private final String key;

        public DummyServiceInstance(String host, String username, String password) throws RemoteException, MalformedURLException {
            super(new URL("https://" + host + "/sdk"), username, password);
            key = host + "/" + username + "/" + password + "/" + instanceCounter + "/";
            instanceCounter++;
        }

        @Override
        protected void constructServiceInstance(URL url, String username, String password, boolean ignoreCert, String namespace, int connectTimeout, int readTimeout, TrustManager trustManager) throws RemoteException, MalformedURLException {
        }

        @Override
        protected void constructServiceInstance(URL url, String sessionStr, boolean ignoreCert, String namespace, int connectTimeout, int readTimeout, TrustManager trustManager) throws RemoteException, MalformedURLException {
        }

        @Override
        public SessionManager getSessionManager() {
            return new SessionManager(new ServerConnection(null, null, this), new ManagedObjectReference()) {
                @Override
                public UserSession getCurrentSession() {
                    if (valid) {
                        return new UserSession();
                    } else {
                        return null;
                    }
                }
            };
        }

        @Override
        public ServerConnection getServerConnection() {
            return new ServerConnection(null, null, null) {
                @Override
                public void logout() {
                    valid = false;
                }
            };
        }

        @Override
        public String toString() {
            return this.key + (this.valid ? "valid" : "invalid");
        }
    }

    @Before
    public void beforeTest() {
        System.getProperties().setProperty("org.opennms.protocols.vmware.housekeepingInterval", "500");
        this.serviceInstancePool = new ServiceInstancePool() {
            @Override
            protected ServiceInstance create(String hostname, String username, String password) throws MalformedURLException, RemoteException {
                return new DummyServiceInstance(hostname, username, password);
            }
        };

        instanceCounter = 1;
    }

    @Test
    public void verifyOperation() throws Exception {
        ServiceInstance s1 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/1/valid", s1.toString());

        ServiceInstance s2 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/2/valid", s2.toString());

        ServiceInstance s3 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/3/valid", s3.toString());

        serviceInstancePool.release(s2);

        ServiceInstance s4 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/2/valid", s4.toString());

        serviceInstancePool.release(s4);

        ((DummyServiceInstance) s4).valid = false;

        ServiceInstance s5 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/4/valid", s5.toString());
    }

    @Test
    public void verifyHousekeeping() throws Exception {
        ServiceInstance s1 = serviceInstancePool.retain("host2.test.de", "username2", "password2");
        Assert.assertEquals("host2.test.de/username2/password2/1/valid", s1.toString());

        // L N N N <- s1 s2 s3 s4: (L)ocked, (U)nlocked, (N)ot existing, (I)nvalid

        ((DummyServiceInstance) s1).valid = false;

        // L/I N N N

        ServiceInstance s2 = serviceInstancePool.retain("host2.test.de", "username2", "password2");
        Assert.assertEquals("host2.test.de/username2/password2/2/valid", s2.toString());

        // L/I L/I N N

        ((DummyServiceInstance) s2).valid = false;

        // L/I L/I N N

        serviceInstancePool.release(s1);

        // U/I L/I N N

        ServiceInstance s3 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/3/valid", s3.toString());

        // U/I L/I L N

        Assert.assertEquals(2, serviceInstancePool.lockedEntryCount());
        Assert.assertEquals(1, serviceInstancePool.unlockedEntryCount());

        Assert.assertEquals(1, serviceInstancePool.lockedEntryCount("host2.test.de/username2/password2"));
        Assert.assertEquals(1, serviceInstancePool.unlockedEntryCount("host2.test.de/username2/password2"));

        Assert.assertEquals(1, serviceInstancePool.lockedEntryCount("host3.test.de/username3/password3"));
        Assert.assertEquals(0, serviceInstancePool.unlockedEntryCount("host3.test.de/username3/password3"));

        serviceInstancePool.release(s3);
        s3 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/3/valid", s3.toString());

        // N N L N

        await().until(() -> serviceInstancePool.lockedEntryCount() == 1 && serviceInstancePool.unlockedEntryCount() == 0);

        ServiceInstance s4 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/4/valid", s4.toString());

        // N N L L

        Assert.assertEquals(2, serviceInstancePool.lockedEntryCount());
        Assert.assertEquals(0, serviceInstancePool.unlockedEntryCount());

        Assert.assertEquals(2, serviceInstancePool.lockedEntryCount("host3.test.de/username3/password3"));
        Assert.assertEquals(0, serviceInstancePool.unlockedEntryCount("host3.test.de/username3/password3"));
    }

    @Test
    public void verifyLogout() throws Exception {
        ServiceInstance s1 = serviceInstancePool.retain("host1.test.de", "username1", "password1");
        Assert.assertEquals("host1.test.de/username1/password1/1/valid", s1.toString());

        ServiceInstance s2 = serviceInstancePool.retain("host2.test.de", "username2", "password2");
        Assert.assertEquals("host2.test.de/username2/password2/2/valid", s2.toString());
        ((DummyServiceInstance) s2).valid = false;

        ServiceInstance s3 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/3/valid", s3.toString());
        serviceInstancePool.release(s3);

        ServiceInstance s4 = serviceInstancePool.retain("host4.test.de", "username4", "password4");
        Assert.assertEquals("host4.test.de/username4/password4/4/valid", s4.toString());
        serviceInstancePool.release(s4);
        ((DummyServiceInstance) s4).valid = false;

        Assert.assertEquals(2, serviceInstancePool.lockedEntryCount());
        Assert.assertEquals(2, serviceInstancePool.unlockedEntryCount());

        Assert.assertTrue(((DummyServiceInstance) s1).valid);
        Assert.assertFalse(((DummyServiceInstance) s2).valid);
        Assert.assertTrue(((DummyServiceInstance) s3).valid);
        Assert.assertFalse(((DummyServiceInstance) s4).valid);

        await().until(() -> serviceInstancePool.lockedEntryCount() == 1 && serviceInstancePool.unlockedEntryCount() == 0);

        Assert.assertNotNull(s1);
        Assert.assertNotNull(s2);
        Assert.assertNotNull(s3);
        Assert.assertNotNull(s4);

        Assert.assertEquals(true,((DummyServiceInstance) s1).valid);
        Assert.assertEquals(false, ((DummyServiceInstance) s2).valid);
        Assert.assertEquals(false, ((DummyServiceInstance) s3).valid);
        Assert.assertEquals(false, ((DummyServiceInstance) s4).valid);
    }

    @Test
    public void testLastAcces() throws MalformedURLException, RemoteException, InterruptedException {
        final ServiceInstancePoolEntry serviceInstancePoolEntry = new ServiceInstancePoolEntry(this.serviceInstancePool, "hostname","username", "password");

        long l1 = System.currentTimeMillis();

        final ServiceInstance s1 = serviceInstancePoolEntry.retain();

        Assert.assertEquals(1, serviceInstancePoolEntry.getAccessTimestamp().size());
        Assert.assertEquals(true, serviceInstancePoolEntry.getAccessTimestamp().get(s1) >= l1);

        long l2 = System.currentTimeMillis();

        final ServiceInstance s2 = serviceInstancePoolEntry.retain();

        Assert.assertEquals(2, serviceInstancePoolEntry.getAccessTimestamp().size());
        Assert.assertEquals(true, serviceInstancePoolEntry.getAccessTimestamp().get(s2) >= l2);

        serviceInstancePoolEntry.release(s2);

        long l3 = System.currentTimeMillis();

        final ServiceInstance s3 = serviceInstancePoolEntry.retain();

        Assert.assertEquals(2, serviceInstancePoolEntry.getAccessTimestamp().size());
        Assert.assertEquals(true, serviceInstancePoolEntry.getAccessTimestamp().get(s3) >= l3);

        serviceInstancePoolEntry.release(s1);
        ((DummyServiceInstance) s3).valid = false;

        Thread.sleep(10);
        serviceInstancePoolEntry.expire(1);
        Assert.assertEquals(0, serviceInstancePoolEntry.getAccessTimestamp().size());
    }
}