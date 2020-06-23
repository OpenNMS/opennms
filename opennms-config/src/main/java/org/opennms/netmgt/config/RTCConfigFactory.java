/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.rtc.RTCConfiguration;
import org.springframework.beans.factory.InitializingBean;

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
 */
public final class RTCConfigFactory implements InitializingBean {

    /**
     * The config class loaded from the config file
     */
    private RTCConfiguration m_config;

    /**
     * Parse the rolling window in the properties file in the format <xx>h <yy>m
     * <zz>s into a long value of milliseconds
     * 
     * @return the rolling window as milliseconds
     */
    private static long parseRollingWindow(String rolling) throws IllegalArgumentException {
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
     * Default constructor.
     */
    public RTCConfigFactory() {}

    /**
     * <p>Constructor for RTCConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public RTCConfigFactory(InputStream stream) throws IOException {
        m_config = unmarshal(stream);
    }

    private static RTCConfiguration unmarshal(InputStream stream) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(stream)) {
            return JaxbUtils.unmarshal(RTCConfiguration.class, isr);
        }
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    @Override
    public void afterPropertiesSet() throws IOException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.RTC_CONFIG_FILE_NAME);

        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            m_config = unmarshal(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * Return the number of updater threads to be started.
     *
     * @return the number of updater threads to be started
     */
    public int getUpdaters() {
        return m_config.getUpdaters();
    }

    /**
     * Return the number of sender to be started.
     *
     * @return the number of sender threads to be started
     */
    public int getSenders() {
        return m_config.getSenders();
    }

    /**
     * Return the rolling window for which availability is to be computed.
     *
     * @return the rolling window for which availability is to be computed
     */
    public String getRollingWindowStr() {
        return m_config.getRollingWindow();
    }

    /**
     * Return the rolling window for which availability is to be computed.
     *
     * @return the rolling window for which availability is to be computed
     */
    public long getRollingWindow() {
        return parseRollingWindow(m_config.getRollingWindow());
    }

    /**
     * Return the max number of events after which data is to resent.
     *
     * @return the max number of events after which data is to resent
     */
    public int getMaxEventsBeforeResend() {
        return m_config.getMaxEventsBeforeResend();
    }

    /**
     * Return the low threshold interval at which data is to be resent.
     *
     * @return the low threshold interval at which data is to be resent
     */
    public String getLowThresholdIntervalStr() {
        return m_config.getLowThresholdInterval();
    }

    /**
     * Return the low threshold interval at which data is to be resent.
     *
     * @return the low threshold interval at which data is to be resent
     */
    public long getLowThresholdInterval() {
        return parseRollingWindow(m_config.getLowThresholdInterval());
    }

    /**
     * Return the high threshold interval at which data is to be resent.
     *
     * @return the high threshold interval at which data is to be resent
     */
    public String getHighThresholdIntervalStr() {
        return m_config.getHighThresholdInterval();
    }

    /**
     * Return the high threshold interval at which data is to be resent.
     *
     * @return the high threshold interval at which data is to be resent
     */
    public long getHighThresholdInterval() {
        return parseRollingWindow(m_config.getHighThresholdInterval());
    }

    /**
     * Return the user refresh interval at which data is to be resent - this is
     * the interval at which data is resent when no events are received.
     *
     * @return the user refresh interval at which data is to be resent
     */
    public String getUserRefreshIntervalStr() {
        return m_config.getUserRefreshInterval();
    }

    /**
     * Return the user refresh interval at which data is to be resent - this is
     * the interval at which data is resent when no events are received.
     *
     * @return the user refresh interval at which data is to be resent
     */
    public long getUserRefreshInterval() {
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
    public int getErrorsBeforeUrlUnsubscribe() {
        return m_config.getErrorsBeforeUrlUnsubscribe();
    }
}
