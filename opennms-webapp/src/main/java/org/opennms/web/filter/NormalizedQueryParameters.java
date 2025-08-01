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
package org.opennms.web.filter;

import java.util.ArrayList;
import java.util.List;

public class NormalizedQueryParameters implements QueryParameters {

    private String sortStyleShortName;
    private NormalizedAcknowledgeType ackType;
    private List<Filter> filters;
    private int limit;
    private int multiple;
    private String display;

    public NormalizedQueryParameters(QueryParameters queryParms) {
        this.sortStyleShortName = queryParms.getSortStyleShortName();
        this.ackType = queryParms.getAckType();
        this.display = queryParms.getDisplay();
        this.filters = new ArrayList<Filter>(queryParms.getFilters());
        this.limit = queryParms.getLimit();
        this.multiple = queryParms.getMultiple();
    }

    public NormalizedQueryParameters() {
        filters = new ArrayList<>();
    }

    @Override
    public String getSortStyleShortName() {
        return sortStyleShortName;
    }

    @Override
    public NormalizedAcknowledgeType getAckType() {
        return ackType;
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getMultiple() {
        return multiple;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    public void setSortStyleShortName(String sortStyleShortName) {
        this.sortStyleShortName = sortStyleShortName;
    }

    public void setAckType(NormalizedAcknowledgeType ackType) {
        this.ackType = ackType;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
