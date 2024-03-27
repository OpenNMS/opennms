/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.element;


/**
 * <p>ElementNotFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ElementNotFoundException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -1707780508592264918L;

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
    @Override
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
