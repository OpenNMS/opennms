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
 * <p>PrefabGraphType class.</p>
 */
public class PrefabGraphType {

    private String m_defaultReport;

    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_graphWidth;

    private String m_graphHeight;

    private String m_includeDirectory;

    private int m_includeRescanInterval;

    /**
     * <p>Constructor for PrefabGraphType.</p>
     */
    public PrefabGraphType() {
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
     * <p>setDefaultReport</p>
     *
     * @param defaultReport a {@link java.lang.String} object.
     */
    public void setDefaultReport(String defaultReport) {
        m_defaultReport = defaultReport;
    }

    /**
     * <p>getDefaultReport</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultReport() {
        return m_defaultReport;
    }
    
    
    /**
     * <p>setGraphWidth</p>
     *
     * @param graphWidth a {@link java.lang.String} object.
     */
    public void setGraphWidth(String graphWidth) {
        m_graphWidth = graphWidth;
    }

    /**
     * <p>getGraphWidth</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphWidth() {
        return m_graphWidth;
    }
    
    /**
     * <p>setGraphHeight</p>
     *
     * @param graphHeight a {@link java.lang.String} object.
     */
    public void setGraphHeight(String graphHeight) {
        m_graphHeight = graphHeight;
    }

    /**
     * <p>getGraphHeight</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphHeight() {
        return m_graphHeight;
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
     * Set the directory from which individual graph files should be included
     * @param includeDirectory - the new path for the include directory 
     * If not absolute, is a path relative to the directory containing the main configuration file
     */
    public void setIncludeDirectory(String includeDirectory) {
        m_includeDirectory = includeDirectory;
    }
    
    /** 
     * @return the include directory in which to look for any individual graph files
     */
    public String getIncludeDirectory() {
        return m_includeDirectory;
    }

    /**
     * Set the interval between rescans of the include directory, in milliseconds.  
     * The includeDirectory will only be rescanned for new files if it's been at least 
     * this long since the last scan, or the initial load.
     * @param timeout - the new timeout, in milliseconds
     */
    public void setIncludeDirectoryRescanInterval(int interval) {
        m_includeRescanInterval = interval;
    }

    /**
     * @return the timeout used
     */
    public int getIncludeDirectoryRescanTimeout() {
        return m_includeRescanInterval;
    }
}
