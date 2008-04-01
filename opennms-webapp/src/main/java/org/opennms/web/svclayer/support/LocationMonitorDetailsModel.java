/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 11, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class LocationMonitorDetailsModel {
    private Errors m_errors;
    private MessageSourceResolvable m_title;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_mainDetails;
    private MessageSourceResolvable m_additionalDetailsTitle;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_additionalDetails;
    
    public LocationMonitorDetailsModel() {
    }

    public Map<MessageSourceResolvable, MessageSourceResolvable> getAdditionalDetails() {
        return m_additionalDetails;
    }

    public void setAdditionalDetails(Map<MessageSourceResolvable, MessageSourceResolvable> additionalDetails) {
        m_additionalDetails = additionalDetails;
    }
    
    public void addAdditionalDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_additionalDetails == null) {
            m_additionalDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_additionalDetails.put(key, value);
    }

    public MessageSourceResolvable getAdditionalDetailsTitle() {
        return m_additionalDetailsTitle;
    }

    public void setAdditionalDetailsTitle(MessageSourceResolvable additionalDetailsTitle) {
        m_additionalDetailsTitle = additionalDetailsTitle;
    }

    public Map<MessageSourceResolvable, MessageSourceResolvable> getMainDetails() {
        return m_mainDetails;
    }

    public void setMainDetails(Map<MessageSourceResolvable, MessageSourceResolvable> mainDetails) {
        m_mainDetails = mainDetails;
    }
    
    public void addMainDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_mainDetails == null) {
            m_mainDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_mainDetails.put(key, value);
    }

    public MessageSourceResolvable getTitle() {
        return m_title;
    }

    public void setTitle(MessageSourceResolvable title) {
        m_title = title;
    }

    public Errors getErrors() {
        return m_errors;
    }

    public void setErrors(Errors errors) {
        m_errors = errors;
    }
}
