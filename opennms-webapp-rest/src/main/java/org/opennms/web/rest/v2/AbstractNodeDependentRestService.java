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
package org.opennms.web.rest.v2;

import java.io.Serializable;

import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.support.Aliases;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract Web Service using REST for entities that depend on OnmsNode.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractNodeDependentRestService<T,Q,K extends Serializable,I extends Serializable> extends AbstractDaoRestService<T,Q,K,I> {

    @Autowired
    protected NodeDao m_nodeDao;

    protected void updateCriteria(final UriInfo uriInfo, final CriteriaBuilder builder) {
        builder.alias("node", Aliases.node.toString());
        final String nodeCriteria = getNodeCriteria(uriInfo);
        if (nodeCriteria.contains(":")) {
            String[] parts = nodeCriteria.split(":");
            builder.eq("node.foreignSource", parts[0]);
            builder.eq("node.foreignId", parts[1]);
        } else {
            builder.eq("node.id", Integer.parseInt(nodeCriteria));
        }
    }

    protected OnmsNode getNode(final UriInfo uriInfo) {
        final String lookupCriteria = getNodeCriteria(uriInfo);
        return m_nodeDao.get(lookupCriteria);
    }

    private String getNodeCriteria(final UriInfo uriInfo) {
        return uriInfo.getPathSegments(true).get(1).getPath();
    }

}
