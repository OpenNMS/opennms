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

package org.opennms.netmgt.rtc;

import java.util.Timer;
import java.util.TimerTask;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Maintains calculations for categories.</p>
 * 
 * <P>
 * The RTCManager maintains data required so as to calculate availability for
 * the different categories configured in categories.xml.
 * </P>
 *
 * <P>
 * The RTC initializes its data from the database when it comes up. It then
 * subscribes to the Events subsystem to receive events of interest to keep the
 * data up-to-date.
 * </P>
 *
 * <P>
 * Availability data is sent out to listeners who indicate that they are
 * listening by sending an RTC 'subscribe' event. The subscribe event has an URL
 * and user/passwd info. so RTC can post data to the URL.
 * </P>
 *
 * <P>
 * A timer defaulting to two minutes determines the interval at which data is sent.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *
 * @see org.opennms.netmgt.rtc.RTCConstants
 * @see org.opennms.netmgt.rtc.DataSender
 */
public final class RTCManager extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(RTCManager.class);
    
    private Logger log() { return LOG; }

    /**
     * The RTC timer
     */
    private Timer m_timer;

    /**
     * The DataSender
     */
    @Autowired
    private DataSender m_dataSender;

    @Autowired
    private RTCConfigFactory m_configFactory;

    /**
     * The scheduled task that runs and triggers the {@link DataSender}.
     */
    private class RTCTimerTask extends TimerTask {
        @Override
        public void run() {
            // send if not paused
            if (isRunning()) {
                m_dataSender.notifyToSend();
            }
        }
    }

    /**
     * The constructor for the RTCManager
     */
    public RTCManager() {
    	super("rtc");
        Logging.putPrefix("rtc");
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected synchronized void onInit() {

        //
        // Get the required attributes
        //

        // create the timer
        m_timer = new Timer();

        if (log().isDebugEnabled()) {
            log().debug("RTC ready to receive events");
        }
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected synchronized void onStart() {
		//
        // Start all the threads
        //

        if (log().isDebugEnabled()) {
            log().debug("Starting data sender ");
        }

        m_dataSender.start();

        if (log().isDebugEnabled()) {
            log().debug("Updater threads and datasender started");
        }

        // Set the user refresh timer
        m_timer.schedule(new RTCTimerTask(), 0, 120000);

        if (log().isDebugEnabled())
            log().debug("userTimer" + " scheduled");
        
        if (log().isDebugEnabled()) {
            log().debug("RTC ready to receive events");
        }
	}

    /**
     * <p>onStop</p>
     */
    @Override
    protected synchronized void onStop() {
        try {
            if (log().isDebugEnabled())
                log().debug("Beginning shutdown process");

            if (log().isDebugEnabled())
                log().debug("Shutting down the data sender");

            // shutdown the data sender
            m_dataSender.stop();

            if (log().isDebugEnabled())
                log().debug("DataSender shutdown");

            m_timer.cancel();

            if (log().isDebugEnabled())
                log().debug("Timer Cancelled");

        } catch (Throwable e) {
            log().error(e.getLocalizedMessage(), e);
        }
    }
}
