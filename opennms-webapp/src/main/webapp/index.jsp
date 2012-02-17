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
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Web Console" />
</jsp:include>
    
    <!-- Top Level Row -->
    <ui:row>
        
        <!-- Entire view -->
        <ui:column columnType="twelve">
            
            <ui:row>
                <!-- Left Column -->
                <ui:column columnType="three">
                    
                    <ui:row>
                        <ui:column columnType="twelve">
                            <ui:panel title="Nodes with Outages" showHeader="true" link="outage/list.html">
                                <jsp:include page="/outage/servicesdown-box.htm" flush="false" />
                            </ui:panel>
                        </ui:column>
	                </ui:row>
	                
	                <ui:row>
	                   <ui:column columnType="twelve">
	                       <ui:panel title="Quick Search" showHeader="true">
	                           <jsp:include page="/includes/quicksearch-box.jsp" flush="false" />
	                       </ui:panel>
	                   </ui:column>
	                </ui:row>
                </ui:column>
                
                <!-- Center Column -->
                <ui:column columnType="six">
                    <jsp:include page="/includes/categories-box.jsp" flush="false" />
                </ui:column>
                
                <!-- Right Column -->
                <ui:column columnType="three">
                    <ui:row>
                        <ui:column columnType="twelve">
                            <jsp:include page="/includes/notification-box.jsp" flush="false" />
                        </ui:column>
                    </ui:row>
                    <ui:row>
                        <ui:column columnType="twelve">
                            <jsp:include page="/includes/resourceGraphs-box.jsp" flush="false" />
                        </ui:column>
                    </ui:row>
                    <ui:row>
                        <ui:column columnType="twelve">
                            <jsp:include page="/KSC/include-box.htm" flush="false" />
                        </ui:column>
                    </ui:row>
                </ui:column>
            </ui:row>
            
        </ui:column>
    </ui:row>

<jsp:include page="/includes/footer.jsp" flush="false" />
