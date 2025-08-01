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
package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class ClassicTable.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "classicTable")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassicTable implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "col")
    private Col col;

    @XmlElement(name = "rows")
    private Rows rows;

    public ClassicTable() {
    }

    /**
     * Returns the value of field 'col'.
     * 
     * @return the value of field 'Col'.
     */
    public Col getCol() {
        return this.col;
    }

    /**
     * Returns the value of field 'rows'.
     * 
     * @return the value of field 'Rows'.
     */
    public Rows getRows() {
        return this.rows;
    }

    /**
     * Sets the value of field 'col'.
     * 
     * @param col the value of field 'col'.
     */
    public void setCol(final Col col) {
        this.col = col;
    }

    /**
     * Sets the value of field 'rows'.
     * 
     * @param rows the value of field 'rows'.
     */
    public void setRows(final Rows rows) {
        this.rows = rows;
    }

}
