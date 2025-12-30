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

import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class SnmpCollectionMibGroupDaoHibernate extends AbstractDaoHibernate<SnmpCollectionMibGroup, Long> implements SnmpCollectionMibGroupDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionMibGroupDaoHibernate.class);

    public SnmpCollectionMibGroupDaoHibernate( ) {
        super(SnmpCollectionMibGroup.class);
    }

    @Override
    public SnmpCollectionMibGroup get(Long id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionMibGroup findByNameAndSource(String name, Long sourceId) {
        List<SnmpCollectionMibGroup> list = find(
                "from SnmpCollectionMibGroup s where s.name = ? and s.collectionSource.id = ?", name, sourceId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllEnabled() {
        return find("from SnmpCollectionMibGroup s where s.enabled = true");
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllBySource(Long sourceId) {
        return find("from SnmpCollectionMibGroup s where s.collectionSource.id = ?", sourceId);
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionMibGroup> list) {
        super.deleteAll(list);
    }
}
