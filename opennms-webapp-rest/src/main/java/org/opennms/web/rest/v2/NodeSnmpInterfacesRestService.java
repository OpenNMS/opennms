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

import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsSnmpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeSnmpInterfacesRestService extends AbstractNodeDependentRestService<OnmsSnmpInterface,OnmsSnmpInterface,Integer,Integer> {

    @Autowired
    private SnmpInterfaceDao m_ipInterfaceDao;

    @Override
    protected SnmpInterfaceDao getDao() {
        return m_ipInterfaceDao;
    }

    @Override
    protected Class<OnmsSnmpInterface> getDaoClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected Class<OnmsSnmpInterface> getQueryBeanClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        updateCriteria(uriInfo, builder);
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsSnmpInterface> createListWrapper(Collection<OnmsSnmpInterface> list) {
        return new OnmsSnmpInterfaceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface snmpInterface) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (snmpInterface == null) {
            throw getException(Status.BAD_REQUEST, "SNMP Interface object cannot be null");
        } else if (snmpInterface.getIfIndex() == null) {
            throw getException(Status.BAD_REQUEST, "SNMP Interface's ifIndex cannot be null");
        }
        node.addSnmpInterface(snmpInterface);
        if (snmpInterface.getPrimaryIpInterface() != null) {
            final OnmsIpInterface iface = snmpInterface.getPrimaryIpInterface();
            iface.setSnmpInterface(snmpInterface);
        }
        getDao().save(snmpInterface);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, snmpInterface.getIfIndex())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface targetObject, MultivaluedMapImpl params) {
        if (params.getFirst("ifIndex") != null) {
            throw getException(Status.BAD_REQUEST, "Cannot change ifIndex.");
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface intf) {
        intf.getNode().getSnmpInterfaces().remove(intf);
        getDao().delete(intf);
    }

    @Override
    protected OnmsSnmpInterface doGet(UriInfo uriInfo, Integer ifIndex) {
        final OnmsNode node = getNode(uriInfo);
        return node == null ? null : node.getSnmpInterfaceWithIfIndex(ifIndex);
    }

}
