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
package org.opennms.features.apilayer.feedback;

import java.util.List;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedbackListener;
import org.osgi.framework.BundleContext;

public class AlarmFeedbackListenerManager extends InterfaceMapper<org.opennms.integration.api.v1.feedback.AlarmFeedbackListener, AlarmFeedbackListener> {

    public AlarmFeedbackListenerManager(BundleContext bundleContext) {
        super(AlarmFeedbackListener.class, bundleContext);
    }

    @Override
    public AlarmFeedbackListener map(org.opennms.integration.api.v1.feedback.AlarmFeedbackListener ext) {
        return new AlarmFeedbackListener() {
            @Override
            public void handleAlarmFeedback(List<AlarmFeedback> alarmFeedback) {
                alarmFeedback.stream()
                        .map(ModelMappers::toFeedback)
                        .forEach(ext::onFeedback);
            }
        };
    }
}
