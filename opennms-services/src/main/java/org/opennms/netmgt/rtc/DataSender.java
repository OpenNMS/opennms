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

import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.IOUtils;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.HttpUtils;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.rtc.datablock.HttpPostInfo;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.rtc.utils.EuiLevelMapper;
import org.opennms.netmgt.rtc.utils.PipedMarshaller;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DataSender is responsible to send data out to 'listeners'
 * 
 * When the RTCManager's timers go off, the DataSender is prompted to send data,
 * which it does by maintaining a 'SendRequest' runnable queue so as to not
 * block the RTCManager
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
final class DataSender implements Fiber {

    private static final Logger LOG = LoggerFactory.getLogger(DataSender.class);

    /**
     * The category map
     */
    private final Map<String,RTCCategory> m_categories;

    /**
     * The listeners like the WebUI that send a URL to which the data is to be
     * sent
     */
    private final Map<String, Set<HttpPostInfo>> m_catUrlMap;

    /**
     * The data sender thread pool
     */
    private final ExecutorService m_dsrPool;

    /**
     * The category to XML mapper
     */
    private final EuiLevelMapper m_euiMapper;

    /**
     * The allowable number of times posts can have errors before an URL is
     * automatically unsubscribed
     */
    private final int POST_ERROR_LIMIT;

    /**
     * The current status of this fiber
     */
    private int m_status;

    /**
     * Inner class to send data to all the categories - this runnable prevents
     * the RTCManager from having to block until data is computed, converted to
     * XML and sent out
     */
    private class SendRequest implements Runnable {
        /**
         * Call the 'sendData()' to send the data out for all the categories
         */
        @Override
        public void run() {
            sendData();
        }
    }

    /**
     * Set the current thread's priority to the passed value and return the old
     * priority
     */
    private int setCurrentThreadPriority(final int priority) {
        final Thread currentThread = Thread.currentThread();
        final int oldPriority = currentThread.getPriority();
        try {
            currentThread.setPriority(priority);
        } catch (final Throwable t) {
            LOG.debug("Error setting thread priority: ", t);
        }

        return oldPriority;
    }

    /**
     * The constructor for this object
     *
     * @param categories
     *            The category map.
     * @param numSenders
     *            The number of senders.
     */
    public DataSender(final Map<String, RTCCategory> categories, final int numSenders) {
        m_categories = categories;

        // create the category URL map
        m_catUrlMap = new HashMap<String, Set<HttpPostInfo>>();

        // create and start the data sender pool
        m_dsrPool = Executors.newFixedThreadPool(numSenders,
                                                 new LogPreservingThreadFactory(getClass().getSimpleName(), numSenders)
                );

        // create category converter
        m_euiMapper = new EuiLevelMapper();

        // get post error limit
        POST_ERROR_LIMIT = RTCConfigFactory.getInstance().getErrorsBeforeUrlUnsubscribe();
    }

    /**
     * Start the data sender thread pool
     */
    @Override
    public synchronized void start() {
        m_status = RUNNING;
    }

    /**
     * <P>
     * Shutdown the data sender thread pool
     */
    @Override
    public synchronized void stop() {
        m_status = STOP_PENDING;

        LOG.info("DataSender - shutting down the data sender pool");
        try {
            m_dsrPool.shutdown();
        } catch (final Throwable t) {
            LOG.error("Error shutting down data sender pool", t);
        }

        m_status = STOPPED;

        LOG.info("DataSender shutdown complete");
    }

    /**
     * Returns a name/ID for this fiber
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "OpenNMS.RTC.DataSender";
    }

    /**
     * Returns the current status
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * Subscribe - Add the received URL and related info to the category->URLs map
     * so the sendData() can send out to appropriate URLs for each category.
     * Also send the latest info for the category
     *
     * @param url a {@link java.lang.String} object.
     * @param catlabel a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param passwd a {@link java.lang.String} object.
     */
    public synchronized void subscribe(final String url, final String catlabel, final String user, final String passwd) {
        // send category data to the newly subscribed URL
        // look up info for this category
        final RTCCategory cat = m_categories.get(catlabel);
        if (cat == null) {
            // oops! category for which we have no info!
            LOG.warn("RTC: No information available for category: {}", catlabel);
            return;
        }

        // create new HttpPostInfo
        final HttpPostInfo postInfo;
        try {
            postInfo = new HttpPostInfo(url, catlabel, user, passwd);
        } catch (final MalformedURLException mue) {
            LOG.warn("ERROR subscribing: Invalid URL '{}' - Data WILL NOT be SENT to the specified url", url);
            return;
        }

        // Add the URL to the list for the specified category
        Set<HttpPostInfo> urlList = m_catUrlMap.get(catlabel);
        if (urlList == null) {
            urlList = new HashSet<HttpPostInfo>();
            m_catUrlMap.put(catlabel, urlList);
        }

        if (!urlList.add(postInfo)) {
            LOG.debug("Already subscribed to URL: {}\tcatlabel: {}\tuser: {} - IGNORING LATEST subscribe event", url, catlabel, user);
        } else {
            LOG.debug("Subscribed to URL: {}\tcatlabel: {}\tuser:{}", url, catlabel, user);
        }

        // send data
        Reader inr = null;
        InputStream inp = null;
        try {
            LOG.debug("DataSender: posting data to: {}", url);

            // Run at a higher than normal priority since we do have to send
            // the update on time
            final int oldPriority = setCurrentThreadPriority(Thread.MAX_PRIORITY);

            final EuiLevel euidata = m_euiMapper.convertToEuiLevelXML(cat);
            inr = new PipedMarshaller(euidata).getReader();
            inp = HttpUtils.post(postInfo.getURL(), inr, user, passwd, 8 * HttpUtils.DEFAULT_POST_BUFFER_SIZE);

            final byte[] tmp = new byte[1024];
            int bytesRead;
            while ((bytesRead = inp.read(tmp)) != -1) {
                if (LOG.isDebugEnabled()) {
                    if (bytesRead > 0) {
                        LOG.debug("DataSender: post response: {}", new String(tmp, 0, bytesRead));
                    }
                }
            }

            // return current thread to its previous priority
            setCurrentThreadPriority(oldPriority);

            LOG.debug("DataSender: posted data for category: {}", catlabel);
        } catch (final Throwable t) {
            LOG.warn("DataSender:  Unable to send category '{}' to URL '{}'", catlabel, url, t);
            setCurrentThreadPriority(Thread.NORM_PRIORITY);
        } finally {
            IOUtils.closeQuietly(inp);
            IOUtils.closeQuietly(inr);
        }
    }

    /**
     * Unsubscribe - remove the received URL and related info from the
     * category->URLs map so the sendData() will know when it sends data out
     *
     * @param urlStr a {@link java.lang.String} object.
     */
    public synchronized void unsubscribe(final String urlStr) {
        final URL url;

        try {
            url = new URL(urlStr);
        } catch (final MalformedURLException mue) {
            LOG.warn("ERROR unsubscribing: Invalid URL: {}", urlStr);
            return;
        }

        // go through the hashtable entries and remove entries with
        // the specified URL
        for (final String key : m_catUrlMap.keySet()) {
            final Set<HttpPostInfo> value = m_catUrlMap.get(key);
            if (value == null) continue;

            final Iterator<HttpPostInfo> postSet = value.iterator();
            while (postSet.hasNext()) {
                final HttpPostInfo postInfo = postSet.next();
                if (url.toExternalForm().equals(postInfo.getURL().toExternalForm())) {
                    postSet.remove();
                }
            }
        }

        LOG.debug("Unsubscribed URL: {}", url);
    }

    /**
     * Loop through the categories and send out data for all categories that
     * have changed
     */
    public synchronized void sendData() {
        LOG.debug("In DataSender sendData()");

        // loop through and send info
        for (final RTCCategory cat : m_categories.values()) {
            // get label
            final String catlabel = cat.getLabel();
            LOG.debug("DataSender:sendData(): Category '{}'", catlabel);

            // get the post info for this category
            final Set<HttpPostInfo> urlList = m_catUrlMap.get(catlabel);
            if (urlList == null || urlList.size() <= 0) {
                // a category that no one is listening for?
                LOG.debug("DataSender: category '{}' has no listeners", catlabel);
                continue;
            }

            LOG.debug("DataSender: category '{}' has listeners - converting to xml...", catlabel);

            // Run at a higher than normal priority since we do have to send
            // the update on time
            final int oldPriority = setCurrentThreadPriority(Thread.MAX_PRIORITY);

            final EuiLevel euidata;
            try {
                euidata = m_euiMapper.convertToEuiLevelXML(cat);
            } catch (final Throwable t) {
                LOG.warn("DataSender: unable to convert data to xml for category: '{}'", catlabel, t);
                setCurrentThreadPriority(Thread.NORM_PRIORITY);
                continue;
            }

            // do a HTTP POST if subscribed
            if (urlList != null && urlList.size() > 0) {
                final Iterator<HttpPostInfo> urlIter = urlList.iterator();
                while (urlIter.hasNext()) {
                    final HttpPostInfo postInfo = urlIter.next();

                    Reader inr = null;
                    InputStream inp = null;
                    try {
                        inr = new PipedMarshaller(euidata).getReader();
                        LOG.debug("DataSender: posting data to: {}", postInfo.getURLString());
                        inp = HttpUtils.post(postInfo.getURL(), inr, postInfo.getUser(), postInfo.getPassword(), 8 * HttpUtils.DEFAULT_POST_BUFFER_SIZE);
                        LOG.debug("DataSender: posted data for category: {}", catlabel);


                        final byte[] tmp = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inp.read(tmp)) != -1) {
                            if (LOG.isDebugEnabled()) {
                                if (bytesRead > 0) {
                                    LOG.debug("DataSender: post response: {}", new String(tmp, 0, bytesRead));
                                }
                            }
                        }

                        postInfo.clearErrors();

                    } catch (final Throwable t) {
                        LOG.warn("DataSender: unable to send data for category: {} due to {}: {}", catlabel, t.getClass().getName(), t.getMessage(), t);
                        postInfo.incrementErrors();
                        setCurrentThreadPriority(Thread.NORM_PRIORITY);
                    } finally {
                        IOUtils.closeQuietly(inp);
                        IOUtils.closeQuietly(inr);
                    }

                    // check to see if URL had too many errors
                    if (POST_ERROR_LIMIT > 0 && postInfo.getErrors() >= POST_ERROR_LIMIT) {
                        // unsubscribe the URL
                        urlIter.remove();

                        LOG.warn("URL {} UNSUBSCRIBED due to reaching error limit {}", postInfo.getURLString(), postInfo.getErrors());
                    }
                }
            }

            // return current thread to its previous priority
            setCurrentThreadPriority(oldPriority);
        }
    }

    /**
     * Notify the DataSender to send data - create a new 'SendRequest' to send
     * the data and queue to the consumer
     */
    public synchronized void notifyToSend() {
        try {
            m_dsrPool.execute(new SendRequest());
        } catch (final RejectedExecutionException e) {
            LOG.warn("Unable to queue datasender to the dsConsumer queue", e);
        }
    }
}
