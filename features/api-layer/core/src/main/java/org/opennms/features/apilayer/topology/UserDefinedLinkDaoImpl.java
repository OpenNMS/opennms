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
package org.opennms.features.apilayer.topology;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.topology.UserDefinedLink;
import org.opennms.integration.api.v1.topology.UserDefinedLinkDao;
import org.opennms.integration.api.v1.topology.immutables.ImmutableUserDefinedLink;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService;

public class UserDefinedLinkDaoImpl implements UserDefinedLinkDao {

    /**
     * Entity DAO which should only be used for READ operations.
     */
    private final org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao userDefinedLinkDao;

    /**
     * Topology service which should be used for all WRITE operations.
     */
    private final UserDefinedLinkTopologyService userDefinedLinkTopologyService;

    private final SessionUtils sessionUtils;

    public UserDefinedLinkDaoImpl(org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao userDefinedLinkDao,
                                  UserDefinedLinkTopologyService userDefinedLinkTopologyService,
                                  SessionUtils sessionUtils) {
        this.userDefinedLinkDao = Objects.requireNonNull(userDefinedLinkDao);
        this.userDefinedLinkTopologyService = Objects.requireNonNull(userDefinedLinkTopologyService);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public List<UserDefinedLink> getLinks() {
        return sessionUtils.withReadOnlyTransaction(() ->
                userDefinedLinkDao.findAll().stream().map(UserDefinedLinkDaoImpl::toApiLink).collect(Collectors.toList()));
    }

    @Override
    public List<UserDefinedLink> getOutLinks(int nodeIdA) {
        return sessionUtils.withReadOnlyTransaction(() ->
                userDefinedLinkDao.getOutLinks(nodeIdA).stream().map(UserDefinedLinkDaoImpl::toApiLink).collect(Collectors.toList()));
    }

    @Override
    public List<UserDefinedLink> getInLinks(int nodeIdZ) {
        return sessionUtils.withReadOnlyTransaction(() ->
                userDefinedLinkDao.getInLinks(nodeIdZ).stream().map(UserDefinedLinkDaoImpl::toApiLink).collect(Collectors.toList()));}

    @Override
    public List<UserDefinedLink> getLinksWithLabel(String label) {
        return sessionUtils.withReadOnlyTransaction(() ->
                userDefinedLinkDao.getLinksWithLabel(label).stream().map(UserDefinedLinkDaoImpl::toApiLink).collect(Collectors.toList()));
    }

    @Override
    public UserDefinedLink saveOrUpdate(UserDefinedLink link) {
        return sessionUtils.withTransaction(() -> {
            final org.opennms.netmgt.enlinkd.model.UserDefinedLink modelLink = toModelLink(link);
            userDefinedLinkTopologyService.saveOrUpdate(modelLink);
            return toApiLink(modelLink);
        });
    }

    @Override
    public void delete(UserDefinedLink link) {
        sessionUtils.withTransaction(() -> {
            userDefinedLinkTopologyService.delete(link.getDbId());
            return null;
        });
    }

    @Override
    public void delete(Collection<UserDefinedLink> links) {
        sessionUtils.withTransaction(() -> {
            for (UserDefinedLink link : links) {
                userDefinedLinkTopologyService.delete(link.getDbId());
            }
            return null;
        });
    }

    protected static UserDefinedLink toApiLink(org.opennms.netmgt.enlinkd.model.UserDefinedLink modelLink) {
        return ImmutableUserDefinedLink.newBuilder()
                .setDbId(modelLink.getDbId())
                .setOwner(modelLink.getOwner())
                .setLinkId(modelLink.getLinkId())
                .setLinkLabel(modelLink.getLinkLabel())
                .setNodeIdA(modelLink.getNodeIdA())
                .setNodeIdZ(modelLink.getNodeIdZ())
                .setComponentLabelA(modelLink.getComponentLabelA())
                .setComponentLabelZ(modelLink.getComponentLabelZ())
                .build();
    }

    protected static org.opennms.netmgt.enlinkd.model.UserDefinedLink toModelLink(UserDefinedLink apiLink) {
        final org.opennms.netmgt.enlinkd.model.UserDefinedLink modelLink = new org.opennms.netmgt.enlinkd.model.UserDefinedLink();
        modelLink.setDbId(apiLink.getDbId());
        modelLink.setOwner(apiLink.getOwner());
        modelLink.setLinkId(apiLink.getLinkId());
        modelLink.setLinkLabel(apiLink.getLinkLabel());
        modelLink.setNodeIdA(apiLink.getNodeIdA());
        modelLink.setNodeIdZ(apiLink.getNodeIdZ());
        modelLink.setComponentLabelA(apiLink.getComponentLabelA());
        modelLink.setComponentLabelZ(apiLink.getComponentLabelZ());
        return modelLink;
    }
}
