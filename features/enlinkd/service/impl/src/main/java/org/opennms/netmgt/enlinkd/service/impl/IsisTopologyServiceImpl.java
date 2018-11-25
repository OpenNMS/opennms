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
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class IsisTopologyServiceImpl implements IsisTopologyService {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private IsIsLinkDao m_isisLinkDao;
    private IsIsElementDao m_isisElementDao;

    public IsisTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_isisElementDao.deleteByNodeId(nodeid);
        m_isisLinkDao.deleteByNodeId(nodeid);
        m_isisElementDao.flush();
        m_isisLinkDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        IsIsElement element = m_isisElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getIsisNodeLastPollTime().getTime() < now.getTime()) {
            m_isisElementDao.delete(element);
            m_isisElementDao.flush();
        }
        m_isisLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_isisLinkDao.flush();
    }

    @Override
    public void store(int nodeId, IsIsLink link) {
        if (link == null)
            return;
        saveIsisLink(nodeId, link);
    }

    @Transactional
    protected void saveIsisLink(final int nodeId, final IsIsLink saveMe) {
        new UpsertTemplate<IsIsLink, IsIsLinkDao>(m_transactionManager,
                                                  m_isisLinkDao) {

            @Override
            protected IsIsLink query() {
                return m_dao.get(nodeId, saveMe.getIsisCircIndex(),
                                 saveMe.getIsisISAdjIndex());
            }

            @Override
            protected IsIsLink doUpdate(IsIsLink dbIsIsLink) {
                dbIsIsLink.merge(saveMe);
                m_dao.update(dbIsIsLink);
                m_dao.flush();
                return dbIsIsLink;
            }

            @Override
            protected IsIsLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setIsisLinkLastPollTime(saveMe.getIsisLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    
    @Override
    @Transactional
    public void store(int nodeId, IsIsElement element) {
        if (element == null)
            return;

        IsIsElement dbelement = m_isisElementDao.findByNodeId(nodeId);
        
        if (dbelement != null) {
            dbelement.merge(element);
            m_isisElementDao.saveOrUpdate(dbelement);
            m_isisElementDao.flush();
            return;
        }
        
        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        element.setNode(node);
        element.setIsisNodeLastPollTime(element.getIsisNodeCreateTime());
        m_isisElementDao.saveOrUpdate(element);
        m_isisElementDao.flush();
    
    }

    public IsIsLinkDao getIsisLinkDao() {
        return m_isisLinkDao;
    }

    public void setIsisLinkDao(IsIsLinkDao isisLinkDao) {
        m_isisLinkDao = isisLinkDao;
    }

    public IsIsElementDao getIsisElementDao() {
        return m_isisElementDao;
    }

    public void setIsisElementDao(IsIsElementDao isisElementDao) {
        m_isisElementDao = isisElementDao;
    }

}
