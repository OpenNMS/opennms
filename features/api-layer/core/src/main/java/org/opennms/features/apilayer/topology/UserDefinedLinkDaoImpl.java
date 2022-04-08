/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
