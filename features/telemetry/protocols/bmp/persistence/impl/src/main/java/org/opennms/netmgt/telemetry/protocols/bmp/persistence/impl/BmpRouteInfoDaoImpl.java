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

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;

public class BmpRouteInfoDaoImpl extends AbstractDaoHibernate<BmpRouteInfo, Long> implements BmpRouteInfoDao {

    public BmpRouteInfoDaoImpl() {
        super(BmpRouteInfo.class);
    }

    @Override
    public BmpRouteInfo findByPrefixAndOriginAs(String prefix, Integer prefixLen, Long originAsn) {

        Criteria criteria = new Criteria(BmpRouteInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("prefixLen", prefixLen));
        criteria.addRestriction(new EqRestriction("originAs", originAsn));
        List<BmpRouteInfo> bmpRouteInfoList = findMatching(criteria);
        if (bmpRouteInfoList != null && bmpRouteInfoList.size() > 0) {
            return bmpRouteInfoList.get(0);
        }
        return null;
    }

    @Override
    public List<BmpRouteInfo> findByPrefix(String prefix) {
        Criteria criteria = new Criteria(BmpRouteInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        return findMatching(criteria);
    }

}
