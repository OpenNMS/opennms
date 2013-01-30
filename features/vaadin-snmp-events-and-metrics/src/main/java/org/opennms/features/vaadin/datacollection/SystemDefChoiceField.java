/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

/**
 * The System Definition Choice Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefChoiceField extends CustomField {

    /** The Constant SINGLE. */
    private static final String SINGLE = "Single";

    /** The Constant MASK. */
    private static final String MASK = "Mask";

    /** The Constant OPTIONS. */
    private static final List<String> OPTIONS = Arrays.asList(new String[] { SINGLE, MASK });

    /** The OID type. */
    private OptionGroup oidType;

    /** The OID value. */
    private TextField oidValue;

    /**
     * Instantiates a new system definition choice field.
     */
    public SystemDefChoiceField() {
        oidType = new OptionGroup("OID Type", OPTIONS);
        oidType.setNullSelectionAllowed(false);
        oidType.select("Single");

        oidValue = new TextField("OID Value");
        oidValue.setWidth("100%");
        oidValue.setNullSettingAllowed(false);
        oidValue.setRequired(true);
        oidValue.setImmediate(true);
        oidValue.addValidator(new RegexpValidator("^\\.[.\\d]+$", "Invalid OID {0}"));

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setWidth("100%");
        layout.addComponent(oidType);
        layout.addComponent(oidValue);
        layout.setExpandRatio(oidValue, 1);

        setWriteThrough(false);
        setCompositionRoot(layout);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return SystemDefChoice.class;
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof SystemDefChoice) {
            SystemDefChoice dto = (SystemDefChoice) value;
            oidType.select(dto.getSysoid() == null ? MASK : SINGLE);
            oidValue.setValue(dto.getSysoid() == null ? dto.getSysoidMask() : dto.getSysoid());
        } else {
            throw new ConversionException("Invalid type");
        }
        super.setPropertyDataSource(newDataSource);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getValue()
     */
    @Override
    public Object getValue() {
        SystemDefChoice dto = new SystemDefChoice();
        String type = (String) oidType.getValue();
        if (type.equals(SINGLE)) {
            dto.setSysoid((String) oidValue.getValue());
        } else {
            dto.setSysoidMask((String) oidValue.getValue());
        }
        return dto;
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
