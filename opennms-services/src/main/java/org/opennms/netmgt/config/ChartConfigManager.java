//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.charts.ChartConfiguration;

/**
 * <p>Abstract ChartConfigManager class.</p>
 *
 * @author david
 * @version $Id: $
 */
public abstract class ChartConfigManager {
    
    static ChartConfiguration m_configuration = null;
    
    /**
     * <p>parseXml</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public synchronized static void parseXml(Reader reader) throws MarshalException, ValidationException, IOException {
        m_configuration = (ChartConfiguration) Unmarshaller.unmarshal(ChartConfiguration.class, reader);
    }
    
    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * <p>saveCurrent</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws MarshalException, ValidationException, IOException {
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_configuration, stringWriter);
        String xml = stringWriter.toString();
        saveXml(xml);
        update();
    }


    /**
     * <p>getConfiguration</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @return a {@link org.opennms.netmgt.config.charts.ChartConfiguration} object.
     */
    public ChartConfiguration getConfiguration() throws IOException, MarshalException, ValidationException {
        return m_configuration;
    }
    
    /**
     * <p>setConfiguration</p>
     *
     * @param configuration a {@link org.opennms.netmgt.config.charts.ChartConfiguration} object.
     */
    public void setConfiguration(ChartConfiguration configuration) {
        m_configuration = configuration;
    }

    /**
     * <p>update</p>
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     */
    protected abstract void update() throws IOException, MarshalException, ValidationException;

}
