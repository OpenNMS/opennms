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
package org.opennms.web.rest.v2.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Where collected samples end up, as the Manage WS-Man page shows it: the
 * configured time-series strategy plus the backend actually behind it.
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class WsmanStorageDto {
    // org.opennms.timeseries.strategy: rrd, newts, integration, osgi, evaluate, tcp
    private String strategy;
    // human label, e.g. "RRD files (JRobin)", "Newts on Cassandra", "Prometheus remote write plugin"
    private String label;
    // where: repository path, Cassandra contact point and keyspace, or the plugin class
    private String detail;
    // false when the strategy needs a plugin and none is registered
    private boolean available;
    // the rrd-repository and RRA settings in the collection files only matter for rrd
    private boolean rrdSettingsUsed;

    public String getStrategy() { return strategy; }
    public void setStrategy(final String strategy) { this.strategy = strategy; }
    public String getLabel() { return label; }
    public void setLabel(final String label) { this.label = label; }
    public String getDetail() { return detail; }
    public void setDetail(final String detail) { this.detail = detail; }
    public boolean isAvailable() { return available; }
    public void setAvailable(final boolean available) { this.available = available; }
    public boolean isRrdSettingsUsed() { return rrdSettingsUsed; }
    public void setRrdSettingsUsed(final boolean rrdSettingsUsed) { this.rrdSettingsUsed = rrdSettingsUsed; }
}
