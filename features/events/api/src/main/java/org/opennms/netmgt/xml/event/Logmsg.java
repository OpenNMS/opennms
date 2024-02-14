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

import org.opennms.netmgt.events.api.model.ILogMsg;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The event logmsg with the destination attribute defining
 *  if event is for display only, logonly, log and display or
 * neither. A
 *  destination attribute of 'donotpersist' indicates that Eventd
 * is not to
 *  persist the event to the database. The optional notify
 * attributed can be 
 *  used to suppress notices on a particular event (by default it
 * is true - 
 *  i.e. a notice will be sent.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="logmsg")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Logmsg implements Serializable {

    private static final long serialVersionUID = -7173862847984790914L;

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
     * Field _notify.
     */
	@XmlAttribute(name="notify")
    private Boolean _notify = true;

    /**
     * Field _dest.
     */
    @XmlAttribute(name="dest")
    @Pattern(regexp="(logndisplay|displayonly|logonly|suppress|donotpersist)")
    private java.lang.String _dest = "logndisplay";


      //----------------/
     //- Constructors -/
    //----------------/

    public Logmsg() {
        super();
        setContent("");
        setDest("logndisplay");
    }

    public static Logmsg copyFrom(ILogMsg source) {
        if (source == null) {
            return null;
        }

        Logmsg logmsg = new Logmsg();
        logmsg.setContent(source.getContent());
        logmsg.setDest(source.getDest());
        logmsg.setNotify(source.hasNotify() ? source.getNotify() : null);
        return logmsg;
    }

      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteNotify(
    ) {
        this._notify = null;
    }

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
     * Returns the value of field 'dest'.
     * 
     * @return the value of field 'Dest'.
     */
    public java.lang.String getDest(
    ) {
        return this._dest;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean getNotify(
    ) {
        return this._notify == null? false : this._notify;
    }

    /**
     * Method hasNotify.
     * 
     * @return true if at least one Notify has been added
     */
    public boolean hasNotify(
    ) {
        return this._notify != null;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean isNotify() {
        return getNotify();
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
     * Sets the value of field 'dest'.
     * 
     * @param dest the value of field 'dest'.
     */
    public void setDest(
            final java.lang.String dest) {
        this._dest = dest;
    }

    /**
     * Sets the value of field 'notify'.
     * 
     * @param notify the value of field 'notify'.
     */
    public void setNotify(
            final Boolean notify) {
        this._notify = notify;
    }

    @Override
    public String toString() {
    	return new OnmsStringBuilder(this).toString();
    }
}
