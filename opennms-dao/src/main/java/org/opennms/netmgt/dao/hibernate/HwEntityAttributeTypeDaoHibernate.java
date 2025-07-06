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

import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;

/**
 * The Class HwEntityAttributeTypeDaoHibernate.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HwEntityAttributeTypeDaoHibernate extends AbstractDaoHibernate<HwEntityAttributeType, Integer> implements HwEntityAttributeTypeDao {

    /**
     * The Constructor.
     */
    public HwEntityAttributeTypeDaoHibernate() {
        super(HwEntityAttributeType.class);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao#findTypeByName(java.lang.String)
     */
    @Override
    public HwEntityAttributeType findTypeByName(String name) {
        return (HwEntityAttributeType) findUnique("from HwEntityAttributeType t where t.name = ?1", name);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao#findTypeByOid(java.lang.String)
     */
    @Override
    public HwEntityAttributeType findTypeByOid(String oid) {
        return (HwEntityAttributeType) findUnique("from HwEntityAttributeType t where t.oid = ?1", oid);
    }

}
