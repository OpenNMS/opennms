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
package org.opennms.netmgt.provision.detector.bsf;

import org.opennms.netmgt.provision.detector.bsf.client.BSFClient;
import org.opennms.netmgt.provision.detector.bsf.request.BSFRequest;
import org.opennms.netmgt.provision.detector.bsf.response.BSFResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;


/**
 * <p>BSFDetector class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */

public class BSFDetector extends BasicDetector<BSFRequest, BSFResponse> {

    private String m_fileName;
    private String m_langClass;
    private String m_bsfEngine;
    private String m_fileExtensions = "";
    private String m_runType = "eval";

    /**
     * <p>Constructor for BsfDetector.</p>
     */
    public BSFDetector() {
        super("BSF", 0);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<BSFRequest, BSFResponse> getClient() {
        final BSFClient client = new BSFClient();
        client.setServiceName(getServiceName());
        client.setFileName(getFileName());
        client.setLangClass(getLangClass());
        client.setBsfEngine(getBsfEngine());
        client.setFileExtensions(getFileExtensions().split(","));
        client.setRunType(getRunType());
        return client;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(responseMatches("OK"));
    }

    private static ResponseValidator<BSFResponse> responseMatches(final String banner) {
        return new ResponseValidator<BSFResponse>(){

            @Override
            public boolean validate(final BSFResponse response) {
                return response.validate(banner);
            }

        };
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

    public String getFileExtensions() {
        return m_fileExtensions;
    }

    public void setFileExtensions(String fileExtensions) {
        this.m_fileExtensions = fileExtensions;
    }

    public String getRunType() {
        return m_runType;
    }

    public void setRunType(String runType) {
        this.m_runType = runType;
    }

}
