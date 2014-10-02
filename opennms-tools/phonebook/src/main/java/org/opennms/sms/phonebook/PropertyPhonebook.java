/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * <p>PropertyPhonebook class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PropertyPhonebook implements Phonebook {
    
    private String m_propertyFile;
    private long m_lastModified = 0;
    private Properties m_lastProperties = null;

    /**
     * Initialize the phone book.  Defaults to assuming "smsPhonebook.properties" will
     * exist in the classpath, and contain IP address -> phone number mappings.
     *
     * @throws PhonebookException if any.
     */
    public PropertyPhonebook() {
        this("smsPhonebook.properties");
    }

    /**
     * Initialize the phone book with the given property filename.  The filename must
     * exist either as a path (a @{link File} object) or a resource in the classpath.
     *
     * @param filename the name of the phone book file
     */
    public PropertyPhonebook(String filename) {
        m_propertyFile = filename;
    }

    /**
     * Get the current properties map.  If it exists as a file (rather than a resource)
     * it will check the last modified time and only bother reading it if it's changed.
     * 
     * @return the @{link Properties} object from the property file.  Can return a null
     * @throws PhonebookException
     */
    private Properties getProperties() throws PhonebookException {
        InputStream stream = null;
        try {
            File propertyFile = new File(m_propertyFile);
            if (propertyFile.exists()) {
                if (propertyFile.lastModified() == m_lastModified && m_lastProperties != null) {
                    return m_lastProperties;
                }
                stream = new FileInputStream(propertyFile);
                m_lastModified = propertyFile.lastModified();
            } else {
                stream = this.getClass().getResourceAsStream(m_propertyFile);
                if (stream == null) {
                    stream = this.getClass().getResourceAsStream("/" + m_propertyFile);
                }
            }
            if (stream == null) {
                throw new PhonebookException(String.format("Unable to find resource '%s' in the classpath.", m_propertyFile));
            }
            Properties p = new Properties();
            p.load(stream);
            m_lastProperties = p;
            return p;
        } catch (IOException e) {
            throw new PhonebookException(String.format("An error occurred reading from %s", m_propertyFile), e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * <p>getPropertyFile</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPropertyFile() {
        return m_propertyFile;
    }

    /**
     * <p>setPropertyFile</p>
     *
     * @param filename a {@link java.lang.String} object.
     */
    public void setPropertyFile(String filename) {
        m_propertyFile = filename;
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetForAddress(String address) throws PhonebookException {
        Properties p = getProperties();
        String property = p.getProperty(address);
        
        if (property == null) {
            throw new PhonebookException("address: "+address+" not found in properties file");
        }
        
        return property;
    }
}
