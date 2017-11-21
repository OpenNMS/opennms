/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Objects;

public class TcpSession implements TemplateManager {

    private final static class Key {
        public final long observationDomainId;
        public final int templateId;

        Key(final long observationDomainId,
            final int templateId) {
            this.observationDomainId = observationDomainId;
            this.templateId = templateId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o.getClass() != Key.class) return false;

            final Key that = (Key) o;
            return this.observationDomainId == that.observationDomainId &&
                    this.templateId == that.templateId;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.observationDomainId, this.templateId);
        }
    }

    private final Map<Key, Template> templates = new HashMap<>();

    public TcpSession() {
    }

    @Override
    public void add(final long observationDomainId, final int templateId, final Template template) {
        this.templates.put(new Key(observationDomainId, templateId), template);
    }

    @Override
    public void remove(final long observationDomainId, final int templateId) {
        this.templates.remove(new Key(observationDomainId, templateId));
    }

    @Override
    public void removeAll(final long observationDomainId, final Template.Type type) {
        this.templates.entrySet().removeIf(e -> e.getKey().observationDomainId == observationDomainId && e.getValue().type == type);
    }

    @Override
    public TemplateResolver getResolver(final long observationDomainId) {
        return templateId -> Optional.ofNullable(this.templates.get(new Key(observationDomainId, templateId)));
    }
}
