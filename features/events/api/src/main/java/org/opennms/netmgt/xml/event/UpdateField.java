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

import org.opennms.netmgt.events.api.model.IUpdateField;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object used to identify which alarm fields should be updated during Alarm reduction.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name="update-field")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class UpdateField implements Serializable {
    
    private static final long serialVersionUID = 4780818827895098397L;

    @XmlAttribute(name="field-name", required=true)
    private java.lang.String m_fieldName;
    
    @XmlAttribute(name="update-on-reduction", required=false)
    private java.lang.Boolean m_updateOnReduction = Boolean.TRUE;
    
    @XmlAttribute(name="value-expression", required=false)
    private java.lang.String m_valueExpression;

    public static UpdateField copyFrom(IUpdateField source) {
        if (source == null) {
            return null;
        }

        UpdateField updateField = new UpdateField();
        updateField.setFieldName(source.getFieldName());
        updateField.setUpdateOnReduction(source.isUpdateOnReduction());
        updateField.setValueExpression(source.getValueExpression());
        return updateField;
    }
    
    public String getFieldName() {
        return m_fieldName;
    }

    public void setFieldName(String fieldName) {
        m_fieldName = fieldName;
    }
    
    public Boolean isUpdateOnReduction() {
        return m_updateOnReduction;
    }
    
    public void setUpdateOnReduction(Boolean update) {
        m_updateOnReduction = update;
    }
    
    public String getValueExpression() {
        return m_valueExpression;
    }
    
    public void setValueExpression(String valueExpression) {
        m_valueExpression = valueExpression;
    }
}
