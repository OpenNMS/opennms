package org.opennms.netmgt.ncs.persistence;

import org.opennms.netmgt.model.ncs.NCSComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NCSComponentServiceImpl implements NCSComponentService {
	@Autowired
	NCSComponentDao m_componentDao;

	@Transactional
	public NCSComponent getComponent(final String type, final String foreignSource, final String foreignId) {
		return m_componentDao.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
	}

	public void addOrUpdateComponents(final NCSComponent component) {
		m_componentDao.saveOrUpdate(component);
	}
}
