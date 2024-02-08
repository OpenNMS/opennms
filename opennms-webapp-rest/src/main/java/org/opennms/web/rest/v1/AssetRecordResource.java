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
package org.opennms.web.rest.v1;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.ISO8601DateEditor;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

@Component("assetRecordResource")
@Path("assetRecord")
@Tag(name = "Asset Records", description = "Asset Records API")
@Transactional
public class AssetRecordResource extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRecordResource.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AssetRecordDao m_assetRecordDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * <p>getAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsAssetRecord getAssetRecord(@PathParam("nodeCriteria") final String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "getAssetRecord: Can't find node " + nodeCriteria);
        }
        return getAssetRecord(node);
    }
    /**
     * <p>updateAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateAssetRecord(@PathParam("nodeCriteria") final String nodeCriteria
            , @Context final HttpServletRequest request
            , final MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "updateAssetRecord: Can't find node " + nodeCriteria);
        }

        OnmsAssetRecord assetRecord = getAssetRecord(node);
        if (assetRecord == null) {
            throw getException(Status.BAD_REQUEST, "updateAssetRecord: Node " + node  + " could not update ");
        }
        if (assetRecord.getGeolocation() == null) {
            assetRecord.setGeolocation(new OnmsGeolocation());
        }
        LOG.debug("updateAssetRecord: updating asset {}", assetRecord);
        boolean modified = false;
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(assetRecord);
        wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
                modified = true;
            }
        }
        if (modified) {
            LOG.debug("updateAssetRecord: assetRecord {} updated", assetRecord);
            assetRecord.setLastModifiedBy(Strings.nullToEmpty(request.getRemoteUser()));
            assetRecord.setLastModifiedDate(new Date());
            node.setAssetRecord(assetRecord);
            m_nodeDao.saveOrUpdate(node);
            try {
                sendEvent(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, node.getId());
            } catch (EventProxyException e) {
                throw getException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return Response.noContent().build();
        }

        return Response.notModified().build();
    }

    private static OnmsAssetRecord getAssetRecord(OnmsNode node) {
        return node.getAssetRecord();
    }
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        EventBuilder bldr = new EventBuilder(uei, "ReST");
        bldr.setNodeid(nodeId);
        m_eventProxy.send(bldr.getEvent());
    }

}
