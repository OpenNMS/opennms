/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.cache;

import com.google.common.reflect.TypeToken;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.features.apilayer.api.InterfaceToNodeInfo;
import org.opennms.features.apilayer.api.InterfaceToNodeInfo.NodeMetadataKey;
import org.opennms.netmgt.dao.api.InterfaceAndMetaInfoToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class InterfaceAndMetaInfoToNodeCacheImpl implements InterfaceAndMetaInfoToNodeCache {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceAndMetaInfoToNodeCacheImpl.class);

    private final TwinSubscriber twinSubscriber;
    private Map<InetAddress, Entry> addressMap = new HashMap<>();
    private Map<NodeMetadataKey, Entry> nodeMetaMap = new HashMap<>();
    private Map<NodeMetadataKey, Entry> ifaceMetaMap = new HashMap<>();

    class ReloadConsumer implements Consumer<InterfaceToNodeInfo> {
        @Override
        public void accept(InterfaceToNodeInfo interfaceToNodeInfo) {
            LOG.debug("Receive size: {}", interfaceToNodeInfo.getEntryMap().size());
            Map<InetAddress, Entry> tmpAddressMap = new HashMap<>(interfaceToNodeInfo.getEntryMap().size());
            for (Entry entry : interfaceToNodeInfo.getEntryMap().values()) {
                tmpAddressMap.put(entry.ipAddr, entry);
            }
            Map<NodeMetadataKey, Entry> tmpNodeMetaMap = new HashMap<>(interfaceToNodeInfo.getNodeMetaToEntry().size());
            interfaceToNodeInfo.getNodeMetaToEntry().forEach((k, v) -> {
                var entry = interfaceToNodeInfo.getEntryMap().get(v);
                if (entry == null) {
                    LOG.warn("Missing entry for key: {}", k);
                    return;
                }
                nodeMetaMap.put(k, entry);
            });

            Map<NodeMetadataKey, Entry> tmpIfaceMetaMap = new HashMap<>(interfaceToNodeInfo.getNodeMetaToEntry().size());
            interfaceToNodeInfo.getNetworkMetaToEntry().forEach((k, v) -> {
                var entry = interfaceToNodeInfo.getEntryMap().get(v);
                if (entry == null) {
                    LOG.warn("Missing entry for key: {}", k);
                    return;
                }
                tmpIfaceMetaMap.put(k, entry);
            });
            addressMap = tmpAddressMap;
            nodeMetaMap = tmpNodeMetaMap;
            ifaceMetaMap = tmpIfaceMetaMap;
        }
    }

    public InterfaceAndMetaInfoToNodeCacheImpl(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = Objects.requireNonNull(twinSubscriber);
        init();
    }

    private List<Closeable> subscribers = new ArrayList<>();

    private void init() {
        LOG.info("Create subscriber");
        subscribers.add(this.twinSubscriber.subscribe(InterfaceToNodeInfo.FULL_KEY, new TypeToken<>() {
        }, new ReloadConsumer()));
        LOG.debug("Done Create subscriber");
    }

    public void destroy() throws IOException {
        for (var s : subscribers) {
            s.close();
        }
    }

    @Override
    public int size() {
        return addressMap.size();
    }

    @Override
    public Optional<Entry> getFirst(String location, InetAddress ipAddr) {
        return matchLocation(location, addressMap.get(ipAddr));
    }

    @Override
    public Optional<Entry> getFirst(String location, String context, String key, String value) {
        var tmp = matchLocation(location, ifaceMetaMap.get(new NodeMetadataKey(new ContextKey(context, key), value)));
        if (tmp.isPresent()) {
            return tmp;
        } else {
            return matchLocation(location, nodeMetaMap.get(new NodeMetadataKey(new ContextKey(context, key), value)));
        }
    }

    private Optional<Entry> matchLocation(String location, Entry entry) {
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.location.equals(location)) {
            return Optional.of(entry);
        } else {
            return Optional.empty();
        }
    }
}
