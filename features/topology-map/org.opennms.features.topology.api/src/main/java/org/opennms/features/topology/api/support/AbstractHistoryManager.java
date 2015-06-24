/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.HistoryOperation;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHistoryManager implements HistoryManager {

    private final List<HistoryOperation> m_operations = new CopyOnWriteArrayList<HistoryOperation>();

    @Override
    public List<HistoryOperation> getHistoryOperations() {
        return Collections.unmodifiableList(m_operations);
    }

    @Override
    public void applyHistory(String userId, String fragment, GraphContainer container) {
        SavedHistory hist = getHistory(userId, fragment);
        if (hist != null) {
            hist.apply(container, m_operations);
        }
    }

    @Override
    public String createHistory(String userId, GraphContainer graphContainer) {
        SavedHistory history = new SavedHistory(graphContainer, m_operations);
        saveHistory(userId, history);
        return history.getFragment();
    }
    
    protected abstract SavedHistory getHistory(String userId, String fragment);

    protected abstract void saveHistory(String userId, SavedHistory history);

    @Override
	public synchronized void onBind(HistoryOperation operation) {
    	try {
    		m_operations.add(operation);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
        }
	}

    @Override
	public synchronized void onUnbind(HistoryOperation operation) {
    	try {
    		m_operations.remove(operation);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
        }
	}

}
