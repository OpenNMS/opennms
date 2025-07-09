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

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.LldpElement;

import java.util.List;

public class LldpElementDaoHibernate extends AbstractDaoHibernate<LldpElement, Integer> implements LldpElementDao {

    /**
     * <p>
     * Constructor for LldpElementDaoHibernate.
     * </p>
     */
    public LldpElementDaoHibernate() {
        super(LldpElement.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.enlinkd.model.LldpElement} object.
     */
    @Override
    public LldpElement findByNodeId(Integer id) {
        return findUnique("from LldpElement rec where rec.node.id = ?1", id);
    }

    @Override
    public List<LldpElement> findByChassisId(String chassisId,
            LldpChassisIdSubType type) {
        return find("from LldpElement rec where rec.lldpChassisId = ?1 and rec.lldpChassisIdSubType = ?2",
                    chassisId, type);
    }

    @Override
    public List<LldpElement> findByChassisOfLldpLinksOfNode(int nodeId) {
        return find("from LldpElement rec where exists (from LldpLink l where rec.lldpChassisId = l.lldpRemChassisId AND rec.lldpChassisIdSubType = l.lldpRemChassisIdSubType AND l.node.id = ?1)",
                nodeId);
    }

    @Override
    public LldpElement findBySysname(String sysname) {
        return findUnique("from LldpElement rec where rec.lldpSysname = ?1",
                          sysname);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from LldpElement rec where rec.node.id = ?1 ",
                                    new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from LldpElement");
    }

}
