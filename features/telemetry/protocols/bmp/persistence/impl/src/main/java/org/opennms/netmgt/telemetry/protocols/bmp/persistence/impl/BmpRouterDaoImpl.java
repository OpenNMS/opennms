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

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;

import com.google.common.base.Strings;

public class BmpRouterDaoImpl extends AbstractDaoHibernate<BmpRouter, Long> implements BmpRouterDao {
    public BmpRouterDaoImpl() {
        super(BmpRouter.class);
    }

    @Override
    public BmpRouter findByRouterHashId(String hashId) {
        if (Strings.isNullOrEmpty(hashId)) {
            return null;
        }
        Criteria criteria = new Criteria(BmpRouter.class);
        criteria.addRestriction(new EqRestriction("hashId", hashId));
        List<BmpRouter> bmpRouters = findMatching(criteria);
        if (bmpRouters != null && bmpRouters.size() > 0) {
            return bmpRouters.get(0);
        }
        return null;
    }

    @Override
    public List<BmpRouter> findRoutersByCollectorHashId(String collectorHashId) {
        if (Strings.isNullOrEmpty(collectorHashId)) {
            return new ArrayList<>();
        }
        Criteria criteria = new Criteria(BmpRouter.class);
        criteria.addRestriction(new EqRestriction("collectorHashId", collectorHashId));
        return findMatching(criteria);
    }
}
