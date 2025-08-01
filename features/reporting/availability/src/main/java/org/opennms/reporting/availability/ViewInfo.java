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
 * element name="viewInfo"
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "viewInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "viewName")
    private String viewName;

    @XmlElement(name = "viewTitle")
    private String viewTitle;

    @XmlElement(name = "viewComments")
    private String viewComments;

    public ViewInfo() {
    }

    /**
     * Returns the value of field 'viewComments'.
     * 
     * @return the value of field 'ViewComments'.
     */
    public String getViewComments() {
        return this.viewComments;
    }

    /**
     * Returns the value of field 'viewName'.
     * 
     * @return the value of field 'ViewName'.
     */
    public String getViewName() {
        return this.viewName;
    }

    /**
     * Returns the value of field 'viewTitle'.
     * 
     * @return the value of field 'ViewTitle'.
     */
    public String getViewTitle() {
        return this.viewTitle;
    }

    /**
     * Sets the value of field 'viewComments'.
     * 
     * @param viewComments the value of field 'viewComments'.
     */
    public void setViewComments(final String viewComments) {
        this.viewComments = viewComments;
    }

    /**
     * Sets the value of field 'viewName'.
     * 
     * @param viewName the value of field 'viewName'.
     */
    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    /**
     * Sets the value of field 'viewTitle'.
     * 
     * @param viewTitle the value of field 'viewTitle'.
     */
    public void setViewTitle(final String viewTitle) {
        this.viewTitle = viewTitle;
    }

}
