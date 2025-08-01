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
package org.opennms.netmgt.config.scriptd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the scriptd-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "scriptd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("scriptd-configuration.xsd")
public class ScriptdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "engine")
    private List<Engine> m_engines = new ArrayList<>();

    @XmlElement(name = "start-script")
    private List<StartScript> m_startScripts = new ArrayList<>();

    @XmlElement(name = "stop-script")
    private List<StopScript> m_stopScripts = new ArrayList<>();

    @XmlElement(name = "reload-script")
    private List<ReloadScript> m_reloadScripts = new ArrayList<>();

    @XmlElement(name = "event-script")
    private List<EventScript> m_eventScripts = new ArrayList<>();

    @XmlAttribute(name = "transactional")
    private Boolean m_transactional;

    public List<Engine> getEngines() {
        return m_engines;
    }

    public void setEngines(final List<Engine> engines) {
        if (engines == m_engines) return;
        m_engines.clear();
        if (engines != null) m_engines.addAll(engines);
    }

    public void addEngine(final Engine engine) {
        m_engines.add(engine);
    }

    public boolean removeEngine(final Engine engine) {
        return m_engines.remove(engine);
    }

    public List<StartScript> getStartScripts() {
        return m_startScripts;
    }

    public void setStartScripts(final List<StartScript> startScripts) {
        if (startScripts == m_startScripts) return;
        m_startScripts.clear();
        if (startScripts != null) m_startScripts.addAll(startScripts);
    }

    public void addStartScript(final StartScript startScript) {
        m_startScripts.add(startScript);
    }

    public boolean removeStartScript(final StartScript startScript) {
        return m_startScripts.remove(startScript);
    }

    public List<StopScript> getStopScripts() {
        return m_stopScripts;
    }

    public void setStopScripts(final List<StopScript> stopScripts) {
        if (stopScripts == m_stopScripts) return;
        m_stopScripts.clear();
        if (stopScripts != null) m_stopScripts.addAll(stopScripts);
    }

    public void addStopScript(final StopScript stopScript) {
        m_stopScripts.add(stopScript);
    }

    public boolean removeStopScript(final StopScript stopScript) {
        return m_stopScripts.remove(stopScript);
    }

    public List<ReloadScript> getReloadScripts() {
        return m_reloadScripts;
    }

    public void setReloadScripts(final List<ReloadScript> reloadScripts) {
        if (reloadScripts == m_reloadScripts) return;
        m_reloadScripts.clear();
        if (reloadScripts != null) m_reloadScripts.addAll(reloadScripts);
    }

    public void addReloadScript(final ReloadScript reloadScript) {
        m_reloadScripts.add(reloadScript);
    }

    public boolean removeReloadScript(final ReloadScript reloadScript) {
        return m_reloadScripts.remove(reloadScript);
    }

    public List<EventScript> getEventScripts() {
        return m_eventScripts;
    }

    public void setEventScripts(final List<EventScript> eventScripts) {
        if (eventScripts == m_eventScripts) return;
        m_eventScripts.clear();
        if (eventScripts != null) m_eventScripts.addAll(eventScripts);
    }

    public void addEventScript(final EventScript eventScript) {
        m_eventScripts.add(eventScript);
    }

    public boolean removeEventScript(final EventScript eventScript) {
        return m_eventScripts.remove(eventScript);
    }

    public Boolean  getTransactional() {
        return m_transactional != null ? m_transactional : Boolean.TRUE;
    }

    public void setTransactional(Boolean transactional) {
        m_transactional = transactional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_engines, 
            m_startScripts, 
            m_stopScripts, 
            m_reloadScripts, 
            m_eventScripts,
            m_transactional);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof ScriptdConfiguration) {
            final ScriptdConfiguration that = (ScriptdConfiguration)obj;
            return Objects.equals(this.m_engines, that.m_engines)
                && Objects.equals(this.m_startScripts, that.m_startScripts)
                && Objects.equals(this.m_stopScripts, that.m_stopScripts)
                && Objects.equals(this.m_reloadScripts, that.m_reloadScripts)
                && Objects.equals(this.m_eventScripts, that.m_eventScripts)
                && Objects.equals(this.m_transactional, that.m_transactional);
        }
        return false;
    }

}
