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
package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Just a generic string used for SQL statements
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "statement")
@XmlAccessorType(XmlAccessType.FIELD)
public class Statement implements Serializable {
    private static final long serialVersionUID = -3916499875634577541L;

    private final static boolean DEFAULT_TRANSACTIONAL_FLAG = true;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * internal content storage
     */
    @XmlValue
    private String _content = "";

    /**
     * Field _transactional.
     */
    @XmlAttribute(name = "transactional")
    private Boolean _transactional;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Statement() {
        super();
    }

    public Statement(final String value, final boolean transactional) {
        super();
        setContent(value);
        setTransactional(transactional);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Statement other = (Statement) obj;
        if (_content == null) {
            if (other._content != null)
                return false;
        } else if (!_content.equals(other._content))
            return false;
        if (_transactional == null) {
            if (other._transactional != null)
                return false;
        } else if (!_transactional.equals(other._transactional))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'content'. The field 'content' has the
     * following description: internal content storage
     *
     * @return the value of field 'Content'.
     */
    public String getContent() {
        return this._content;
    }

    /**
     * Returns the value of field 'transactional'.
     *
     * @return the value of field 'Transactional'.
     */
    public boolean getTransactional() {
        return _transactional == null ? DEFAULT_TRANSACTIONAL_FLAG
                                     : _transactional;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_content == null) ? 0 : _content.hashCode());
        result = prime * result
                + ((_transactional == null) ? 0 : _transactional.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'transactional'.
     *
     * @return the value of field 'Transactional'.
     */
    public boolean isTransactional() {
        return _transactional == null ? DEFAULT_TRANSACTIONAL_FLAG
                                     : _transactional;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has the
     * following description: internal content storage
     *
     * @param content
     *            the value of field 'content'.
     */
    public void setContent(final String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'transactional'.
     *
     * @param transactional
     *            the value of field 'transactional'.
     */
    public void setTransactional(final boolean transactional) {
        this._transactional = transactional;
    }
}
