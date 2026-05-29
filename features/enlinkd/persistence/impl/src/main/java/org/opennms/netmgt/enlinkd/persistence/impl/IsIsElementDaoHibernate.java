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

import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.IsIsElement;

public class IsIsElementDaoHibernate extends AbstractDaoHibernate<IsIsElement, Integer> implements IsIsElementDao {

    /**
     * <p>
     * Constructor for IsIsElementDaoHibernate.
     * </p>
     */
    public IsIsElementDaoHibernate() {
        super(IsIsElement.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.enlinkd.model.IsIsElement} object.
     */
    @Override
    public IsIsElement findByNodeId(Integer id) {
        return findUnique("from IsIsElement rec where rec.node.id = ?1", id);
    }

    @Override
    public IsIsElement findByIsIsSysId(String isisSysId) {
        return findUnique("from IsIsElement rec where rec.isisSysID = ?1",
                          isisSysId);
    }

    @Override
    public List<IsIsElement> findBySysIdOfIsIsLinksOfNode(int nodeId) {
        return find("from IsIsElement rec where rec.isisSysID in (select l.isisISAdjNeighSysID from IsIsLink l where l.node.id = ?1)", nodeId);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from IsIsElement rec where rec.node.id = ?1 ",
                                    new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from IsIsElement");
    }

}
