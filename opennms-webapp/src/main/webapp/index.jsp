<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

--%>

<%@page language="java" contentType="text/html" session="true"  %>
<%@taglib tagdir="/WEB-INF/tags/ui" prefix="ui" %>
<%@ taglib tagdir="/WEB-INF/tags/ui/layout" prefix="layout" %>
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Web Console" />
</jsp:include>
    
    <!-- Top Level Row -->
    <layout:row>
        
        <!-- Entire view -->
        <layout:column columnType="twelve">
            
            <layout:row>
                <!-- Left Column -->
                <layout:column columnType="three">
                    
                    <layout:row>
                        <layout:column columnType="twelve">
                            <ui:panel title="Nodes with Outages" showHeader="true" link="outage/list.htm">
                                <jsp:include page="/outage/servicesdown-box.htm" flush="false" />
                            </ui:panel>
                        </layout:column>
	                </layout:row>
	                
	                <layout:row>
	                   <layout:column columnType="twelve">
	                       <ui:panel title="Quick Search" showHeader="true">
	                           <jsp:include page="/includes/quicksearch-box.jsp" flush="false" />
	                       </ui:panel>
	                   </layout:column>
	                </layout:row>
                </layout:column>
                
                <!-- Center Column -->
                <layout:column columnType="six">
                    <ui:panel title="Availability" noPadding="true">
                        <jsp:include page="/includes/categories-box.jsp" flush="false" />
                    </ui:panel>
                </layout:column>
                
                <!-- Right Column -->
                <layout:column columnType="three">
                    <layout:row>
                        <layout:column columnType="twelve">
                            <ui:panel title="Notifications" showHeader="true" link="notification/index.jsp">
                            <jsp:include page="/includes/notification-box.jsp" flush="false" />
                            </ui:panel>
                        </layout:column>
                    </layout:row>
                    <layout:row>
                        <layout:column columnType="twelve">
                            <ui:panel title="Resource Graphs" showHeader="true" link="graph/index.jsp">
                            <jsp:include page="/includes/resourceGraphs-box.jsp" flush="false" />
                            </ui:panel>
                        </layout:column>
                    </layout:row>
                    <layout:row>
                        <layout:column columnType="twelve">
                            <ui:panel title="KSC Reports" showHeader="true">
                            <jsp:include page="/KSC/include-box.htm" flush="false" />
                            </ui:panel>
                        </layout:column>
                    </layout:row>
                </layout:column>
            </layout:row>
            
        </layout:column>
    </layout:row>

<jsp:include page="/includes/footer.jsp" flush="false" />
