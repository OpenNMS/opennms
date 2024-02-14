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

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opennms.netmgt.events.api.model.IParm;

/**
 * A varbind from the trap
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="parm")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Parm implements Serializable {
	private static final long serialVersionUID = 2841420030575276257L;

	//--------------------------/
	//- Class/Member Variables -/
    //--------------------------/

    /**
     * parm name
     */
	@XmlElement(name="parmName", required=true)
	@NotNull
    private java.lang.String _parmName;

    /**
     * parm value
     */
	@XmlElement(name="value", required=true)
	@NotNull
	@Valid
	private org.opennms.netmgt.xml.event.Value _value;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parm() {
        super();
    }

    public static Parm copyFrom(IParm source) {
        if (source == null) {
            return null;
        }

        Parm parm = new Parm();
        parm.setParmName(source.getParmName());
        parm.setValue(Value.copyFrom(source.getValue()));
        return parm;
    }

      //-----------/
     //- Methods -/
    //-----------/

    public Parm(final String name, final String value) {
    	this();
    	setParmName(name);
    	this._value = new Value(value);
	}


	/**
     * Returns the value of field 'parmName'. The field 'parmName'
     * has the following description: parm name
     * 
     * @return the value of field 'ParmName'.
     */
    public java.lang.String getParmName(
    ) {
        return this._parmName;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has
     * the following description: parm value
     * 
     * @return the value of field 'Value'.
     */
    public org.opennms.netmgt.xml.event.Value getValue(
    ) {
        return this._value;
    }

    /**
     * Sets the value of field 'parmName'. The field 'parmName' has
     * the following description: parm name
     * 
     * @param parmName the value of field 'parmName'.
     */
    public void setParmName(
            final java.lang.String parmName) {
        this._parmName = parmName;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the
     * following description: parm value
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(
            final org.opennms.netmgt.xml.event.Value value) {
        this._value = value;
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
    		.append(_parmName, _value == null ? null : _value.getContent())
    		.toString();
    }


	public boolean isValid() {
		return getParmName() != null && getValue() != null;
	}
}
