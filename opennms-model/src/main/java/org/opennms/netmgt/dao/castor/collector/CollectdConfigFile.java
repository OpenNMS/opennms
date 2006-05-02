package org.opennms.netmgt.dao.castor.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;

public class CollectdConfigFile {
	
	File m_file;
	
	public CollectdConfigFile(File file) {
		m_file = file;
	}
	
	public void visit(CollectdConfigVisitor visitor) {
        CollectdConfiguration collectdConfiguration = getCollectdConfiguration();
        visitor.visitCollectdConfiguration(collectdConfiguration);
        
        for (Iterator it = collectdConfiguration.getCollectorCollection().iterator(); it.hasNext();) {
            Collector collector = (Collector) it.next();
            doVisit(collector, visitor);
        }
        visitor.completeCollectdConfiguration(collectdConfiguration);
    }
	
	private void doVisit(Collector collector, CollectdConfigVisitor visitor) {
        visitor.visitCollectorCollection(collector);
        visitor.completeCollectorCollection(collector);
    }

    private CollectdConfiguration getCollectdConfiguration() {
		FileReader in = null;
		try {
			in = new FileReader(m_file);
			return (CollectdConfiguration) Unmarshaller.unmarshal(CollectdConfiguration.class, in);
		} catch (MarshalException e) {
			throw runtimeException("Syntax error in "+m_file, e);
		} catch (ValidationException e) {
			throw runtimeException("invalid attribute in "+m_file, e);
		} catch (FileNotFoundException e) {
			throw runtimeException("Unable to find file "+m_file, e);
		} finally {
			closeQuietly(in);
		}
	}

	private RuntimeException runtimeException(String msg, Exception e) {
		log().error(msg, e);
		return new RuntimeException(msg, e);
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void closeQuietly(FileReader in) {
		try {
			if (in != null) in.close();
		} catch (IOException e) {
		}		
	}

}
