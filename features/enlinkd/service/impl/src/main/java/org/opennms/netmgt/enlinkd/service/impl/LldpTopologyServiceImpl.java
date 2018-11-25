/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.impl;

import java.util.Date;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class LldpTopologyServiceImpl implements LldpTopologyService {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;

    public LldpTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_lldpElementDao.deleteByNodeId(nodeid);
        m_lldpLinkDao.deleteByNodeId(nodeid);
        m_lldpElementDao.flush();
        m_lldpLinkDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        LldpElement element = m_lldpElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getLldpNodeLastPollTime().getTime() < now.getTime()) {
            m_lldpElementDao.delete(element);
            m_lldpElementDao.flush();
        }
        m_lldpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_lldpLinkDao.flush();
    }

    @Override
    public void store(int nodeId, LldpLink link) {
        if (link == null)
            return;
        saveLldpLink(nodeId, link);
    }

    @Transactional
    protected void saveLldpLink(final int nodeId, final LldpLink saveMe) {
        new UpsertTemplate<LldpLink, LldpLinkDao>(m_transactionManager,
                                                  m_lldpLinkDao) {

            @Override
            protected LldpLink query() {
                return m_dao.get(nodeId, saveMe.getLldpLocalPortNum());
            }

            @Override
            protected LldpLink doUpdate(LldpLink dbLldpLink) {
                dbLldpLink.merge(saveMe);
                m_dao.update(dbLldpLink);
                m_dao.flush();
                return dbLldpLink;
            }

            @Override
            protected LldpLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setLldpLinkLastPollTime(saveMe.getLldpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    @Transactional
    public void store(int nodeId, LldpElement element) {
        if (element == null)
            return;

        LldpElement dbelement = m_lldpElementDao.findByNodeId(nodeId);
        if (dbelement != null) {
            dbelement.merge(element);
            m_lldpElementDao.saveOrUpdate(dbelement);
            m_lldpElementDao.flush();
            return;
        }

        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        element.setNode(node);
        element.setLldpNodeLastPollTime(element.getLldpNodeCreateTime());
        m_lldpElementDao.saveOrUpdate(element);
        m_lldpElementDao.flush();
    }

    public LldpLinkDao getLldpLinkDao() {
        return m_lldpLinkDao;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }

    public LldpElementDao getLldpElementDao() {
        return m_lldpElementDao;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        m_lldpElementDao = lldpElementDao;
    }

}
