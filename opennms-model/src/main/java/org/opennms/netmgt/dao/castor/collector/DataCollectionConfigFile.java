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
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

public class DataCollectionConfigFile {
	
	File m_file;
	
	public DataCollectionConfigFile(File file) {
		m_file = file;
	}
	
	public void visit(DataCollectionVisitor visitor) {
        DatacollectionConfig dataCollectionConfig = getDataCollectionConfig();
        visitor.visitDataCollectionConfig(dataCollectionConfig);
        
        for (Iterator it = dataCollectionConfig.getSnmpCollectionCollection().iterator(); it.hasNext();) {
            SnmpCollection snmpCollection = (SnmpCollection) it.next();
            doVisit(snmpCollection, visitor);
        }
        visitor.completeDataCollectionConfig(dataCollectionConfig);
    }
	
	private void doVisit(SnmpCollection snmpCollection, DataCollectionVisitor visitor) {
        visitor.visitSnmpCollection(snmpCollection);
        visitor.completeSnmpCollection(snmpCollection);
    }

    private DatacollectionConfig getDataCollectionConfig() {
		FileReader in = null;
		try {
			in = new FileReader(m_file);
			return (DatacollectionConfig) Unmarshaller.unmarshal(DatacollectionConfig.class, in);
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
