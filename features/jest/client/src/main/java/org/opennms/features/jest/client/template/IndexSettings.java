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
package org.opennms.features.jest.client.template;

import com.google.common.base.Strings;

public class IndexSettings {

    private String indexPrefix;

    private Integer numberOfShards;

    private Integer numberOfReplicas;

    private Integer routingPartitionSize;

    private String refreshInterval;

    public String getIndexPrefix() {
        return indexPrefix;
    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public void setNumberOfShards(Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
    }


    public Integer getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public void setNumberOfReplicas(Integer numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }

    public Integer getRoutingPartitionSize() {
        return routingPartitionSize;
    }

    public void setRoutingPartitionSize(Integer routingPartitionSize) {
        this.routingPartitionSize = routingPartitionSize;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = Strings.isNullOrEmpty(refreshInterval) ? null : refreshInterval;
    }

    public void setRoutingPartitionSize(String routingPartitionSize) {
        if (!Strings.isNullOrEmpty(routingPartitionSize)) {
            setRoutingPartitionSize(Integer.valueOf(routingPartitionSize));
        }
    }

    public void setNumberOfShards(String numberOfShards) {
        if (!Strings.isNullOrEmpty(numberOfShards)) {
            setNumberOfShards(Integer.valueOf(numberOfShards));
        }
    }

    public void setNumberOfReplicas(String numberOfReplicas) {
        if (!Strings.isNullOrEmpty(numberOfReplicas)) {
            setNumberOfReplicas(Integer.valueOf(numberOfReplicas));
        }
    }

    public boolean isEmpty() {
        return indexPrefix == null
                && numberOfShards == null
                && numberOfReplicas == null
                && routingPartitionSize == null
                && refreshInterval == null;
    }
}
