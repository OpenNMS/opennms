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
package org.opennms.vaadin.extender;

import com.vaadin.annotations.JavaScript;

/**
 * @deprecated Use the {@link JavaScript} annotation from Vaadin 7 instead.
 */
public class ScriptTag {

    private String m_source;
    private String m_type;
    private String m_contents;

    public ScriptTag() {
    }

    public ScriptTag(final String source, final String type, final String contents) {
        m_source   = source;
        m_type     = type;
        m_contents = contents;
    }

    public String getSource() {
        return m_source;
    }
    
    public void setSource(final String source) {
        m_source = source;
    }
    
    public String getType() {
        return m_type;
    }
    
    public void setType(final String type) {
        m_type = type;
    }
    
    public String getContents() {
        return m_contents;
    }
    
    public void setContents(final String contents) {
        m_contents = contents;
    }
}
