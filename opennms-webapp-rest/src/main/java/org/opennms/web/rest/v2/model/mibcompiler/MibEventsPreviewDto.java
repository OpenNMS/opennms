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
package org.opennms.web.rest.v2.model.mibcompiler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MibEventsPreviewDto extends MibParseResultDto {

    private String ueiBase;
    private int eventCount;
    private String suggestedFileName;
    private String eventsXml;
    /**
     * Generated UEIs that other event sources already define, mapped to those sources in evaluation
     * order. The source the events will be saved as is left out, since saving replaces it.
     */
    private Map<String, List<MibEventSourceDto>> existingUeis = new LinkedHashMap<>();

    public String getUeiBase() {
        return ueiBase;
    }

    public void setUeiBase(String ueiBase) {
        this.ueiBase = ueiBase;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public String getSuggestedFileName() {
        return suggestedFileName;
    }

    public void setSuggestedFileName(String suggestedFileName) {
        this.suggestedFileName = suggestedFileName;
    }

    public String getEventsXml() {
        return eventsXml;
    }

    public void setEventsXml(String eventsXml) {
        this.eventsXml = eventsXml;
    }

    public Map<String, List<MibEventSourceDto>> getExistingUeis() {
        return existingUeis;
    }

    public void setExistingUeis(Map<String, List<MibEventSourceDto>> existingUeis) {
        this.existingUeis = existingUeis;
    }
}
