/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class CollectionException extends Exception {
    
    private static final long serialVersionUID = 1L;

    private int m_errorCode = ServiceCollector.COLLECTION_FAILED;

    public CollectionException() {
        super();
    }

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionException(Throwable cause) {
        super(cause);
    }

    public int reportError() {
        logError();
    	return getErrorCode();
    }

    protected void logError() {
        if (getCause() == null) {
            log().error(getMessage());
    	} else {
            log().error(getMessage(), getCause());
    	}
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    void setErrorCode(int errorCode) {
        m_errorCode = errorCode;
    }

    int getErrorCode() {
        return m_errorCode;
    }

}
