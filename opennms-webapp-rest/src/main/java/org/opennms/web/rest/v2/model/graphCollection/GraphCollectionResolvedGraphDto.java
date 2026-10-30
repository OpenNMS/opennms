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
package org.opennms.web.rest.v2.model.graphCollection;

/**
 * A graph collection graph made ready to render: the view-time timespan and
 * graph type overrides applied, the named timespan turned into start and end
 * epoch milliseconds, and the resource and prefabricated graph looked up.
 * valid is false when the resource no longer exists or the prefabricated graph
 * is not available for it; error then says which.
 */
public class GraphCollectionResolvedGraphDto {
    private int index;
    private String kind;
    private String title;
    private String timespan;
    private String graphtype;
    private String resourceId;
    private GraphCollectionResourceDto resource;
    private String prefabGraphTitle;
    private boolean valid;
    private String error;
    private long start;
    private long end;
    private String extlink;

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getTimespan() {
        return timespan;
    }

    public void setTimespan(final String timespan) {
        this.timespan = timespan;
    }

    public String getGraphtype() {
        return graphtype;
    }

    public void setGraphtype(final String graphtype) {
        this.graphtype = graphtype;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public GraphCollectionResourceDto getResource() {
        return resource;
    }

    public void setResource(final GraphCollectionResourceDto resource) {
        this.resource = resource;
    }

    public String getPrefabGraphTitle() {
        return prefabGraphTitle;
    }

    public void setPrefabGraphTitle(final String prefabGraphTitle) {
        this.prefabGraphTitle = prefabGraphTitle;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        this.end = end;
    }

    public String getExtlink() {
        return extlink;
    }

    public void setExtlink(final String extlink) {
        this.extlink = extlink;
    }
}
