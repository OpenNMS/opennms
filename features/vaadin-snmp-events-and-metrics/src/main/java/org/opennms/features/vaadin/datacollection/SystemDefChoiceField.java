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
package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.config.datacollection.SystemDefChoice;

import com.vaadin.v7.data.validator.RegexpValidator;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextField;

/**
 * The System Definition Choice Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefChoiceField extends CustomField<SystemDefChoice> {

    /** The Constant SINGLE. */
    private static final String SINGLE = "Single";

    /** The Constant MASK. */
    private static final String MASK = "Mask";

    /** The Constant OPTIONS. */
    private static final List<String> OPTIONS = Arrays.asList(new String[] { SINGLE, MASK });

    /** The OID type. */
    private final OptionGroup oidType = new OptionGroup("OID Type", OPTIONS);

    /** The OID value. */
    private final TextField oidValue = new TextField("OID Value");

    /**
     * Instantiates a new system definition choice field.
     *
     * @param caption the caption
     */
    public SystemDefChoiceField(String caption) {
        setCaption(caption);
        oidType.setNullSelectionAllowed(false);
        oidType.select("Single");

        oidValue.setWidth("100%");
        oidValue.setNullSettingAllowed(false);
        oidValue.setRequired(true);
        oidValue.setImmediate(true);
        oidValue.addValidator(new RegexpValidator("^\\.[.\\d]+$", "Invalid OID {0}"));
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setWidth("100%");
        layout.addComponent(oidType);
        layout.addComponent(oidValue);
        layout.setExpandRatio(oidValue, 1);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<SystemDefChoice> getType() {
        return SystemDefChoice.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(SystemDefChoice systemDef) {
        boolean oidTypeState = oidType.isReadOnly();
        oidType.setReadOnly(false);
        oidType.select(systemDef.getSysoid() == null ? MASK : SINGLE);
        if (oidTypeState) {
            oidType.setReadOnly(true);
        }
        boolean oidValueState = oidValue.isReadOnly();
        oidValue.setReadOnly(false);
        oidValue.setValue(systemDef.getSysoid() == null ? systemDef.getSysoidMask() : systemDef.getSysoid());
        if (oidValueState) {
            oidValue.setReadOnly(true);
        }
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected SystemDefChoice getInternalValue() {
        SystemDefChoice systemDef = new SystemDefChoice();
        String type = (String) oidType.getValue();
        if (type.equals(SINGLE)) {
            systemDef.setSysoid((String) oidValue.getValue());
        } else {
            systemDef.setSysoidMask((String) oidValue.getValue());
        }
        return systemDef;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        oidValue.setReadOnly(readOnly);
        oidType.setReadOnly(readOnly);
        super.setReadOnly(readOnly);
    }

}
