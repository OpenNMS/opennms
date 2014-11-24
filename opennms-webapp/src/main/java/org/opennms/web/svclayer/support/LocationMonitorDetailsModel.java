/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * <p>LocationMonitorDetailsModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorDetailsModel {
    private Errors m_errors;
    private MessageSourceResolvable m_title;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_mainDetails;
    private MessageSourceResolvable m_additionalDetailsTitle;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_additionalDetails;
    
    /**
     * <p>Constructor for LocationMonitorDetailsModel.</p>
     */
    public LocationMonitorDetailsModel() {
    }

    /**
     * <p>getAdditionalDetails</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<MessageSourceResolvable, MessageSourceResolvable> getAdditionalDetails() {
        return m_additionalDetails;
    }

    /**
     * <p>setAdditionalDetails</p>
     *
     * @param additionalDetails a {@link java.util.Map} object.
     */
    public void setAdditionalDetails(Map<MessageSourceResolvable, MessageSourceResolvable> additionalDetails) {
        m_additionalDetails = additionalDetails;
    }
    
    /**
     * <p>addAdditionalDetail</p>
     *
     * @param key a {@link org.springframework.context.MessageSourceResolvable} object.
     * @param value a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public void addAdditionalDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_additionalDetails == null) {
            m_additionalDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_additionalDetails.put(key, value);
    }

    /**
     * <p>getAdditionalDetailsTitle</p>
     *
     * @return a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public MessageSourceResolvable getAdditionalDetailsTitle() {
        return m_additionalDetailsTitle;
    }

    /**
     * <p>setAdditionalDetailsTitle</p>
     *
     * @param additionalDetailsTitle a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public void setAdditionalDetailsTitle(MessageSourceResolvable additionalDetailsTitle) {
        m_additionalDetailsTitle = additionalDetailsTitle;
    }

    /**
     * <p>getMainDetails</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<MessageSourceResolvable, MessageSourceResolvable> getMainDetails() {
        return m_mainDetails;
    }

    /**
     * <p>setMainDetails</p>
     *
     * @param mainDetails a {@link java.util.Map} object.
     */
    public void setMainDetails(Map<MessageSourceResolvable, MessageSourceResolvable> mainDetails) {
        m_mainDetails = mainDetails;
    }
    
    /**
     * <p>addMainDetail</p>
     *
     * @param key a {@link org.springframework.context.MessageSourceResolvable} object.
     * @param value a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public void addMainDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_mainDetails == null) {
            m_mainDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_mainDetails.put(key, value);
    }

    /**
     * <p>getTitle</p>
     *
     * @return a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public MessageSourceResolvable getTitle() {
        return m_title;
    }

    /**
     * <p>setTitle</p>
     *
     * @param title a {@link org.springframework.context.MessageSourceResolvable} object.
     */
    public void setTitle(MessageSourceResolvable title) {
        m_title = title;
    }

    /**
     * <p>getErrors</p>
     *
     * @return a {@link org.springframework.validation.Errors} object.
     */
    public Errors getErrors() {
        return m_errors;
    }

    /**
     * <p>setErrors</p>
     *
     * @param errors a {@link org.springframework.validation.Errors} object.
     */
    public void setErrors(Errors errors) {
        m_errors = errors;
    }
}
