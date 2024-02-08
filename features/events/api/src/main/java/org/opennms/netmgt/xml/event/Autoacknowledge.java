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

import org.opennms.netmgt.events.api.model.IAutoAcknowledge;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The autoacknowledge information for the user with state
 *  controlling if event is marked acknoledged when inserted into
 * the
 *  database
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="autoacknowledge")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Autoacknowledge implements Serializable {
	private static final long serialVersionUID = -3959431672151994490L;

	//--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
	@NotNull
    private java.lang.String _content = "";

    /**
     * Field _state.
     */
	@XmlAttribute(name="state")
	@Pattern(regexp="(on|off)")
    private java.lang.String _state = "on";


      //----------------/
     //- Constructors -/
    //----------------/

    public Autoacknowledge() {
        super();
        setContent("");
        setState("on");
    }

    public static Autoacknowledge copyFrom(IAutoAcknowledge source) {
        if (source == null) {
            return null;
        }

        Autoacknowledge autoAck = new Autoacknowledge();
        autoAck.setContent(source.getContent());
        autoAck.setState(source.getState());
        return autoAck;
    }

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public java.lang.String getContent(
    ) {
        return this._content;
    }

    /**
     * Returns the value of field 'state'.
     * 
     * @return the value of field 'State'.
     */
    public java.lang.String getState(
    ) {
        return this._state;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(
            final java.lang.String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'state'.
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
