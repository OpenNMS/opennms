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

package org.opennms.core.rpc.utils.mate;

import static org.opennms.core.rpc.utils.mate.EntityScopeProvider.Contexts.ASSET;
import static org.opennms.core.rpc.utils.mate.EntityScopeProvider.Contexts.INTERFACE;
import static org.opennms.core.rpc.utils.mate.EntityScopeProvider.Contexts.NODE;
import static org.opennms.core.rpc.utils.mate.EntityScopeProvider.Contexts.SERVICE;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import ch.hsr.geohash.GeoHash;

public class EntityScopeProviderImpl implements EntityScopeProvider {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private SecureCredentialsVault scv;

    @Override
    public Scope getScopeForNode(final Integer nodeId) {
        if (nodeId == null) {
            return EmptyScope.EMPTY;
        }

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsNode node = nodeDao.get(nodeId);
            if (node == null) {
                return EmptyScope.EMPTY;
            }

            List<Scope> scopes = new ArrayList<>();
            scopes.add(transform(Scope.ScopeName.NODE, node.getMetaData()));

            Scope nodeScope = new ObjectScope<>(Scope.ScopeName.NODE, node)
                    .map(NODE, "criteria", this::getNodeCriteria)
                    .map(NODE, "label", (n) -> Optional.ofNullable(n.getLabel()))
                    .map(NODE, "foreign-source", (n) -> Optional.ofNullable(n.getForeignSource()))
                    .map(NODE, "foreign-id", (n) -> Optional.ofNullable(n.getForeignId()))
                    .map(NODE, "netbios-domain", (n) -> Optional.ofNullable(n.getNetBiosDomain()))
                    .map(NODE, "netbios-name", (n) -> Optional.ofNullable(n.getNetBiosName()))
                    .map(NODE, "os", (n) -> Optional.ofNullable(n.getOperatingSystem()))
                    .map(NODE, "sys-name", (n) -> Optional.ofNullable(n.getSysName()))
                    .map(NODE, "sys-location", (n) -> Optional.ofNullable(n.getSysLocation()))
                    .map(NODE, "sys-contact", (n) -> Optional.ofNullable(n.getSysContact()))
                    .map(NODE, "sys-description", (n) -> Optional.ofNullable(n.getSysDescription()))
                    .map(NODE, "sys-object-id", (n) -> Optional.ofNullable(n.getSysObjectId()))
                    .map(NODE, "location", (n) -> Optional.ofNullable(n.getLocation().getLocationName()))
                    .map(NODE, "area", (n) -> Optional.ofNullable(n.getLocation().getMonitoringArea()))
                    .map(NODE, "geohash", this::getNodeGeoHash);
            scopes.add(nodeScope);

            if (node.getAssetRecord() != null) {
                Scope assetScope = new ObjectScope<>(Scope.ScopeName.NODE, node.getAssetRecord())
                        .map(ASSET, "category", (a) -> Optional.ofNullable(a.getCategory()))
                        .map(ASSET, "manufacturer", (a) -> Optional.ofNullable(a.getManufacturer()))
                        .map(ASSET, "vendor", (a) -> Optional.ofNullable(a.getVendor()))
                        .map(ASSET, "model-number", (a) -> Optional.ofNullable(a.getModelNumber()))
                        .map(ASSET, "serial-number", (a) -> Optional.ofNullable(a.getSerialNumber()))
                        .map(ASSET, "description", (a) -> Optional.ofNullable(a.getDescription()))
                        .map(ASSET, "circuit-id", (a) -> Optional.ofNullable(a.getCircuitId()))
                        .map(ASSET, "asset-number", (a) -> Optional.ofNullable(a.getAssetNumber()))
                        .map(ASSET, "operating-system", (a) -> Optional.ofNullable(a.getOperatingSystem()))
                        .map(ASSET, "rack", (a) -> Optional.ofNullable(a.getRack()))
                        .map(ASSET, "slot", (a) -> Optional.ofNullable(a.getSlot()))
                        .map(ASSET, "port", (a) -> Optional.ofNullable(a.getPort()))
                        .map(ASSET, "region", (a) -> Optional.ofNullable(a.getRegion()))
                        .map(ASSET, "division", (a) -> Optional.ofNullable(a.getDivision()))
                        .map(ASSET, "department", (a) -> Optional.ofNullable(a.getDepartment()))
                        .map(ASSET, "building", (a) -> Optional.ofNullable(a.getBuilding()))
                        .map(ASSET, "floor", (a) -> Optional.ofNullable(a.getFloor()))
                        .map(ASSET, "room", (a) -> Optional.ofNullable(a.getRoom()))
                        .map(ASSET, "vendor-phone", (a) -> Optional.ofNullable(a.getVendorPhone()))
                        .map(ASSET, "vendor-fax", (a) -> Optional.ofNullable(a.getVendorFax()))
                        .map(ASSET, "vendor-asset-number", (a) -> Optional.ofNullable(a.getVendorAssetNumber()))
                        .map(ASSET, "username", (a) -> Optional.ofNullable(a.getUsername()))
                        .map(ASSET, "password", (a) -> Optional.ofNullable(a.getPassword()))
                        .map(ASSET, "enable", (a) -> Optional.ofNullable(a.getEnable()))
                        .map(ASSET, "connection", (a) -> Optional.ofNullable(a.getConnection()))
                        .map(ASSET, "autoenable", (a) -> Optional.ofNullable(a.getAutoenable()))
                        .map(ASSET, "last-modified-by", (a) -> Optional.ofNullable(a.getLastModifiedBy()))
                        .map(ASSET, "last-modified-date", (a) -> Optional.ofNullable(a.getLastModifiedDate()).map(Date::toString))
                        .map(ASSET, "date-installed", (a) -> Optional.ofNullable(a.getDateInstalled()))
                        .map(ASSET, "lease", (a) -> Optional.ofNullable(a.getLease()))
                        .map(ASSET, "lease-expires", (a) -> Optional.ofNullable(a.getLeaseExpires()))
                        .map(ASSET, "support-phone", (a) -> Optional.ofNullable(a.getSupportPhone()))
                        .map(ASSET, "maintcontract", (a) -> Optional.ofNullable(a.getMaintcontract()))
                        .map(ASSET, "maint-contract-expiration", (a) -> Optional.ofNullable(a.getMaintContractExpiration()))
                        .map(ASSET, "display-category", (a) -> Optional.ofNullable(a.getDisplayCategory()))
                        .map(ASSET, "notify-category", (a) -> Optional.ofNullable(a.getNotifyCategory()))
                        .map(ASSET, "poller-category", (a) -> Optional.ofNullable(a.getPollerCategory()))
                        .map(ASSET, "threshold-category", (a) -> Optional.ofNullable(a.getThresholdCategory()))
                        .map(ASSET, "comment", (a) -> Optional.ofNullable(a.getComment()))
                        .map(ASSET, "cpu", (a) -> Optional.ofNullable(a.getCpu()))
                        .map(ASSET, "ram", (a) -> Optional.ofNullable(a.getRam()))
                        .map(ASSET, "storagectrl", (a) -> Optional.ofNullable(a.getStoragectrl()))
                        .map(ASSET, "hdd1", (a) -> Optional.ofNullable(a.getHdd1()))
                        .map(ASSET, "hdd2", (a) -> Optional.ofNullable(a.getHdd2()))
                        .map(ASSET, "hdd3", (a) -> Optional.ofNullable(a.getHdd3()))
                        .map(ASSET, "hdd4", (a) -> Optional.ofNullable(a.getHdd4()))
                        .map(ASSET, "hdd5", (a) -> Optional.ofNullable(a.getHdd5()))
                        .map(ASSET, "hdd6", (a) -> Optional.ofNullable(a.getHdd6()))
                        .map(ASSET, "numpowersupplies", (a) -> Optional.ofNullable(a.getNumpowersupplies()))
                        .map(ASSET, "inputpower", (a) -> Optional.ofNullable(a.getInputpower()))
                        .map(ASSET, "additionalhardware", (a) -> Optional.ofNullable(a.getAdditionalhardware()))
                        .map(ASSET, "admin", (a) -> Optional.ofNullable(a.getAdmin()))
                        .map(ASSET, "snmpcommunity", (a) -> Optional.ofNullable(a.getSnmpcommunity()))
                        .map(ASSET, "rackunitheight", (a) -> Optional.ofNullable(a.getRackunitheight()))
                        .map(ASSET, "managed-object-type", (a) -> Optional.ofNullable(a.getManagedObjectType()))
                        .map(ASSET, "managed-object-instance", (a) -> Optional.ofNullable(a.getManagedObjectInstance()))
                        .map(ASSET, "geolocation", (a) -> Optional.ofNullable(a.getGeolocation()).map(Object::toString));
                scopes.add(assetScope);

                scopes.add(new SecureCredentialsVaultScope(this.scv));
            }

            return new FallbackScope(scopes);
        });

    }

    private Optional<String> getNodeCriteria(final OnmsNode node) {
        Objects.requireNonNull(node, "Node can not be null");
        if (node.getForeignSource() != null) {
            return Optional.of(node.getForeignSource() + ":" + node.getForeignId());
        } else {
            return Optional.of(Integer.toString(node.getId()));
        }
    }

    /**
     * Computes a geohash from the lat/lon associated with the node.
     *
     * This function is expected to be called in the context of a transaction.
     *
     * @param node node from which to derive the geohash
     * @return geohash
     */
    private Optional<String> getNodeGeoHash(final OnmsNode node) {
        double latitude = Double.NaN;
        double longitude = Double.NaN;

        // Safely retrieve the geo-location from the node's asset record
        final OnmsAssetRecord assetRecord = node.getAssetRecord();
        if (assetRecord == null) {
            return Optional.empty();
        }
        final OnmsGeolocation geolocation = assetRecord.getGeolocation();
        if (geolocation == null) {
            return Optional.empty();
        }

        // Safely retrieve the lat/lon value from the geo-location
        if (geolocation.getLatitude() != null) {
            latitude = geolocation.getLatitude();
        }
        if (geolocation.getLongitude() != null) {
            longitude = geolocation.getLongitude();
        }
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            return Optional.empty();
        }

        // We have a finite lat/lon, compute the geohash using maximum precision
        return Optional.of(GeoHash.withCharacterPrecision(latitude, longitude, 12).toBase32());
    }

    @Override
    public Scope getScopeForInterface(final Integer nodeId, final String ipAddress) {
        if (nodeId == null || Strings.isNullOrEmpty(ipAddress)) {
            return EmptyScope.EMPTY;
        }

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsIpInterface ipInterface = this.ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
            if (ipInterface == null) {
                return EmptyScope.EMPTY;
            }

            return new FallbackScope(transform(Scope.ScopeName.INTERFACE, ipInterface.getMetaData()),
                    mapIpInterfaceKeys(ipInterface)
                            .map(INTERFACE, "if-alias", (i) -> Optional.ofNullable(i.getSnmpInterface()).map(OnmsSnmpInterface::getIfAlias))
                            .map(INTERFACE, "if-description", (i) -> Optional.ofNullable(i.getSnmpInterface()).map(OnmsSnmpInterface::getIfDescr))
                            .map(INTERFACE, "phy-addr", (i) -> Optional.ofNullable(i.getSnmpInterface()).map(OnmsSnmpInterface::getPhysAddr)),
                                     new SecureCredentialsVaultScope(this.scv)
            );
        });
    }

    private static ObjectScope<OnmsIpInterface> mapIpInterfaceKeys(OnmsIpInterface ipInterface) {
        return new ObjectScope<>(Scope.ScopeName.INTERFACE, ipInterface)
                .map(INTERFACE, "hostname", (i) -> Optional.ofNullable(i.getIpHostName()))
                .map(INTERFACE, "address", (i) -> Optional.ofNullable(i.getIpAddress()).map(InetAddressUtils::toIpAddrString))
                .map(INTERFACE, "netmask", (i) -> Optional.ofNullable(i.getNetMask()).map(InetAddressUtils::toIpAddrString))
                .map(INTERFACE, "if-index", (i) -> Optional.ofNullable(i.getIfIndex()).map(Object::toString));
    }

    @Override
    public Scope getScopeForInterfaceByIfIndex(final Integer nodeId, final int ifIndex) {
        if (nodeId == null) {
            return EmptyScope.EMPTY;
        }

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsSnmpInterface snmpInterface = this.snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex);
            if (snmpInterface == null) {
                return EmptyScope.EMPTY;
            }

            ArrayList<Scope> scopes = new ArrayList<>();

            // SNMP interface facts
            scopes.add(new ObjectScope<>(Scope.ScopeName.INTERFACE, snmpInterface)
                    .map(INTERFACE, "if-alias", (i) -> Optional.ofNullable(i.getIfAlias()))
                    .map(INTERFACE, "if-description", (i) -> Optional.ofNullable(i.getIfDescr()))
                    .map(INTERFACE, "phy-addr", (i) -> Optional.ofNullable(i.getPhysAddr())));

            // IP interface facts w/ meta-data extracted from IP interface
            Optional.ofNullable(snmpInterface.getPrimaryIpInterface())
                    .ifPresent(ipInterface -> {
                        scopes.add(transform(Scope.ScopeName.INTERFACE, ipInterface.getMetaData()));
                        scopes.add(mapIpInterfaceKeys(ipInterface));
                    });

            scopes.add(new SecureCredentialsVaultScope(this.scv));

            return new FallbackScope(scopes);
        });
    }

    @Override
    public Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        if (nodeId == null || ipAddress == null || Strings.isNullOrEmpty(serviceName)) {
            return EmptyScope.EMPTY;
        }

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsMonitoredService monitoredService = this.monitoredServiceDao.get(nodeId, ipAddress, serviceName);
            if (monitoredService == null) {
                return EmptyScope.EMPTY;
            }

            return new FallbackScope(transform(Scope.ScopeName.SERVICE, monitoredService.getMetaData()),
                    new ObjectScope<>(Scope.ScopeName.SERVICE, monitoredService)
                            .map(SERVICE, "name", (s) -> Optional.of(s.getServiceName())),
                                     new SecureCredentialsVaultScope(this.scv)
            );
        });
    }

    private static MapScope transform(final Scope.ScopeName scopeName, final Collection<OnmsMetaData> metaData) {
        final Map<ContextKey, String> map = metaData.stream()
                .collect(Collectors.toMap(e -> new ContextKey(e.getContext(), e.getKey()), OnmsMetaData::getValue));
        return new MapScope(scopeName, map);
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        this.snmpInterfaceDao = snmpInterfaceDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        this.monitoredServiceDao = Objects.requireNonNull(monitoredServiceDao);
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    public void setScv(SecureCredentialsVault scv) {
        this.scv = scv;
    }
}
