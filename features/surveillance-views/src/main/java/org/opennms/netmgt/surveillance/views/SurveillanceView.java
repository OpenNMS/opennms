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
package org.opennms.netmgt.surveillance.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * A surveillance view definition: a named grid whose rows and columns are
 * each a labelled set of node categories. A grid cell reports on the nodes
 * in the intersection of its row's and column's category sets.
 *
 * <p>This is the successor to the {@code view} element of the legacy
 * {@code surveillance-views.xml}. Views are persisted as JSON blobs in the
 * generic key-value store (see {@code SurveillanceViewJsonStore}); the
 * {@code id} is a store-assigned key (a UUID), not a database sequence value.
 *
 * <p>Per-user targeting is by naming convention, as it always was: a view
 * whose name equals a username (or one of the user's group names) is that
 * user's view; otherwise the configured default applies. There is no ACL on
 * the view itself. {@link #getOwner() owner} records who created it
 * (informational).
 */
public class SurveillanceView {

    public static final int DEFAULT_REFRESH_SECONDS = 300;

    private String id;
    private String name;
    private Integer refreshSeconds;
    private List<SurveillanceViewDef> rows = new ArrayList<>();
    private List<SurveillanceViewDef> columns = new ArrayList<>();
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

    public Integer getRefreshSeconds() {
        return refreshSeconds;
    }

    public void setRefreshSeconds(Integer refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }

    public List<SurveillanceViewDef> getRows() {
        return rows;
    }

    public void setRows(List<SurveillanceViewDef> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public List<SurveillanceViewDef> getColumns() {
        return columns;
    }

    public void setColumns(List<SurveillanceViewDef> columns) {
        this.columns = columns == null ? new ArrayList<>() : columns;
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
                .add("refreshSeconds", refreshSeconds)
                .add("rows", rows)
                .add("columns", columns)
                .add("owner", owner)
                .toString();
    }
}
