package org.opennms.web.svclayer.daemonstatus.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.dao.jmx.ServiceDaemonStub;

public class DefaultDaemonStatusServiceTest extends TestCase {

	private DefaultDaemonStatusService defaultDaemonStatusService;

	private DaemonStatusDao daemonStatusDao;

	protected void setUp() throws Exception {
		super.setUp();
		daemonStatusDao = createMock(DaemonStatusDao.class);
		defaultDaemonStatusService = new DefaultDaemonStatusService();
		defaultDaemonStatusService.setDaemonStatusDao(daemonStatusDao);
	}

	public List<ServiceInfo> testGetCurrentDaemonStatusNotEmpty() {
		return null;
	}

	public List<ServiceInfo> testGetCurrentDaemonStatusEmpty() {
		return null;
	}

	public void testStartDaemon() {
		Map<String, ServiceInfo> info2Return = new HashMap<String, ServiceInfo>();
		ServiceInfo sinfo1 = new ServiceInfo("ignore", "failed");
		info2Return.put("notifd", sinfo1);
		ServiceInfo sinfo2 = new ServiceInfo("notifd", "Started");
		info2Return.put("notifd", sinfo2);

		ServiceDaemonStub dstub = new ServiceDaemonStub();
		expect(daemonStatusDao.getServiceHandle("notifd")).andReturn(dstub);
		expect(daemonStatusDao.getCurrentDaemonStatus()).andReturn(info2Return); // expecting
												                                 // this
																					// method
																					// to
																					// be
																					// called
		replay(daemonStatusDao); // done recording

		Map<String, ServiceInfo> listServiceInfo = defaultDaemonStatusService
				.startDaemon("notifd");
		String status = listServiceInfo.get("notifd").getServiceStatus();

		verify(daemonStatusDao); // verify that I called everything that I
									// "expect" ed to
        assertTrue("Service not started", dstub.getStartCalled());
		assertEquals("status must be 'Started'", "Started", status);
	}

	public void testStopDaemon() {

	}

	public void testRestartDaemon() {

	}

	public void testPerformOperationOnDaemonsEmptySetOfDaemons() {
		// TODO Auto-generated method stub

	}

	public void testPerformOperationOnDaemonsNonEmptySetOfDaemons() {
		// TODO Auto-generated method stub

	}

	public void testPerformOperationOnDaemonsNonEmptySetOfDaemonsInvalidOperation() {
		// TODO Auto-generated method stub

	}
}
