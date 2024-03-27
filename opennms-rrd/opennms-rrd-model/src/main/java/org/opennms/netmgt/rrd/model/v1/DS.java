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
package org.opennms.netmgt.rrd.model.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.rrd.model.AbstractDS;

/**
 * The Class DS (Data Source).
 * <ul>
 * <li><b>ds.decl:</b> name, type, minimal_heartbeat, min, max, last_ds, value, unknown_sec</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DS extends AbstractDS {

    /** The type of the datasource. */
    private DSType type;

    /**
     * Gets the type.
     *
     * @return the type
     */
    @XmlElement(required=true)
    @XmlJavaTypeAdapter(DSAdapter.class)
    public DSType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(DSType type) {
        this.type = type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = DSType.fromValue(type);
    }

    /**
     * Format equals.
     *
     * @param ds the DS object
     * @return true, if successful
     */
    public boolean formatEquals(DS ds) {
        if (this.type != null) {
            if (ds.type == null) return false;
            else if (!(this.type.equals(ds.type))) 
                return false;
        }
        else if (ds.type != null)
            return false;

        return super.formatEquals(ds);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractDS#isCounter()
     */
    @Override
    public boolean isCounter() {
        return !getType().equals(DSType.GAUGE);
    }
}
