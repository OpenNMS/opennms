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

package org.opennms.netmgt.flows.classification.service.internal;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ReloadingClassificationEngine;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.service.ClassificationService;

public class ListeningClassificationEngine implements ClassificationEngine {

    private final ReloadingClassificationEngine delegate;

    private final Closeable listener;

    public ListeningClassificationEngine(final ReloadingClassificationEngine delegate,
                                         final ClassificationService classificationService) {
        this.delegate = Objects.requireNonNull(delegate);
        this.listener = classificationService.listen(this::listen);
    }

    @Override
    public String classify(final ClassificationRequest classificationRequest) {
        return this.delegate.classify(classificationRequest);
    }

    private void listen(final List<RuleDTO> rules) {
        try {
            this.delegate.load(rules);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
