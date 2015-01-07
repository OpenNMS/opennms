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

package org.opennms.web.category;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.logging.Logging;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RTCPostSubscriber class.</p>
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RTCPostSubscriber {
    private static final String LOGGING_PREFIX = "rtc";

    private static final Logger LOG = LoggerFactory.getLogger(RTCPostSubscriber.class);

    protected EventProxy m_proxy;

    protected String m_url;
    protected String m_username = "rtc";
    protected String m_password = "rtc";

    /** Constant <code>log</code> */

    /**
     * <p>Constructor for RTCPostSubscriber.</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public RTCPostSubscriber() throws IOException, MarshalException, ValidationException {
        m_proxy = Util.createEventProxy();
    }

    /**
     * <p>sendSubscribeEvent</p>
     *
     * @param proxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     * @param url a {@link java.lang.String} object.
     * @param username a {@link java.lang.String} object.
     * @param password a {@link java.lang.String} object.
     * @param categoryName a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public static void sendSubscribeEvent(final EventProxy proxy, final String url, final String username, final String password, final String categoryName) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || url == null || username == null || password == null || categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        try {
            Logging.withPrefix(LOGGING_PREFIX, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    final EventBuilder bldr = new EventBuilder(EventConstants.RTC_SUBSCRIBE_EVENT_UEI, "RTCPostSubscriber");
                    bldr.setHost("host");
                    bldr.addParam(EventConstants.PARM_URL, url);
                    bldr.addParam(EventConstants.PARM_USER, username);
                    bldr.addParam(EventConstants.PARM_PASSWD, password);
                    bldr.addParam(EventConstants.PARM_CAT_LABEL, categoryName);

                    proxy.send(bldr.getEvent());

                    LOG.info("Subscription requested for {} to {}", username, url);
                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            if (e instanceof EventProxyException)      throw (EventProxyException)e;
        }
    }

    /**
     * <p>sendUnsubscribeEvent</p>
     *
     * @param proxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     * @param url a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public static void sendUnsubscribeEvent(final EventProxy proxy, final String url) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || url == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        try {
            Logging.withPrefix(LOGGING_PREFIX, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    final EventBuilder bldr = new EventBuilder(EventConstants.RTC_UNSUBSCRIBE_EVENT_UEI, "RTCPostSubscriber");
                    bldr.setHost("host");
                    bldr.addParam(EventConstants.PARM_URL, url);
                    proxy.send(bldr.getEvent());

                    LOG.info("Unsubscription sent for {}", url);
                    return null;
                }
            });
        } catch (final Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
            if (e instanceof EventProxyException)      throw (EventProxyException)e;
        }
    }

    /**
     * <p>subscribe</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public String subscribe(final String categoryName) throws IllegalArgumentException, EventProxyException {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        initFromRtcPropertyFile(categoryName);
        sendSubscribeEvent(m_proxy, m_url, m_username, m_password, categoryName);
        return (m_url);
    }

    /**
     * <p>unsubscribe</p>
     *
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public void unsubscribe() throws IllegalArgumentException, EventProxyException {
        sendUnsubscribeEvent(m_proxy, m_url);
    }

    /**
     * <p>close</p>
     */
    public void close() {
        m_proxy = null;
    }

    /**
     * <p>initFromRtcPropertyFile</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    protected void initFromRtcPropertyFile(final String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("categoryName can not be null");
        }

        final String username = Vault.getProperty("opennms.rtc-client.http-post.username");
        if (username != null) {
            m_username = username;
        }

        Logging.withPrefix(LOGGING_PREFIX, new Runnable() {
            @Override public void run() {
                final String password = Vault.getProperty("opennms.rtc-client.http-post.password");
                if (password != null) {
                    m_password = password;
                }

                String baseUrl = Vault.getProperty("opennms.rtc-client.http-post.base-url");
                if (baseUrl == null) {
                    baseUrl = "http://localhost:8080/opennms/rtc/post";
                }

                if (baseUrl.endsWith("/")) {
                    m_url = baseUrl + Util.encode(categoryName);
                } else {
                    m_url = baseUrl + "/" + Util.encode(categoryName);
                }

                LOG.debug("RTCPostSubscriber initialized: url={}, user={}", m_url, m_username);
            }
        });
    }

    /**
     * <p>subscribeAll</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public static void subscribeAll(final String viewName) throws IOException, MarshalException, ValidationException, EventProxyException {
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        try {
            Logging.withPrefix(LOGGING_PREFIX, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    // get the list of categories from the viewsdisplay.xml
                    ViewsDisplayFactory.init();
                    ViewsDisplayFactory factory = ViewsDisplayFactory.getInstance();

                    final View view = factory.getView(viewName);

                    if (view != null) {
                        // create a JMS connection to subscribe
                        final RTCPostSubscriber subscriber = new RTCPostSubscriber();

                        try {
                            for (final Section section : view.getSectionCollection()) {
                                for (final String categoryName : section.getCategoryCollection()) {
                                    subscriber.subscribe(categoryName);
                                    LOG.info("Sent subscription event to RTC for category: {}",  categoryName);
                                }
                            }
                        } finally {
                            // Close the subscription JMS connection.
                            subscriber.close();
                        }
                    }

                    return null;
                }
            });
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
