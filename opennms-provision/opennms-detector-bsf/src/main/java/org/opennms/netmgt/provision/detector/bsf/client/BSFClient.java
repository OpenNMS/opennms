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
package org.opennms.netmgt.provision.detector.bsf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFManagerTerminator;
import org.apache.bsf.util.IOUtils;
import org.opennms.netmgt.provision.detector.bsf.request.BSFRequest;
import org.opennms.netmgt.provision.detector.bsf.response.BSFResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BSFClient class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class BSFClient implements Client<BSFRequest, BSFResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(BSFClient.class);

    private String m_serviceName;
    private String m_fileName;
    private String m_langClass;
    private String m_bsfEngine;
    private String[] m_fileExtensions = {};
    private String m_runType = "eval";

    private Map<String,String> m_results = new HashMap<String,String>();

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
                String code = IOUtils.getStringFromReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

                // Declare some beans that can be used inside the script
                bsfManager.declareBean("map", map, Map.class);
                bsfManager.declareBean("ip_addr", address.getHostAddress(), String.class);
                // TODO: I'm not sure how to deal with it on detectors. Is the node exists before running detectors? If so, I need NodeDao here.
                // bsfManager.declareBean("node_id",svc.getNodeId(),int.class );
                // bsfManager.declareBean("node_label", svc.getNodeLabel(), String.class);
                bsfManager.declareBean("svc_name", m_serviceName, String.class);
                bsfManager.declareBean("results", m_results, Map.class);
                bsfManager.declareBean("port", port, Integer.class);
                bsfManager.declareBean("timeout", timeout, Integer.class);

                for (final Entry<String, Object> entry : map.entrySet()) {
                    bsfManager.declareBean(entry.getKey(), entry.getValue(), String.class);
                }

                LOG.info("Executing {} for {}", m_langClass, file.getAbsoluteFile());
                if ("eval".equals(m_runType)) {
                    m_results.put("status", bsfManager.eval(m_langClass, "BSFDetector", 0, 0, code).toString());
                } else if ("exec".equals(m_runType)) {
                    bsfManager.exec(m_langClass, "BSFDetector", 0, 0, code);
                } else {
                    LOG.warn("Invalid run-type parameter value '{}' for service '{}'. Only 'eval' and 'exec' are supported.", m_runType, m_serviceName);
                    throw new RuntimeException("Invalid run-type '" + m_runType + "'");
                }

                if ("exec".equals(m_runType) && !m_results.containsKey("status")) {
                    LOG.warn("The exec script '{}' for service '{}' never put a 'status' entry in the 'results' bean. Exec scripts should put this entry with a value of 'OK' for up.", m_fileName, m_serviceName);
                }
            } else {
                LOG.warn("Cannot locate or read BSF script file '{}'. Marking service '{}' down.", m_fileName, m_serviceName);
            }

        } catch (BSFException e) {
            m_results.clear();
            LOG.warn("BSFDetector poll for service '{}' failed with BSFException: {}", m_serviceName, e.getMessage(), e);
        } catch (FileNotFoundException e) {
            m_results.clear();
            LOG.warn("Could not find BSF script file '{}'. Marking service '{}' down.", m_fileName, m_serviceName);
        } catch (IOException e) {
            m_results.clear();
            LOG.warn("BSFDetector poll for service '{}' failed with IOException: {}", m_serviceName, e.getMessage(), e);
        } catch (Throwable e) {
            m_results.clear();
            LOG.warn("BSFDetector poll for service '{}' failed with unexpected throwable: {}", m_serviceName, e.getMessage(), e);
        } finally {
            BSFManagerTerminator.terminate(bsfManager);
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
        LOG.debug("Results: {}", m_results);
        return new BSFResponse(m_results);
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
        this.m_fileExtensions = Arrays.copyOf(fileExtensions, fileExtensions.length);
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
