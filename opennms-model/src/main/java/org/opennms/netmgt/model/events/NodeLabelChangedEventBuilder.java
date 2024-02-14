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
