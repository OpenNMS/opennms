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
package org.opennms.features.apilayer.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.LocationUtils;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class NodeDaoImpl implements NodeDao {

    private final org.opennms.netmgt.dao.api.NodeDao nodeDao;
    private final SessionUtils sessionUtils;

    public NodeDaoImpl(org.opennms.netmgt.dao.api.NodeDao nodeDao, SessionUtils sessionUtils) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public String getDefaultLocationName() {
        return LocationUtils.DEFAULT_LOCATION_NAME;
    }

    @Override
    public List<Node> getNodes() {
        return sessionUtils.withReadOnlyTransaction(() ->
                nodeDao.findAll().stream().map(ModelMappers::toNode).collect(Collectors.toList()));
    }

    @Override
    public Long getNodeCount() {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class);
        return sessionUtils.withReadOnlyTransaction(() -> (long)nodeDao.countMatching(criteriaBuilder.toCriteria()));
    }

    @Override
    public List<Integer> getNodeIds() {
        return sessionUtils.withReadOnlyTransaction(() -> Lists.newArrayList(nodeDao.getNodeIds()));
    }

    @Override
    public Node getNodeByCriteria(String nodeCriteria) {
        return sessionUtils.withReadOnlyTransaction(() -> ModelMappers.toNode(nodeDao.get(nodeCriteria)));
    }

    @Override
    public Node getNodeById(Integer nodeId) {
        return sessionUtils.withReadOnlyTransaction(() -> ModelMappers.toNode(nodeDao.get(nodeId)));
    }

    @Override
    public Node getNodeByLabel(String nodeLabel) {
        return sessionUtils.withReadOnlyTransaction(() -> ModelMappers.toNode(nodeDao.findByLabel(nodeLabel).stream()
                .min(Comparator.comparingInt(OnmsNode::getId))
                .orElse(null)));
    }

    @Override
    public Node getNodeByForeignSourceAndForeignId(String foreignSource, String foreignId) {
        return sessionUtils.withReadOnlyTransaction(() ->
                ModelMappers.toNode(nodeDao.findByForeignId(foreignSource, foreignId)));
    }

    @Override
    public List<Node> getNodesInLocation(String locationName) {
        final Criteria criteria = new CriteriaBuilder(OnmsNode.class)
                .alias("location", "location", Alias.JoinType.LEFT_JOIN)
                .eq("location.id", locationName)
                .toCriteria();
        return getNodesMatching(criteria);
    }

    @Override
    public List<Node> getNodesInForeignSource(String foreignSource) {
        final Criteria criteria = new CriteriaBuilder(OnmsNode.class)
                .eq("foreignSource", foreignSource)
                .toCriteria();
        return getNodesMatching(criteria);
    }

    private List<Node> getNodesMatching(Criteria criteria) {
        return sessionUtils.withReadOnlyTransaction(() ->
                nodeDao.findMatching(criteria).stream().map(ModelMappers::toNode).collect(Collectors.toList()));
    }
}
