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
                            style="width: 100%">Deployment Guide</a></td>
                    <td style="border-top: none;">Learn how to deploy and configure the OpenNMS core, Minion, and Sentinel. The guide also covers how to set up a message broker, time series storage options,and setting up Jaeger tracing.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.com/horizon/<%=Vault.getProperty("version.display")%>/operation/overview/overview.html#overview"
                            target="_blank" class="btn btn-secondary" role="button"
                            style="width: 100%">Operation Guide</a></td>
                    <td style="border-top: none;">Learn how to configure OpenNMS to monitor your infrastructure and services.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.com/horizon/<%=Vault.getProperty("version.display")%>/development/overview/overview.html#overview"
                            target="_blank" class="btn btn-secondary" role="button"
                            style="width: 100%">Development Guide</a></td>
                    <td style="border-top: none;">For developers who want to extend and improve the OpenNMS platform and use the ReST APIs for integration. Learn how to set up a development environment and get started with writing features for OpenNMS.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="api/v2/openapi.json" target="_blank"
                            class="btn btn-secondary" role="button" style="width: 100%">OpenAPI Docs</a></td>
                    <td style="border-top: none;">The OpenAPI docs explain how each OpenNMS RESTful API works. You can also test and generate client code from them.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="api/v2/api-docs/?url=/opennms/api/v2/openapi.json#/" target="_blank"
                            class="btn btn-secondary" role="button" style="width: 100%">Swagger UI</a></td>
                    <td style="border-top: none;">A graphical user interface for the OpenNMS OpenAPI docs.</td>
                </tr>
            </table>
        </span>
    </div>
</div>
