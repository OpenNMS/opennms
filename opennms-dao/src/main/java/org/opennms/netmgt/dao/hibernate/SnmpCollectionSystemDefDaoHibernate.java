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

import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class SnmpCollectionSystemDefDaoHibernate extends AbstractDaoHibernate<SnmpCollectionSystemDef, Integer> implements SnmpCollectionSystemDefDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSystemDefDaoHibernate.class);

    public SnmpCollectionSystemDefDaoHibernate() {
        super(SnmpCollectionSystemDef.class);
    }

    @Override
    public SnmpCollectionSystemDef get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionSystemDef findByNameAndSource(String name, Integer sourceId) {
        List<SnmpCollectionSystemDef> list = find(
                "from SnmpCollectionSystemDef d where d.name = ? and d.collectionSource.id = ?", name, sourceId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionSystemDef> findAllEnabled() {
        return find("from SnmpCollectionSystemDef d where d.enabled = true");

    }

    @Override
    public List<SnmpCollectionSystemDef> findAllBySource(Integer sourceId) {
        return find("from SnmpCollectionSystemDef d where d.collectionSource.id = ?", sourceId);
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionSystemDef> list) {
        super.deleteAll(list);
    }
}
