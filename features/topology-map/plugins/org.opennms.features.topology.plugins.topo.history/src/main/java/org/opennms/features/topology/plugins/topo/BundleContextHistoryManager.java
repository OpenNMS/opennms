package org.opennms.features.topology.plugins.topo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.opennms.features.topology.api.support.AbstractHistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

public class BundleContextHistoryManager extends AbstractHistoryManager {

	private final BundleContext m_bundleContext;
	public static final String DATA_FILE_NAME = BundleContextHistoryManager.class.getName() + ".properties";

	public BundleContextHistoryManager(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	@Override
	protected synchronized void saveHistory(String userId, SavedHistory hist) {
		Properties props = loadProperties(m_bundleContext);
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
		props.put(hist.getFragment(), writer.toString());
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
	public synchronized String getHistoryForUser(String userId) {
		return loadProperties(m_bundleContext).getProperty(userId);
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
