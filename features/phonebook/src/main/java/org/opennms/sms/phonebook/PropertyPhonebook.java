package org.opennms.sms.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class PropertyPhonebook implements Phonebook {
    
    private String m_propertyFile;
    private long m_lastModified = 0;
    private Properties m_lastProperties = null;

    /**
     * Initialize the phone book.  Defaults to assuming "smsPhonebook.properties" will
     * exist in the classpath, and contain address -> phone number mappings.
     * @throws PhonebookException 
     */
    public PropertyPhonebook() {
        this("smsPhonebook.properties");
    }

    /**
     * Initialize the phone book with the given property filename.  The filename must
     * exist either as a path (a @{link File} object) or a resource in the class path.
     * 
     * @param filename the name of the phone book file
     * @throws PhonebookException 
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

    public String getPropertyFile() {
        return m_propertyFile;
    }

    public void setPropertyFile(String filename) {
        m_propertyFile = filename;
    }

    public String getTargetForAddress(String address) throws PhonebookException {
        Properties p = getProperties();
        String property = p.getProperty(address);
        
        if (property == null) {
            throw new PhonebookException("address: "+address+" not found in properties file");
        }
        
        return property;
    }
}
