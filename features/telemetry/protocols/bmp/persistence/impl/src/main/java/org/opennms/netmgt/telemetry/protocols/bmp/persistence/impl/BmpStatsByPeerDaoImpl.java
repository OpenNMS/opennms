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
package org.opennms.netmgt.telemetry.protocols.bmp.persistence.impl;

import java.util.Date;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeerDao;

public class BmpStatsByPeerDaoImpl extends AbstractDaoHibernate<BmpStatsByPeer, Long> implements BmpStatsByPeerDao {
    public BmpStatsByPeerDaoImpl() {
        super(BmpStatsByPeer.class);
    }

    @Override
    public BmpStatsByPeer findByPeerAndIntervalTime(String peerHashId, Date intervalTime) {

        Criteria criteria = new Criteria(BmpStatsByPeer.class);
        criteria.addRestriction(new EqRestriction("peerHashId", peerHashId));
        criteria.addRestriction(new EqRestriction("timestamp", intervalTime));
        List<BmpStatsByPeer> bmpStatsByPeers = findMatching(criteria);
        if (bmpStatsByPeers != null && bmpStatsByPeers.size() > 0) {
            return bmpStatsByPeers.get(0);
        }
        return null;
    }
}
