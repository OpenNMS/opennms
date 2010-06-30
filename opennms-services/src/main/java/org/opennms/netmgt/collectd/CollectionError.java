//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>CollectionError class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectionError extends Exception {
    
    private static final long serialVersionUID = 1L;

    private int m_errorCode = ServiceCollector.COLLECTION_FAILED;

    /**
     * <p>Constructor for CollectionError.</p>
     */
    public CollectionError() {
        super();
    }

    /**
     * <p>Constructor for CollectionError.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public CollectionError(String message) {
        super(message);
    }

    /**
     * <p>Constructor for CollectionError.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public CollectionError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for CollectionError.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public CollectionError(Throwable cause) {
        super(cause);
    }

    /**
     * <p>reportError</p>
     *
     * @return a int.
     */
    public int reportError() {
        logError();
    	return getErrorCode();
    }

    /**
     * <p>logError</p>
     */
    protected void logError() {
        if (getCause() == null) {
            log().error(getMessage());
    	} else {
            log().error(getMessage(), getCause());
    	}
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
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
