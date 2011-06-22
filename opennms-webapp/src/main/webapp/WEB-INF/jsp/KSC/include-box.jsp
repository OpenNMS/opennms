<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

<%--
  This page is included by other JSPs to create a box containing an
  entry to the performance reporting system.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>


<%@ page language="java" contentType="text/html" session="true" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<h3 class="o-box"><a href="KSC/index.htm">KSC Reports</a></h3>
<div class="boxWrapper">
  <c:choose>
    <c:when test="${fn:length(reports) == 0}">
      <p class="noBottomMargin">
        No KSC reports defined
      </p>
    </c:when>
    
    <c:otherwise>
      <script type="text/javascript">      
      var kscComboData = [<c:set var="first" value="true"/>
                      <c:forEach var="report" items="${reports}" varStatus="reportCount">
                        <c:choose>
                          <c:when test="${first == true}">
                            <c:set var="first" value="false"/>
                              [${report.key}, "${report.value}"]
                          </c:when>
                          <c:otherwise>
                            ,[${report.key}, "${report.value}"]
                          </c:otherwise>
                        </c:choose>
                      </c:forEach>];
      
      Ext.onReady(function(){
          var kscCombo = new Ext.form.ComboBox({
                triggerAction: 'all',
                displayField: 'value',
                valueField: 'id',
                lazyRender:true,
                mode: 'local',
                store: new Ext.data.Store({
                    data: kscComboData,
                    reader: new Ext.data.ArrayReader({
                        fields: Ext.data.Record.create([
                                {name: 'id', mapping: 0},
                                {name: 'value', mapping: 1}
                        ]),
                        idIndex: 0
                    })
                }),
                renderTo:'ksc-combo',
                emptyText:"-- Choose A Report to View --",
                width:220,
                onSelect:chooseKSCBoxChange
            })
          });

      function chooseKSCBoxChange(record){
    	    window.location="KSC/customView.htm?type=custom&report=" + record.data.id;
      }
      </script>
      
    </c:otherwise>
  </c:choose>
  <div id="ksc-combo"></div>
</div>
