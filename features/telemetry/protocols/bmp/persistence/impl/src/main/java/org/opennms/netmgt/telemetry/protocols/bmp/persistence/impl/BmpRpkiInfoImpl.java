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
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfoDao;

public class BmpRpkiInfoImpl extends AbstractDaoHibernate<BmpRpkiInfo, Long> implements BmpRpkiInfoDao {
    public BmpRpkiInfoImpl() {
        super(BmpRpkiInfo.class);
    }

    @Override
    public BmpRpkiInfo findBmpRpkiInfoWith(String prefix, Integer prefixLenMax, Long originAsn) {
        Criteria criteria = new Criteria(BmpRpkiInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("prefixLenMax", prefixLenMax));
        criteria.addRestriction(new EqRestriction("originAs", originAsn));
        List<BmpRpkiInfo> bmpRpkiInfos = findMatching(criteria);
        if (bmpRpkiInfos != null && bmpRpkiInfos.size() > 0) {
            return bmpRpkiInfos.get(0);
        }
        return null;
    }

    @Override
    public BmpRpkiInfo findMatchingRpkiInfoForGlobalRIb(String prefix, Integer prefixLen) {
        Criteria criteria = new Criteria(BmpRpkiInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new LeRestriction("prefixLenMax", prefixLen));
        criteria.addRestriction(new GeRestriction("prefixLen", prefixLen));
        List<BmpRpkiInfo> bmpRpkiInfos = findMatching(criteria);
        if (bmpRpkiInfos != null && bmpRpkiInfos.size() > 0) {
            return bmpRpkiInfos.get(0);
        }
        return null;
    }
}
