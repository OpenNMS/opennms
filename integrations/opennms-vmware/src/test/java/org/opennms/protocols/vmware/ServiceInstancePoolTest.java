/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2018 The OpenNMS Group, Inc.
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

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.TrustManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

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
        this.serviceInstancePool = new ServiceInstancePool(500) {
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

        // L N N N

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

        Thread.sleep(200);

        serviceInstancePool.release(s3);
        s3 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/3/valid", s3.toString());

        Thread.sleep(200);

        serviceInstancePool.release(s3);
        s3 = serviceInstancePool.retain("host3.test.de", "username3", "password3");
        Assert.assertEquals("host3.test.de/username3/password3/3/valid", s3.toString());

        Thread.sleep(250);

        // N N L N

        Assert.assertEquals(1, serviceInstancePool.lockedEntryCount());
        Assert.assertEquals(0, serviceInstancePool.unlockedEntryCount());

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

        Thread.sleep(550);

        Assert.assertEquals(1, serviceInstancePool.lockedEntryCount());
        Assert.assertEquals(0, serviceInstancePool.unlockedEntryCount());

        Assert.assertNotNull(s1);
        Assert.assertNotNull(s2);
        Assert.assertNotNull(s3);
        Assert.assertNotNull(s4);

        Assert.assertTrue(((DummyServiceInstance) s1).valid);
        Assert.assertFalse(((DummyServiceInstance) s2).valid);
        Assert.assertFalse(((DummyServiceInstance) s3).valid);
        Assert.assertFalse(((DummyServiceInstance) s4).valid);
    }
}