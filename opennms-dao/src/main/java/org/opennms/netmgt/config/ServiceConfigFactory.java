//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * <p>
 * This factory class is designed to be the main interface between the service
 * configuration information and the users of the information. When initialized
 * the factory loads the configuration from the file system, allowing access to
 * the information by others. The <code>init<code> method may be called by more
 * than one thread, but <em>MUST</em> be called by at least one thread before
 * the factory can be used.</p>
 *
 * <p>The factory supports the singleton design pattern, and thus the configuration
 * is loaded only once. All callers get the same reference unless a call to <code>
 * reload</code> is made. After than any saved instances of the factory can still
 * be referenced. Old references will not reflect any changes in the file if the
 * factory is reloaded.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
public final class ServiceConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static ServiceConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The loaded configuration after is has been unmarhsalled by castor.
     */
    private ServiceConfiguration m_config;

    /**
     * Private constructor. This constructor used to load the specified
     * configuration file and initialized an instance of the class.
     * 
     * @param configFile
     *            The name of the configuration file.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private ServiceConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgStream = new FileInputStream(new File(configFile));
        m_config = CastorUtils.unmarshal(ServiceConfiguration.class, cfgStream);
        cfgStream.close();
    }
    
    @Deprecated
    public ServiceConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(ServiceConfiguration.class, rdr);
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);

        ThreadCategory log = ThreadCategory.getInstance(ServiceConfigFactory.class);
        if (log.isDebugEnabled())
            log.debug("ServiceConfigFactory.init: config file path " + cfgFile.getPath());

        m_singleton = new ServiceConfigFactory(cfgFile.getPath());
        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Returns the currently defined singleton instance of the factory. There is
     * only one instance of the configuration information, and it will not
     * change unless the <code>reload</code> method is called.
     * 
     * @return The singular instance of the factory class.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     * 
     */
    public static synchronized ServiceConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("Factory not initialized");

        return m_singleton;
    }
    
    public static synchronized void setInstance(ServiceConfigFactory instance) {
        m_loaded = true;
        m_singleton = instance;
    }

    /**
     * Returns an array of all the defined configuration information for the
     * <em>Services</em>. If there are no defined services an array of length
     * zero is returned to the caller.
     * 
     * @return An array holding a reference to all the Service configuration
     *         instances.
     * 
     */
    public Service[] getServices() {
        int count = m_config.getServiceCount();
        Service[] slist = new Service[count];

        count = 0;
        for (Service s : m_config.getServiceCollection()) {
            slist[count++] = s;
        }

        return slist;
    }
}
