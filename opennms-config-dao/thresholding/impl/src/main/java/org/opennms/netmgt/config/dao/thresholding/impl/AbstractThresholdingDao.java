/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dao.thresholding.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThresholdingDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

public abstract class AbstractThresholdingDao implements ReadableThresholdingDao {
    private final Map<String, Group> groupMap = new HashMap<>();
    final JsonStore jsonStore;
    public static final String JSON_STORE_KEY = "thresholds";
    public static final String JSON_STORE_CONTEXT = "config";

    AbstractThresholdingDao() {
        jsonStore = null;
        reload();
    }

    AbstractThresholdingDao(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    /**
     * Subclasses should call this reload after they have performed their reload logic.
     */
    @Override
    public void reload() {
        initGroupMap();
    }

    @Override
    public String getRrdRepository(String groupName) {
        return getGroup(groupName).getRrdRepository();
    }

    @Override
    public synchronized Group getGroup(String groupName) {
        Group group = groupMap.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Thresholding group " + groupName + " does not exist.");
        }
        return group;
    }

    @Override
    public Collection<Basethresholddef> getThresholds(String groupName) {
        Group group = getGroup(groupName);
        Collection<Basethresholddef> result = new ArrayList<>();
        result.addAll(group.getThresholds());
        result.addAll(group.getExpressions());
        return result;
    }

    @Override
    public synchronized Collection<String> getGroupNames() {
        return Collections.unmodifiableCollection(groupMap.keySet());
    }

    synchronized void initGroupMap() {
        ThresholdingConfig thresholdingConfig = getConfig();

        if (thresholdingConfig != null) {
            for (Group g : getConfig().getGroups()) {
                groupMap.put(g.getName(), g);
            }
        }
    }
}
