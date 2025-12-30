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

import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class SnmpCollectionSourceDaoHibernate extends AbstractDaoHibernate<SnmpCollectionSource, Long> implements SnmpCollectionSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSourceDaoHibernate.class);

    public SnmpCollectionSourceDaoHibernate() {
        super(SnmpCollectionSource.class);
    }

    @Override
    public SnmpCollectionSource get(Long id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionSource findByName(String name) {
        List<SnmpCollectionSource> list = find("from SnmpCollectionSource s where s.name = ?", name);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionSource> findAllEnabled() {
        return find("from SnmpCollectionSource s where s.enabled = true");
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionSource> list) {
        super.deleteAll(list);
    }
}
