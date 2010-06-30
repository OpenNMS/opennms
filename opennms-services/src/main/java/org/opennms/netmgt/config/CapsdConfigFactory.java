/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: Moved non-static bits to DefaultCapsdConfigurationManager
 *              and leave this as just a factory. - dj@opennms.org
 * 2004 Dec 27: Updated code to determine primary SNMP interface to select
 *              an interface from collectd-configuration.xml first, and if
 *              none found, then from all interfaces on the node. In either
 *              case, a loopback interface is preferred if available.
 * 2004 Jan 06: Added support for STATUS_SUSPEND abd STATUS_RESUME
 * 2003 Nov 11: Merged changes from Rackspace project
 * 2003 Sep 17: Fixed an SQL parameter problem.
 * 2003 Sep 16: Changed rescan information to let OpenNMS handle duplicate IPs.
 * 2003 Jan 31: Cleaned up some unused imports.
 * 2002 Aug 27: Fixed <range> tag. Bug #655
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.springframework.util.Assert;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Capsd service from the capsd-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class CapsdConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static CapsdConfig s_singleton = null;
        
    /**
     * This class only has static methods.
     */
    private CapsdConfigFactory() {
        
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (isLoaded()) {
            /*
             * init already called - return
             * to reload, reload() will need to be called
             */
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);

        if (log().isDebugEnabled()) {
            log().debug("init: config file path: " + cfgFile.getPath());
        }

        DefaultCapsdConfigManager capsdConfig = new DefaultCapsdConfigManager();
        capsdConfig.update();
        setInstance(capsdConfig);
    }

    private static boolean isLoaded() {
        return s_singleton != null;
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        s_singleton = null;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized CapsdConfig getInstance() {
        Assert.state(isLoaded(), "The factory has not been initialized");

        return s_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public static synchronized void setInstance(CapsdConfig instance) {
        s_singleton = instance;
    }

    private static Category log() {
        return ThreadCategory.getInstance(CapsdConfigFactory.class);
    }

}
