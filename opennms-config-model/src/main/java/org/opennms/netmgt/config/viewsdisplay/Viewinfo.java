/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.viewsdisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the viewsdisplay.xml configuration file.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "viewinfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Viewinfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DEFAULT_VIEW = "WebConsoleView";

    @XmlAttribute(name = "disconnect-timeout")
    private Integer disconnectTimeout;

    @XmlAttribute(name = "default-view")
    private String defaultView;

    @XmlElement(name = "view")
    private List<View> viewList = new ArrayList<>();

    /**
     * 
     * 
     * @param vView
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addView(final View vView) throws IndexOutOfBoundsException {
        this.viewList.add(vView);
    }

    /**
     * 
     * 
     * @param index
     * @param vView
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addView(final int index, final View vView) throws IndexOutOfBoundsException {
        this.viewList.add(index, vView);
    }

    /**
     */
    public void deleteDisconnectTimeout() {
        this.disconnectTimeout= null;
    }

    /**
     * Method enumerateView.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<View> enumerateView() {
        return Collections.enumeration(this.viewList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Viewinfo) {
            Viewinfo temp = (Viewinfo)obj;
            boolean equals = Objects.equals(temp.disconnectTimeout, disconnectTimeout)
                && Objects.equals(temp.defaultView, defaultView)
                && Objects.equals(temp.viewList, viewList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultView'.
     * 
     * @return the value of field 'DefaultView'.
     */
    public String getDefaultView() {
        return this.defaultView != null ? this.defaultView : DEFAULT_DEFAULT_VIEW;
    }

    /**
     * Returns the value of field 'disconnectTimeout'.
     * 
     * @return the value of field 'DisconnectTimeout'.
     */
    public Integer getDisconnectTimeout() {
        return this.disconnectTimeout != null ? this.disconnectTimeout : Integer.valueOf("130000");
    }

    /**
     * Method getView.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the View at the
     * given index
     */
    public View getView(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.viewList.size()) {
            throw new IndexOutOfBoundsException("getView: Index value '" + index + "' not in range [0.." + (this.viewList.size() - 1) + "]");
        }
        
        return (View) viewList.get(index);
    }

    /**
     * Method getView.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public View[] getView() {
        View[] array = new View[0];
        return (View[]) this.viewList.toArray(array);
    }

    /**
     * Method getViewCollection.Returns a reference to 'viewList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<View> getViewCollection() {
        return this.viewList;
    }

    /**
     * Method getViewCount.
     * 
     * @return the size of this collection
     */
    public int getViewCount() {
        return this.viewList.size();
    }

    /**
     * Method hasDisconnectTimeout.
     * 
     * @return true if at least one DisconnectTimeout has been added
     */
    public boolean hasDisconnectTimeout() {
        return this.disconnectTimeout != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            disconnectTimeout, 
            defaultView, 
            viewList);
        return hash;
    }

    /**
     * Method iterateView.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<View> iterateView() {
        return this.viewList.iterator();
    }

    /**
     */
    public void removeAllView() {
        this.viewList.clear();
    }

    /**
     * Method removeView.
     * 
     * @param vView
     * @return true if the object was removed from the collection.
     */
    public boolean removeView(final View vView) {
        boolean removed = viewList.remove(vView);
        return removed;
    }

    /**
     * Method removeViewAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public View removeViewAt(final int index) {
        Object obj = this.viewList.remove(index);
        return (View) obj;
    }

    /**
     * Sets the value of field 'defaultView'.
     * 
     * @param defaultView the value of field 'defaultView'.
     */
    public void setDefaultView(final String defaultView) {
        this.defaultView = defaultView;
    }

    /**
     * Sets the value of field 'disconnectTimeout'.
     * 
     * @param disconnectTimeout the value of field 'disconnectTimeout'.
     */
    public void setDisconnectTimeout(final Integer disconnectTimeout) {
        this.disconnectTimeout = disconnectTimeout;
    }

    /**
     * 
     * 
     * @param index
     * @param vView
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setView(final int index, final View vView) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.viewList.size()) {
            throw new IndexOutOfBoundsException("setView: Index value '" + index + "' not in range [0.." + (this.viewList.size() - 1) + "]");
        }
        
        this.viewList.set(index, vView);
    }

    /**
     * 
     * 
     * @param vViewArray
     */
    public void setView(final View[] vViewArray) {
        //-- copy array
        viewList.clear();
        
        for (int i = 0; i < vViewArray.length; i++) {
                this.viewList.add(vViewArray[i]);
        }
    }

    /**
     * Sets the value of 'viewList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vViewList the Vector to copy.
     */
    public void setView(final List<View> vViewList) {
        // copy vector
        this.viewList.clear();
        
        this.viewList.addAll(vViewList);
    }

    /**
     * Sets the value of 'viewList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param viewList the Vector to set.
     */
    public void setViewCollection(final List<View> viewList) {
        this.viewList = viewList;
    }

}
