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
package org.opennms.features.topology.app.internal.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

public class SearchBoxState extends AbstractComponentState {

    List<SearchSuggestion> m_suggestions = new ArrayList<>();
    List<SearchSuggestion> m_selected = new ArrayList<>();
    List<SearchSuggestion> m_focused = new ArrayList<>();
    int m_triggerCount = 0;

    public void setSuggestions(List<SearchSuggestion> suggestions){
        m_suggestions = suggestions;
        //This is a stupid hack to get VAADIN state to push all changes all the time
        m_triggerCount += 1;
    }

    public List<SearchSuggestion> getSuggestions(){
        return m_suggestions;
    }

    public void setSelected(List<SearchSuggestion> selected){
        m_selected = selected;
    }

    //Needed to have this method so that triggerCount would trigger a push to client
    public void setTriggerCount(int count){
        m_triggerCount = count;
    }
    public int getTriggerCount(){
        return m_triggerCount;
    }


    public List<SearchSuggestion> getSelected(){
        return m_selected;
    }

    public void setFocused(List<SearchSuggestion> focused) {
        m_focused = focused;
    }

    public List<SearchSuggestion> getFocused() {
        return m_focused;
    }

}
