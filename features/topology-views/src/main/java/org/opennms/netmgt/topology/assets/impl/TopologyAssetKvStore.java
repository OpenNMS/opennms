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
package org.opennms.netmgt.topology.assets.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.features.distributed.kvstore.blob.postgres.PostgresBlobStore;
import org.opennms.netmgt.topology.assets.TopologyAsset;
import org.opennms.netmgt.topology.assets.TopologyAssetDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Stores topology image assets in the generic key-value stores: the image
 * bytes in the binary store ({@link BlobStore}, the {@code kvstore_bytea}
 * table) and the catalog metadata as a JSON document in the {@link JsonStore}
 * ({@code kvstore_jsonb}), both under the {@value #CONTEXT} context and keyed
 * by the same generated UUID. Splitting the two keeps catalog listings cheap
 * — enumerating metadata never touches the image payloads.
 *
 * <p>The {@link BlobStore} is constructed here from the {@link DataSource}
 * rather than injected: the only BlobStore exposed as a shared bean in the
 * core context is the no-op used by thresholding, and registering a second
 * BlobStore service would make that lookup ambiguous for its consumers.
 * Assets are a core-webapp feature, always backed by the OpenNMS database.
 */
public class TopologyAssetKvStore implements TopologyAssetDao {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyAssetKvStore.class);

    static final String CONTEXT = "topology-assets";

    private final JsonStore jsonStore;
    private final BlobStore blobStore;
    private final Gson gson = new Gson();

    public TopologyAssetKvStore(final JsonStore jsonStore, final DataSource dataSource) {
        this(jsonStore, new PostgresBlobStore(Objects.requireNonNull(dataSource)));
    }

    /** For tests: inject the blob store directly. */
    TopologyAssetKvStore(final JsonStore jsonStore, final BlobStore blobStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
        this.blobStore = Objects.requireNonNull(blobStore);
    }

    @Override
    public TopologyAsset save(final TopologyAsset asset, final byte[] bytes) {
        Objects.requireNonNull(asset);
        Objects.requireNonNull(bytes);
        final Date now = new Date();
        asset.setId(UUID.randomUUID().toString());
        asset.setSizeBytes(bytes.length);
        asset.setCreated(now);
        asset.setLastModified(now);
        // Bytes first: if the metadata write fails the orphaned blob is
        // invisible (nothing lists it) and gets overwritten only by its own
        // UUID key; the reverse order could list an asset with no bytes.
        blobStore.put(asset.getId(), bytes, CONTEXT);
        jsonStore.put(asset.getId(), gson.toJson(Doc.of(asset)), CONTEXT);
        return asset;
    }

    @Override
    public Optional<TopologyAsset> get(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        // flatMap + ofNullable: a malformed stored document (deserialize -> null)
        // reads as absent rather than surfacing a half-built asset.
        return jsonStore.get(id, CONTEXT).flatMap(json -> Optional.ofNullable(deserialize(id, json)));
    }

    @Override
    public Optional<byte[]> getBytes(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        return blobStore.get(id, CONTEXT);
    }

    @Override
    public List<TopologyAsset> findAll() {
        final Map<String, String> entries = jsonStore.enumerateContext(CONTEXT);
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        final List<TopologyAsset> assets = new ArrayList<>(entries.size());
        for (final Map.Entry<String, String> entry : entries.entrySet()) {
            final TopologyAsset asset = deserialize(entry.getKey(), entry.getValue());
            if (asset != null) {
                assets.add(asset);
            }
        }
        return assets;
    }

    @Override
    public boolean delete(final String id) {
        if (id == null || !jsonStore.get(id, CONTEXT).isPresent()) {
            return false;
        }
        jsonStore.delete(id, CONTEXT);
        blobStore.delete(id, CONTEXT);
        return true;
    }

    private TopologyAsset deserialize(final String id, final String json) {
        try {
            final Doc doc = gson.fromJson(json, Doc.class);
            if (doc == null) {
                return null;
            }
            return doc.toAsset(id);
        } catch (final JsonSyntaxException e) {
            LOG.warn("Skipping malformed topology asset document for key {}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * The stored metadata document. Timestamps travel as epoch millis so the
     * JSON shape doesn't depend on gson's default (locale-bound) Date format.
     */
    private static final class Doc {
        String name;
        String kind;
        String mimeType;
        long sizeBytes;
        String owner;
        Long created;
        Long lastModified;

        static Doc of(final TopologyAsset asset) {
            final Doc doc = new Doc();
            doc.name = asset.getName();
            doc.kind = asset.getKind();
            doc.mimeType = asset.getMimeType();
            doc.sizeBytes = asset.getSizeBytes();
            doc.owner = asset.getOwner();
            doc.created = asset.getCreated() == null ? null : asset.getCreated().getTime();
            doc.lastModified = asset.getLastModified() == null ? null : asset.getLastModified().getTime();
            return doc;
        }

        TopologyAsset toAsset(final String id) {
            final TopologyAsset asset = new TopologyAsset();
            asset.setId(id);
            asset.setName(name);
            asset.setKind(kind);
            asset.setMimeType(mimeType);
            asset.setSizeBytes(sizeBytes);
            asset.setOwner(owner);
            asset.setCreated(created == null ? null : new Date(created));
            asset.setLastModified(lastModified == null ? null : new Date(lastModified));
            return asset;
        }
    }
}
