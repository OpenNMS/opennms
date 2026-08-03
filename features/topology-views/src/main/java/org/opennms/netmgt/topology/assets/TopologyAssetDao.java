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
package org.opennms.netmgt.topology.assets;

import java.util.List;
import java.util.Optional;

/**
 * Access to the shared catalog of topology image assets (backgrounds and
 * custom node icons). Metadata and bytes are stored and fetched separately so
 * listings never drag image payloads along.
 *
 * <p>Validation (allowed MIME types, size limits per kind) is the caller's
 * concern — the REST layer enforces it; this store is format-agnostic.
 */
public interface TopologyAssetDao {

    /**
     * Stores a new asset. The id, byte size, and timestamps are assigned here;
     * everything else (name, kind, MIME type, owner) comes filled in on
     * {@code asset}.
     *
     * @return the stored metadata, with the generated id set.
     */
    TopologyAsset save(TopologyAsset asset, byte[] bytes);

    /** The metadata for an asset, if it exists. */
    Optional<TopologyAsset> get(String id);

    /** The image bytes for an asset, if it exists. */
    Optional<byte[]> getBytes(String id);

    /** All assets' metadata (never the bytes). */
    List<TopologyAsset> findAll();

    /**
     * Removes an asset (metadata and bytes).
     *
     * @return whether an asset with that id existed.
     */
    boolean delete(String id);
}
