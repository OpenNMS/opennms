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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.xmlrpcd.ExternalServers;
import org.opennms.netmgt.config.xmlrpcd.SubscribedEvent;
import org.opennms.netmgt.config.xmlrpcd.Subscription;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * xmlrpcd service from the xmlrpcd-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class XmlrpcdConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static XmlrpcdConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The config class loaded from the config file
     */
    private XmlrpcdConfiguration m_config;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private XmlrpcdConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream is = null;
        try {
            is = new FileInputStream(configFile);
            unmarshal(is);
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }
    
    /**
     * Constructor for testing
     *
     * @exception java.io.IOException
     *                Thrown if the specified reader cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public XmlrpcdConfigFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        unmarshal(rdr);
    }

    /**
     * <p>Constructor for XmlrpcdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public XmlrpcdConfigFactory(InputStream stream) throws MarshalException, ValidationException {
        unmarshal(stream);
    }

    @Deprecated
    private void unmarshal(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(XmlrpcdConfiguration.class, rdr);

        handleLegacyConfiguration();
    }

    private void unmarshal(InputStream stream) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(XmlrpcdConfiguration.class, stream);

        handleLegacyConfiguration();
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
        if (m_loaded) {
            // Already initialized.  To reload, reload() will need to be called.
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMLRPCD_CONFIG_FILE_NAME);

        init(cfgFile);
    }

    /**
     * Load the specified config file and create the singleton instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @param cfgFile a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init(File cfgFile) throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        log().debug("init: config file path: " + cfgFile.getPath());

        setInstance(new XmlrpcdConfigFactory(cfgFile.getPath()));
    }

    private void handleLegacyConfiguration() {
        String generatedSubscriptionName = null;

        /* Be backwards-compatible with old configurations.
         * 
         * The old style configuration did not have a <serverSubscription> field
         * inside the <external-servers> tag, so create a default one.
         */
        Enumeration<ExternalServers> e = getExternalServerEnumeration();
        while (e.hasMoreElements()) {
            ExternalServers es = e.nextElement();
            if (es.getServerSubscriptionCollection().size() == 0) {
                if (generatedSubscriptionName == null) {
                    generatedSubscriptionName = "legacyServerSubscription-" + java.util.UUID.randomUUID().toString();
                }
                es.addServerSubscription(generatedSubscriptionName);
            }
        }

        if (generatedSubscriptionName != null) {
            boolean foundUnnamedSubscription = false;
            for (Subscription s : getConfiguration().getSubscriptionCollection()) {
                if (s.getName() == null) {
                    s.setName(generatedSubscriptionName);
                    foundUnnamedSubscription = true;
                    break;
                }
            }
            if (! foundUnnamedSubscription) {
                String[] ueis = {
                        EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                        EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI,
                        EventConstants.NODE_UP_EVENT_UEI,
                        EventConstants.NODE_DOWN_EVENT_UEI,
                        EventConstants.INTERFACE_UP_EVENT_UEI,
                        EventConstants.INTERFACE_DOWN_EVENT_UEI,
                        EventConstants.UPDATE_SERVER_EVENT_UEI,
                        EventConstants.UPDATE_SERVICE_EVENT_UEI,
                        EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI
                };
                Subscription subscription = new Subscription();
                subscription.setName(generatedSubscriptionName);
                SubscribedEvent subscribedEvent = null;
                for (String uei : ueis) {
                    subscribedEvent = new SubscribedEvent();
                    subscribedEvent.setUei(uei);
                    subscription.addSubscribedEvent(subscribedEvent);
                }
                getConfiguration().addSubscription(subscription);
            }
        }
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(XmlrpcdConfigFactory.class);
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
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized XmlrpcdConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.XmlrpcdConfigFactory} object.
     */
    public static synchronized void setInstance(XmlrpcdConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * Return the xmlrpcd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration} object.
     */
    public synchronized XmlrpcdConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * Retrieves configured list of subscribed event uei for the given
     *  subscribing server.
     *
     * @throws org.exolab.castor.xml.ValidationException if a serverSubscription
     *          element references a subscription name that doesn't exist
     * @return an enumeration of subscribed event ueis.
     * @param server a {@link org.opennms.netmgt.config.xmlrpcd.ExternalServers} object.
     */
    public synchronized List<SubscribedEvent> getEventList(ExternalServers server) throws ValidationException {
        List<SubscribedEvent> allEventsList = new ArrayList<SubscribedEvent>();
        for (String name : server.getServerSubscriptionCollection()) {
            List<Subscription> subscriptions = m_config.getSubscriptionCollection();

            boolean foundSubscription = false;
            for (Subscription sub : subscriptions) {
                if (sub.getName().equals(name)) {
                    allEventsList.addAll(sub.getSubscribedEventCollection());
                    foundSubscription = true;
                    break;
                }
            }

            if (!foundSubscription) {
                /*
                 * Oops -- a serverSubscription element referenced a 
                 * subscription element that doesn't exist.
                 */
                log().error("serverSubscription element " + name + 
                            " references a subscription that does not exist");
                throw new ValidationException("serverSubscription element " +
                    name + " references a subscription that does not exist");
            }
        }

        return allEventsList;
    }

    /**
     * Retrieves configured list of xmlrpc servers and the events to which
     *  they subscribe.
     *
     * @return an enumeration of xmlrpc servers.
     */
    public synchronized Enumeration<ExternalServers> getExternalServerEnumeration() {
        return m_config.enumerateExternalServers();
    }

    /**
     * Retrieves configured list of server subscriptions and the UEIs they
     * are associated with.
     *
     * @return an enumeration of subscriptions.
     */
    public synchronized Enumeration<Subscription> getSubscriptionEnumeration() {
    	return m_config.enumerateSubscription();
    }
    
    /**
     * Retrieves configured list of xmlrpc servers and the events to which
     *  they subscribe.
     *
     * @return a collection of xmlrpc servers.
     */
    public synchronized Collection<ExternalServers> getExternalServerCollection() {
    	return m_config.getExternalServersCollection();
    }

    /**
     * Retrieves configured list of server subscriptions and the UEIs they
     * are associated with.
     *
     * @return a collection of subscriptions.
     */
    public synchronized Collection<Subscription> getSubscriptionCollection() {
    	return m_config.getSubscriptionCollection();
    }
    
    /**
     * Retrieves the max event queue size from configuration.
     *
     * @return the max size of the xmlrpcd event queue.
     */
    public synchronized int getMaxQueueSize() {
        return m_config.getMaxEventQueueSize();
    }
}
