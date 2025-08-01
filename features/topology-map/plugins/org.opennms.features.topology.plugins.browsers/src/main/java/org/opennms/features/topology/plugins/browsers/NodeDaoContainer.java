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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.OnmsContainerDatasource;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.transaction.support.TransactionOperations;

public class NodeDaoContainer extends OnmsVaadinContainer<OnmsNode,Integer> {

	private static final long serialVersionUID = -5697472655705494537L;

    public static class NodeDaoContainerDatasource extends OnmsDaoContainerDatasource<OnmsNode, Integer> {
        public NodeDaoContainerDatasource(OnmsDao<OnmsNode, Integer> dao, TransactionOperations transactionTemplate) {
            super(dao, transactionTemplate);
        }

        @Override
        public void findMatchingCallback(OnmsNode node) {
            node.getPrimaryInterface();
        }
    }

	public NodeDaoContainer(NodeDao dao, TransactionOperations transactionTemplate) {
	    super(OnmsNode.class, new NodeDaoContainerDatasource(dao, transactionTemplate));
        addBeanToHibernatePropertyMapping("primaryInterface", "ipInterfaces.ipAddress");
	}

	@Override
	protected Integer getId(OnmsNode bean){
		return bean == null ? null : bean.getId();
	}

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {
        if (!doOrder) return;
        // We join the ipInterfaces table, to be able to sort on ipInterfaces.ipAddress.
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("ipInterfaces", "ipInterfaces", Alias.JoinType.LEFT_JOIN, new EqRestriction("ipInterfaces.snmpPrimary", PrimaryType.PRIMARY.getCharCode()))
        }));
    }

    @Override
    protected List<OnmsNode> getItemsForCache(final OnmsContainerDatasource<OnmsNode, Integer> datasource, final Page page) {
        // The join criteria isSnmpPrimary = PrimaryType.PRIMARY is used to only consider primary interfaces.
        // It is supposed that only one primary interface exists, but that is not always the case.
        // In order to create a valid result set, we "unify" the result.
        // See http://issues.opennms.org/browse/NMS-8079 for more details
        List<OnmsNode> itemsForCache = super.getItemsForCache(datasource, page);
        return new ArrayList<>(new LinkedHashSet<>(itemsForCache));
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.Node;
    }
}


