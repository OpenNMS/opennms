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
package org.opennms.netmgt.poller;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

public class QueryManagerDaoImplTest {

    private final MonitoredServiceDao m_monitoredServiceDao = mock(MonitoredServiceDao.class);
    private final OutageDao m_outageDao = mock(OutageDao.class);
    private final QueryManagerDaoImpl m_queryManager = new QueryManagerDaoImpl();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(m_queryManager, "m_monitoredServiceDao", m_monitoredServiceDao);
        ReflectionTestUtils.setField(m_queryManager, "m_outageDao", m_outageDao);
        ReflectionTestUtils.setField(m_queryManager, "m_transcationOps", new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) throws TransactionException {
                return action.doInTransaction(mock(TransactionStatus.class));
            }
        });
    }

    @Test
    public void openOutageReturnsNullWhenServiceIsGone() {
        when(m_monitoredServiceDao.get(1, InetAddress.getLoopbackAddress(), "ICMP")).thenReturn(null);

        assertNull(m_queryManager.openOutagePendingLostEventId(1, "127.0.0.1", "ICMP", new Date()));
        verify(m_outageDao, never()).saveOrUpdate(any());
    }

    @Test
    public void openOutageReturnsNullWhenSaveFails() {
        when(m_monitoredServiceDao.get(1, InetAddress.getLoopbackAddress(), "ICMP")).thenReturn(new OnmsMonitoredService());
        doThrow(new RuntimeException("insert failed")).when(m_outageDao).saveOrUpdate(any(OnmsOutage.class));

        assertNull(m_queryManager.openOutagePendingLostEventId(1, "127.0.0.1", "ICMP", new Date()));
    }
}
