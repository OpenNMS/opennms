/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.service.publisher.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.service.ClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassificationRulePublisher implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationRulePublisher.class);

    private final TwinPublisher.Session<List<RuleDTO>> publisher;

    private final Closeable ruleWatcher;

    public ClassificationRulePublisher(final ClassificationService classificationService,
                                       final TwinPublisher twinPublisher) throws IOException {
        this.publisher = twinPublisher.register(RuleDTO.TWIN_KEY, RuleDTO.TWIN_TYPE, null);

        this.ruleWatcher = classificationService.listen(this::rulesChanged);
    }

    private void rulesChanged(final List<RuleDTO> rules) {
        try {
            this.publisher.publish(rules);
        } catch (final IOException e) {
            LOG.error("Publishing classification rules failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.ruleWatcher != null) {
            this.ruleWatcher.close();
        }

        if (this.publisher != null) {
            this.publisher.close();
        }
    }
}
