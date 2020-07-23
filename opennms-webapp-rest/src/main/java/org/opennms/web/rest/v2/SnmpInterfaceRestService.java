/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsSnmpInterface} entity.
 *
 * <p>This end-point exist to retrieve and update a set of SNMP interfaces,
 * based on a given criteria.</p>
 */
@Component
@Path("snmpinterfaces")
@Transactional
public class SnmpInterfaceRestService extends AbstractDaoRestService<OnmsSnmpInterface,SearchBean,Integer,String> {

    @Autowired
    private SnmpInterfaceDao m_dao;

    @Override
    protected SnmpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsSnmpInterface> getDaoClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        // 1st level JOINs
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        return builder;
    }

    @Override
    protected final JaxbListWrapper<OnmsSnmpInterface> createListWrapper(Collection<OnmsSnmpInterface> list) {
        return new OnmsSnmpInterfaceList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.SNMP_INTERFACE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        return map;
    }

    @Override
    protected final OnmsSnmpInterface doGet(final UriInfo uriInfo, final String id) {
        return getDao().get(Integer.valueOf(id));
    }
}
