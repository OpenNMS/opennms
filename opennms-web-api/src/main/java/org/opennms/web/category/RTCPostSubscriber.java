/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RTCPostSubscriber class.</p>
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public class RTCPostSubscriber extends Object {
	
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
     * @param proxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     * @param url a {@link java.lang.String} object.
     * @param username a {@link java.lang.String} object.
     * @param password a {@link java.lang.String} object.
     * @param categoryName a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public static void sendSubscribeEvent(EventProxy proxy, String url, String username, String password, String categoryName) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || url == null || username == null || password == null || categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        EventBuilder bldr = new EventBuilder(EventConstants.RTC_SUBSCRIBE_EVENT_UEI, "RTCPostSubscriber");
        bldr.setHost("host");
        
        bldr.addParam(EventConstants.PARM_URL, url);
        bldr.addParam(EventConstants.PARM_USER, username);
        bldr.addParam(EventConstants.PARM_PASSWD, password);
        bldr.addParam(EventConstants.PARM_CAT_LABEL, categoryName);

        proxy.send(bldr.getEvent());

        LOG.info("Subscription requested for {} to {}", username, url);
    }

    /**
     * <p>sendUnsubscribeEvent</p>
     *
     * @param proxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     * @param url a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public static void sendUnsubscribeEvent(EventProxy proxy, String url) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || url == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        EventBuilder bldr = new EventBuilder(EventConstants.RTC_UNSUBSCRIBE_EVENT_UEI, "RTCPostSubscriber");
        bldr.setHost("host");
        
        bldr.addParam(EventConstants.PARM_URL, url);

        proxy.send(bldr.getEvent());

        LOG.info("Unsubscription sent for {}", url);
    }

    /**
     * <p>subscribe</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.IllegalArgumentException if any.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public String subscribe(String categoryName) throws IllegalArgumentException, EventProxyException {
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
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
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
    protected void initFromRtcPropertyFile(String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("categoryName can not be null");
        }

        String username = Vault.getProperty("opennms.rtc-client.http-post.username");
        if (username != null) {
            m_username = username;
        }
        
        String password = Vault.getProperty("opennms.rtc-client.http-post.password");
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

    /**
     * <p>subscribeAll</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public static void subscribeAll(String viewName) throws IOException, MarshalException, ValidationException, EventProxyException {
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        // get the list of categories from the viewsdisplay.xml
        ViewsDisplayFactory.init();
        ViewsDisplayFactory factory = ViewsDisplayFactory.getInstance();

        View view = factory.getView(viewName);

        if (view != null) {
            Section[] sections = view.getSection();

            // create a JMS connection to subscribe
            RTCPostSubscriber subscriber = new RTCPostSubscriber();

            for (int i = 0; i < sections.length; i++) {
                Section section = sections[i];
                String[] categories = section.getCategory();

                for (int j = 0; j < categories.length; j++) {
                    subscriber.subscribe(categories[j]);
                    LOG.info("Sent subscription event to RTC for category: {}",  categories[j]);
                }
            }

            // Close the subscription JMS connection.
            subscriber.close();
        }
    }

}
