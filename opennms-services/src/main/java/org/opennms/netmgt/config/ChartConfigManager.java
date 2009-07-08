/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * @author david
 *
 */
public abstract class ChartConfigManager {
    
    static ChartConfiguration m_configuration = null;
    
    /**
     * @param reader
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    @Deprecated
    public synchronized static void parseXml(Reader reader) throws MarshalException, ValidationException, IOException {
        m_configuration = CastorUtils.unmarshal(ChartConfiguration.class, reader);
    }
    
    public synchronized static void parseXml(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_configuration = CastorUtils.unmarshal(ChartConfiguration.class, stream);
    }
    
    /**
     * @param xml
     * @throws IOException
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * 
     */
    public synchronized void saveCurrent() throws MarshalException, ValidationException, IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_configuration, stringWriter);
        String xml = stringWriter.toString();
        saveXml(xml);
        update();
    }


    /**
     * @return
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public ChartConfiguration getConfiguration() throws IOException, MarshalException, ValidationException {
        return m_configuration;
    }
    
    public void setConfiguration(ChartConfiguration configuration) {
        m_configuration = configuration;
    }

    /**
     * @throws ValidationException
     * @throws MarshalException
     * @throws IOException
     * 
     */
    protected abstract void update() throws IOException, MarshalException, ValidationException;

}
