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
package org.opennms.netmgt.enlinkd.persistence.impl;

import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.OspfElement;

public class OspfElementDaoHibernate extends AbstractDaoHibernate<OspfElement, Integer> implements OspfElementDao {

    /**
     * <p>
     * Constructor for OspfElementDaoHibernate.
     * </p>
     */
    public OspfElementDaoHibernate() {
        super(OspfElement.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.enlinkd.model.OspfElement} object.
     */
    @Override
    public OspfElement findByNodeId(Integer id) {
        return findUnique("from OspfElement rec where rec.node.id = ?", id);
    }

    @Override
    public OspfElement findByRouterId(InetAddress routerId) {
        return findUnique("from OspfElement rec where rec.ospfRouterId = ?",
                          routerId);
    }

    @Override
    public List<OspfElement> findAllByRouterId(InetAddress routerId) {
        return find("from OspfElement rec where rec.ospfRouterId = ?", routerId);
    }

    @Override
    public List<OspfElement> findByRouterIdOfRelatedOspfLink(int nodeId) {
        return find("from OspfElement rec where rec.ospfRouterId in (select l.ospfRemRouterId from OspfLink l where l.node.id = ?)", nodeId);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from OspfElement rec where rec.node.id = ? ",
                                 new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from OspfElement");
    }

}
