/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

/**
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
     * Since instances of this class are short lived (unlike BSFMonitor) we need to
     * make a static instance and synchronize access to it to
     * take advantage of caching script engines. If this becomes a bottleneck
     * perhaps a small pool of managers would help.
     */
    private static BSFManager s_bsfManager;
    static {
        LOG.debug("creating static BSFManager");
        s_bsfManager = new BSFManager();
    }
    
    private static synchronized int executeScript(String fileName, BSFNotificationStrategy obj){
        String lang = obj.getLangClass();
        String engine = obj.getBsfEngine();
        String runType = obj.getBsfRunType();
        String[] extensions = obj.getFileExtensions();

        LOG.info("Loading notification script from file '{}'", fileName);
        File scriptFile = new File(fileName);
        int ret = -1;
        try {

            if(lang==null) lang = BSFManager.getLangFromFilename(fileName);

            // Declare some beans that can be used inside the script                    
            HashMap<String,String> results = new HashMap<String,String>();
            s_bsfManager.declareBean("results", results, Map.class);
            declareBeans(obj);

            if(engine != null && lang != null && extensions != null && extensions.length > 0 ){
              //We register the scripting engine again no matter what since  
                //BSFManager doesn't let us know what engine is currently registered
                //for this language and it might not be the same as what we want. 
                LOG.debug("Registering scripting engine '{}' for '{}'", engine, lang);
                BSFManager.registerScriptingEngine(lang,engine,extensions);
            }

            if(scriptFile.exists() && scriptFile.canRead()){   
                String code = IOUtils.getStringFromReader(new InputStreamReader(new FileInputStream(scriptFile), "UTF-8"));

                // Check foot before firing
                obj.checkAberrantScriptBehaviors(code);

                // Execute the script
                if("eval".equals(runType)){
                    results.put("status", s_bsfManager.eval(lang, "BSFNotificationStrategy", 0, 0, code).toString());  
                }else if("exec".equals(runType)){
                    s_bsfManager.exec(lang, "BSFNotificationStrategy", 0, 0, code);
                }else{
                    LOG.warn("Invalid run-type parameter value '{}' for BSF notification script '{}'. Only 'eval' and 'exec' are supported.", runType, scriptFile);
                }

                // Check whether the script finished successfully
                if ("OK".equals(results.get("status"))) {
                    LOG.info("Execution succeeded and successful status passed back for script '{}'", scriptFile);
                    ret = 0;
                } else {
                    LOG.warn("Execution succeeded for script '{}', but script did not indicate successful notification by putting an entry into the 'results' bean with key 'status' and value 'OK'", scriptFile);
                    ret = -1;
                }
            } else {
                LOG.warn("Cannot locate or read BSF script file '{}'. Returning failure indication.", fileName);
                ret = -1;
            }
        } catch (BSFException e) {
            LOG.warn("Execution of script '{}' failed with BSFException: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        } catch (FileNotFoundException e){
            LOG.warn("Could not find BSF script file '{}'.", fileName);
            ret = -1;
        } catch (IOException e) {
            LOG.warn("Execution of script '{}' failed with IOException: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        } catch (Throwable e) {
            // Catch any RuntimeException throws
            LOG.warn("Execution of script '{}' failed with unexpected throwable: {}", scriptFile, e.getMessage(), e);
            ret = -1;
        } finally { 
            undeclareBean("results");
            undeclareBeans(obj);
        }
        LOG.debug("Finished running BSF script notification.");
        return ret;
    }
    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    @Override
    public int send(List<Argument> arguments) {
        m_arguments = arguments;
        String fileName = getFileName();
        int returnCode = executeScript(fileName, this);
        return returnCode;
    }

    private static void declareBeans(BSFNotificationStrategy obj) throws BSFException {
        NodeDao nodeDao = BeanUtils.getFactory("notifdContext", NodeDao.class);
        Integer nodeId;
        try {
            nodeId = Integer.valueOf(obj.m_notifParams.get(NotificationManager.PARAM_NODE));
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

        s_bsfManager.declareBean("bsf_notif_strategy", obj, BSFNotificationStrategy.class);
        
        obj.retrieveParams();
        s_bsfManager.declareBean("logger", LOG, Logger.class);
        s_bsfManager.declareBean("notif_params", obj.m_notifParams, Map.class);

        s_bsfManager.declareBean("node_label", nodeLabel, String.class);
        s_bsfManager.declareBean("foreign_source", foreignSource, String.class);
        s_bsfManager.declareBean("foreign_id", foreignId, String.class);
        s_bsfManager.declareBean("node_assets", assets, OnmsAssetRecord.class);
        s_bsfManager.declareBean("node_categories", categories, List.class);
        s_bsfManager.declareBean("node", node, OnmsNode.class);

        for (Argument arg : obj.m_arguments) {
            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) s_bsfManager.declareBean("text_message", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) s_bsfManager.declareBean("numeric_message", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NODE.equals(arg.getSwitch())) s_bsfManager.declareBean("node_id", arg.getValue(), String.class);
            if (NotificationManager.PARAM_INTERFACE.equals(arg.getSwitch())) s_bsfManager.declareBean("ip_addr", arg.getValue(), String.class);
            if (NotificationManager.PARAM_SERVICE.equals(arg.getSwitch())) s_bsfManager.declareBean("svc_name", arg.getValue(), String.class);
            if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) s_bsfManager.declareBean("subject", arg.getValue(), String.class);
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) s_bsfManager.declareBean("email", arg.getValue(), String.class);
            if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) s_bsfManager.declareBean("pager_email", arg.getValue(), String.class);
            if (NotificationManager.PARAM_XMPP_ADDRESS.equals(arg.getSwitch())) s_bsfManager.declareBean("xmpp_address", arg.getValue(), String.class);
            if (NotificationManager.PARAM_TEXT_PAGER_PIN.equals(arg.getSwitch())) s_bsfManager.declareBean("text_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_NUM_PAGER_PIN.equals(arg.getSwitch())) s_bsfManager.declareBean("numeric_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_WORK_PHONE.equals(arg.getSwitch())) s_bsfManager.declareBean("work_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_HOME_PHONE.equals(arg.getSwitch())) s_bsfManager.declareBean("home_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_MOBILE_PHONE.equals(arg.getSwitch())) s_bsfManager.declareBean("mobile_phone", arg.getValue(), String.class);
            if (NotificationManager.PARAM_TUI_PIN.equals(arg.getSwitch())) s_bsfManager.declareBean("phone_pin", arg.getValue(), String.class);
            if (NotificationManager.PARAM_MICROBLOG_USERNAME.equals(arg.getSwitch())) s_bsfManager.declareBean("microblog_username", arg.getValue(), String.class);
        }
    }
    
    private static void undeclareBean( String beanName){
        try{
            s_bsfManager.undeclareBean(beanName);
        }catch(BSFException e) {
            LOG.warn("Unable to undeclareBean '{}'", beanName);
        }
    }
    private static void undeclareBeans(BSFNotificationStrategy obj) {
        undeclareBean("logger");
        undeclareBean("bsf_notif_strategy");
        undeclareBean("notif_params");
        undeclareBean("node_label");
        undeclareBean("foreign_source");
        undeclareBean("foreign_id");
        undeclareBean("node_assets");
        undeclareBean("node_categories");
        undeclareBean("node");
        for (Argument arg : obj.m_arguments) {
            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) undeclareBean("text_message");
            if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) undeclareBean("numeric_message");
            if (NotificationManager.PARAM_NODE.equals(arg.getSwitch())) undeclareBean("node_id");
            if (NotificationManager.PARAM_INTERFACE.equals(arg.getSwitch())) undeclareBean("ip_addr");
            if (NotificationManager.PARAM_SERVICE.equals(arg.getSwitch())) undeclareBean("svc_name");
            if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) undeclareBean("subject");
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) undeclareBean("email");
            if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) undeclareBean("pager_email");
            if (NotificationManager.PARAM_XMPP_ADDRESS.equals(arg.getSwitch())) undeclareBean("xmpp_address");
            if (NotificationManager.PARAM_TEXT_PAGER_PIN.equals(arg.getSwitch())) undeclareBean("text_pin");
            if (NotificationManager.PARAM_NUM_PAGER_PIN.equals(arg.getSwitch())) undeclareBean("numeric_pin");
            if (NotificationManager.PARAM_WORK_PHONE.equals(arg.getSwitch())) undeclareBean("work_phone");
            if (NotificationManager.PARAM_HOME_PHONE.equals(arg.getSwitch())) undeclareBean("home_phone");
            if (NotificationManager.PARAM_MOBILE_PHONE.equals(arg.getSwitch())) undeclareBean("mobile_phone");
            if (NotificationManager.PARAM_TUI_PIN.equals(arg.getSwitch())) undeclareBean("phone_pin");
            if (NotificationManager.PARAM_MICROBLOG_USERNAME.equals(arg.getSwitch())) undeclareBean("microblog_username");
        }
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

    private void checkAberrantScriptBehaviors(String script) {
        if (script.matches("(?s)\\.exec\\s*\\(")) {
            // Here we should check for stupid stuff like use of System.exec()
            // and log stern warnings if found.
        }
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
