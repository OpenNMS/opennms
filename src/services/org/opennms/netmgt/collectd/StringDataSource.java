//
// // // This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc. All
// rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights for
// modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
//
package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * @author craig.miskell@agresearch.co.nz
 *  
 */
public class StringDataSource extends DataSource {
	
	/**
	 * @param objectType MIB object type being inquired about
	 * @return true if StringDataSource can  handle the given type, false if it can't
	 */
	public static boolean handlesType(String objectType) {
		return (objectType.toLowerCase().equals("string"));
	}

	/**
	 * @param obj mibObject to use as the basis for configuring this datasource
	 */
	public StringDataSource(MibObject obj) {
		super(obj);
		//Nothing else custom to do
	}

	public boolean performUpdate(
	        String collectionName,
	        String owner,
	        File repository,
	        String dsName,
	        String val) {
	    
	    Category log = ThreadCategory.getInstance(getClass());
	    	    
	    Properties props = new Properties();
	    File propertiesFile =	 new File(repository,"strings.properties");
	    
        FileInputStream fileInputStream = null;
	    //Preload existing data
	    if (propertiesFile.exists()) {
	        try {
	            fileInputStream = new FileInputStream(propertiesFile);
	            props.load(fileInputStream);
	        } catch (Exception e) {
                log.error("performUpdate: Error openning properties file.", e);
	            return true;
	        } finally {
	            try {
                    if (fileInputStream != null) fileInputStream.close();
                } catch (IOException e) {
                    log.error("performUpdate: Error closing file.", e);
                }
            }
	    }
	    props.setProperty(this.getName(), val);
        FileOutputStream fileOutputStream = null;
	    try {
	        fileOutputStream = new FileOutputStream(propertiesFile);
            props.store(fileOutputStream, null);
	    } catch (Exception e) {
	        //Ouch, something went wrong that we should mention to the outside world
	        e.printStackTrace();
	        return true;
	    } finally {
	        try {
	            if (fileOutputStream != null) {
	                fileOutputStream.flush();
	                fileOutputStream.close();
	            }
	        } catch (IOException e) {
	            log.error("performUpdate: Error closing file.", e);
	        }
        }
	    return false;
	    
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		// Build the buffer
		buffer.append("\n   oid:       ").append(m_oid);
		buffer.append("\n   name: 	 ").append(m_name);

		return buffer.toString();
	}

    public String getStorableValue(SnmpValue snmpVar) {
        return snmpVar.toString();
    }

}
