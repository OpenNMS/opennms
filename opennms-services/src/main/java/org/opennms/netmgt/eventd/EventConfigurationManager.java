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
// 2007 Mar 02: Remove some unneeded casts and format the code a bit. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 23: Added include files to eventconf.xml.
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

package org.opennms.netmgt.eventd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.eventd.datablock.EventConfData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.util.StringUtils;

/**
 * <p>
 * This is the singleton class used to manage the event configuration. When the
 * class is loaded an attempt is made to read and load the file named
 * <tt>eventconf.xml</tt>. The file must be in the applications searched
 * classpaths in order to be loaded when the class is loaded.
 * </p>
 * 
 * <p>
 * Once the class is constructed it is possible to reconfigure or merge
 * additional events into the configuration by using the methods provided by the
 * class.
 * </p>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EventConfigurationManager {
    /**
     * The mapping of all the Event Configuration objects
     */
    private static EventConfData m_eventConf;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static volatile boolean m_loaded;

    /**
     * The list of secure tags.
     */
    private static volatile String[] m_secureTags;

    //
    // Attempts to load the configuration by default if
    // it's named eventconf.xml and in the classpath.
    //
    static {
        m_loaded = false;
        m_eventConf = new EventConfData();
    }

    /**
     * Returns true if the configuration has been loaded.
     * 
     * @return True if the configuration is loaded.
     */
    public static boolean isConfigured() {
        return m_loaded;
    }
    
    public static void init() throws MarshalException, ValidationException, IOException {
    	File configFile = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
    	loadConfiguration(configFile.getPath());
    }

    /**
     * Reloads the configuration off disk
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    public static void reload() throws MarshalException, ValidationException, IOException {
        //just calls init at the moment because it's safe to do so in this case
        //is a separate method to match the signature of other config managers in other packages
        init();
    }
    /**
     * This method is used to load the passed configuration into the currently
     * managed configuration instance. Any events that previously existed are
     * cleared.
     * 
     * @param file
     *            The file to load.
     * 
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @exception java.lang.IOException
     *                Thrown if the file cannot be opened for reading.
     */
    public static void loadConfiguration(String file) throws IOException, MarshalException, ValidationException {
        InputStream cfgIn = new FileInputStream(file);
        if (cfgIn == null) {
            throw new IOException("Failed to load/locate events conf file: " + file);
        }

        Reader rdr = new InputStreamReader(cfgIn);
        loadConfiguration(rdr);
    }

    /**
     * This method is used to load the passed configuration into the currently
     * managed configuration instance. Any events that previously existed are
     * cleared. After the contents of the reader stream is loaded the stream is
     * closed.
     * 
     * @param rdr
     *            The reader used to load the configuration.
     * 
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static void loadConfiguration(Reader rdr) throws IOException, MarshalException, ValidationException {
        Category log = ThreadCategory.getInstance();
        synchronized (m_eventConf) {
            Events toplevel = null;
            toplevel = (Events) Unmarshaller.unmarshal(Events.class, rdr);

            m_eventConf.clear();

            Enumeration e = toplevel.enumerateEvent();
            while (e.hasMoreElements()) {
                Event event = (Event) e.nextElement();
                m_eventConf.put(event);
            }

            m_secureTags = toplevel.getGlobal().getSecurity().getDoNotOverride();

            Enumeration e2 = toplevel.enumerateEventFile();
            while (e2.hasMoreElements()) {
                String eventfile = (String) e2.nextElement();
                InputStream fileIn = new FileInputStream(eventfile);
                if (fileIn == null) {
                    throw new IOException("Eventconf: Failed to load/locate events file: " + eventfile);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Eventconf: Loading event file: " + eventfile);
                }

                Reader filerdr = new InputStreamReader(fileIn);
                Events filelevel = null;
                filelevel = (Events) Unmarshaller.unmarshal(Events.class, filerdr);
                if (filelevel.getGlobal() != null) {
                    throw new ValidationException("The event file " + eventfile + " included from the top-level event configuration file cannot have a 'global' element");
                }
                if (filelevel.getEventFileCollection().size() > 0) {
                    throw new ValidationException("The event file " + eventfile + " included from the top-level event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(filelevel.getEventFileCollection()));
                }
                Enumeration efile = filelevel.enumerateEvent();
                while (efile.hasMoreElements()) {
                    Event event = (Event) efile.nextElement();
                    m_eventConf.put(event);
                }
            }

            try {
                rdr.close();
            } catch (Throwable t) {
            }
        }
        m_loaded = true;
    }

    /**
     * This method is used to load the passed configuration into the currently
     * managed configuration instance. Any events that previously existed are
     * overwritten by the new events in this configuration. This call will
     * replace the current override settings.
     * 
     * @param file
     *            The configuration file name.
     * 
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @exception java.lang.IOException
     *                Thrown if the file cannot be opened for reading.
     *                
     * @deprecated this is currently unused and will be deleted. brozow - 23-Jan-07
     */
    public static void mergeConfiguration(String file) throws IOException, MarshalException, ValidationException {
        Reader rdr = new FileReader(file);
        mergeConfiguration(rdr);
    }

    /**
     * This method is used to load the passed configuration into the currently
     * managed configuration instance. Any events that previously existed are
     * overwritten by the new events in this configuration. This call will
     * replace the current override settings. After the contents of the reader
     * stream is loaded, the reader is closed.
     * 
     * @param rdr
     *            The reader used to load the configuration.
     * 
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * 
     * @deprecated this is currently unused and will be deleted. brozow - 23-Jan-07
     */
    public static void mergeConfiguration(Reader rdr) throws MarshalException, ValidationException {
        synchronized (m_eventConf) {
            Events toplevel = null;
            toplevel = (Events) Unmarshaller.unmarshal(Events.class, rdr);

            Enumeration e = toplevel.enumerateEvent();
            while (e.hasMoreElements()) {
                Event event = (Event) e.nextElement();
                m_eventConf.put(event);
            }

            m_secureTags = toplevel.getGlobal().getSecurity().getDoNotOverride();

            try {
                rdr.close();
            } catch (Throwable t) {
            }
        }
        m_loaded = true;
    }

    /**
     * Returns the matching event configuration instance that is indexed by the
     * passed UEI. This is an instance of the <code>Event</code> that was
     * loaded from the event configuration file.
     * 
     * @return The loaded event matching the UEI
     */
    public static Event getByUei(String uei) {
        return m_eventConf.getEventByUEI(uei);
    }

    /**
     * Returns the matching event configuration instance for the event that just
     * came in. This is an instance of the <code>Event</code> that was loaded
     * from the event configuration file.
     * 
     * @return The loaded event matching the event.
     */
    public static Event get(org.opennms.netmgt.xml.event.Event event) {
        return m_eventConf.getEvent(event);
    }

    /**
     * Returns true if the tag is marked as secure.
     */
    public static boolean isSecureTag(String name) {
        for (int i = 0; i < m_secureTags.length; i++) {
            if (m_secureTags[i].equals(name)) {
                return true;
            }
        }

        return false;
    }

}
