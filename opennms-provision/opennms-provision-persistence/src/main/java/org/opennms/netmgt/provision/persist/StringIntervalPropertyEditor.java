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
package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.joda.time.Duration;
import org.joda.time.Period;

public class StringIntervalPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    /** {@inheritDoc} */
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else {
            if ("0".equals(text.trim())) {
                setValue(Duration.ZERO);
            } else {
                if ("-1".equals(text.trim())) {
                    setValue(Duration.ZERO.minus(1000));
                } else {
                    setValue(StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.parsePeriod(text.trim()).toStandardDuration());
                }
            }
        }
    }

    /**
     * <p>getAsText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getAsText() {
        final Duration value = (Duration)getValue();
        if (value.equals(Duration.ZERO)) {
            return "0";
        }
        Period p = value.toPeriod().normalizedStandard();
        return StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.print(p);
    } 
}
