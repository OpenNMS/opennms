/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleProcessor;
import org.opennms.newts.api.SampleProcessorService;

/**
 * A simple sample processing service that executes the sample processors
 * serially in the caller's thread.
 *
 * @author jwhite
 */
public class SimpleSampleProcessorService implements SampleProcessorService {

    private final Set<SampleProcessor> m_processors;

    public SimpleSampleProcessorService(Set<SampleProcessor> processors) {
        if (NewtsUtils.DISABLE_INDEXING) {
            // Currently the only processor is the indexing processor so
            // we always use an empty set of processors when indexing is disabled
            m_processors = Collections.emptySet();
        } else {
            m_processors = Objects.requireNonNull(processors);
        }
    }

    @Override
    public void submit(Collection<Sample> samples) {
        m_processors.stream().forEach(p -> p.submit(samples));
    }

    @Override
    public void shutdown() throws InterruptedException {
        // pass
    }

    @Override
    public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
