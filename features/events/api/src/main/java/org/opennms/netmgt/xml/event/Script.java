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

import org.opennms.netmgt.events.api.model.IScript;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The script information for this event - describes a
 *  script to be executed whenever the event occurs.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="script")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Script implements Serializable {
	private static final long serialVersionUID = -4421693308782820218L;

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
     * Field _language.
     */
	@XmlAttribute(name="language", required=true)
	@NotNull
    private java.lang.String _language;


      //----------------/
     //- Constructors -/
    //----------------/

    public Script() {
        super();
        setContent("");
    }

    public static Script copyFrom(IScript source) {
        if (source == null) {
            return null;
        }

        Script script = new Script();
        script.setContent(source.getContent());
        script.setLanguage(source.getLanguage());
        return script;
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
     * Returns the value of field 'language'.
     * 
     * @return the value of field 'Language'.
     */
    public java.lang.String getLanguage(
    ) {
        return this._language;
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
     * Sets the value of field 'language'.
     * 
     * @param language the value of field 'language'.
     */
    public void setLanguage(
            final java.lang.String language) {
        this._language = language;
    }

        @Override
    public String toString() {
    	return new OnmsStringBuilder(this).toString();
    }
}
