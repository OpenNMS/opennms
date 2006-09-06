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
		throw new UnsupportedOperationException("not implemented.. Invalid ");
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
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getSuppressedOutages() {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getOpenAndResolved() {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getCurrentOutagesByRange(Integer offset,
			Integer limit, String orderProperty, String direction) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer Offset,
			Integer Limit) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getOpenAndResolved(Integer Offset,
			Integer Limit) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");

	}

	public Collection<OnmsOutage> getCurrentOutages(String ordering) {
		throw new UnsupportedOperationException("not implemented.. Invalid ");
	}

	public OnmsOutage load(Integer outageid) {
		return m_dao.load(outageid);
	}

	public void update(OnmsOutage outage) {
		this.m_dao.update(outage);
	}

	public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}
	
	public Integer getOutageCount() {
		return m_dao.countAll();
	}

	public Integer outageCountFiltered(String filter) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

	public Integer outageResolvedCountFiltered(String searchFilter) {
		throw new UnsupportedOperationException("not implemented since switch to hibernate");
	}

}
