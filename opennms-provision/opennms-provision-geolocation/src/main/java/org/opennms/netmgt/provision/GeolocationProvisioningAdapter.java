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
package org.opennms.netmgt.provision;

import java.util.Objects;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.features.geolocation.api.Coordinates;
import org.opennms.features.geolocation.api.GeolocationResolver;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Strings;

public class GeolocationProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(GeolocationProvisioningAdapter.class);

    private boolean enabled = Boolean.valueOf(System.getProperty("org.opennms.provisiond.resolveMissingCoordinatesFromAddressString", "true"));

    private NodeDao nodeDao;

    private ServiceRegistry serviceRegistry;

    public GeolocationProvisioningAdapter() {
        super("GeolocationProvisioningAdapter");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        serviceRegistry = BeanUtils.getBean("soaContext", "serviceRegistry", ServiceRegistry.class);
        Objects.requireNonNull(nodeDao);
        Objects.requireNonNull(serviceRegistry);
    }

    @Override
    public void doAddNode(int nodeId) {
        LOG.debug("Invoked doAddNode on node with id {}.", nodeId);
        doUpdateNodeInternal(nodeId);
    }

    @Override
    public void doUpdateNode(int nodeId) {
        LOG.debug("Invoked doUpdateNode on node with id {}.", nodeId);
        doUpdateNodeInternal(nodeId);
    }

    @Override
    public void doDeleteNode(int nodeid) {
        LOG.debug("Invoked doDeleteNode on node with id {}. Nothing to do.", nodeid);
    }

    @Override
    public void doNotifyConfigChange(int nodeid) {
        LOG.debug("Invoked doNotifyConfigChange on node with id {}. Nothing to do.", nodeid);
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    private OnmsNode getNode(int nodeId) {
        final OnmsNode onmsNode = nodeDao.get(nodeId);
        if (onmsNode == null) {
            throw new ProvisioningAdapterException("Node with id " + nodeId + " not found in database");
        }
        return onmsNode;
    }

    private GeolocationResolver getGeolocationResolver() {
        return serviceRegistry.findProvider(GeolocationResolver.class);
    }

    private void doUpdateNodeInternal(int nodeId) {
        if (!enabled || getGeolocationResolver() == null) {
            LOG.info("{} is either disabled manually or no GeocoderService is available (disabled={}, GeocoderService available={})", getName(), !enabled, getGeolocationResolver() != null);
            return;
        }

        // Update geolocation information if required
        final OnmsNode node = getNode(nodeId);
        updateGeolocation(getGeolocationResolver(), node);
    }

    protected void updateGeolocation(GeolocationResolver geolocationResolver, OnmsNode node) {
        Objects.requireNonNull(geolocationResolver);
        Objects.requireNonNull(node);

        // Only resolve long/lat if not already set and an address is set
        final OnmsGeolocation geolocation = node.getAssetRecord().getGeolocation();
        if (geolocation != null
                && geolocation.getLatitude() == null
                && geolocation.getLatitude() == null
                && !Strings.isNullOrEmpty(geolocation.asAddressString())) {

            final Coordinates coordinates = geolocationResolver.resolve(geolocation.asAddressString());
            if (coordinates != null) {
                geolocation.setLongitude(coordinates.getLongitude());
                geolocation.setLatitude(coordinates.getLatitude());
                nodeDao.saveOrUpdate(node);
            } else {
                LOG.warn("Could not resolve address string '{}' for node with id {}", geolocation.asAddressString(), node.getId());
            }
        }
    }

}
