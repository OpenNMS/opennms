//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
// 2005 Apr 18: Created this file from EventIdNotFoundException.java
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>ElementIdNotFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ElementIdNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected String badId;

    protected String message;
    
    protected String elemType;
    
    protected String detailUri;
    
    protected String detailParam;
    
    protected String browseUri;

    /**
     * <p>Constructor for ElementIdNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     * @param detailUri a {@link java.lang.String} object.
     * @param detailParam a {@link java.lang.String} object.
     * @param browseUri a {@link java.lang.String} object.
     */
    public ElementIdNotFoundException(String msg, String id, String elemType, String detailUri, String detailParam, String browseUri) {
        this.message = msg;
        setBadId(id);
        this.elemType = elemType;
        this.detailUri = detailUri;
        this.detailParam = detailParam;
    }
    
    /**
     * <p>Constructor for ElementIdNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     * @param browseUri a {@link java.lang.String} object.
     */
    public ElementIdNotFoundException(String msg, String id, String elemType, String browseUri) {
    	this.message = msg;
    	setBadId(id);
    	this.elemType = elemType;
    	this.browseUri = browseUri;
    	this.detailUri = null;
    	this.detailParam = null;
    }

    /**
     * <p>Constructor for ElementIdNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     */
    public ElementIdNotFoundException(String msg, String id, String elemType) {
    	this.message = msg;
    	setBadId(id);
    	this.elemType = elemType;
    	this.browseUri = null;
    	this.detailUri = null;
    	this.detailParam = null;
    }
    
    private void setBadId(String idIn) {
    	try {
			this.badId = URLEncoder.encode(idIn, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			this.badId = "";
		}
    }

    
    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * <p>getBadID</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBadID() {
        return this.badId;
    }

	/**
	 * <p>Getter for the field <code>elemType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getElemType() {
		return getElemType(false);
	}
	
	/**
	 * <p>Getter for the field <code>elemType</code>.</p>
	 *
	 * @param initialCap a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String getElemType(boolean initialCap) {
		String result;
		if (initialCap) {
			result = elemType.substring(0,1).toUpperCase() + elemType.substring(1);
		} else {
			result = elemType;
		}
		return result;
	}

	/**
	 * <p>Getter for the field <code>detailUri</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDetailUri() {
		return detailUri;
	}

	/**
	 * <p>Getter for the field <code>detailParam</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDetailParam() {
		return detailParam;
	}
	
	/**
	 * <p>Getter for the field <code>browseUri</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBrowseUri() {
		return browseUri;
	}
    
}
