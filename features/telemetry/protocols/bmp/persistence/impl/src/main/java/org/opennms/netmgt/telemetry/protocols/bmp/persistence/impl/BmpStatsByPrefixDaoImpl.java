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
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefixDao;

public class BmpStatsByPrefixDaoImpl extends AbstractDaoHibernate<BmpStatsByPrefix, Long> implements BmpStatsByPrefixDao {
    public BmpStatsByPrefixDaoImpl() {
        super(BmpStatsByPrefix.class);
    }

    @Override
    public BmpStatsByPrefix findByPrefixAndIntervalTime(String peerHashId, String prefix, Date intervalTime) {
        Criteria criteria = new Criteria(BmpStatsByPrefix.class);
        criteria.addRestriction(new EqRestriction("peerHashId", peerHashId));
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("timestamp", intervalTime));
        List<BmpStatsByPrefix> bmpStatsByPrefixList = findMatching(criteria);
        if (bmpStatsByPrefixList != null && bmpStatsByPrefixList.size() > 0) {
            return bmpStatsByPrefixList.get(0);
        }
        return null;
    }
}
