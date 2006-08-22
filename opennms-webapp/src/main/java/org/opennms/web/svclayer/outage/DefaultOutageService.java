package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;

public class DefaultOutageService implements OutageService {

	private OutageDao m_dao;

	public DefaultOutageService() {

	}

	public DefaultOutageService(OutageDao dao) {
		m_dao = dao;
	}

	public OutageDao getDao() {
		return m_dao;
	}

	public void setDao(OutageDao dao) {
		this.m_dao = dao;
	}

	public Integer getCurrentOutageCount() {
		return m_dao.currentOutageCount();
	}

	public Collection<OnmsOutage> getCurrentOutages() {
		return m_dao.currentOutages();
	}

	public Collection<OnmsOutage> getCurrentOutagesOrdered(String orderBy) {
		return m_dao.currentOutages(orderBy);
	}

	public Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
			String ipInterface) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
			String ipAddr, Date time) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForNode(int nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForService(int nodeId,
			String ipInterface, int serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<OnmsOutage> getOutagesForService(int nodeId,
			String ipAddr, int serviceId, Date time) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getSuppressedOutageCount() {
		return m_dao.currentSuppressedOutageCount();
	}

	public Collection<OnmsOutage> getSuppressedOutages() {
		return m_dao.suppressedOutages();
	}

	public Collection<OnmsOutage> getOpenAndResolved() {
		return m_dao.openAndResolvedOutages();
	}

	public Collection<OnmsOutage> getCurrentOutagesByRange(Integer Offset,
			Integer Limit, String orderBy, String direction) {
		return m_dao.currentOutages(Offset, Limit, orderBy, direction);
	}

	public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer Offset,
			Integer Limit) {
		return m_dao.suppressedOutages(Offset, Limit);
	}

	public Collection<OnmsOutage> getOpenAndResolved(Integer Offset,
			Integer Limit) {
		return m_dao.findAll(Offset, Limit);

	}

	public Collection<OnmsOutage> getCurrentOutages(String ordering) {
		return m_dao.currentOutages(ordering);
	}

	public OnmsOutage load(Integer outageid) {
		return m_dao.load(outageid);
	}

	public void update(OnmsOutage outage) {
		this.m_dao.update(outage);
	}

	public Collection<OnmsOutage> getOutagesByRange(Integer Offset, Integer Limit, String Order, String Direction) {
		return m_dao.getOutagesByRange(Offset, Limit,Order,Direction);
	}

	public Integer getOutageCount() {
		return m_dao.outageCount();
	}
	

	

}
