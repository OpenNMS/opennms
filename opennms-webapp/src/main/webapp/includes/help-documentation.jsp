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

<script type="text/javascript">
    // Shorthand for 'onload'
    $(function () {
        // Check to see if the URL provided by the opennms-docs package is present on the system.
        // If so, display the links to the offline docs symlinked from /usr/share/doc/opennms-${version}.
        $.ajax({
            url: 'docs/guide-install/index.html',
            method: 'HEAD',
            error: function () {
                $('#online-documentation').css("display", "inline-block");
            },
            success: function () {
                $('#offline-documentation').css("display", "inline-block");
            }
        });
    });
</script>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Documentation</h3>
    </div>
    <div class="panel-body">
        <span id="online-documentation" style="display:none;">
            <table class="table">
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.org/opennms/releases/<%=Vault.getProperty("version.display")%>/guide-install/guide-install.html"
                            target="_blank" class="btn btn-default" role="button"
                            style="width: 100%">Installation Guide</a></td>
                    <td style="border-top: none;">OpenNMS can be installed several operating systems and can be deployed for several scenarios with different technologies. Have a look in the Installation Guide to find instructions to deploy and maintain your OpenNMS instance.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.org/opennms/releases/<%=Vault.getProperty("version.display")%>/guide-admin/guide-admin.html"
                            target="_blank" class="btn btn-default" role="button"
                            style="width: 100%">Admin Guide</a></td>
                    <td style="border-top: none;">Have a look into the Admin Guide to find instructions how to configure OpenNMS to monitor your infrastructure and services.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.org/opennms/releases/<%=Vault.getProperty("version.display")%>/guide-user/guide-user.html"
                            target="_blank" class="btn btn-default" role="button"
                            style="width: 100%">Users Guide</a></td>
                    <td style="border-top: none;">OpenNMS users tend to have a broad network monitoring skill set. This User Guide contains an overview of concepts and explains how to use OpenNMS for day-to-day monitoring.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://docs.opennms.org/opennms/releases/<%=Vault.getProperty("version.display")%>/guide-development/guide-development.html"
                            target="_blank" class="btn btn-default" role="button"
                            style="width: 100%">Developers Guide</a></td>
                    <td style="border-top: none;">Developers can extend and improve the OpenNMS platform. The Developers Guide is a good starting point for extending OpenNMS and using the ReST APIs for integration.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="https://wiki.opennms.org" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">OpenNMS Wiki</a></td>
                    <td style="border-top: none;">With the large variety of devices and applications you can monitor with OpenNMS, the Wiki provides space to share experience with How Tos and Tutorials to address specific use cases.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://wiki.opennms.org/wiki/Community/Welcome_Guide" target="_blank"
                            class="btn btn-default" role="button" style="width: 100%">Welcome Guide</a></td>
                    <td style="border-top: none;">If you are new in the project, you can find useful information in your Welcome Guide to get anything you need to get started.</td>
                </tr>
            </table>
        </span>
        <span id="offline-documentation" style="display:none;">
            <table class="table">
                <tr>
                    <td style="border-top: none;"><a href="docs/guide-install/index.html" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">Installation Guide</a></td>
                    <td style="border-top: none;">OpenNMS can be installed several operating systems and can be deployed for several scenarios with different technologies. Have a look in the Installation Guide to find instructions to deploy and maintain your OpenNMS instance.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="docs/guide-admin/index.html" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">Admin Guide</a></td>
                    <td style="border-top: none;">Have a look into the Admin Guide to find instructions how to configure OpenNMS to monitor your infrastructure and services.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="docs/guide-user/index.html" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">Users Guide</a></td>
                    <td style="border-top: none;">OpenNMS users tend to have a broad network monitoring skill set. This User Guide contains an overview of concepts and explains how to use OpenNMS for day-to-day monitoring.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="docs/guide-development/index.html" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">Developers Guide</a></td>
                    <td style="border-top: none;">Developers can extend and improve the OpenNMS platform. The Developers Guide is a good starting point for extending OpenNMS and using the ReST APIs for integration.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a href="https://wiki.opennms.org" target="_blank"
                                                     class="btn btn-default" role="button" style="width: 100%">OpenNMS Wiki</a></td>
                    <td style="border-top: none;">With the large variety of devices and applications you can monitor with OpenNMS, the Wiki provides space to share experience with How Tos and Tutorials to address specific use cases.</td>
                </tr>
                <tr>
                    <td style="border-top: none;"><a
                            href="https://wiki.opennms.org/wiki/Community/Welcome_Guide" target="_blank"
                            class="btn btn-default" role="button" style="width: 100%">Welcome Guide</a></td>
                    <td style="border-top: none;">If you are new in the project, you can find useful information in your Welcome Guide to get anything you need to get started.</td>
                </tr>
            </table>
        </span>
    </div>
</div>
