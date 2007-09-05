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
// 2006 Aug 22: Use generics for Collections, clean up error messages. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 24: Replaced references to HashTable with HashMap.
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

package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.HttpUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.rtc.datablock.HttpPostInfo;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.rtc.utils.EuiLevelMapper;
import org.opennms.netmgt.rtc.utils.PipedMarshaller;
import org.opennms.netmgt.xml.rtc.EuiLevel;

/**
 * The DataSender is responsible to send data out to 'listeners'
 * 
 * When the RTCManager's timers go off, the DataSender is prompted to send data,
 * which it does by maintaining a 'SendRequest' runnable queue so as to not
 * block the RTCManager
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
final class DataSender implements Fiber {
    /**
     * The category map
     */
    private Map<String,RTCCategory> m_categories;

    /**
     * The listeners like the webui that send a URL to which the data is to be
     * sent
     */
    private Map<String, List<HttpPostInfo>> m_catUrlMap;

    /**
     * The data sender thread pool
     */
    private RunnableConsumerThreadPool m_dsrPool;

    /**
     * The queue to which data send requests are queued
     */
    private FifoQueue m_dataSenderQ;

    /**
     * The category to xml mapper
     */
    private EuiLevelMapper m_euiMapper;

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
        public void run() {
            sendData();
        }
    }

    /**
     * Set the current thread's priority to the passed value and return the old
     * priority
     */
    private int setCurrentThreadPriority(int priority) {
        Thread currentThread = Thread.currentThread();
        int oldPriority = currentThread.getPriority();
        try {
            currentThread.setPriority(priority);
        } catch (Exception e) {
            Category log = ThreadCategory.getInstance(this.getClass());
            if (log.isDebugEnabled()) {
                log.debug("Error setting thread priority: ", e);
            }
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
    public DataSender(Map<String, RTCCategory> categories, int numSenders) {
        m_categories = categories;

        // create the category url map
        m_catUrlMap = new HashMap<String, List<HttpPostInfo>>();

        // create and start the data sender pool
        m_dsrPool = new RunnableConsumerThreadPool("DataSenderPool", 0.6f, 1.0f, numSenders);

        // get queue reference
        m_dataSenderQ = m_dsrPool.getRunQueue();

        // create category converter
        m_euiMapper = new EuiLevelMapper();

        // get post error limit
        POST_ERROR_LIMIT = RTCConfigFactory.getInstance().getErrorsBeforeUrlUnsubscribe();
    }

    /**
     * Start the datasender threadpool
     */
    public synchronized void start() {
        m_status = STARTING;

        Category log = ThreadCategory.getInstance(this.getClass());

        log.info("Starting the datasender thread pool..");
        try {
            m_dsrPool.start();
            log.info("Datasender thread pool started..");
        } catch (Exception e) {
            log.error("Error starting data sender pool", e);
        }

        m_status = RUNNING;
    }

    /**
     * <P>
     * Shutdown the data sender thread pool
     */
    public synchronized void stop() {
        m_status = STOP_PENDING;

        Category log = ThreadCategory.getInstance(this.getClass());

        log.info("DataSender - shutting down the data sender pool");
        try {
            m_dsrPool.stop();
        } catch (Exception e) {
            log.error("Error shutting down data sender pool", e);
        }

        m_status = STOPPED;

        log.info("DataSender shutdown complete");
    }

    /**
     * Returns a name/id for this fiber
     */
    public String getName() {
        return "OpenNMS.RTC.DataSender";
    }

    /**
     * Returns the current status
     */
    public int getStatus() {
        return m_status;
    }

    /**
     * Subscribe - Add the recd. URL and related info to the category->Urls map
     * so the sendData() can send out to appropriate Urls for each category.
     * Also send the latest info for the category
     */
    public synchronized void subscribe(String url, String catlabel, String user, String passwd) {
        Category log = ThreadCategory.getInstance(this.getClass());

        // send category data to the newly subscribed URL
        // look up info for this category
        RTCCategory cat = (RTCCategory) m_categories.get(catlabel);
        if (cat == null) {
            // oops! category for which we have no info!
            log.warn("RTC: No information available for category: " + catlabel);
            return;
        }

        // create new HttpPostInfo
        HttpPostInfo postInfo = null;
        try {
            postInfo = new HttpPostInfo(url, catlabel, user, passwd);
        } catch (MalformedURLException mue) {
            log.warn("ERROR subscribing: Invalid URL \'" + url + "\' - Data WILL NOT be SENT to the specified url");
            return;
        }

        // Add the URL to the list for the specified category
        List<HttpPostInfo> urlList = m_catUrlMap.get(catlabel);
        if (urlList == null) {
            // create one
            urlList = new ArrayList<HttpPostInfo>();
            urlList.add(postInfo);
            m_catUrlMap.put(catlabel, urlList);
        } else {
            // add to list
            if (!urlList.contains(postInfo))
                urlList.add(postInfo);
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Already subscribed to URL: " + url + "\tcatlabel: " + catlabel + "\tuser:" + user + " - IGNORING LATEST subscribe event");
                }

                return;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Subscribed to URL: " + url + "\tcatlabel: " + catlabel + "\tuser:" + user);
        }

        // send data
        try {
            // Run at a higher than normal priority since we do have to send
            // the update on time
            int oldPriority = setCurrentThreadPriority(Thread.MAX_PRIORITY);

            EuiLevel euidata = m_euiMapper.convertToEuiLevelXML(cat);

            Reader inr = new PipedMarshaller(euidata).getReader();

            if (log.isDebugEnabled())
                log.debug("DataSender: posting data to: " + url);

            InputStream inp = HttpUtils.post(postInfo.getURL(), inr, user, passwd, 8 * HttpUtils.DEFAULT_POST_BUFFER_SIZE);

            byte[] tmp = new byte[1024];
            int bytesRead;
            while ((bytesRead = inp.read(tmp)) != -1) {
                if (log.isDebugEnabled()) {
                    if (bytesRead > 0)
                        log.debug("DataSender: post response: " + new String(tmp, 0, bytesRead));
                }
            }

            inp.close();
            inr.close();

            // return current thread to its previous priority
            oldPriority = setCurrentThreadPriority(oldPriority);

            if (log.isDebugEnabled())
                log.debug("DataSender: posted data for category: " + catlabel);
        } catch (IOException ioE) {
            log.warn("DataSender:  Unable to send category \'" + catlabel + "\' to URL \'" + url + "\': ", ioE);
            setCurrentThreadPriority(Thread.NORM_PRIORITY);
        } catch (java.lang.OutOfMemoryError oe) {
            log.warn("DataSender:  Unable to send category \'" + catlabel + "\' to URL \'" + url + "\': ", oe);
            setCurrentThreadPriority(Thread.NORM_PRIORITY);
        } catch (RuntimeException e) {
            log.warn("DataSender:  Unable to send category \'" + catlabel + "\' to URL \'" + url + "\': ", e);
            setCurrentThreadPriority(Thread.NORM_PRIORITY);
        } catch (Throwable t) {
            log.warn("DataSender:  Unable to send category \'" + catlabel + "\' to URL \'" + url + "\': ", t);
            setCurrentThreadPriority(Thread.NORM_PRIORITY);
        }
    }

    /**
     * Unsubscribe - remove the recd. URL and related info from the
     * category->Urls map so the sendData() will know when it sends data out
     */
    public synchronized void unsubscribe(String urlStr) {
        URL url = null;
        Category log = ThreadCategory.getInstance(this.getClass());

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException mue) {
            log.warn("ERROR unsubscribing: Invalid URL: " + url);
            return;
        }

        // go through the hashtable entries and remove entries with
        // the specified url
        String key;
        List value;
        Iterator vuIter = m_catUrlMap.keySet().iterator();
        while (vuIter.hasNext()) {
            key = (String) vuIter.next();
            value = (List) m_catUrlMap.get(key);

            if (value == null)
                continue;

            ListIterator listIter = value.listIterator();
            while (listIter.hasNext()) {
                HttpPostInfo postInfo = (HttpPostInfo) listIter.next();
                if (url.equals(postInfo.getURL())) {
                    // remove current entry from list
                    listIter.remove();
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Unsubscribed URL: " + url);
    }

    /**
     * Loop through the categories and send out data for all categories that
     * have changed
     */
    public synchronized void sendData() {
        Category log = ThreadCategory.getInstance(this.getClass());

        log.debug("In DataSender sendData()");

        // loop through and send info
        Iterator catsIter = m_categories.values().iterator();
        while (catsIter.hasNext()) {
            RTCCategory cat = (RTCCategory) catsIter.next();

            // get label
            String catlabel = cat.getLabel();

            if (log.isDebugEnabled())
                log.debug("DataSender:sendData(): Category \'" + catlabel);

            // get the post info for this category
            List urlList = (List) m_catUrlMap.get(catlabel);
            if (urlList == null || urlList.size() <= 0) {
                // a category that no one is listening for?
                if (log.isDebugEnabled())
                    log.debug("DataSender: category \'" + catlabel + "\' has no listeners");

                continue;
            }

            if (log.isDebugEnabled())
                log.debug("DataSender: category \'" + catlabel + "\' has listeners - converting to xml...");

            // Run at a higher than normal priority since we do have to send
            // the update on time
            int oldPriority = setCurrentThreadPriority(Thread.MAX_PRIORITY);

            EuiLevel euidata = null;
            try {
                euidata = m_euiMapper.convertToEuiLevelXML(cat);
            } catch (java.lang.OutOfMemoryError oe) {
                log.warn("DataSender: unable to convert data to xml for category: " + catlabel, oe);
                setCurrentThreadPriority(Thread.NORM_PRIORITY);
                continue;
            } catch (Throwable t) {
                log.warn("DataSender: unable to convert data to xml for category: " + catlabel, t);
                setCurrentThreadPriority(Thread.NORM_PRIORITY);
            }

            // do a HTTP POST if subscribed
            if (urlList != null && urlList.size() > 0) {
                Iterator urlIter = urlList.listIterator();
                while (urlIter.hasNext()) {
                    HttpPostInfo postInfo = (HttpPostInfo) urlIter.next();

                    Reader inr = null;
                    InputStream inp = null;
                    try {
                        inr = new PipedMarshaller(euidata).getReader();

                        if (log.isDebugEnabled())
                            log.debug("DataSender: posting data to: " + postInfo.getURLString());

                        inp = HttpUtils.post(postInfo.getURL(), inr, postInfo.getUser(), postInfo.getPassword(), 8 * HttpUtils.DEFAULT_POST_BUFFER_SIZE);

                        if (log.isDebugEnabled())
                            log.debug("DataSender: posted data for category: " + catlabel);
                        

                        byte[] tmp = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inp.read(tmp)) != -1) {
                            if (log.isDebugEnabled()) {
                                if (bytesRead > 0)
                                    log.debug("DataSender: post response: " + new String(tmp, 0, bytesRead));
                            }
                        }

                        postInfo.clearErrors();

                    } catch (IOException e) {
                        log.warn("DataSender: unable to send data for category: " + catlabel + " due to " + e.getClass().getName() + ": " + e.getMessage(), e);
                        postInfo.incrementErrors();
                        setCurrentThreadPriority(Thread.NORM_PRIORITY);
                    } catch (java.lang.OutOfMemoryError e) {
                        log.warn("DataSender: unable to send data for category: " + catlabel + " due to " + e.getClass().getName() + ": " + e.getMessage(), e);
                        setCurrentThreadPriority(Thread.NORM_PRIORITY);
                    } catch (RuntimeException e) {
                        log.warn("DataSender: unable to send data for category: " + catlabel + " due to " + e.getClass().getName() + ": " + e.getMessage(), e);
                        setCurrentThreadPriority(Thread.NORM_PRIORITY);
                    } catch (Throwable t) {
                        log.warn("DataSender: unable to send data for category: " + catlabel + " due to " + t.getClass().getName() + ": " + t.getMessage(), t);
                        setCurrentThreadPriority(Thread.NORM_PRIORITY);
                    } finally {
                        IOUtils.closeQuietly(inp);
                        IOUtils.closeQuietly(inr);
                    }

                    // check to see if URL had too many errors
                    if (POST_ERROR_LIMIT > 0 && postInfo.getErrors() >= POST_ERROR_LIMIT) {
                        // unsubscribe the URL
                        urlIter.remove();

                        log.warn("URL " + postInfo.getURLString() + " UNSUBSCRIBED due to reaching error limit " + postInfo.getErrors());
                    }
                }
            }

            // return current thread to its previous priority
            oldPriority = setCurrentThreadPriority(oldPriority);
        }
    }

    /**
     * Notify the DataSender to send data - create a new 'SendRequest' to send
     * the data and queue to the consumer
     */
    public synchronized void notifyToSend() {
        try {
            m_dataSenderQ.add(new SendRequest());
        } catch (InterruptedException iE) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.warn("Unable to queue datasender to the dsConsumer queue", iE);
        } catch (FifoQueueException qE) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.warn("Unable to queue datasender to the dsConsumer queue", qE);
        }
    }
}
