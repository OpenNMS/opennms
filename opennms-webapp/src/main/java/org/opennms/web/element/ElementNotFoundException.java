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


public class ElementNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected String message;
    
    protected String elemType;
    
    protected String detailUri;
    
    protected String detailParam;
    
    protected String browseUri;

    public ElementNotFoundException(String msg, String elemType, String detailUri, String detailParam, String browseUri) {
        this.message = msg;
        this.elemType = elemType;
        this.detailUri = detailUri;
        this.detailParam = detailParam;
    }
    
    public ElementNotFoundException(String msg, String elemType, String browseUri) {
    	this.message = msg;
    	this.elemType = elemType;
    	this.browseUri = browseUri;
    	this.detailUri = null;
    	this.detailParam = null;
    }

    public ElementNotFoundException(String msg, String elemType) {
    	this.message = msg;
    	this.elemType = elemType;
    	this.browseUri = null;
    	this.detailUri = null;
    	this.detailParam = null;
    }
    
    public String getMessage() {
        return this.message;
    }

	public String getElemType() {
		return getElemType(false);
	}
	
	public String getElemType(boolean initialCap) {
		String result;
		if (initialCap) {
			result = elemType.substring(0,1).toUpperCase() + elemType.substring(1);
		} else {
			result = elemType;
		}
		return result;
	}

	public String getDetailUri() {
		return detailUri;
	}

	public String getDetailParam() {
		return detailParam;
	}
	
	public String getBrowseUri() {
		return browseUri;
	}
    
}
