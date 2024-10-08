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
package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.opennms.netmgt.events.api.model.ICorrelation;

import io.swagger.v3.oas.annotations.Hidden;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The event correlation information
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="correlation")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Correlation implements Serializable {
	private static final long serialVersionUID = 7883869597194555535L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * The state determines if event is
     *  correlated
     */
	@XmlAttribute(name="state")
	@Pattern(regexp="(on|off)")
    private java.lang.String _state = "off";

    /**
     * Field _path.
     */
	@XmlAttribute(name="path")
	@Pattern(regexp="(suppressDuplicates|cancellingEvent|suppressAndCancel|pathOutage)")
    private java.lang.String _path = "suppressDuplicates".intern();

    /**
     * A cancelling UEI for this event
     */
	@XmlElement(name="cuei")
    private java.util.List<java.lang.String> _cueiList;

    /**
     * The minimum count for this event
     */
	@XmlElement(name="cmin")
    private java.lang.String _cmin;

    /**
     * The maximum count for this event
     */
	@XmlElement(name="cmax")
    private java.lang.String _cmax;

    /**
     * The correlation time for this event
     */
	@XmlElement(name="ctime")
    private java.lang.String _ctime;


      //----------------/
     //- Constructors -/
    //----------------/

    public Correlation() {
        super();
        setState("off");
        setPath("suppressDuplicates".intern());
        this._cueiList = new java.util.ArrayList<>();
    }

    public static Correlation copyFrom(ICorrelation source) {
        if (source == null) {
            return null;
        }

        Correlation correlation = new Correlation();
        correlation.setState(source.getState());
        correlation.setPath(source.getPath());
        correlation.getCueiCollection().addAll(source.getCueiCollection());
        correlation.setCmin(source.getCmin());
        correlation.setCmax(source.getCmax());
        correlation.setCtime(source.getCtime());
        return correlation;
    }

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCuei(
            final java.lang.String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        this._cueiList.add(vCuei);
    }

    /**
     * 
     * 
     * @param index
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCuei(
            final int index,
            final java.lang.String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        this._cueiList.add(index, vCuei);
    }

    /**
     * Method enumerateCuei.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateCuei(
    ) {
        return java.util.Collections.enumeration(this._cueiList);
    }

    /**
     * Returns the value of field 'cmax'. The field 'cmax' has the
     * following description: The maximum count for this event
     * 
     * @return the value of field 'Cmax'.
     */
    public java.lang.String getCmax(
    ) {
        return this._cmax;
    }

    /**
     * Returns the value of field 'cmin'. The field 'cmin' has the
     * following description: The minimum count for this event
     * 
     * @return the value of field 'Cmin'.
     */
    public java.lang.String getCmin(
    ) {
        return this._cmin;
    }

    /**
     * Returns the value of field 'ctime'. The field 'ctime' has
     * the following description: The correlation time for this
     * event
     * 
     * @return the value of field 'Ctime'.
     */
    public java.lang.String getCtime(
    ) {
        return this._ctime;
    }

    /**
     * Method getCuei.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getCuei(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._cueiList.size()) {
            throw new IndexOutOfBoundsException("getCuei: Index value '" + index + "' not in range [0.." + (this._cueiList.size() - 1) + "]");
        }
        
        return (java.lang.String) _cueiList.get(index);
    }

    /**
     * Method getCuei.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getCuei(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._cueiList.toArray(array);
    }

    /**
     * Method getCueiCollection.Returns a reference to '_cueiList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getCueiCollection(
    ) {
        return this._cueiList;
    }

    /**
     * Method getCueiCount.
     * 
     * @return the size of this collection
     */
    public int getCueiCount(
    ) {
        return this._cueiList.size();
    }

    /**
     * Returns the value of field 'path'.
     * 
     * @return the value of field 'Path'.
     */
    public java.lang.String getPath(
    ) {
        return this._path;
    }

    /**
     * Returns the value of field 'state'. The field 'state' has
     * the following description: The state determines if event is
     *  correlated
     * 
     * @return the value of field 'State'.
     */
    public java.lang.String getState(
    ) {
        return this._state;
    }

    /**
     * Method iterateCuei.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateCuei(
    ) {
        return this._cueiList.iterator();
    }

    /**
     */
    public void removeAllCuei(
    ) {
        this._cueiList.clear();
    }

    /**
     * Method removeCuei.
     * 
     * @param vCuei
     * @return true if the object was removed from the collection.
     */
    public boolean removeCuei(
            final java.lang.String vCuei) {
        return _cueiList.remove(vCuei);
    }

    /**
     * Method removeCueiAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeCueiAt(
            final int index) {
        return this._cueiList.remove(index);
    }

    /**
     * Sets the value of field 'cmax'. The field 'cmax' has the
     * following description: The maximum count for this event
     * 
     * @param cmax the value of field 'cmax'.
     */
    public void setCmax(
            final java.lang.String cmax) {
        this._cmax = cmax;
    }

    /**
     * Sets the value of field 'cmin'. The field 'cmin' has the
     * following description: The minimum count for this event
     * 
     * @param cmin the value of field 'cmin'.
     */
    public void setCmin(
            final java.lang.String cmin) {
        this._cmin = cmin;
    }

    /**
     * Sets the value of field 'ctime'. The field 'ctime' has the
     * following description: The correlation time for this event
     * 
     * @param ctime the value of field 'ctime'.
     */
    public void setCtime(
            final java.lang.String ctime) {
        this._ctime = ctime;
    }

    /**
     * 
     * @deprecated
     * @param index
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    @Hidden
    public void setCuei(
            final int index,
            final java.lang.String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._cueiList.size()) {
            throw new IndexOutOfBoundsException("setCuei: Index value '" + index + "' not in range [0.." + (this._cueiList.size() - 1) + "]");
        }
        
        this._cueiList.set(index, vCuei);
    }

    /**
     * 
     * @deprecated
     * @param vCueiArray
     */
    @Hidden
    public void setCuei(
            final java.lang.String[] vCueiArray) {
        //-- copy array
        _cueiList.clear();
        
        for (int i = 0; i < vCueiArray.length; i++) {
                this._cueiList.add(vCueiArray[i]);
        }
    }

    /**
     * Sets the value of '_cueiList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vCueiList the Vector to copy.
     */
    public void setCuei(
            final java.util.List<java.lang.String> vCueiList) {
        // copy vector
        this._cueiList.clear();
        
        this._cueiList.addAll(vCueiList);
    }

    /**
     * Sets the value of '_cueiList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param cueiList the Vector to set.
     */
    @Hidden
    public void setCueiCollection(
            final java.util.List<java.lang.String> cueiList) {
        this._cueiList = cueiList;
    }

    /**
     * Sets the value of field 'path'.
     * 
     * @param path the value of field 'path'.
     */
    public void setPath(
            final java.lang.String path) {
        this._path = path.intern();
    }

    /**
     * Sets the value of field 'state'. The field 'state' has the
     * following description: The state determines if event is
     *  correlated
     * 
     * @param state the value of field 'state'.
     */
    public void setState(
            final java.lang.String state) {
        this._state = state;
    }

        @Override
    public String toString() {
    	return new OnmsStringBuilder(this).toString();
    }
}
