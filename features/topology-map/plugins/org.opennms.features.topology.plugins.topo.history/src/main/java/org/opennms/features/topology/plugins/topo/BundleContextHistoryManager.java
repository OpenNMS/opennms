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

package org.opennms.features.topology.plugins.topo;

import org.opennms.features.topology.api.support.AbstractHistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.Properties;

public class BundleContextHistoryManager extends AbstractHistoryManager {

	private final BundleContext m_bundleContext;
	public static final String DATA_FILE_NAME = BundleContextHistoryManager.class.getName() + ".properties";

	public BundleContextHistoryManager(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	@Override
	protected synchronized void saveHistory(String userId, SavedHistory hist) {
		Properties props = loadProperties(m_bundleContext);
		String historyXml = toXML(hist);
		
		//cleanUp(userId, props);
		
		props.put(hist.getFragment(), historyXml);
		props.put(userId, hist.getFragment());
		storeProperties(m_bundleContext, props);
	}

	@Override
	protected synchronized SavedHistory getHistory(String userId, String fragmentId) {
        if(fragmentId != null){
            Properties props = loadProperties(m_bundleContext);
            String xml = props.getProperty(fragmentId);
            if (xml == null || "".equals(xml)) {
                // There is no stored history for this fragment ID
                return null;
            } else {
                return JAXB.unmarshal(new StringReader(xml), SavedHistory.class);
            }
        }

        return null;
	}

	@Override
	public synchronized String getHistoryHash(String userId) {
		return loadProperties(m_bundleContext).getProperty(userId);
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
		StringWriter writer = new StringWriter();
		Marshaller marshaller;
		try {
			JAXBContext context = JAXBContext.newInstance(SavedHistory.class);
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			marshaller.marshal(hist, writer);
		} catch (JAXBException e) {
			LoggerFactory.getLogger(getClass()).error("Could not marshall SavedHistory object to String", e);
		}
		return writer.toString();
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
