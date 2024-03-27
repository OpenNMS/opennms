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
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollector;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollectorDao;

import com.google.common.base.Strings;

public class BmpCollectorDaoImpl extends AbstractDaoHibernate<BmpCollector, Long> implements BmpCollectorDao {
    public BmpCollectorDaoImpl() {
        super(BmpCollector.class);
    }

    @Override
    public BmpCollector findByCollectorHashId(String hashId) {
        if (Strings.isNullOrEmpty(hashId)) {
            return null;
        }
        Criteria criteria = new Criteria(BmpCollector.class);
        criteria.addRestriction(new EqRestriction("hashId", hashId));
        List<BmpCollector> bmpCollectors = findMatching(criteria);
        if (bmpCollectors != null && bmpCollectors.size() > 0) {
            return bmpCollectors.get(0);
        }
        return null;
    }
}
