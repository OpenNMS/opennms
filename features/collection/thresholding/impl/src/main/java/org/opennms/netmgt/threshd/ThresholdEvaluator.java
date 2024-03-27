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
package org.opennms.netmgt.threshd;

import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.threshd.api.ThresholdingSession;

/**
 * Class used to provide information to ThresholdEntity about the
 * threshold evaluators that are available and a way to create
 * threshold evaluator state classes.
 *
 * @author ranger
 * @version $Id: $
 */
public interface ThresholdEvaluator {

    /**
     * <p>supportsType</p>
     *
     * @param type a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean supportsType(ThresholdType type);

    /**
     * <p>getThresholdEvaluatorState</p>
     *
     * @param threshold a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     * @return a {@link org.opennms.netmgt.threshd.ThresholdEvaluatorState} object.
     */
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession);

}
