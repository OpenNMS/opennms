/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import java.util.Date;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

import com.google.common.base.Strings;

public class NodeLabelChangedEventBuilder extends EventBuilder {

    public NodeLabelChangedEventBuilder(final String source) {
        super(EventConstants.NODE_LABEL_CHANGED_EVENT_UEI, source, new Date());
    }

    @Override
    public Event getEvent() {
        Event event = super.getEvent();
        checkParam(event, EventConstants.PARM_OLD_NODE_LABEL);
        checkParam(event, EventConstants.PARM_NEW_NODE_LABEL);
        return event;
    }

    private void checkParam(Event event, String name){
        Parm param = event.getParm(name);
        if(param == null || param.getValue() == null || Strings.isNullOrEmpty(Strings.nullToEmpty(param.getValue().toString()).trim())){
            throw new IllegalStateException("NodeLabelChangedEvent is not ready to be build, parameter "+name+" is missing.");
        }
    }

    public NodeLabelChangedEventBuilder setOldNodeLabel(String oldNodeLabel){
        setParam(EventConstants.PARM_OLD_NODE_LABEL, oldNodeLabel);
        return this;
    }

    public NodeLabelChangedEventBuilder setNewNodeLabel(String newNodeLabel){
        setParam(EventConstants.PARM_NEW_NODE_LABEL, newNodeLabel);
        return this;
    }

    public NodeLabelChangedEventBuilder setOldNodeLabelSource(String oldNodeLabelSource){
        setParam(EventConstants.PARM_OLD_NODE_LABEL_SOURCE, oldNodeLabelSource);
        return this;
    }

    public NodeLabelChangedEventBuilder setNewNodeLabelSource(String newNodeLabelSource){
        setParam(EventConstants.PARM_NEW_NODE_LABEL_SOURCE, newNodeLabelSource);
        return this;
    }

    @Override
    protected void checkForIllegalUei(){
        // do nothing since we can be sure that we set the right Event type. Once the generic EventBuilder is abstract we
        // can remove this method from here and the super class
    }
}
