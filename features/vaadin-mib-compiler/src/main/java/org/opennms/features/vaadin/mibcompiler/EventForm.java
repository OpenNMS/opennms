/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.mibcompiler;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;

/**
 * The Class EventForm.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventForm extends Form implements ClickListener {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] { "uei", "eventLabel", "descr", "logmsgContent", "logmsgDest", "severity", "maskElements", "maskVarbinds", "varbindsdecodeCollection" };

    /** The save. */
    private final Button save = new Button("Save");
    
    /** The edit. */
    private final Button edit = new Button("Edit");
    
    /** The cancel. */
    private final Button cancel = new Button("Cancel");

    /**
     * Instantiates a new event form.
     */
    public EventForm() {
        setCaption("Event Detail");
        setWriteThrough(false);
        setVisible(false);
        setFormFieldFactory(new EventFieldFactory());
        initToolbar();
    }

    /**
     * Inits the toolbar.
     */
    private void initToolbar() {
        save.addListener((ClickListener)this);
        edit.addListener((ClickListener)this);
        cancel.addListener((ClickListener)this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.addComponent(save);
        toolbar.addComponent(edit);
        toolbar.addComponent(cancel);

        setFooter(toolbar);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Form#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        String[] tables = new String[] { "maskElements", "maskVarbinds", "varbindsdecodeCollection" };
        for (String tableId : tables ) {
            Field f = getField(tableId);
            if (f != null && f instanceof Table) {
                ((Table) f).setEditable(!readOnly);
            }
        }
        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
        edit.setVisible(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == save) {
            commit();
            customCommit();
            setReadOnly(true);
        }
        if (source == edit) {
            setReadOnly(false);
        }
        if (source== cancel) {
            discard();
            setReadOnly(true);
        }
    }

    /**
     * Custom commit.
     */
    public abstract void customCommit();

}
