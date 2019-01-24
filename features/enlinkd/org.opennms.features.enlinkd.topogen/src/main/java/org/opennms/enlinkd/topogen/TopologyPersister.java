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

package org.opennms.enlinkd.topogen;

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
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyPersister {

    private final static Logger LOG = LoggerFactory.getLogger(TopologyPersister.class);

    private NodeDao nodeDao;
    private CdpElementDao cdpElementDao;
    private IsIsElementDao isIsElementDao;
    private LldpElementDao lldpElementDao;
    private CdpLinkDao cdpLinkDao;
    private IsIsLinkDao isIsLinkDao;
    private LldpLinkDao lldpLinkDao;
    private OspfLinkDao ospfLinkDao;
    private IpInterfaceDao ipInterfaceDao;
    private SnmpInterfaceDao snmpInterfaceDao;

    private TopologyPersister(
        final NodeDao nodeDao,
        final CdpElementDao cdpElementDao,
        final IsIsElementDao isIsElementDao,
        final LldpElementDao lldpElementDao,
        final CdpLinkDao cdpLinkDao,
        final IsIsLinkDao isIsLinkDao,
        final LldpLinkDao lldpLinkDao,
        final OspfLinkDao ospfLinkDao,
        final IpInterfaceDao ipInterfaceDao,
        final SnmpInterfaceDao snmpInterfaceDao
    ) {
        this.nodeDao = nodeDao;
        this.cdpElementDao = cdpElementDao;
        this.isIsElementDao = isIsElementDao;
        this.lldpElementDao = lldpElementDao;
        this.cdpLinkDao = cdpLinkDao;
        this.isIsLinkDao = isIsLinkDao;
        this.lldpLinkDao = lldpLinkDao;
        this.ospfLinkDao = ospfLinkDao;
        this.ipInterfaceDao = ipInterfaceDao;
        this.snmpInterfaceDao = snmpInterfaceDao;
    }

    public static TopologyPersisterDaoBuilder builder() {
        return new TopologyPersisterDaoBuilder();
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
        if(elements.size() < 1) {
            return; // nothing do do
        }
        LOG.info("starting to insert {} {}s", elements.size(), elements.get(0).getClass().getSimpleName());

        for(int i = 0; i< elements.size(); i++) {
            E element = elements.get(i);
            dao.save(element);
            if (i % 100 == 0 || i == elements.size()-1) {
                LOG.info("inserting {} of {} {}s done.", i, elements.size(), elements.get(0).getClass().getSimpleName());
            }
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

    public static class TopologyPersisterDaoBuilder {
        private NodeDao nodeDao;
        private CdpElementDao cdpElementDao;
        private IsIsElementDao isIsElementDao;
        private LldpElementDao lldpElementDao;
        private CdpLinkDao cdpLinkDao;
        private IsIsLinkDao isIsLinkDao;
        private LldpLinkDao lldpLinkDao;
        private OspfLinkDao ospfLinkDao;
        private IpInterfaceDao ipInterfaceDao;
        private SnmpInterfaceDao snmpInterfaceDao;

        TopologyPersisterDaoBuilder() {
        }

        public TopologyPersisterDaoBuilder nodeDao(NodeDao nodeDao) {
            this.nodeDao = nodeDao;
            return this;
        }

        public TopologyPersisterDaoBuilder cdpElementDao(CdpElementDao cdpElementDao) {
            this.cdpElementDao = cdpElementDao;
            return this;
        }

        public TopologyPersisterDaoBuilder isIsElementDao(IsIsElementDao isIsElementDao) {
            this.isIsElementDao = isIsElementDao;
            return this;
        }

        public TopologyPersisterDaoBuilder lldpElementDao(LldpElementDao lldpElementDao) {
            this.lldpElementDao = lldpElementDao;
            return this;
        }

        public TopologyPersisterDaoBuilder cdpLinkDao(CdpLinkDao cdpLinkDao) {
            this.cdpLinkDao = cdpLinkDao;
            return this;
        }

        public TopologyPersisterDaoBuilder isIsLinkDao(IsIsLinkDao isIsLinkDao) {
            this.isIsLinkDao = isIsLinkDao;
            return this;
        }

        public TopologyPersisterDaoBuilder lldpLinkDao(LldpLinkDao lldpLinkDao) {
            this.lldpLinkDao = lldpLinkDao;
            return this;
        }

        public TopologyPersisterDaoBuilder ospfLinkDao(OspfLinkDao ospfLinkDao) {
            this.ospfLinkDao = ospfLinkDao;
            return this;
        }

        public TopologyPersisterDaoBuilder ipInterfaceDao(IpInterfaceDao ipInterfaceDao) {
            this.ipInterfaceDao = ipInterfaceDao;
            return this;
        }

        public TopologyPersisterDaoBuilder snmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
            this.snmpInterfaceDao = snmpInterfaceDao;
            return this;
        }

        public TopologyPersister build() {
            return new TopologyPersister(nodeDao, cdpElementDao, isIsElementDao, lldpElementDao, cdpLinkDao, isIsLinkDao, lldpLinkDao, ospfLinkDao, ipInterfaceDao, snmpInterfaceDao);
        }

        public String toString() {
            return "TopologyPersisterDao.TopologyPersisterDaoBuilder(nodeDao=" + this.nodeDao + ", cdpElementDao=" + this.cdpElementDao + ", isIsElementDao=" + this.isIsElementDao + ", lldpElementDao=" + this.lldpElementDao + ", cdpLinkDao=" + this.cdpLinkDao + ", isIsLinkDao=" + this.isIsLinkDao + ", lldpLinkDao=" + this.lldpLinkDao + ", ospfLinkDao=" + this.ospfLinkDao + ", ipInterfaceDao=" + this.ipInterfaceDao + ", snmpInterfaceDao=" + this.snmpInterfaceDao + ")";
        }
    }
}


