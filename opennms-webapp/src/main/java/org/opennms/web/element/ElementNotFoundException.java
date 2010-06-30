//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// Modifications:
//
// 2009 Aug 27: Created
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
// Foundation, Inc.:
// 51 Franklin Street
// 5th Floor
// Boston, MA 02110-1301
// USA
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.element;


/**
 * <p>ElementNotFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ElementNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected String message;
    
    protected String elemType;
    
    protected String detailUri;
    
    protected String detailParam;
    
    protected String browseUri;

    /**
     * <p>Constructor for ElementNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     * @param detailUri a {@link java.lang.String} object.
     * @param detailParam a {@link java.lang.String} object.
     * @param browseUri a {@link java.lang.String} object.
     */
    public ElementNotFoundException(String msg, String elemType, String detailUri, String detailParam, String browseUri) {
        this.message = msg;
        this.elemType = elemType;
        this.detailUri = detailUri;
        this.detailParam = detailParam;
    }
    
    /**
     * <p>Constructor for ElementNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     * @param browseUri a {@link java.lang.String} object.
     */
    public ElementNotFoundException(String msg, String elemType, String browseUri) {
    	this.message = msg;
    	this.elemType = elemType;
    	this.browseUri = browseUri;
    	this.detailUri = null;
    	this.detailParam = null;
    }

    /**
     * <p>Constructor for ElementNotFoundException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param elemType a {@link java.lang.String} object.
     */
    public ElementNotFoundException(String msg, String elemType) {
    	this.message = msg;
    	this.elemType = elemType;
    	this.browseUri = null;
    	this.detailUri = null;
    	this.detailParam = null;
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
