/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.bsf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.bsf.request.BSFRequest;
import org.opennms.netmgt.provision.detector.bsf.response.BSFResponse;
import org.opennms.netmgt.provision.support.Client;

/**
 * <p>BSFClient class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class BSFClient implements Client<BSFRequest, BSFResponse> {

    private String m_serviceName;
    private String m_fileName;
    private String m_langClass;
    private String m_bsfEngine;
    private String[] m_fileExtensions = {};
    private String m_runType = "eval";

    private HashMap<String,String> m_results;

    /**
     * <p>close</p>
     */
    @Override
    public void close() {
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        m_results = new HashMap<String,String>();
        BSFManager bsfManager = new BSFManager();
        File file = new File(m_fileName);
        Map<String,Object> map = getParametersMap();
        try {

            if (m_langClass == null) {
                m_langClass = BSFManager.getLangFromFilename(m_fileName);
            }
            if (m_bsfEngine != null && m_langClass != null && m_fileExtensions.length > 0 ) {
                BSFManager.registerScriptingEngine(m_langClass, m_bsfEngine, m_fileExtensions);
            }

            if (file.exists() && file.canRead()) {
                String code = IOUtils.getStringFromReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

                // Declare some beans that can be used inside the script
                bsfManager.declareBean("map", map, Map.class);
                bsfManager.declareBean("ip_addr", address.getHostAddress(), String.class);
                // TODO: I'm not sure how to deal with it on detectors. Is the node exists before running detectors? If so, I need NodeDao here.
                // bsfManager.declareBean("node_id",svc.getNodeId(),int.class );
                // bsfManager.declareBean("node_label", svc.getNodeLabel(), String.class);
                bsfManager.declareBean("svc_name", m_serviceName, String.class);
                bsfManager.declareBean("results", m_results, HashMap.class);

                for (final Entry<String, Object> entry : map.entrySet()) {
                    bsfManager.declareBean(entry.getKey(), entry.getValue(), String.class);
                }

                LogUtils.infof(this, "Executing %s for %s", m_langClass, file.getAbsoluteFile());
                if ("eval".equals(m_runType)) {
                    m_results.put("status", bsfManager.eval(m_langClass, "BSFDetector", 0, 0, code).toString());
                } else if ("exec".equals(m_runType)) {
                    bsfManager.exec(m_langClass, "BSFDetector", 0, 0, code);
                } else {
                    LogUtils.warnf(this, "Invalid run-type parameter value '%s' for service '%s'. Only 'eval' and 'exec' are supported.", m_runType, m_serviceName);
                    throw new RuntimeException("Invalid run-type '" + m_runType + "'");
                }

                if ("exec".equals(m_runType) && !m_results.containsKey("status")) {
                    LogUtils.warnf(this, "The exec script '%s' for service '%s' never put a 'status' entry in the 'results' bean. Exec scripts should put this entry with a value of 'OK' for up.", m_fileName, m_serviceName);
                }
            } else {
                LogUtils.warnf(this, "Cannot locate or read BSF script file '%s'. Marking service '%s' down.", m_fileName, m_serviceName);
            }

        } catch (BSFException e) {
            m_results.clear();
            LogUtils.warnf(this, e, "BSFDetector poll for service '%s' failed with BSFException: %s", m_serviceName, e.getMessage());
        } catch (FileNotFoundException e) {
            m_results.clear();
            LogUtils.warnf(this, "Could not find BSF script file '%s'. Marking service '%s' down.", m_fileName, m_serviceName);
        } catch (IOException e) {
            m_results.clear();
            LogUtils.warnf(this, e, "BSFDetector poll for service '%s' failed with IOException: %s", m_serviceName, e.getMessage());
        } catch (Throwable e) {
            m_results.clear();
            LogUtils.warnf(this, e, "BSFDetector poll for service '%s' failed with unexpected throwable: %s", m_serviceName, e.getMessage());
        } finally {
            bsfManager.terminate();
        }
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.bsf.response.BSFResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public BSFResponse receiveBanner() throws IOException, Exception {
        LogUtils.debugf(this, "Results: %s", m_results);
        final BSFResponse response = new BSFResponse(m_results);
        return response;
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.bsf.request.BSFRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.bsf.response.BSFResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public BSFResponse sendRequest(final BSFRequest request) throws IOException, Exception {
        return null;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public void setServiceName(String serviceName) {
        this.m_serviceName = serviceName;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String fileName) {
        this.m_fileName = fileName;
    }

    public String getLangClass() {
        return m_langClass;
    }

    public void setLangClass(String langClass) {
        this.m_langClass = langClass;
    }

    public String getBsfEngine() {
        return m_bsfEngine;
    }

    public void setBsfEngine(String bsfEngine) {
        this.m_bsfEngine = bsfEngine;
    }

    public String[] getFileExtensions() {
        return m_fileExtensions;
    }

    public void setFileExtensions(String[] fileExtensions) {
        this.m_fileExtensions = fileExtensions;
    }

    public String getRunType() {
        return m_runType;
    }

    public void setRunType(String runType) {
        this.m_runType = runType;
    }

    private Map<String,Object> getParametersMap() {
        final Map<String,Object> map = new HashMap<String,Object>();
        map.put("file-name", getFileName());
        map.put("lang-class", getLangClass());
        map.put("bsf-engine", getBsfEngine());
        map.put("file-extensions", getFileExtensions());
        map.put("run-type", getRunType());
        return Collections.unmodifiableMap(map);
    }

}
