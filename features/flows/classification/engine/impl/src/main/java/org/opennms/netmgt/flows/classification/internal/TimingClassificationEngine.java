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
package org.opennms.netmgt.flows.classification.internal;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class TimingClassificationEngine implements ClassificationEngine {

    private final ClassificationEngine delegate;
    private final Timer classifyTimer;
    private final Timer reloadTimer;
    private final Timer getInvalidRulesTimer;

    public TimingClassificationEngine(MetricRegistry metricRegistry, ClassificationEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.classifyTimer = metricRegistry.timer("classify");
        this.reloadTimer = metricRegistry.timer("reload");
        this.getInvalidRulesTimer = metricRegistry.timer("getInvalidrules");
    }
    
    @Override
    public String classify(ClassificationRequest classificationRequest) {
        try (final Timer.Context ctx = classifyTimer.time()) {
            return delegate.classify(classificationRequest);
        }
    }

    @Override
    public void reload() throws InterruptedException {
        try (final Timer.Context ctx = reloadTimer.time()) {
            delegate.reload();
        }
    }

    @Override
    public List<Rule> getInvalidRules() {
        try (final Timer.Context ctx = getInvalidRulesTimer.time()) {
            return delegate.getInvalidRules();
        }
    }

    public void addClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.delegate.addClassificationRulesReloadedListener(classificationRulesReloadedListener);
    }

    public void removeClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.delegate.removeClassificationRulesReloadedListener(classificationRulesReloadedListener);
    }
}
