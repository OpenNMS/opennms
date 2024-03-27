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
package org.opennms.netmgt.model;


/**
 * <p>AdhocGraphType class.</p>
 */
public class AdhocGraphType {
    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_titleTemplate;

    private String m_dataSourceTemplate;

    private String m_graphLineTemplate;

    /**
     * <p>Constructor for AdhocGraphType.</p>
     */
    public AdhocGraphType() {
        
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setCommandPrefix</p>
     *
     * @param commandPrefix a {@link java.lang.String} object.
     */
    public void setCommandPrefix(String commandPrefix) {
        m_commandPrefix = commandPrefix;
    }
    
    /**
     * <p>getCommandPrefix</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommandPrefix() {
        return m_commandPrefix;
    }

    /**
     * <p>setOutputMimeType</p>
     *
     * @param outputMimeType a {@link java.lang.String} object.
     */
    public void setOutputMimeType(String outputMimeType) {
        m_outputMimeType = outputMimeType;
    }
    
    /**
     * <p>getOutputMimeType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputMimeType() {
        return m_outputMimeType;
    }

    /**
     * <p>setTitleTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setTitleTemplate(String template) {
        m_titleTemplate = template;
    }

    /**
     * <p>setDataSourceTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setDataSourceTemplate(String template) {
        m_dataSourceTemplate = template;
    }

    /**
     * <p>setGraphLineTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setGraphLineTemplate(String template) {
        m_graphLineTemplate = template;
    }

    /**
     * <p>getDataSourceTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDataSourceTemplate() {
        return m_dataSourceTemplate;
    }

    /**
     * <p>getGraphLineTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphLineTemplate() {
        return m_graphLineTemplate;
    }

    /**
     * <p>getTitleTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitleTemplate() {
        return m_titleTemplate;
    }

}
