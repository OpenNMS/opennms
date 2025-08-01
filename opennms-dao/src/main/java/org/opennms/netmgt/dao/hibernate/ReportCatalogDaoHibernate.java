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

import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;

/**
 * <p>ReportCatalogDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportCatalogDaoHibernate extends
        AbstractDaoHibernate<ReportCatalogEntry, Integer> implements
        ReportCatalogDao {

    /**
     * <p>Constructor for ReportCatalogDaoHibernate.</p>
     */
    public ReportCatalogDaoHibernate() {
        super(ReportCatalogEntry.class);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(Integer id) {
        ReportCatalogEntry catalogEntry = (ReportCatalogEntry)getHibernateTemplate().get(ReportCatalogEntry.class, id);
        super.delete(catalogEntry);

    }


}
