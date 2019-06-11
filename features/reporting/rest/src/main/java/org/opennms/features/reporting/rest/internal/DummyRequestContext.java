/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.rest.internal;

import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;

// TODO MVR get rid of this ugly beast
public class DummyRequestContext implements RequestContext {

    private final MessageContext messageContext = new DefaultMessageContext();

    @Override
    public FlowDefinition getActiveFlow() throws IllegalStateException {
        return null;
    }

    @Override
    public StateDefinition getCurrentState() throws IllegalStateException {
        return null;
    }

    @Override
    public TransitionDefinition getMatchingTransition(String eventId) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean inViewState() {
        return false;
    }

    @Override
    public MutableAttributeMap getRequestScope() {
        return null;
    }

    @Override
    public MutableAttributeMap getFlashScope() {
        return null;
    }

    @Override
    public MutableAttributeMap getViewScope() throws IllegalStateException {
        return null;
    }

    @Override
    public MutableAttributeMap getFlowScope() throws IllegalStateException {
        return null;
    }

    @Override
    public MutableAttributeMap getConversationScope() {
        return null;
    }

    @Override
    public ParameterMap getRequestParameters() {
        return null;
    }

    @Override
    public ExternalContext getExternalContext() {
        return null;
    }

    @Override
    public MessageContext getMessageContext() {
        return messageContext;
    }

    @Override
    public FlowExecutionContext getFlowExecutionContext() {
        return null;
    }

    @Override
    public Event getCurrentEvent() {
        return null;
    }

    @Override
    public TransitionDefinition getCurrentTransition() {
        return null;
    }

    @Override
    public View getCurrentView() {
        return null;
    }

    @Override
    public MutableAttributeMap getAttributes() {
        return null;
    }

    @Override
    public String getFlowExecutionUrl() throws IllegalStateException {
        return null;
    }
}
