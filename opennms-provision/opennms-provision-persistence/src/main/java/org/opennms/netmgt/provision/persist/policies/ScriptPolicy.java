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
package org.opennms.netmgt.provision.persist.policies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.ScriptUtil;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.persist.JSR223ScriptCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>ScriptPolicy class.</p>
 *
 * @author cpape
 */
@Scope("prototype")
@Policy("Script Policy")
public class ScriptPolicy extends BasePolicy<OnmsNode> implements NodePolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ScriptPolicy.class);

    private Path m_scriptPath;

    private String m_script;

    @Autowired
    private JSR223ScriptCache m_scriptCache;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SessionUtils m_sessionUtils;

    public ScriptPolicy() {
        this(Paths.get(System.getProperty("opennms.home"), "etc", "script-policies"));
    }

    public ScriptPolicy(final Path scriptPath) {
        this.m_scriptPath = scriptPath;
    }

    protected CompiledScript compileScript(final String script) throws IOException, ScriptException {
        if (script == null) {
            throw new IllegalArgumentException("Script must not be null");
        }

        final File scriptFile = m_scriptPath.resolve(script).toFile();

        if (!ScriptUtil.isDescendantOf(m_scriptPath, scriptFile.toPath())) {
            throw new IOException("The location of the script must not be outside " + m_scriptPath + ".");
        }

        if (!scriptFile.canRead()) {
            throw new IllegalStateException("Cannot read script at '" + scriptFile + "'.");
        }

        return m_scriptCache.getCompiledScript(scriptFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OnmsNode act(final OnmsNode node, Map<String, Object> attributes) {
        try {
            final CompiledScript compiledScript = compileScript(getScript());
            if (compiledScript == null) {
                LOG.warn("No compiled script available for execution.");
            } else {
                // For the case where we can run script in transaction, at the end of scan.
                if (attributes.size() > 0 && attributes.get(RUN_IN_TRANSACTION) != null
                        && attributes.get(RUN_IN_TRANSACTION).equals(true)) {

                    // Run script in transaction.
                    return (OnmsNode) m_sessionUtils.withTransaction(() -> {
                        try {
                            // Fetch node object again from DB to make it attached.
                            if (node.getId() != null && node.getId() > 0) {
                                OnmsNode onmsNode = m_nodeDao.get(node.getId());
                                return runScript(compiledScript, onmsNode, attributes);
                            } else {
                                LOG.warn("Unexpected node {} which is not persisted yet", node);
                            }

                        } catch (ScriptException e) {
                            LOG.warn("Error applying ScriptPolicy.", e);
                        }
                        return node;
                    });
                } else {
                    runScript(compiledScript, node, attributes);
                }
            }
        } catch (ScriptException e) {
            LOG.warn("Error while compiling script.", e);
        } catch (IOException e) {
            LOG.warn("Error while opening script file {}.", m_script, e);
        } catch (Exception e) {
            LOG.warn("Unknown error while applying script.", e);
        }

        return node;
    }

    private OnmsNode runScript(CompiledScript compiledScript, OnmsNode node, Map<String, Object> attributes) throws ScriptException {
        final SimpleBindings globals = new SimpleBindings();
        globals.put("LOG", LOG);
        globals.put("node", node);
        globals.putAll(attributes);
        return (OnmsNode) compiledScript.eval(globals);
    }

    @Require(value = {})
    public String getScript() {
        return m_script;
    }

    public void setScript(String script) {
        m_script = script;
    }

    public String getType() {
        return getCriteria("type");
    }

    public void setType(String type) {
        putCriteria("type", type);
    }

    public String getSysObjectId() {
        return getCriteria("sysObjectId");
    }

    public void setSysObjectId(String sysObjectId) {
        putCriteria("sysObjectId", sysObjectId);
    }

    public String getSysName() {
        return getCriteria("sysName");
    }

    public void setSysName(String sysName) {
        putCriteria("sysName", sysName);
    }

    public String getSysDescription() {
        return getCriteria("sysDescription");
    }

    public void setSysDescription(String sysDescription) {
        putCriteria("sysDescription", sysDescription);
    }

    public String getSysLocation() {
        return getCriteria("sysLocation");
    }

    public void setSysLocation(String sysLocation) {
        putCriteria("sysLocation", sysLocation);
    }

    public String getSysContact() {
        return getCriteria("sysContact");
    }

    public void setSysContact(String sysContact) {
        putCriteria("sysContact", sysContact);
    }

    public String getLabel() {
        return getCriteria("label");
    }

    public void setLabel(String label) {
        putCriteria("label", label);
    }

    public String getLabelSource() {
        return getCriteria("labelSource");
    }

    public void setLabelSource(String labelSource) {
        putCriteria("labelSource", labelSource);
    }

    public String getNetBiosName() {
        return getCriteria("netBiosName");
    }

    public void setNetBiosName(String netBiosName) {
        putCriteria("netBiosName", netBiosName);
    }

    public String getNetBiosDomain() {
        return getCriteria("netBiosDomain");
    }

    public void setNetBiosDomain(String netBiosDomain) {
        putCriteria("netBiosDomain", netBiosDomain);
    }

    public String getOperatingSystem() {
        return getCriteria("operatingSystem");
    }

    public void setOperatingSystem(String operatingSystem) {
        putCriteria("operatingSystem", operatingSystem);
    }

    public String getForeignId() {
        return getCriteria("foreignId");
    }

    public void setForeignId(String foreignId) {
        putCriteria("foreignId", foreignId);
    }

    public String getForeignSource() {
        return getCriteria("foreignSource");
    }

    public void setForeignSource(String foreignSource) {
        putCriteria("foreignSource", foreignSource);
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        m_sessionUtils = sessionUtils;
    }

    public void setScriptCache(JSR223ScriptCache scriptCache) {
        m_scriptCache = scriptCache;
    }
}
