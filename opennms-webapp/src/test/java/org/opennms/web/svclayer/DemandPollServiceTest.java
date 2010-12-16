//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.support.DefaultDemandPollService;

public class DemandPollServiceTest extends TestCase {
	
	private DefaultDemandPollService m_demandPollService;
	private DemandPollDao m_demandPollDao;
	private MonitoredServiceDao m_monitoredServiceDao;
	private PollerService m_pollerService;
	private SingleDemandPollStore m_pollStore;

	protected void setUp() throws Exception {
		m_demandPollDao = createMock(DemandPollDao.class);
		m_monitoredServiceDao = createMock(MonitoredServiceDao.class);
		m_pollerService = createMock(PollerService.class);
		m_pollStore = new SingleDemandPollStore();

		m_demandPollService = new DefaultDemandPollService();
		m_demandPollService.setDemandPollDao(m_demandPollDao);
		m_demandPollService.setPollerAPI(m_pollerService);
		m_demandPollService.setMonitoredServiceDao(m_monitoredServiceDao);
	}

	protected void tearDown() throws Exception {
	}
	
	class SingleDemandPollStore implements DemandPollDao {
		
		int m_id = 13;
		DemandPoll m_demandPoll = null;
		
		public int getExpectedId() {
			return m_id;
		}

		public void clear() {
		}

		public int countAll() {
			return (m_demandPoll == null ? 0 : 1);
		}

		public void delete(DemandPoll entity) {
			if (entity.getId() == m_demandPoll.getId())
				m_demandPoll = null;
		}

		public List<DemandPoll> findAll() {
			return Collections.singletonList(m_demandPoll);
		}

		public void flush() {
		}

		public DemandPoll get(Integer id) {
			if (id.intValue() == m_id)
				return m_demandPoll;
			return null;
		}

		public DemandPoll load(Integer id) {
			return get(id);
		}

		public void saveOrUpdate(DemandPoll entity) {
			if (entity.getId() == null)
				save(entity);
			else
				update(entity);
		}

		public void update(DemandPoll entity) {
			if (entity.getId().intValue() == m_id)
				m_demandPoll = entity;
		}

		public void save(DemandPoll entity) {
			if (entity.getId() == null) {
				entity.setId(m_id);
				m_demandPoll = entity;
			} else {
				throw new RuntimeException("Can't save an entity that already has an id");
			}
		}

		public void initialize(Object obj) {
			// TODO Auto-generated method stub
			
		}

        public List<DemandPoll> findMatching(OnmsCriteria criteria) {
            throw new UnsupportedOperationException("not yet implemeneted");
        }

        public int countMatching(OnmsCriteria onmsCrit) {
            throw new UnsupportedOperationException("OnmsDao<DemandPoll,Integer>.countMatching is not yet implemented");
        }
		
		
	}
	
	public void testPollMonitoredService() throws EventProxyException {
		
		final int expectedResultId = m_pollStore.getExpectedId();

		// anticipate a call to the dao save with a pollResult
		m_demandPollDao.save(isA(DemandPoll.class));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				DemandPoll poll = (DemandPoll)getCurrentArguments()[0];
				m_pollStore.save(poll);
				return null;
			}
			
		});
		
		OnmsServiceType svcType = new OnmsServiceType();
		svcType.setId(3);
		svcType.setName("HTTP");
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(node, 1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		iface.setSnmpInterface(snmpIface);
		OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);

		expect(m_monitoredServiceDao.get(1, "192.168.1.1", 1, 3)).andReturn(monSvc);

		m_pollerService.poll(monSvc, expectedResultId);
		
		replay(m_demandPollDao);
		replay(m_monitoredServiceDao);
		replay(m_pollerService);
		
		DemandPoll result = m_demandPollService.pollMonitoredService(1, "192.168.1.1", 1, 3);

		verify(m_demandPollDao);
		verify(m_monitoredServiceDao);
		verify(m_pollerService);

		assertNotNull("Null is an invalid response from pollMonitoredService", result);
		assertEquals("Expected Id to be set by dao", expectedResultId, result.getId().intValue());
		
	}
	
	public void testGetUpdatedResults() {
		
		final int resultId = 3;
		
		DemandPoll expectedResult = new DemandPoll();
		
		
		expect(m_demandPollDao.get(resultId)).andReturn(expectedResult);
		replay(m_demandPollDao);
		
		DemandPoll result = m_demandPollService.getUpdatedResults(resultId);
		
		verify(m_demandPollDao);
		
		assertEquals(expectedResult, result);
	}

}
