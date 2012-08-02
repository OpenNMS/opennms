/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class C3P0ConnectionFactoryTest extends TestCase {
    public void testMarshalDataSourceFromConfig() throws Exception {
        C3P0ConnectionFactory factory1 = null;
        C3P0ConnectionFactory factory2 = null;
        
        try {
        	factory1 = makeFactory("opennms");
        	factory2 = makeFactory("opennms2");

        	Connection conn = null;
        	Statement s = null;
        	try {
        		conn = factory2.getConnection();
        		s = conn.createStatement();
        		s.execute("select * from pg_proc");
        	} finally {
        		if (s != null) {
        			s.close();
        		}
        		if (conn != null) {
        			conn.close();
        		}
        	}
        } finally {
        	Throwable t1 = null;
        	Throwable t2 = null;
        	
    		if (factory1 != null) {
    			try {
    				factory1.close();
    				factory1 = null;
    			} catch (Throwable e1) {
    				t1 = e1;
    			}
    		}

    		if (factory2 != null) {
    			try {
    				factory2.close();
    				factory2 = null;
    			} catch (Throwable e2) {
    				t2 = e2;
    			}
    		}
    		
    		if (t1 != null || t2 != null) {
    			StringBuffer message = new StringBuffer();
    			message.append("Could not successfully close both C3P0 factories.  Future tests might fail.");
    			
    			Throwable choice;
    			if (t1 != null) {
    				message.append("  First factory failed with: " + t1.getMessage() + "; see stack back trace.");
    				choice = t1;
    				
    				if (t2 != null) {
    					System.err.println("  Both factories failed to close.  See stderr for second stack back trace.");
    					t2.printStackTrace(System.err);
    				}
    			} else {
    				choice = t2;
    			}
    			AssertionError e = new AssertionError(message.toString());
    			e.initCause(choice);
    			throw e;
    		}
        }
    }

    private C3P0ConnectionFactory makeFactory(String database) throws MarshalException, ValidationException, PropertyVetoException, SQLException, IOException {
        InputStream stream = this.getClass().getResourceAsStream(ConfigFileConstants.getFileName(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME));
        try {
            return new C3P0ConnectionFactory(stream, database);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
