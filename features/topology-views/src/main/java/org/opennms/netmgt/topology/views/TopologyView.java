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
package org.opennms.netmgt.topology.views;

import java.util.Date;

import com.google.common.base.MoreObjects;

/**
 * A custom (user-composed) topology view.
 *
 * <p>A view is a named document describing a free-form canvas of OpenNMS
 * nodes, free-standing text labels, and user-drawn edges. The canvas itself
 * is held as an opaque JSON string in {@link #getDefinition() definition} so
 * the canvas model can evolve with the front-end without touching storage;
 * only the surrounding catalog metadata (name, ownership, timestamps) is
 * modelled as fields.
 *
 * <p>This is a plain document model. Views are persisted as JSON blobs in the
 * generic key-value store (see {@code TopologyViewJsonStore}); the {@code id}
 * is a store-assigned key (a UUID), not a database sequence value.
 *
 * <p>Views are a shared catalog rather than per-user; access is governed by
 * the standard REST role rules, not by anything on the view itself.
 * {@link #getOwner() owner} records who created it (informational).
 */
public class TopologyView {

    private String id;
    private String name;
    private String definition;
    private String owner;
    private Date created;
    private Date lastModified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The canvas document as an opaque JSON string (nodes, edges, labels,
     * viewport, optional background). Stored verbatim; not interpreted by the
     * persistence layer.
     */
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("owner", owner)
                .add("created", created)
                .add("lastModified", lastModified)
                .toString();
    }
}
