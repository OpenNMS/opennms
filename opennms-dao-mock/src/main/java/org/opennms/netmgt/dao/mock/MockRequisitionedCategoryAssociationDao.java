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
package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao;
import org.opennms.netmgt.model.RequisitionedCategoryAssociation;

public class MockRequisitionedCategoryAssociationDao extends AbstractMockDao<RequisitionedCategoryAssociation, Integer> implements RequisitionedCategoryAssociationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final RequisitionedCategoryAssociation cat) {
        Integer retval = super.save(cat);
        updateSubObjects(cat);
        return retval;
    }

    private void updateSubObjects(final RequisitionedCategoryAssociation cat) {
        getNodeDao().save(cat.getNode());
        getCategoryDao().save(cat.getCategory());
    }

    @Override
    public List<RequisitionedCategoryAssociation> findByNodeId(final Integer nodeId) {
        final List<RequisitionedCategoryAssociation> ret = new ArrayList<>();
        if (nodeId != null) {
            for (final RequisitionedCategoryAssociation assoc : findAll()) {
                if (assoc.getNode() != null && nodeId.equals(assoc.getNode().getId())) {
                    ret.add(assoc);
                }
            }
        }
        return ret;
    }

    @Override
    protected Integer getId(final RequisitionedCategoryAssociation entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final RequisitionedCategoryAssociation entity) {
        entity.setId(m_id.incrementAndGet());
    }

}
