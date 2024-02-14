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
package org.opennms.features.topology.plugins.topo.application.browsers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.browsers.OnmsContainerDatasource;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;

public class ApplicationOutageDatasource implements OnmsContainerDatasource<ApplicationOutage, Integer> {

    private final OutageDao dao;

    public ApplicationOutageDatasource(final OutageDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot add new items to this container");
    }

    @Override
    public void delete(Integer itemId) {
        throw new UnsupportedOperationException("Cannot remove items from this container");
    }

    @Override
    public List<ApplicationOutage> findMatching(Criteria criteria) {
        criteria.setClass(OnmsOutage.class); // Map it back to delegate class
        return dao.findMatching(criteria).stream()
                .map(ApplicationOutage::new)
                .collect(Collectors.toList());
    }

    @Override
    public int countMatching(Criteria criteria) {
        criteria.setClass(OnmsOutage.class); // Map it back to delegate class
        return dao.countMatching(criteria);
    }

    @Override
    public ApplicationOutage createInstance(Class<ApplicationOutage> itemClass) {
        return new ApplicationOutage(new OnmsOutage());
    }
}
