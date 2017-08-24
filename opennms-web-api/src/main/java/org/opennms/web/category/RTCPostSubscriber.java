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

    /** Constant <code>LOG</code> */
    private static final Logger LOG = LoggerFactory.getLogger(RTCPostSubscriber.class);

    protected final EventProxy m_proxy;

    /**
     * <p>Constructor for RTCPostSubscriber.</p>
     *
     * @throws java.io.IOException if any.
     */
    public RTCPostSubscriber() throws IOException {
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
    protected static void sendSubscribeEvent(final EventProxy proxy, final String url, final String username, final String password, final String categoryName) throws IllegalArgumentException, EventProxyException {
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
        } catch (final Throwable e) {
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
    protected static void sendUnsubscribeEvent(final EventProxy proxy, final String url) throws IllegalArgumentException, EventProxyException {
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
        } catch (final Throwable e) {
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
    protected void subscribe(final String categoryName) throws IllegalArgumentException, EventProxyException {
        String url;
        String username = "rtc";
        String password = "rtc";

        if (categoryName == null) {
        	throw new IllegalArgumentException("categoryName cannot be null");
        }

        final String usernameProperty = Vault.getProperty("opennms.rtc-client.http-post.username");
        if (usernameProperty != null) {
            username = usernameProperty;
        }

        final String passwordProperty = Vault.getProperty("opennms.rtc-client.http-post.password");
        if (passwordProperty != null) {
            password = passwordProperty;
        }

        String baseUrl = Vault.getProperty("opennms.rtc-client.http-post.base-url");
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080/opennms/rtc/post";
        }

        if (baseUrl.endsWith("/")) {
            url = baseUrl + Util.encode(categoryName);
        } else {
            url = baseUrl + "/" + Util.encode(categoryName);
        }

        final String logUrl = url;
        final String logUsername = username; 
        Logging.withPrefix(LOGGING_PREFIX, new Runnable() {
            @Override 
            public void run() {
                LOG.debug("RTCPostSubscriber initialized: url={}, user={}", logUrl, logUsername);
            }
        });
        sendSubscribeEvent(m_proxy, url, username, password, categoryName);
    }

    /**
     * <p>unsubscribe</p>
     * 
     * TODO: Call this during a destroy() or close() method
     *
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public void unsubscribe(String url) throws IllegalArgumentException, EventProxyException {
        sendUnsubscribeEvent(m_proxy, url);
    }

    /**
     * Fetch all of the categories that are part of the given {@link View}
     * and send subscription events for each category.
     *
     * @param viewName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public static void subscribeAll(final String viewName) {
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        try {
            Logging.withPrefix(LOGGING_PREFIX, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // get the list of categories from the viewsdisplay.xml
                    ViewsDisplayFactory.init();
                    ViewsDisplayFactory factory = ViewsDisplayFactory.getInstance();

                    final View view = factory.getView(viewName);

                    if (view != null) {
                        // create a JMS connection to subscribe
                        final RTCPostSubscriber subscriber = new RTCPostSubscriber();

                        for (final Section section : view.getSections()) {
                            for (final String categoryName : section.getCategories()) {
                                subscriber.subscribe(categoryName);
                                LOG.info("Sent subscription event to RTC for category: {}",  categoryName);
                            }
                        }
                    }

                    return null;
                }
            });
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

}
