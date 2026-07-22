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
package org.opennms.web.rest.v2.infopanel;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * One rendered info-panel item: a titled HTML fragment produced from an
 * {@code etc/infopanel/} Jinjava template. {@code order} controls display
 * sequence (ascending). The HTML is operator-authored; the consuming client is
 * responsible for sanitizing it before injecting into the DOM.
 */
public class InfoPanelItem {

    @JsonProperty("title")
    private String title;

    @JsonProperty("order")
    private int order;

    @JsonProperty("html")
    private String html;

    public InfoPanelItem() {
    }

    public InfoPanelItem(final String title, final int order, final String html) {
        this.title = title;
        this.order = order;
        this.html = html;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(final String html) {
        this.html = html;
    }
}
