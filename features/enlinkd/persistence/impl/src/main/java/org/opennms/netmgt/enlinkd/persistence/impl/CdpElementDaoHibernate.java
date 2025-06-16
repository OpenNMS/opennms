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

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.slf4j.LoggerFactory;

public class CdpElementDaoHibernate extends AbstractDaoHibernate<CdpElement, Integer> implements CdpElementDao {

    /**
     * <p>
     * Constructor for CdpElementDaoHibernate.
     * </p>
     */
    public CdpElementDaoHibernate() {
        super(CdpElement.class);
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
    public CdpElement findByNodeId(Integer id) {
        return findUnique("from CdpElement rec where rec.node.id = ?", id);
    }

    @Override
    public CdpElement findByGlobalDeviceId(String deviceId) {
        List<CdpElement> elements = find("from CdpElement rec where rec.cdpGlobalDeviceId = ? order by rec.id",
                                         deviceId);
        if (elements.size() > 1) {
            LoggerFactory.getLogger(getClass()).warn("Expected 1 CdpElement for device with id '{}' but found {}. Using CdpElement {} and ignoring others.",
                                                     deviceId,
                                                     elements.size(),
                                                     elements.get(0));
        }
        return elements.isEmpty() ? null : elements.get(0);
    }

    @Override
    public List<CdpElement> findByCacheDeviceIdOfCdpLinksOfNode(int nodeId) {
        return find("from CdpElement rec where rec.cdpGlobalDeviceId in (select l.cdpCacheDeviceId from CdpLink l where l.node.id = ?)", nodeId);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from CdpElement rec where rec.node.id = ? ",
                                    new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from CdpElement");
    }


}
