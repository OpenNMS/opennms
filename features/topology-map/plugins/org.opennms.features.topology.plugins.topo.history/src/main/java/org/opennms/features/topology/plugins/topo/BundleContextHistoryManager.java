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
package org.opennms.features.topology.plugins.topo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.support.SavedHistory;
import org.opennms.features.topology.api.support.ServiceLocator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

public class BundleContextHistoryManager implements HistoryManager {

	public static final String DATA_FILE_NAME = BundleContextHistoryManager.class.getName() + ".properties";

	private final BundleContext m_bundleContext;

	private final ServiceLocator m_serviceLocator;

	private final List<HistoryOperation> m_operations = new CopyOnWriteArrayList<>();

	public BundleContextHistoryManager(BundleContext bundleContext, ServiceLocator serviceLocator) {
		m_bundleContext = bundleContext;
		m_serviceLocator = serviceLocator;
	}

	public synchronized void onBind(HistoryOperation operation) {
		try {
			m_operations.add(operation);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
		}
	}

	public synchronized void onUnbind(HistoryOperation operation) {
		try {
			m_operations.remove(operation);
		} catch (Throwable e) {
			LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
		}
	}

	@Override
	public void applyHistory(String fragment, GraphContainer container) {
		SavedHistory hist = getHistoryByFragment(fragment);
		if (hist != null) {
			hist.apply(container, m_operations, m_serviceLocator);
		}
	}

	@Override
	public String saveOrUpdateHistory(String userId, GraphContainer graphContainer) {
		SavedHistory history = new SavedHistory(graphContainer, m_operations);
		saveHistory(userId, history);
		return history.getFragment();
	}

	protected synchronized void saveHistory(String userId, SavedHistory hist) {
		Properties props = loadProperties(m_bundleContext);
		String historyXml = toXML(hist);
		
		//cleanUp(userId, props);
		
		props.put(hist.getFragment(), historyXml);
		props.put(userId, hist.getFragment());
		storeProperties(m_bundleContext, props);
	}

	@Override
	public synchronized SavedHistory getHistoryByFragment(String fragment) {
            if(fragment != null){
                Properties props = loadProperties(m_bundleContext);
                String xml = props.getProperty(fragment);
                if (xml == null || "".equals(xml)) {
                    // There is no stored history for this fragment ID
                    return null;
                } else {
                    return JaxbUtils.unmarshal(SavedHistory.class, new StringReader(xml));
                }
            }
            return null;
	}

	@Override
	public synchronized SavedHistory getHistoryByUserId(String userId) {
		String fragment = getHistoryFragment(userId);
		if (fragment != null) {
			return getHistoryByFragment(fragment);
		}
		return null;
	}

	@Override
	public synchronized String getHistoryFragment(String userId) {
		return loadProperties(m_bundleContext).getProperty(userId);
	}

	@Override
	public synchronized void deleteHistory() {
		m_bundleContext.getDataFile(DATA_FILE_NAME).delete();
	}

	/**
	 * Removes the saved history entry for userId.
	 * It also removes the history-Entry for the historyHash originally used by the user.
	 * But only if it is not referenced anywhere else.
	 * 
	 * @param userId The user we want to clean up the history entries for.
	 * @param properties The already loaded properties, where the user and the history is stored in.
	 */
    // TODO this cleanup does not work, because the history button uses the existing fragments. We need an aging algorithm for that
	private void cleanUp(String userId, Properties properties) {
		// we only need to cleanup if there is a entry
		if (properties.containsKey(userId)) { 
			String historyHash = properties.getProperty(userId);
			int usageCount = 0;
			for (Object eachKey : properties.keySet()) {
				String eachValue = properties.getProperty((String)eachKey);
				if (eachValue != null && eachValue.equals(historyHash)) {
					usageCount++;
				}
			}
			
			// if usageCount == 1, we can delete the entry for 
			// the historyHash. Otherwise it must stay, because
			// another user has the same history.
			if (usageCount == 1) {
				properties.remove(historyHash);
			}
		}
		
	}

	private String toXML(SavedHistory hist) {
		return JaxbUtils.marshal(hist);
	}
	
	private static Properties loadProperties(BundleContext context) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(context.getDataFile(DATA_FILE_NAME)));
		} catch (FileNotFoundException e) {
			LoggerFactory.getLogger(BundleContextHistoryManager.class).warn("BundleContextHistoryManager data file does not exist yet");
		} catch (IOException e) {
			LoggerFactory.getLogger(BundleContextHistoryManager.class).warn("IOException when reading BundleContextHistoryManager data file", e);
		}
		return props;
	}

	private static void storeProperties(BundleContext context, Properties props) {
		try {
			props.store(new FileOutputStream(context.getDataFile(DATA_FILE_NAME)), BundleContextHistoryManager.class.getName() + " History Data");
		} catch (FileNotFoundException e) {
			LoggerFactory.getLogger(BundleContextHistoryManager.class).warn("BundleContextHistoryManager data file does not exist");
		} catch (IOException e) {
			LoggerFactory.getLogger(BundleContextHistoryManager.class).warn("IOException when writing to BundleContextHistoryManager data file", e);
		}
	}
}
