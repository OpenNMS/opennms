/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc. OpenNMS(R) is a
 * registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version. OpenNMS(R) is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/ For more information
 * contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import org.opennms.features.topology.api.Widget.ViewData;

import com.vaadin.ui.Component;

public interface WidgetUpdateListener {

    enum WidgetEventType {
        BIND, UNBIND;

        public boolean isBind() {
            return BIND.equals(this);
        }
        
        public boolean isUnbind() {
            return UNBIND.equals(this);
        }
    }

    class WidgetUpdateEvent {
        private final WidgetEventType type;
        private final Component changedElement;
        private final Widget source;
        private final ViewData viewData;
        
        public WidgetUpdateEvent(Widget widget, WidgetEventType type,
                Component component, ViewData viewData) {
            this.type = type;
            this.source = widget;
            this.changedElement = component;
            this.viewData = viewData;
        }

        public Component getChangedElement() {
            return changedElement;
        }

        public WidgetEventType getType() {
            return type;
        }

        public Widget getSource() {
            return source;
        }
        
        public ViewData getViewData() {
            return viewData;
        }
    }

    public void widgetContentUpdated(WidgetUpdateEvent e);
}
