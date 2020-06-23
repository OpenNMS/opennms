/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ConversationExchangeDefaultImpl<Request, RespType> implements ConversationExchange<Request, RespType> {
    private final RequestBuilder<Request> m_requestBuilder;
    private final ResponseValidator<RespType> m_responseValidator;

    public ConversationExchangeDefaultImpl(RequestBuilder<Request> reqBuilder, ResponseValidator<RespType> respValidator) {
        m_requestBuilder = reqBuilder;
        m_responseValidator = respValidator;
    }

    @Override
    public Request getRequest() {
        return m_requestBuilder == null ? null : m_requestBuilder.getRequest();
    }

    @Override
    public boolean validate(RespType response) {
        return m_responseValidator.validate(response);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("request", getRequest());
        builder.append("responseValidator", m_responseValidator);
        return builder.toString();
    }
}
