// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//





/*
 * Created on 08-Dec-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.openoss.opennms.spring.qosd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

/**
 * loads the Qosd configuration from QoSD-configuration.xml
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSDConfigFactory {
	
	private static QoSDConfiguration config;
    
    /** Constant <code>is_loaded=false</code> */
    public static boolean is_loaded = false;
    
    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static void reload() throws IOException,MarshalException,ValidationException{
    	
    	String configFile = System.getProperty("opennms.home");
    	if(configFile.endsWith(java.io.File.separator)){
    		configFile = configFile.substring(0, configFile.length() - 1);
    	}
    	configFile += "/etc/QoSD-configuration.xml";

    	InputStream cfgIn = new FileInputStream(configFile);

		config = (QoSDConfiguration) Unmarshaller.unmarshal(QoSDConfiguration.class, new InputStreamReader(cfgIn));
		
		is_loaded = true;
		
		cfgIn.close();  	
    }
    
    /**
     * <p>Getter for the field <code>config</code>.</p>
     *
     * @return a {@link org.openoss.opennms.spring.qosd.QoSDConfiguration} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static QoSDConfiguration getConfig() throws IOException,MarshalException,ValidationException{
        if(!is_loaded) reload();
        return config;
    }
}
