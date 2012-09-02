/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.CastorUtils;
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
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public synchronized static void parseXml(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_configuration = CastorUtils.unmarshal(ChartConfiguration.class, stream);
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
