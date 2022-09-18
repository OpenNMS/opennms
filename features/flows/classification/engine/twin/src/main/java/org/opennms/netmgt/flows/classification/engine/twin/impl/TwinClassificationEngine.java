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

package org.opennms.netmgt.flows.classification.engine.twin.impl;

import java.util.List;
import java.util.Objects;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ReloadingClassificationEngine;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;

public class TwinClassificationEngine implements ClassificationEngine {

    private final ReloadingClassificationEngine delegate;

    public TwinClassificationEngine(final ReloadingClassificationEngine delegate,
                                    final TwinSubscriber subscriber) {
        this.delegate = Objects.requireNonNull(delegate);

        subscriber.<List<RuleDTO>>subscribe(RuleDTO.TWIN_KEY, RuleDTO.TWIN_TYPE, (rules) -> {
            try {
                this.delegate.load(rules);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String classify(final ClassificationRequest classificationRequest) {
        return this.delegate.classify(classificationRequest);
    }
}
