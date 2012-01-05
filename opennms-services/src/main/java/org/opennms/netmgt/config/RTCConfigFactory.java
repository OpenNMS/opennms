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

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.rtc.RTCConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * RTC from the rtc-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class RTCConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static RTCConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private RTCConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Parse the rolling window in the properties file in the format <xx>h <yy>m
     * <zz>s into a long value of milliseconds
     * 
     * @return the rolling window as milliseconds
     */
    private long parseRollingWindow(String rolling) throws IllegalArgumentException {
        String hrStr = null;
        String minStr = null;
        String secStr = null;

        rolling = rolling.toLowerCase();

        int hIndex = rolling.indexOf('h');
        int mIndex = rolling.indexOf('m');
        int sIndex = rolling.indexOf('s');

        // get the hour component
        if (hIndex != -1)
            hrStr = rolling.substring(0, hIndex);

        if (mIndex != -1) // min component present
        {
            if (hIndex != -1) // hours also present
            {
                // make sure format is right
                if (hIndex >= mIndex)
                    throw new IllegalArgumentException("RTC: Value " + rolling + " - format incorrect");

                minStr = rolling.substring(hIndex + 1, mIndex);
            } else
                minStr = rolling.substring(0, mIndex);
        }

        if (sIndex != -1) // seconds component present
        {
            if (mIndex != -1) {
                if (mIndex >= sIndex)
                    throw new IllegalArgumentException("RTC: Value " + rolling + " - format incorrect");
                secStr = rolling.substring(mIndex + 1, sIndex);
            } else if (hIndex != -1) {
                if (hIndex >= sIndex)
                    throw new IllegalArgumentException("RTC: Value " + rolling + " - format incorrect");
                secStr = rolling.substring(hIndex + 1, sIndex);

            } else
                secStr = rolling.substring(0, sIndex);
        }

        int hours = 0;
        int min = 0;
        int sec = 0;

        try {
            if (hrStr != null)
                hours = Integer.parseInt(hrStr);

            if (minStr != null)
                min = Integer.parseInt(minStr);

            if (secStr != null)
                sec = Integer.parseInt(secStr);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("RTC: Value " + rolling + " - format incorrect");
        }

        return (long) ((hours * 3600) + (min * 60) + sec) * 1000;
    }

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
    private RTCConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            marshal(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    /**
     * <p>Constructor for RTCConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public RTCConfigFactory(InputStream stream) throws IOException, MarshalException, ValidationException {
        marshal(stream);
    }

    private void marshal(InputStream stream) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(RTCConfiguration.class, stream);
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.RTCConfigFactory} object.
     */
    public static void setInstance(RTCConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
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
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.RTC_CONFIG_FILE_NAME);

        setInstance(new RTCConfigFactory(cfgFile.getPath()));
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
    public static synchronized RTCConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the number of updater threads to be started.
     *
     * @return the number of updater threads to be started
     */
    public synchronized int getUpdaters() {
        return m_config.getUpdaters();
    }

    /**
     * Return the number of sender to be started.
     *
     * @return the number of sender threads to be started
     */
    public synchronized int getSenders() {
        return m_config.getSenders();
    }

    /**
     * Return the rolling window for which availability is to be computed.
     *
     * @return the rolling window for which availability is to be computed
     */
    public synchronized String getRollingWindowStr() {
        return m_config.getRollingWindow();
    }

    /**
     * Return the rolling window for which availability is to be computed.
     *
     * @return the rolling window for which availability is to be computed
     */
    public synchronized long getRollingWindow() {
        return parseRollingWindow(m_config.getRollingWindow());
    }

    /**
     * Return the max number of events after which data is to resent.
     *
     * @return the max number of events after which data is to resent
     */
    public synchronized int getMaxEventsBeforeResend() {
        return m_config.getMaxEventsBeforeResend();
    }

    /**
     * Return the low threshold interval at which data is to be resent.
     *
     * @return the low threshold interval at which data is to be resent
     */
    public synchronized String getLowThresholdIntervalStr() {
        return m_config.getLowThresholdInterval();
    }

    /**
     * Return the low threshold interval at which data is to be resent.
     *
     * @return the low threshold interval at which data is to be resent
     */
    public synchronized long getLowThresholdInterval() {
        return parseRollingWindow(m_config.getLowThresholdInterval());
    }

    /**
     * Return the high threshold interval at which data is to be resent.
     *
     * @return the high threshold interval at which data is to be resent
     */
    public synchronized String getHighThresholdIntervalStr() {
        return m_config.getHighThresholdInterval();
    }

    /**
     * Return the high threshold interval at which data is to be resent.
     *
     * @return the high threshold interval at which data is to be resent
     */
    public synchronized long getHighThresholdInterval() {
        return parseRollingWindow(m_config.getHighThresholdInterval());
    }

    /**
     * Return the user refresh interval at which data is to be resent - this is
     * the interval at which data is resent when no events are received.
     *
     * @return the user refresh interval at which data is to be resent
     */
    public synchronized String getUserRefreshIntervalStr() {
        return m_config.getUserRefreshInterval();
    }

    /**
     * Return the user refresh interval at which data is to be resent - this is
     * the interval at which data is resent when no events are received.
     *
     * @return the user refresh interval at which data is to be resent
     */
    public synchronized long getUserRefreshInterval() {
        return parseRollingWindow(m_config.getUserRefreshInterval());
    }

    /**
     * Return the number of times posts are tried with errors before an URL is
     * automatically unsubscribed. A negative value means URL is never
     * unsubscribed
     *
     * @return the number of times posts are tried with errors before an URL is
     *         automatically unsubscribed
     */
    public synchronized int getErrorsBeforeUrlUnsubscribe() {
        return m_config.getErrorsBeforeUrlUnsubscribe();
    }
}
