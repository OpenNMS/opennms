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
package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.opennms.core.spring.BeanFactoryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

/**
 * Runs a script as a notification command via JSR-223 ({@code javax.script}).
 *
 * <p>This strategy originally ran on the (now retired) Apache Bean Scripting
 * Framework; the class name and every script variable name are retained so
 * existing notificationCommands.xml entries and scripts keep working. Two
 * things from the BSF era no longer exist: the implicit {@code bsf} helper
 * object BSF injected into engines, and the {@code bsf-engine} /
 * {@code file-extensions} switches (ignored with a warning). Script engines
 * are discovered from the classpath; BeanShell and Groovy ship with
 * OpenNMS.</p>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="mailto:dschlenk@converge-one.com</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 *
 */
public class BSFNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(BSFNotificationStrategy.class);

    private List<Argument> m_arguments;
    private Map<String,String> m_notifParams = new HashMap<String,String>();

    /*
     * Instances of this class are short lived (one per notification), so the
     * engine manager and the compiled-script cache are static. Caching the
     * compiled form matters for Groovy, which leaks a class per compilation
     * (see JSR223ScriptCache in opennms-provision-persistence, which this
     * mirrors).
     */
    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();

    private static final ConcurrentHashMap<String, ScriptState> SCRIPT_CACHE = new ConcurrentHashMap<>();

    private static class ScriptState {
        private final Object lock = new Object();
        private long lastCompiled = -1;
        private CompiledScript compiled;
        private boolean compileUnsupported;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    @Override
    public int send(List<Argument> arguments) {
        m_arguments = arguments;
        String fileName = getFileName();
        return executeScript(fileName);
    }

    private int executeScript(String fileName) {
        warnAboutDeprecatedSwitches();

        LOG.info("Loading notification script from file '{}'", fileName);
        if (fileName == null) {
            LOG.warn("No 'file-name' argument supplied for the BSF notification command. Returning failure indication.");
            return -1;
        }
        File scriptFile = new File(fileName);
        int ret = -1;
        try {
            if (!scriptFile.exists() || !scriptFile.canRead()) {
                LOG.warn("Cannot locate or read script file '{}'. Returning failure indication.", fileName);
                return -1;
            }

            final ScriptEngine engine = resolveEngine(getLangClass(), fileName);
            if (engine == null) {
                LOG.error("No JSR-223 script engine found for script '{}' (lang-class '{}'). BeanShell and Groovy are available by default; other engines must be on the classpath.",
                        fileName, getLangClass());
                return -1;
            }

            String runType = getBsfRunType();
            if (!"eval".equals(runType) && !"exec".equals(runType)) {
                LOG.warn("Invalid run-type parameter value '{}' for BSF notification script '{}'. Only 'eval' and 'exec' are supported.", runType, scriptFile);
                return -1;
            }

            // Variables the script can use
            HashMap<String,String> results = new HashMap<String,String>();
            Bindings bindings = buildBindings(results);
            ScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            // Execute the script
            Object returnValue;
            CompiledScript compiled = getOrCompile(engine, scriptFile);
            if (compiled != null) {
                returnValue = compiled.eval(context);
            } else {
                returnValue = engine.eval(Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8), context);
            }
            if ("eval".equals(runType)) {
                results.put("status", String.valueOf(returnValue));
            }

            // Check whether the script finished successfully
            if ("OK".equals(results.get("status"))) {
                LOG.info("Execution succeeded and successful status passed back for script '{}'", scriptFile);
                ret = 0;
            } else {
                LOG.warn("Execution succeeded for script '{}', but script did not indicate successful notification by putting an entry into the 'results' bean with key 'status' and value 'OK'", scriptFile);
                ret = -1;
            }
        } catch (ScriptException e) {
            LOG.warn("Execution of script '{}' failed with ScriptException: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        } catch (IOException e) {
            LOG.warn("Execution of script '{}' failed with IOException: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        } catch (Throwable e) {
            // Catch any RuntimeException throws
            LOG.warn("Execution of script '{}' failed with unexpected throwable: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        }
        LOG.debug("Finished running BSF script notification.");
        return ret;
    }

    /**
     * Resolution order: an explicit lang-class is looked up as a JSR-223
     * engine name (the BSF names "beanshell" and "groovy" match), then as an
     * extension; otherwise the file extension decides, with BSF's ".gy" alias
     * mapped to Groovy since the Groovy engine does not register it.
     */
    private static ScriptEngine resolveEngine(String langClass, String fileName) {
        if (langClass != null) {
            ScriptEngine engine = MANAGER.getEngineByName(langClass);
            if (engine == null) {
                engine = MANAGER.getEngineByExtension(langClass);
            }
            return engine;
        }
        final int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        String extension = fileName.substring(dot + 1);
        if ("gy".equals(extension)) {
            extension = "groovy";
        }
        ScriptEngine engine = MANAGER.getEngineByExtension(extension);
        if (engine == null) {
            engine = MANAGER.getEngineByName(extension);
        }
        return engine;
    }

    /**
     * Returns the cached compiled form of the script, recompiling when the
     * file changes, or null when the engine cannot compile (the script is
     * then evaluated from source with the per-invocation engine, which keeps
     * that path free of shared state).
     */
    private static CompiledScript getOrCompile(ScriptEngine engine, File scriptFile) throws IOException {
        final String key = scriptFile.getAbsolutePath() + "|" + engine.getFactory().getEngineName();
        final ScriptState state = SCRIPT_CACHE.computeIfAbsent(key, k -> new ScriptState());
        synchronized (state.lock) {
            if (state.compileUnsupported) {
                return null;
            }
            final long lastModified = scriptFile.lastModified();
            if (lastModified > state.lastCompiled) {
                if (!(engine instanceof Compilable)) {
                    state.compileUnsupported = true;
                    return null;
                }
                try {
                    state.compiled = ((Compilable) engine).compile(Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8));
                    state.lastCompiled = lastModified;
                } catch (Throwable t) {
                    // must catch Throwable: BeanShell declares Compilable but its
                    // compile() throws java.lang.Error("unimplemented")
                    LOG.debug("Script engine '{}' cannot compile '{}' ({}); evaluating from source instead",
                            engine.getFactory().getEngineName(), scriptFile, t.toString());
                    state.compileUnsupported = true;
                    return null;
                }
            }
            return state.compiled;
        }
    }

    private void warnAboutDeprecatedSwitches() {
        if (getBsfEngine() != null || getFileExtensions() != null) {
            LOG.warn("The 'bsf-engine' and 'file-extensions' switches are no longer supported now that the BSF notification strategy runs on JSR-223; they are ignored. Use 'lang-class' or the script file extension to select the engine.");
        }
    }

    private Bindings buildBindings(Map<String,String> results) {
        // Retrieve the parameters before accessing them
        retrieveParams();

        Integer nodeId;
        try {
            nodeId = Integer.valueOf(m_notifParams.get(NotificationManager.PARAM_NODE));
        } catch (NumberFormatException nfe) {
            nodeId = null;
        }

        OnmsNode node = null;
        OnmsAssetRecord assets = null;
        final List<String> categories = new ArrayList<>();
        String nodeLabel = null;
        String foreignSource = null;
        String foreignId = null;

        if (nodeId != null) {
            final BeanFactoryReference bf = BeanUtils.getBeanFactory("notifdContext");
            final NodeDao nodeDao = BeanUtils.getBean(bf, "nodeDao", NodeDao.class);
            final TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate", TransactionTemplate.class);

            try {
                // Redeclare the node id as final
                final int theNodeId = nodeId;
                node = transTemplate.execute(new TransactionCallback<OnmsNode>() {
                    @Override
                    public OnmsNode doInTransaction(final TransactionStatus status) {
                        final OnmsNode node = nodeDao.get(theNodeId);
                        // Retrieve the categories in the context of the transaction
                        if (node != null) {
                            for (OnmsCategory cat : node.getCategories()) {
                                categories.add(cat.getName());
                            }
                        }
                        return  node;
                    }
                });

                if (node == null) {
                    LOG.error("Could not find a node with id: {}", theNodeId);
                } else {
                    nodeLabel = node.getLabel();
                    assets = node.getAssetRecord();
                    foreignSource = node.getForeignSource();
                    foreignId = node.getForeignId();
                }
            } catch (final RuntimeException e) {
                LOG.error("Error while retrieving node with id {}", nodeId, e);
            }
        }

        Bindings bindings = new SimpleBindings();
        bindings.put("results", results);
        bindings.put("bsf_notif_strategy", this);

        bindings.put("logger", LOG);
        bindings.put("notif_params", m_notifParams);

        bindings.put("node_label", nodeLabel);
        bindings.put("foreign_source", foreignSource);
        bindings.put("foreign_id", foreignId);
        bindings.put("node_assets", assets);
        bindings.put("node_categories", categories);
        bindings.put("node", node);

        for (Argument arg : m_arguments) {
            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) bindings.put("text_message", arg.getValue());
            if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) bindings.put("numeric_message", arg.getValue());
            if (NotificationManager.PARAM_NODE.equals(arg.getSwitch())) bindings.put("node_id", arg.getValue());
            if (NotificationManager.PARAM_INTERFACE.equals(arg.getSwitch())) bindings.put("ip_addr", arg.getValue());
            if (NotificationManager.PARAM_SERVICE.equals(arg.getSwitch())) bindings.put("svc_name", arg.getValue());
            if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) bindings.put("subject", arg.getValue());
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) bindings.put("email", arg.getValue());
            if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) bindings.put("pager_email", arg.getValue());
            if (NotificationManager.PARAM_TEXT_PAGER_PIN.equals(arg.getSwitch())) bindings.put("text_pin", arg.getValue());
            if (NotificationManager.PARAM_NUM_PAGER_PIN.equals(arg.getSwitch())) bindings.put("numeric_pin", arg.getValue());
            if (NotificationManager.PARAM_WORK_PHONE.equals(arg.getSwitch())) bindings.put("work_phone", arg.getValue());
            if (NotificationManager.PARAM_HOME_PHONE.equals(arg.getSwitch())) bindings.put("home_phone", arg.getValue());
            if (NotificationManager.PARAM_MOBILE_PHONE.equals(arg.getSwitch())) bindings.put("mobile_phone", arg.getValue());
            if (NotificationManager.PARAM_TUI_PIN.equals(arg.getSwitch())) bindings.put("phone_pin", arg.getValue());
        }

        return bindings;
    }

    private String getSwitchValue(String argSwitch) {
        String value = null;
        for (Argument arg : m_arguments) {
            if (arg.getSwitch().equals(argSwitch)) {
                value = arg.getValue();
            }
        }
        if (value != null && value.equals("")) value = null;

        return value;
    }

    @SuppressWarnings("unused")
    private String getSwitchSubstitution(String argSwitch) {
        String value = null;
        for (Argument arg : m_arguments) {
            if (arg.getSwitch().equals(argSwitch)) {
                value = arg.getSubstitution();
            }
        }
        if (value != null && value.equals("")) value = null;

        return value;
    }

    private String getFileName() {
        return getSwitchValue("file-name");
    }

    private String getLangClass() {
        return getSwitchValue("lang-class");
    }

    private String getBsfEngine() {
        return getSwitchValue("bsf-engine");
    }

    private String[] getFileExtensions() {
        String exts = getSwitchValue("file-extensions");
        if (exts == null) return null;
        return exts.split(",");
    }

    private String getBsfRunType() {
        String runType = getSwitchValue("run-type");
        if(runType == null){
            runType = "exec";
        }
        return runType;
    }

    private void retrieveParams() {
        for (Argument arg : m_arguments) {
            m_notifParams.put(arg.getSwitch(), arg.getValue());
        }
    }

    public void log(String level, String format, Object... args) {
        if ("TRACE".equals(level)) LOG.trace(format, args);
        if ("DEBUG".equals(level)) LOG.debug(format, args);
        if ("INFO".equals(level)) LOG.info(format, args);
        if ("WARN".equals(level)) LOG.warn(format, args);
        if ("ERROR".equals(level)) LOG.error(format, args);
        if ("FATAL".equals(level)) LOG.error(format, args);
    }
}
