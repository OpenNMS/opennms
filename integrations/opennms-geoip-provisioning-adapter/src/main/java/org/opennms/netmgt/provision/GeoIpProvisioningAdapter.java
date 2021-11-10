/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.geoip.*;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import com.google.common.base.Strings;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import inet.ipaddr.IPAddressString;

@EventListener(name = GeoIpProvisioningAdapter.NAME)
public class GeoIpProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(GeoIpProvisioningAdapter.class);
    public static final String PREFIX = "Provisiond.";
    public static final String NAME = "GeoIpProvisioningAdapter";

    private NodeDao nodeDao;
    private EventForwarder eventForwarder;
    private GeoIpConfigDao geoIpConfigDao;
    private DatabaseReader databaseReader;
    private AssetRecordDao assetRecordDao;

    public GeoIpProvisioningAdapter() {
        super(NAME);
    }

    private DatabaseReader getDatabaseReader() throws IOException {
        if (databaseReader == null) {
            final GeoIpConfig geoIpConfig = geoIpConfigDao.getContainer().getObject();
            databaseReader = new DatabaseReader.Builder(new File(geoIpConfig.getDatabase())).build();
        }

        return databaseReader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(nodeDao, "Node DAO must not be null");
        Assert.notNull(assetRecordDao, "Asset Record DAO must not be null");
        Assert.notNull(eventForwarder, "Event Forwarder must not be null");
        Assert.notNull(geoIpConfigDao, "GeoIp Configuration DAO must not be null");
    }

    @Override
    public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
        queryNode(nodeId);
    }

    @Override
    public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
        queryNode(nodeId);
    }

    protected boolean isPublicAddress(final InetAddress address) {
        final boolean isUla = address.getAddress().length == 16 ? (address.getAddress()[0] & 0xfe) == 0xfc : false;

        return !(address.isSiteLocalAddress() ||
                address.isAnyLocalAddress() ||
                address.isLinkLocalAddress() ||
                address.isLoopbackAddress() ||
                address.isMulticastAddress() ||
                isUla);
    }

    public void queryNode(final int nodeId) {
        final GeoIpConfig geoIpConfig = geoIpConfigDao.getContainer().getObject();

        if (!geoIpConfig.isEnabled()) {
            return;
        }

        final OnmsNode node = nodeDao.get(nodeId);
        if (node == null) {
            LOG.debug("Failed to return node for given nodeId: {}", nodeId);
            return;
        }

        OnmsAssetRecord assetRecord = node.getAssetRecord();

        if (assetRecord == null) {
            assetRecord = new OnmsAssetRecord();
            assetRecord.setNode(node);
        }

        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        InetAddress addressToUse = null;

        if (primaryInterface != null) {
           addressToUse = primaryInterface.getIpAddress();
        } else {
            final Optional<OnmsIpInterface> optionalOnmsIpInterface = node.getIpInterfaces().stream().findFirst();
            if (optionalOnmsIpInterface.isPresent()) {
                addressToUse = optionalOnmsIpInterface.get().getIpAddress();
            }
       }


        if (GeoIpConfig.Resolve.PUBLIC.equals(geoIpConfig.getResolve()) || GeoIpConfig.Resolve.PUBLIC_IPV4.equals(geoIpConfig.getResolve()) || GeoIpConfig.Resolve.PUBLIC_IPV6.equals(geoIpConfig.getResolve())) {
            // prefer primary address if public
            if (addressToUse == null || !isPublicAddress(addressToUse) || !(
                    (isPublicAddress(addressToUse) && GeoIpConfig.Resolve.PUBLIC_IPV4.equals(geoIpConfig.getResolve()) && (addressToUse instanceof Inet4Address)) ||
                            (isPublicAddress(addressToUse) && GeoIpConfig.Resolve.PUBLIC_IPV6.equals(geoIpConfig.getResolve()) && (addressToUse instanceof Inet6Address)) ||
                            (isPublicAddress(addressToUse) && GeoIpConfig.Resolve.PUBLIC.equals(geoIpConfig.getResolve())))) {

                final Set<OnmsIpInterface> ipInterfaceSet = node.getIpInterfaces();
                for (final OnmsIpInterface onmsIpInterface : ipInterfaceSet) {
                    if (isPublicAddress(onmsIpInterface.getIpAddress())) {
                        if ((GeoIpConfig.Resolve.PUBLIC_IPV4.equals(geoIpConfig.getResolve()) && onmsIpInterface.getIpAddress() instanceof Inet4Address) ||
                                (GeoIpConfig.Resolve.PUBLIC_IPV6.equals(geoIpConfig.getResolve()) && onmsIpInterface.getIpAddress() instanceof Inet6Address) ||
                                (GeoIpConfig.Resolve.PUBLIC.equals(geoIpConfig.getResolve()))) {
                            addressToUse = onmsIpInterface.getIpAddress();
                            break;
                        }
                    }
                }
            }
        }

        if (addressToUse != null) {
            if (assetRecord.getGeolocation() == null) {
                assetRecord.setGeolocation(new OnmsGeolocation());
            }

            final Subnet subnet = getSubnet(geoIpConfig, node.getLocation().getLocationName(), InetAddressUtils.str(addressToUse));

            if (subnet != null) {
                for (final Asset asset : subnet.getAssets()) {
                    if ("id".equals(asset.getName()) || "node".equals(asset.getName())) {
                        continue;
                    }

                    try {
                        if (geoIpConfig.isOverwrite() || PropertyUtils.getProperty(assetRecord, asset.getName()) == null) {
                            try {
                                final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(asset.getName(), OnmsAssetRecord.class);
                                final String clazz = propertyDescriptor.getPropertyType().getSimpleName();

                                switch (clazz) {
                                    case "String": {
                                        PropertyUtils.setProperty(assetRecord, asset.getName(), asset.getValue());
                                        LOG.debug("Asset '{}' set to '{}' for node {} with IP address {} (config)...", asset.getName(), asset.getValue(), nodeId, InetAddressUtils.str(addressToUse));
                                        break;
                                    }
                                    case "Integer": {
                                        try {
                                            PropertyUtils.setProperty(assetRecord, asset.getName(), Integer.parseInt(asset.getValue()));
                                            LOG.warn("Asset '{}' set to '{}' for node {} with IP address {} (config)...", asset.getName(), asset.getValue(), nodeId, InetAddressUtils.str(addressToUse));
                                        } catch (NumberFormatException e) {
                                            LOG.debug("Error converting value '{}' to Integer...", asset.getValue(), e);
                                        }
                                        break;
                                    }
                                    case "Double": {
                                        try {
                                            PropertyUtils.setProperty(assetRecord, asset.getName(), Double.parseDouble(asset.getValue()));
                                        } catch (NumberFormatException e) {
                                            LOG.warn("Error converting value '{}' to Double...", asset.getValue(), e);
                                        }
                                        LOG.debug("Asset '{}' set to '{}' for node {} with IP address {} (config)...", asset.getName(), asset.getValue(), nodeId, InetAddressUtils.str(addressToUse));
                                        break;
                                    }
                                    default: {
                                        LOG.warn("Value type '{}' not supported", clazz);
                                    }
                                }
                            } catch (IntrospectionException e) {
                                LOG.warn("Error retrieving type class for property '{}'", asset.getName());
                            }
                        }
                    } catch (PropertyException e) {
                        LOG.error("Error setting bean property '{}'", asset.getName(), e);
                    }
                }

                assetRecordDao.saveOrUpdate(assetRecord);
                assetRecordDao.flush();
            } else {
                try {
                    if (!isPublicAddress(addressToUse)) {
                        return;
                    }

                    final CityResponse cityResponse = getDatabaseReader().city(addressToUse);

                    if (geoIpConfig.isOverwrite() || assetRecord.getLongitude() == null) {
                        assetRecord.setLongitude(cityResponse.getLocation().getLongitude());
                        LOG.debug("Asset 'longitude' set to '{}' for node {} with IP address {} (lookup)...", cityResponse.getLocation().getLongitude(), nodeId, InetAddressUtils.str(addressToUse));
                    }

                    if (geoIpConfig.isOverwrite() || assetRecord.getLatitude() == null) {
                        assetRecord.setLatitude(cityResponse.getLocation().getLatitude());
                        LOG.debug("Asset 'latitude' set to '{}' for node {} with IP address {} (lookup)...", cityResponse.getLocation().getLatitude(), nodeId, InetAddressUtils.str(addressToUse));
                    }

                    if (geoIpConfig.isOverwrite() || assetRecord.getCity() == null) {
                        if (cityResponse.getCity() != null && !Strings.isNullOrEmpty(cityResponse.getCity().getName())) {
                            assetRecord.setCity(cityResponse.getCity().getName());
                            LOG.debug("Asset 'city' set to '{}' for node {} with IP address {} (lookup)...", cityResponse.getCity().getName(), nodeId, InetAddressUtils.str(addressToUse));
                        }
                    }

                    if (geoIpConfig.isOverwrite() || assetRecord.getCountry() == null) {
                        if (cityResponse.getCountry() != null && !Strings.isNullOrEmpty(cityResponse.getCountry().getName())) {
                            assetRecord.setCountry(cityResponse.getCountry().getName());
                            LOG.debug("Asset 'country' set to '{}' for node {} with IP address {} (lookup)...", cityResponse.getCountry().getName(), nodeId, InetAddressUtils.str(addressToUse));
                        }
                    }

                    assetRecordDao.saveOrUpdate(assetRecord);
                    assetRecordDao.flush();
                } catch (IOException e) {
                    LOG.error("Error looking up location for IP address '" + InetAddressUtils.str(addressToUse) + "'", e);
                } catch (GeoIp2Exception e) {
                    LOG.warn("No location data found for IP address '" + InetAddressUtils.str(addressToUse) + "'");
                }
            }
        }
    }

    Subnet getSubnet(final GeoIpConfig config, final String locationName, final String addressToUse) {
        final IPAddressString ipAddressString = new IPAddressString(addressToUse);
        for (final Location location : config.getLocations()) {
            if (locationName.equals(location.getName())) {
                for (final Subnet subnet : location.getSubnets()) {
                    final IPAddressString subnetAddress = new IPAddressString(subnet.getCidr());
                    if (subnetAddress.contains(ipAddressString)) {
                        return subnet;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(final IEvent event) {
        if (isReloadConfigEventTarget(event)) {
            EventBuilder ebldr = null;
            LOG.debug("Reloading the Hardware Inventory adapter configuration");
            try {
                geoIpConfigDao.getContainer().reload();
                databaseReader = null;

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
            } catch (Throwable e) {
                LOG.warn("Unable to reload Hardware Inventory adapter configuration", e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
            }
            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
        }
    }

    private boolean isReloadConfigEventTarget(final IEvent event) {
        boolean isTarget = false;
        for (final IParm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && (PREFIX + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        LOG.debug("isReloadConfigEventTarget: Provisiond. {} was target of reload event: {}", NAME, isTarget);
        return isTarget;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public GeoIpConfigDao getGeoIpConfigDao() {
        return geoIpConfigDao;
    }

    public void setGeoIpConfigDao(final GeoIpConfigDao geoIpConfigDao) {
        this.geoIpConfigDao = geoIpConfigDao;
    }

    public EventForwarder getEventForwarder() {
        return eventForwarder;
    }

    public void setEventForwarder(final EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    public AssetRecordDao getAssetRecordDao() {
        return assetRecordDao;
    }

    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        this.assetRecordDao = assetRecordDao;
    }

    public void setDatabaseReader(DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }
}
