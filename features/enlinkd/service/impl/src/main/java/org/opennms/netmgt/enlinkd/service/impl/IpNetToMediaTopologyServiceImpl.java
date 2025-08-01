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
package org.opennms.netmgt.enlinkd.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class IpNetToMediaTopologyServiceImpl implements
        IpNetToMediaTopologyService {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private final static Logger LOG = LoggerFactory.getLogger(IpNetToMediaTopologyServiceImpl.class);

    private IpNetToMediaDao m_ipNetToMediaDao;
    private IpInterfaceDao m_ipInterfaceDao;    

    public IpNetToMediaTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_ipNetToMediaDao.deleteBySourceNodeId(nodeid);
        m_ipNetToMediaDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        m_ipNetToMediaDao.deleteBySourceNodeIdOlderThen(nodeId, now);
        m_ipNetToMediaDao.flush();
    }

    @Override
    public void store(int nodeId, IpNetToMedia ipnettomedia) {
        if (ipnettomedia == null)
            return;
        saveIpNetToMedia(nodeId, ipnettomedia);
    }

    @Transactional
    protected void saveIpNetToMedia(final int nodeId,
            final IpNetToMedia saveMe) {
        new UpsertTemplate<>(
                m_transactionManager,
                m_ipNetToMediaDao) {

            @Override
            protected IpNetToMedia query() {
                return m_dao.getByNetAndPhysAddress(saveMe.getNetAddress(),
                        saveMe.getPhysAddress());
            }

            @Override
            protected IpNetToMedia doUpdate(IpNetToMedia dbIpNetToMedia) {
                final OnmsNode sourceNode = new OnmsNode();
                sourceNode.setId(nodeId);
                saveMe.setSourceNode(sourceNode);
                putOnmsPropertyForIpNetToMedia(saveMe);
                dbIpNetToMedia.merge(saveMe);
                m_dao.update(dbIpNetToMedia);
                m_dao.flush();
                return dbIpNetToMedia;
            }

            @Override
            protected IpNetToMedia doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setSourceNode(node);
                putOnmsPropertyForIpNetToMedia(saveMe);
                saveMe.setLastPollTime(saveMe.getCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }
    
    
    private void putOnmsPropertyForIpNetToMedia(final IpNetToMedia ipnetToMedia) {

        List<OnmsIpInterface> onmsiplist = m_ipInterfaceDao.findByIpAddress(InetAddressUtils.str(ipnetToMedia.getNetAddress()));
        if (onmsiplist.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No OnmsIpInterface found for {}", ipnetToMedia);
            }
            return;
        }
    
        if (onmsiplist.size() > 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found {} OnmsIpInterface for {}", onmsiplist.size(),ipnetToMedia);
            }
            List<Integer> nodeids =onmsiplist.stream().map(OnmsIpInterface::getNodeId).collect(Collectors.toList());
            ipnetToMedia.setPort("Multiple Nodes: " + nodeids);
            return;
        }

        OnmsIpInterface onmsip = onmsiplist.iterator().next();
        ipnetToMedia.setNode(onmsip.getNode());
        if (onmsip.getSnmpInterface() == null) {
            ipnetToMedia.setIfIndex(-1);
            return;
        }
        ipnetToMedia.setIfIndex(onmsip.getIfIndex());
        ipnetToMedia.setPort(Topology.getPortTextString(SnmpInterfaceTopologyEntity.create(onmsip.getSnmpInterface())));
    }
    
    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

}
