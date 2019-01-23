/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.shell.topogen;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyPersisterDao implements TopologyPersister{

    private final static Logger LOG = LoggerFactory.getLogger(TopologyPersisterDao.class);

    private NodeDao nodeDao;
    private CdpElementDao cdpElementDao;
    private IsIsElementDao isIsElementDao;
    private LldpElementDao lldpElementDao;
    private OspfElementDao ospfElementDao;
    private CdpLinkDao cdpLinkDao;
    private IsIsLinkDao isIsLinkDao;
    private LldpLinkDao lldpLinkDao;
    private OspfLinkDao ospfLinkDao;
    private IpInterfaceDao ipInterfaceDao;
    private SnmpInterfaceDao snmpInterfaceDao;

    public TopologyPersisterDao(
        final NodeDao nodeDao,
        final CdpElementDao cdpElementDao,
        final IsIsElementDao isIsElementDao,
        final LldpElementDao lldpElementDao,
        final OspfElementDao ospfElementDao,
        final CdpLinkDao cdpLinkDao,
        final IsIsLinkDao isIsLinkDao,
        final LldpLinkDao lldpLinkDao,
        final OspfLinkDao ospfLinkDao,
        final IpInterfaceDao ipInterfaceDao,
        final SnmpInterfaceDao snmpInterfaceDao
    ) throws IOException {
        this.nodeDao = nodeDao;
        this.cdpElementDao = cdpElementDao;
        this.isIsElementDao = isIsElementDao;
        this.lldpElementDao = lldpElementDao;
        this.ospfElementDao = ospfElementDao;
        this.cdpLinkDao = cdpLinkDao;
        this.isIsLinkDao = isIsLinkDao;
        this.lldpLinkDao = lldpLinkDao;
        this.ospfLinkDao = ospfLinkDao;
        this.ipInterfaceDao = ipInterfaceDao;
        this.snmpInterfaceDao = snmpInterfaceDao;
    }

    public void persistNodes(List<OnmsNode> nodes) throws SQLException {
        persist(nodeDao, nodes);
    }

    public void persistCdpElements(List<CdpElement> elements) throws SQLException {
        persist(cdpElementDao, elements);
    }

    public void persistIsIsElements(List<IsIsElement> elements) throws SQLException {
        persist(isIsElementDao, elements);
    }

    public void persistLldpElements(List<LldpElement> elements) throws SQLException {
        persist(lldpElementDao, elements);
    }

    public void persistCdpLinks(List<CdpLink> links) throws SQLException {
        persist(cdpLinkDao, links);
    }

    public void persistIsIsLinks(List<IsIsLink> links) throws SQLException {
        persist(isIsLinkDao, links);
    }

    public void persistLldpLinks(List<LldpLink> links) throws SQLException {
        persist(lldpLinkDao, links);
    }

    public void persistOspfLinks(List<OspfLink> links) throws SQLException {
        persist(ospfLinkDao, links);
    }

    public void persistOnmsInterfaces(List<OnmsSnmpInterface> onmsSnmpInterfaces) throws SQLException {
        persist(snmpInterfaceDao, onmsSnmpInterfaces);
    }

    public void persistIpInterfaces(List<OnmsIpInterface> ipInterfaces) throws SQLException {
        persist(ipInterfaceDao, ipInterfaces);
    }

    private <E> void  persist(OnmsDao<E, ?> dao, List<E> elements) throws SQLException {
        for(E element : elements) {
            dao.save(element);
        }
    }

    public void deleteTopology() throws SQLException {
        LOG.info("deleting existing topology");
        // we need to delete in this order to avoid foreign key conflicts:
        List<OnmsDao> deleteOperations = Arrays.asList(
            this.cdpLinkDao,
            this.isIsLinkDao,
            this.lldpLinkDao,
            this.cdpElementDao,
            this.isIsElementDao,
            this.lldpElementDao,
            this.ospfLinkDao,
            this.ipInterfaceDao,
            this.snmpInterfaceDao,
            this.nodeDao);

        for (OnmsDao dao : deleteOperations) {
            dao.deleteAll();
        }
    }
}


