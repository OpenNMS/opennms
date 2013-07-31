/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains calculations for categories.
 * <P>
 * The RTCManager maintains data required so as to calculate availability for
 * the different categories configured in categories.xml
 * </P>
 *
 * <P>
 * The RTC initializes its data from the database when it comes up. It then
 * subscribes to the Events subsystem to receive events of interest to keep the
 * data up-to-date
 * </P>
 *
 * <P>
 * Availability data is sent out to listeners who indicate that they are
 * listening by sending an RTC 'subscribe' event. The subscribe event has an URL
 * and user/passwd info. so RTC can post data to the URL
 * </P>
 *
 * <P>
 * The RTC has two timers(a low threshold and a high threshold) and a counter
 * that can run upto a configurable max number of events - these are used to
 * determine when availability information is to be sent out when event streams
 * are coming in at normal rates. When no events are received, a timer
 * configured with a user configured time(defaulting to one minute) decides the
 * interval at which data is sent
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @see org.opennms.netmgt.rtc.RTCConstants
 * @see org.opennms.netmgt.rtc.DataSender
 * @see org.opennms.netmgt.rtc.DataManager
 * @version $Id: $
 */
public final class RTCManager extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(RTCManager.class);
    
    private Logger log() { return LOG; }

    /**
     * Singleton instance of this class
     */
    private static final RTCManager m_singleton = new RTCManager();

    /**
     * The id for the low threshold timer task
     */
    private static final String LOWT_TASK = "lowTtask";

    /**
     * The id for the high threshold timer task
     */
    private static final String HIGHT_TASK = "highTtask";

    /**
     * The id for the user refresh timer task
     */
    private static final String USERTIMER = "userTimer";

    /**
     * The initial number of updater threads
     */
    @SuppressWarnings("unused")
    private static final int NUM_UPDATERS = 5;

    /**
     * The configurable rolling window read from the properties file
     */
    private static long m_rollingWindow = -1;

    /**
     * The RTC timer
     */
    private Timer m_timer;

    /**
     * The low threshold timer task
     */
    private TimerTask m_lowTtask;

    /**
     * The low threshold refresh interval. The low threshold at which data is
     * sent out
     */
    private long m_lowThresholdInterval = -1;

    /**
     * The high threshold timer task
     */
    private TimerTask m_highTtask;

    /**
     * The high threshold refresh interval. The high threshold at which data is
     * sent out
     */
    private long m_highThresholdInterval = -1;

    /**
     * The user refresh timer task
     */
    private TimerTask m_userTask;

    /**
     * The user refresh interval. The interval at which data is sent even if no
     * events are received
     */
    private long m_userRefreshInterval = -1;

    /**
     * The counter keeping track of the number of messages
     */
    private int m_counter = -1;

    /**
     * The maximum number of events that are received before a resend. Note that
     * this or the timers going off, whichever occurs first triggers a resend
     */
    private int MAX_EVENTS_BEFORE_RESEND = -1;

    /**
     * The events receiver
     */
    private BroadcastEventProcessor m_eventReceiver;

    /**
     * The RunnableConsumerThreadPool that runs updaters that interpret and
     * update the data
     */
    private ExecutorService m_updaterPool;

    /**
     * The DataSender
     */
    private DataSender m_dataSender;

    /**
     * manager of the data maintained by the RTC
     */
    private static DataManager m_dataMgr;

    /**
     * The timer scheduled task that runs and informs the RTCManager when the
     * timer goes off
     */
    private class RTCTimerTask extends TimerTask {
        /**
         * The timer id
         */
        private String m_id;

        /**
         * Constructor for the timer task
         * 
         * @param id
         *            the timertask ID
         */
        RTCTimerTask(String id) {
            m_id = id;
        }

        /**
         * Return the ID
         * 
         * @return the ID
         */
        public String getID() {
            return m_id;
        }

        /**
         * Starts the task. When run, simply inform the manager that this has
         * been called by the timer
         */
        @Override
        public void run() {
            timerTaskComplete(this);
        }
    }

    /**
     * Handles a completed task.
     * 
     * <P>
     * If the low threshold or high threshold timers expire, send category data
     * out and set both timer(task)s to null so they can be reset when the next
     * event comes in
     * <P>
     * 
     * <P>
     * If the user refresh timer is the one that expired, send category data out
     * and reset the user timer(task)
     * <P>
     * 
     * @param tt
     *            the task that is finishing.
     */
    private synchronized void timerTaskComplete(RTCTimerTask tt) {
        LOG.debug("TimerTask \'{}\' complete, status: {}", tt.getID(), getStatus());

        if (tt.getID().equals(LOWT_TASK)) {
            // cancel user timer
            boolean ret = m_userTask.cancel();
            LOG.debug("timerTaskComplete: {} cancelled: {}", USERTIMER, ret);

            // send out the info and reset both timers
            if (m_highTtask != null) {
                ret = m_highTtask.cancel();
                LOG.debug("timerTaskComplete: {} cancelled: {}", HIGHT_TASK, ret);

                m_highTtask = null;
            }

            if (isRunning()) {
                m_dataSender.notifyToSend();
            }

            m_lowTtask = null;

            m_counter = -1;

            // reset the user timer
            m_timer.schedule((m_userTask = new RTCTimerTask(USERTIMER)), 0, m_userRefreshInterval);
            LOG.debug("timerTaskComplete: {} scheduled", USERTIMER);
        } else if (tt.getID().equals(HIGHT_TASK)) {
            // cancel user timer
            boolean ret = m_userTask.cancel();
            LOG.debug("timerTaskComplete: {} cancelled: {}", USERTIMER, ret);

            // send the category information out reset all timers
            if (m_lowTtask != null) {
                ret = m_lowTtask.cancel();
                LOG.debug("timerTaskComplete: {} cancelled: {}", LOWT_TASK, ret);

                m_lowTtask = null;
            }

            if (isRunning()) {
                m_dataSender.notifyToSend();
            }

            m_highTtask = null;

            m_counter = -1;

            // reset the user timer
            m_timer.schedule((m_userTask = new RTCTimerTask(USERTIMER)), 0, m_userRefreshInterval);
            LOG.debug("timerTaskComplete: {} scheduled", USERTIMER);
        } else if (tt.getID().equals(USERTIMER)) {
            // send if not pasued
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
    }

    /**
     * Check the timer tasks. Reset any of the timer tasks if they need to be
     * reset (indicated by their being set to null on timer task completion). If
     * the events counter has exceeded maxEventsBeforeResend, send data out and
     * reset timers
     */
    public synchronized void checkTimerTasksOnEventReceipt() {
        LOG.debug("checkTimerTasksOnEventReceipt: Checking if timer tasks need to be reset or data needs to be sent out");

        // cancel user timer
        boolean ret = m_userTask.cancel();
        LOG.debug("checkTimerTasksOnEventReceipt: {} cancelled: {}", USERTIMER, ret);

        // Check the counter to see if timers need to be started afresh
        if (m_counter == -1) {
            m_counter = 0;

            //
            // set timers
            //

            // set the low threshold timer task
            if (m_lowTtask == null) {
                try {

                    m_timer.schedule((m_lowTtask = new RTCTimerTask(LOWT_TASK)), m_lowThresholdInterval);
                    LOG.debug("checkTimerTasksOnEventReceipt: {} scheduled", LOWT_TASK);
                } catch (IllegalStateException isE) {
                    LOG.error("checkTimerTasksOnEventReceipt: Illegal State adding new RTCTimerTask", isE);
                }
            }

            // set the high threshold timer task only if currently null
            if (m_highTtask == null) {
                try {
                    m_timer.schedule((m_highTtask = new RTCTimerTask(HIGHT_TASK)), m_highThresholdInterval);
                    LOG.debug("checkTimerTasksOnEventReceipt: {} scheduled", HIGHT_TASK);
                } catch (IllegalStateException isE) {
                    LOG.error("checkTimerTasksOnEventReceipt: Illegal State adding new RTCTimerTask", isE);
                }
            }
        }

        if (MAX_EVENTS_BEFORE_RESEND > 0 && m_counter >= MAX_EVENTS_BEFORE_RESEND) {
            LOG.debug("checkTimerTasksOnEventReceipt: max events before resend limit reached, resetting timers");

            // send the category information out and reset all timers
            if (m_lowTtask != null) {
                ret = m_lowTtask.cancel();
                LOG.debug("checkTimerTasksOnEventReceipt: {} cancelled: {}", LOWT_TASK, ret);

                m_lowTtask = null;
            }

            if (m_highTtask != null) {
                ret = m_highTtask.cancel();
                LOG.debug("checkTimerTasksOnEventReceipt: {} cancelled: {}", HIGHT_TASK, ret);
                m_highTtask = null;
            }

            LOG.debug("checkTimerTasksOnEventReceipt: max events before resend limit reached, sending data to listeners");

            m_dataSender.notifyToSend();

            LOG.debug("checkTimerTasksOnEventReceipt: max events before resend limit reached, datasender notified to send data");

            m_counter = -1;
        } else if (m_counter != 0) {
            // reset the low threshold timer since getting here means
            // we got an event before the low threshold timer
            // went off
            if (m_lowTtask != null) {
                ret = m_lowTtask.cancel();
                LOG.debug("checkTimerTasksOnEventReceipt: {} cancelled: {}", LOWT_TASK, ret);
                m_lowTtask = null;
            }

            try {
                m_timer.schedule((m_lowTtask = new RTCTimerTask(LOWT_TASK)), m_lowThresholdInterval);
                LOG.debug("checkTimerTasksOnEventReceipt: {} scheduled", LOWT_TASK);
            } catch (IllegalStateException isE) {
                LOG.error("checkTimerTasksOnEventReceipt: Illegal State adding new RTCTimerTask", isE);
            }
        }

    }

    /**
     * Reset the user timer.
     */
    public synchronized void resetUserTimer() {
        // Reset the user timer
        if (m_userTask != null)
            return;

        try {
            m_timer.schedule((m_userTask = new RTCTimerTask(USERTIMER)), 0, m_userRefreshInterval);
            LOG.debug("resetUserTimer: {} scheduled", USERTIMER);
        } catch (IllegalStateException isE) {
            LOG.error("dataReceived: Illegal State adding new RTCTimerTask", isE);
        }

    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        // load the rtc configuration
        RTCConfigFactory rFactory = null;
        try {
            RTCConfigFactory.reload();
            rFactory = RTCConfigFactory.getInstance();

        } catch (IOException ex) {
            log().error("Failed to load rtc configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (MarshalException ex) {
            log().error("Failed to load rtc configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().error("Failed to load rtc configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        //
        // Get the required attributes
        //

        // parse the rolling window info
        m_rollingWindow = rFactory.getRollingWindow();

        // get maxEventsBeforeResend
        MAX_EVENTS_BEFORE_RESEND = rFactory.getMaxEventsBeforeResend();

        // parse the low threshold interval
        m_lowThresholdInterval = rFactory.getLowThresholdInterval();

        // parse the high threshold interval
        m_highThresholdInterval = rFactory.getHighThresholdInterval();

        // parse the user threshold interval
        String ur = rFactory.getUserRefreshIntervalStr();
        if (ur != null) {
            try {
                m_userRefreshInterval = rFactory.getUserRefreshInterval();
            } catch (Throwable nfE) {
                log().warn("User refresh time has an incorrect format - using 1 minute instead");
                m_userRefreshInterval = 60 * 1000;
            }
        } else {
            log().warn("User refresh time not specified - using 1 minute instead");
            m_userRefreshInterval = 60 * 1000;
        }

        // high and low thresholds cannot be the same
        if (m_highThresholdInterval == m_lowThresholdInterval) {
            throw new RuntimeException("The values for the high and low threshold intervals CANNOT BE EQUAL");
        }

        // if high threshold is smaller than the low threshold, swap 'em
        if (m_highThresholdInterval < m_lowThresholdInterval) {
            log().warn("Swapping high and low threshold intervals..");
            long tmp = m_highThresholdInterval;
            m_highThresholdInterval = m_lowThresholdInterval;
            m_lowThresholdInterval = tmp;
        }

        log().info("Rolling Window: " + m_rollingWindow + "(milliseconds)");
        log().info("Low Threshold Refresh Interval: " + m_lowThresholdInterval + "(milliseconds)");
        log().info("High Threshold Refresh Interval: " + m_highThresholdInterval + "(milliseconds)");
        log().info("User Refresh Interval: " + m_userRefreshInterval + "(milliseconds)");

        // Intialize the data from the database
        try {
            m_dataMgr = new DataManager();
        } catch (Throwable ex) {
            throw new UndeclaredThrowableException(ex);
        }

        m_updaterPool = Executors.newFixedThreadPool(
            rFactory.getUpdaters(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), rFactory.getUpdaters(), false)
        );

        if (log().isDebugEnabled())
            log().debug("Created updater pool");

        m_eventReceiver = new BroadcastEventProcessor(m_updaterPool);
        if (log().isDebugEnabled())
            log().debug("Created event receiver");

        // create the data sender
        m_dataSender = new DataSender(getCategories(), rFactory.getSenders());
        log().debug("Created DataSender");

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

        // set the user refresh timer
        m_timer.schedule((m_userTask = new RTCTimerTask(USERTIMER)), 0, m_userRefreshInterval);
        if (log().isDebugEnabled())
            log().debug(USERTIMER + " scheduled");

        //
        // Subscribe to events
        //
        try {
            m_eventReceiver.start();
        } catch (Throwable t) {
            m_dataSender.stop();
            if (log().isDebugEnabled())
                log().debug("DataSender shutdown");

            m_updaterPool.shutdown();
            if (log().isDebugEnabled())
                log().debug("Updater pool shutdown");

            m_timer.cancel();
            if (log().isDebugEnabled())
                log().debug("Timer cancelled");

            throw new UndeclaredThrowableException(t);
        }


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

            //
            // Close connection to the event subsystem and free associated
            // resources.
            //
            m_eventReceiver.close();

            if (log().isDebugEnabled())
                log().debug("Shutting down the data sender");

            // shutdown the data sender
            m_dataSender.stop();

            if (log().isDebugEnabled())
                log().debug("DataSender shutdown");

            if (log().isDebugEnabled())
                log().debug("sending shutdown to updaters");

            m_updaterPool.shutdown();

            if (log().isDebugEnabled())
                log().debug("RTC Updaters shutdown");

            // cancel the timer and the timer tasks
            if (m_lowTtask != null)
                m_lowTtask.cancel();

            if (m_highTtask != null)
                m_highTtask.cancel();

            if (m_userTask != null)
                m_userTask.cancel();

            if (log().isDebugEnabled())
                log().debug("shutdown: Timer tasks Canceled");

            m_timer.cancel();

            if (log().isDebugEnabled())
                log().debug("shutdown: Timer Canceled");

        } catch (Throwable e) {
            log().error(e.getLocalizedMessage(), e);
        }
	}

    /**
     * Updates the number of events received. Increment the counter that keeps
     * track of number of events received since data was last sent out
     */
    public synchronized void incrementCounter() {
        m_counter++;
    }

    /**
     * Get the data sender.
     *
     * @return the data sender
     */
    public DataSender getDataSender() {
        return m_dataSender;
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    public static Map<String, RTCCategory> getCategories() {
        return m_dataMgr.getCategories();
    }

    /**
     * Gets the data manager.
     *
     * @return the data manager
     */
    public static DataManager getDataManager() {
        return m_dataMgr;
    }

    /**
     * Sets the data manager.
     *
     * @param dataMgr a {@link org.opennms.netmgt.rtc.DataManager} object.
     */
    public static void setDataManager(DataManager dataMgr) {
        m_dataMgr = dataMgr;
    }

    /**
     * Gets the rolling window.
     *
     * @return the configured rolling window
     */
    public static long getRollingWindow() {
        return m_rollingWindow;
    }

    /**
     * Gets the instance of the RTCmanager.
     *
     * @return the RTCManager singleton.
     */
    public static RTCManager getInstance() {
        return m_singleton;
    }

}
