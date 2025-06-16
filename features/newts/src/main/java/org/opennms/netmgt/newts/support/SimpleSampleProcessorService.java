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
