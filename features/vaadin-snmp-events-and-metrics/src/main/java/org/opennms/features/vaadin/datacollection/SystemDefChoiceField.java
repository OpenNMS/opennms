/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.config.datacollection.SystemDefChoice;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

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
