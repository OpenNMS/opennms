/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

/**
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 *
 */
public class BSFNotificationStrategy implements NotificationStrategy {

    private List<Argument> m_arguments;
    private Map<String,String> m_notifParams = new HashMap<String,String>();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    @Override
    public int send(List<Argument> arguments) {
        m_arguments = arguments;
        String fileName = getFileName();
        String lang = getLangClass();
        String engine = getBsfEngine();
        String extensions[] = getFileExtensions();

        LogUtils.infof(this, "Loading notification script from file '%s'", fileName);
        File scriptFile = new File(fileName);
        BSFManager bsfManager = new BSFManager();
        int returnCode = -1;

        try {

            if(lang==null) lang = BSFManager.getLangFromFilename(fileName);

            // Declare some beans that can be used inside the script                    
            HashMap<String,String> results = new HashMap<String,String>();
            bsfManager.declareBean("results", results, HashMap.class);
            declareBeans(bsfManager);

            if(engine != null && lang != null && extensions != null && extensions.length > 0 ){
                BSFManager.registerScriptingEngine(lang, engine, extensions);
            }

            if(scriptFile.exists() && scriptFile.canRead()){   
                String code = IOUtils.getStringFromReader(new InputStreamReader(new FileInputStream(scriptFile), "UTF-8"));

                // Check foot before firing
                checkAberrantScriptBehaviors(code);

                // Execute the script
                bsfManager.exec(lang, "BSFNotificationStrategy", 0, 0, code);

                // Check whether the script finished successfully
                if ("OK".equals(results.get("status"))) {
                    LogUtils.infof(this, "Execution succeeded and successful status passed back for script '%s'", scriptFile);
                    returnCode = 0;
                } else {
                    LogUtils.warnf(this, "Execution succeeded for script '%s', but script did not indicate successful notification by putting an entry into the 'results' bean with key 'status' and value 'OK'", scriptFile);
                    returnCode = -1;
                }
            } else {
                LogUtils.warnf(this, "Cannot locate or read BSF script file '%s'. Returning failure indication.", fileName);
                returnCode = -1;
            }
        } catch (BSFException e) {
            LogUtils.warnf(this, e, "Execution of script '%s' failed with BSFException: %s", scriptFile, e.getMessage());
            returnCode = -1;
        } catch (FileNotFoundException e){
            LogUtils.warnf(this, "Could not find BSF script file '%s'.", fileName);
            returnCode = -1;
        } catch (IOException e) {
            LogUtils.warnf(this, e, "Execution of script '%s' failed with IOException: %s", scriptFile, e.getMessage());
            returnCode = -1;
        } catch (Throwable e) {
            // Catch any RuntimeException throws
            LogUtils.warnf(this, e, "Execution of script '%s' failed with unexpected throwable: %s", scriptFile, e.getMessage());
            returnCode = -1;
        } finally { 
            bsfManager.terminate();
        }

        return returnCode;
    }

    private void declareBeans(BSFManager bsfManager) throws BSFException {
        NodeDao nodeDao = Notifd.getInstance().getNodeDao();
        Integer nodeId;
        try {
            nodeId = Integer.valueOf(m_notifParams.get(NotificationManager.PARAM_NODE));
        } catch (NumberFormatException nfe) {
            nodeId = null;
        }

        OnmsNode node = null;
        OnmsAssetRecord assets = null;
        List<String> categories = new ArrayList<String>();
        String nodeLabel = null;
        String foreignSource = null;
        String foreignId = null;

        if (nodeId != null) {
            node = nodeDao.get(nodeId);
            nodeLabel = node.getLabel();
            assets = node.getAssetRecord();
            for (OnmsCategory cat : node.getCategories()) {
                categories.add(cat.getName());
            }
            foreignSource = node.getForeignSource();
            foreignId = node.getForeignId();
        }

        bsfManager.declareBean("bsf_notif_strategy", this, BSFNotificationStrategy.class);
        
        retrieveParams();
        bsfManager.declareBean("notif_params", m_notifParams, Map.class);

        bsfManager.declareBean("node_label", nodeLabel, String.class);
        bsfManager.declareBean("foreign_source", foreignSource, String.class);
        bsfManager.declareBean("foreign_id", foreignId, String.class);
        bsfManager.declareBean("node_assets", assets, OnmsAssetRecord.class);
        bsfManager.declareBean("node_categories", categories, List.class);
        bsfManager.declareBean("node", node, OnmsNode.class);

        for (Argument arg : m_arguments) {
            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) bsfManager.declareBean("text_message", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) bsfManager.declareBean("numeric_message", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NODE.equals(arg.getSwitch())) bsfManager.declareBean("node_id", arg.getValue(), String.class);
            if (NotificationManager.PARAM_INTERFACE.equals(arg.getSwitch())) bsfManager.declareBean("ip_addr", arg.getValue(), String.class);
            if (NotificationManager.PARAM_SERVICE.equals(arg.getSwitch())) bsfManager.declareBean("svc_name", arg.getValue(), String.class);
            if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) bsfManager.declareBean("subject", arg.getValue(), String.class);
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) bsfManager.declareBean("email", arg.getValue(), String.class);
            if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) bsfManager.declareBean("pager_email", arg.getValue(), String.class);
            if (NotificationManager.PARAM_XMPP_ADDRESS.equals(arg.getSwitch())) bsfManager.declareBean("xmpp_address", arg.getValue(), String.class);
            if (NotificationManager.PARAM_TEXT_PAGER_PIN.equals(arg.getSwitch())) bsfManager.declareBean("text_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NUM_PAGER_PIN.equals(arg.getSwitch())) bsfManager.declareBean("numeric_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_WORK_PHONE.equals(arg.getSwitch())) bsfManager.declareBean("work_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_HOME_PHONE.equals(arg.getSwitch())) bsfManager.declareBean("home_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_MOBILE_PHONE.equals(arg.getSwitch())) bsfManager.declareBean("mobile_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_TUI_PIN.equals(arg.getSwitch())) bsfManager.declareBean("phone_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_MICROBLOG_USERNAME.equals(arg.getSwitch())) bsfManager.declareBean("microblog_username", arg.getValue(), String.class);
        }
    }

    @SuppressWarnings("unused")
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

    private void checkAberrantScriptBehaviors(String script) {
        if (script.matches("(?s)\\.exec\\s*\\(")) {
            // Here we should check for stupid stuff like use of System.exec()
            // and log stern warnings if found.
        }
    }

    private String getFileName() {
        return getSwitchSubstitution("file-name");
    }

    private String getLangClass() {
        return getSwitchSubstitution("lang-class");
    }

    private String getBsfEngine() {
        return getSwitchSubstitution("bsf-engine");
    }

    private String[] getFileExtensions() {
        String exts = getSwitchSubstitution("file-extensions");
        if (exts == null) return null;
        return exts.split(",");
    }

    private void retrieveParams() {
        for (Argument arg : m_arguments) {
            m_notifParams.put(arg.getSwitch(), arg.getValue()); 
        }
    }

    public void log(String level, String format, Object... args) {
        if ("TRACE".equals(level)) LogUtils.tracef(this, format, args);
        if ("DEBUG".equals(level)) LogUtils.debugf(this, format, args);
        if ("INFO".equals(level)) LogUtils.infof(this, format, args);
        if ("WARN".equals(level)) LogUtils.warnf(this, format, args);
        if ("ERROR".equals(level)) LogUtils.errorf(this, format, args);
        if ("FATAL".equals(level)) LogUtils.errorf(this, format, args);
    }
}
