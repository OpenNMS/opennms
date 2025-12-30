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
package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class SnmpCollectionResourceTypeDaoHibernate extends AbstractDaoHibernate<SnmpCollectionResourceType, Long>  implements SnmpCollectionResourceTypeDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionResourceTypeDaoHibernate.class);


    public SnmpCollectionResourceTypeDaoHibernate() {
        super(SnmpCollectionResourceType.class);
    }

    @Override
    public SnmpCollectionResourceType get(Long id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionResourceType findByNameAndSource(String name, Long sourceId) {
        List<SnmpCollectionResourceType> list = find(
                "from SnmpCollectionResourceType t where t.name = ? and t.collectionSource.id = ?", name, sourceId);
        return list.isEmpty() ? null : list.get(0);    }

    @Override
    public List<SnmpCollectionResourceType> findAllBySource(Long sourceId) {
        return find("from SnmpCollectionResourceType t where t.collectionSource.id = ?", sourceId);
    }

    @Override
    public List<SnmpCollectionResourceType> findAllEnabled() {
        return find("from SnmpCollectionResourceType t where t.enabled = true");
    }
    @Override
    public void deleteAll(final Collection<SnmpCollectionResourceType> list) {
        super.deleteAll(list);
    }
}
