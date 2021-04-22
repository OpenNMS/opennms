<%@ page import="org.opennms.core.resource.Vault" %><%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2018 The OpenNMS Group, Inc.
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
--%>

<div class="card">
    <div class="card-header">
        <span>Documentation</span>
    </div>
    <div class="card-body">
        <span id="online-documentation">
            <table class="table">
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.com/horizon/<%=Vault.getProperty("version.display")%>/deployment/core/introduction.html"
                            target="_blank" class="btn btn-secondary" role="button"
                            style="width: 100%">Installation Guide</a></td>
                    <td style="border-top: none;">OpenNMS can be installed several operating systems and can be deployed for several scenarios with different technologies. Have a look in the Installation Guide to find instructions to deploy and maintain your OpenNMS instance.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.com/horizon/<%=Vault.getProperty("version.display")%>/operation/overview/overview.html#overview"
                            target="_blank" class="btn btn-secondary" role="button"
                            style="width: 100%">Admin Guide</a></td>
                    <td style="border-top: none;">Have a look into the Admin Guide to find instructions how to configure OpenNMS to monitor your infrastructure and services.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.com/horizon/<%=Vault.getProperty("version.display")%>/development/overview/overview.html#overview"
                            target="_blank" class="btn btn-secondary" role="button"
                            style="width: 100%">Developers Guide</a></td>
                    <td style="border-top: none;">Developers can extend and improve the OpenNMS platform. The Developers Guide is a good starting point for extending OpenNMS and using the ReST APIs for integration.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="https://wiki.opennms.org" target="_blank"
                                                     class="btn btn-secondary" role="button" style="width: 100%">OpenNMS Wiki</a></td>
                    <td style="border-top: none;">With the large variety of devices and applications you can monitor with OpenNMS, the Wiki provides space to share experience with How Tos and Tutorials to address specific use cases.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://opennms.discourse.group/t/community-welcome-guide/560" target="_blank"
                            class="btn btn-secondary" role="button" style="width: 100%">Welcome Guide</a></td>
                    <td style="border-top: none;">If you are new in the project, you can find useful information in your Welcome Guide to get anything you need to get started.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="api/v2/openapi.json" target="_blank"
                            class="btn btn-secondary" role="button" style="width: 100%">OpenAPI doc</a></td>
                    <td style="border-top: none;">With OpenAPI doc, you can know how each OpenNMS RESTful API works. You can also test the APIs and generate client code from the doc.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="api/v2/api-docs/?url=/opennms/api/v2/openapi.json#/" target="_blank"
                            class="btn btn-secondary" role="button" style="width: 100%">Swagger UI</a></td>
                    <td style="border-top: none;">This is a graph interface of OpenNMS OpenAPI doc.</td>
                </tr>
            </table>
        </span>
    </div>
</div>
