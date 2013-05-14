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

package org.opennms.netmgt.provision.detector.bsf;

import org.opennms.netmgt.provision.detector.bsf.client.BSFClient;
import org.opennms.netmgt.provision.detector.bsf.request.BSFRequest;
import org.opennms.netmgt.provision.detector.bsf.response.BSFResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>BSFDetector class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
@Scope("prototype")
public class BSFDetector extends BasicDetector<BSFRequest, BSFResponse> {

    private String m_fileName;
    private String m_langClass;
    private String m_bsfEngine;
    private String m_fileExtensions = "";
    private String m_runType = "eval";

    /**
     * <p>Constructor for BsfDetector.</p>
     */
    protected BSFDetector() {
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
