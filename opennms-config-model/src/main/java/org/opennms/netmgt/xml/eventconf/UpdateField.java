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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Object used to identify which alarm fields should be updated during Alarm reduction.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
@XmlRootElement(name="update-field")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class UpdateField implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="field-name", required=true)
    private String m_fieldName;

    @XmlAttribute(name="update-on-reduction", required=false)
    private Boolean m_updateOnReduction = Boolean.TRUE;

    public String getFieldName() {
        return m_fieldName;
    }

    public void setFieldName(final String fieldName) {
        m_fieldName = ConfigUtils.assertNotEmpty(fieldName, "field-name");
    }

    public Boolean getUpdateOnReduction() {
        return m_updateOnReduction;
    }

    public void setUpdateOnReduction(final Boolean update) {
        m_updateOnReduction = update;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_fieldName, m_updateOnReduction);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UpdateField) {
            final UpdateField that = (UpdateField) obj;
            return Objects.equals(this.m_fieldName, that.m_fieldName) &&
                    Objects.equals(this.m_updateOnReduction, that.m_updateOnReduction);
        }
        return false;
    }

}