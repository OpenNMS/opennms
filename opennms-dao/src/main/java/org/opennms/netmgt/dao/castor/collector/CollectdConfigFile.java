//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
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

/**
 * <p>CollectdConfigFile class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectdConfigFile {
	
	File m_file;
	
	/**
	 * <p>Constructor for CollectdConfigFile.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 */
	public CollectdConfigFile(File file) {
		m_file = file;
	}
	
	/**
	 * <p>visit</p>
	 *
	 * @param visitor a {@link org.opennms.netmgt.dao.castor.collector.CollectdConfigVisitor} object.
	 */
	public void visit(CollectdConfigVisitor visitor) {
        CollectdConfiguration collectdConfiguration = getCollectdConfiguration();
        visitor.visitCollectdConfiguration(collectdConfiguration);
        
        for (Iterator<Collector> it = collectdConfiguration.getCollectorCollection().iterator(); it.hasNext();) {
            Collector collector = it.next();
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
