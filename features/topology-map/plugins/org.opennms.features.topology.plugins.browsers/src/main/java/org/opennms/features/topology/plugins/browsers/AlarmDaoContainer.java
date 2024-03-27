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
package org.opennms.features.topology.plugins.browsers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.transaction.support.TransactionOperations;

public class AlarmDaoContainer extends OnmsVaadinContainer<OnmsAlarm,Integer> {

    private static final long serialVersionUID = -4026870931086916312L;

    public AlarmDaoContainer(AlarmDao dao, TransactionOperations transactionTemplate) {
        super(OnmsAlarm.class, new OnmsDaoContainerDatasource<>(dao, transactionTemplate));
        addBeanToHibernatePropertyMapping("nodeLabel", "node.label");
    }

    @Override
    protected void updateContainerPropertyIds(Map<Object, Class<?>> properties) {
        // Causes problems because it is a map of values
        properties.remove("details");

        // Causes referential integrity problems
        // @see http://issues.opennms.org/browse/NMS-5750
        properties.remove("distPoller");
    }

    @Override
    protected Integer getId(OnmsAlarm bean){
        return bean == null ? null : bean.getId();
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        Collection<Object> propertyIds = new HashSet<>();
        propertyIds.addAll(getContainerPropertyIds());

        // This column is a checkbox so we can't sort on it either
        propertyIds.remove("selection");

        return Collections.unmodifiableCollection(propertyIds);
    }

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {
        // we join table node, to be able to sort by node.label
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.Alarm;
    }
}
