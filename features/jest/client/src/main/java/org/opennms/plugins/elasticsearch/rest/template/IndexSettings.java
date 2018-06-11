/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.template;

import com.google.common.base.Strings;

public class IndexSettings {
    private Integer numberOfShards;

    private Integer numberOfReplicas;

    private Integer routingPartitionSize;

    private String refreshInterval;

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
        boolean empty = numberOfShards == null && numberOfReplicas == null && routingPartitionSize == null && refreshInterval == null;
        return empty;
    }
}
