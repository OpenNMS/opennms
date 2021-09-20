/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist.policies;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;

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
    private final ScriptEngineManager m_scriptManager = new ScriptEngineManager();
    private long m_lastCompiled = -1;
    private String m_script;
    private CompiledScript m_compiledScript;

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

    private CompiledScript compileScript(final String script) throws IOException, ScriptException {
        if (script == null) {
            throw new IllegalArgumentException("Script must not be null");
        }

        final File scriptFile = m_scriptPath.resolve(script).toFile();

        if (!scriptFile.canRead()) {
            throw new IllegalStateException("Cannot read script at '" + scriptFile + "'.");
        }

        if (scriptFile.lastModified() > m_lastCompiled) {
            final String fileExtension = Files.getFileExtension(scriptFile.getAbsolutePath());

            final ScriptEngine engine = m_scriptManager.getEngineByExtension(fileExtension);
            if (engine == null) {
                throw new IllegalStateException("No engine found for file extension: " + fileExtension);
            }

            if (!(engine instanceof Compilable)) {
                throw new IllegalStateException("Only engines that can compile scripts are supported.");
            }
            final Compilable compilable = (Compilable) engine;
            try (FileReader reader = new FileReader(scriptFile)) {
                m_compiledScript = compilable.compile(reader);
            }
            m_lastCompiled = scriptFile.lastModified();
        }

        return m_compiledScript;
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
            LOG.warn("Unkown error while applying script.", e);
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
}
