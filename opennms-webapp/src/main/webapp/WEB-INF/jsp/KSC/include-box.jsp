<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2002 Nov 12: Added response time, based on original  performance code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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

<script type="text/javascript">
  function resetKscBoxSelected() {
    document.kscBoxReportList.report[0].selected = true;
  }
  
  function validateKscBoxReportChosen() {
    var report = -1;
    
    for (i = 0; i < document.kscBoxReportList.report.length; i++) {
      // make sure something is checked before proceeding
      if (document.kscBoxReportList.report[i].selected     
          && document.kscBoxReportList.report[i].value != "") {
        report = document.kscBoxReportList.report[i].value;
        break;
      }
    }
    
    return report;
  }
  
  function goKscBoxChange() {
    var reportChosen = validateKscBoxReportChosen();
    if (reportChosen != -1) {
      document.kscBoxForm.report.value = reportChosen;
      document.kscBoxForm.submit();
      /*
       * We reset the selection after submitting the form so if the user
       * uses the back button to get back to this page, it will be set at
       * the "choose a node" option.  Without this, they wouldn't be able
       * to proceed forward to the same node because won't trigger the
       * onChange action on the <select/> element.  We also do the submit
       * in a separate form after we copy the chosen value over, just to
       * ensure that no problems happen by resetting the selection
       * immediately after calling submit().
       */
      resetKscBoxSelected();
    }
  }
</script>

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
                onSelect:chooseResourceBoxChange,
            })
          });

      function chooseResourceBoxChange(record){
    	    window.location="KSC/customView.htm?type=custom&report=" + record.data.id;
      }
      </script>
      
      <script type="text/javascript">
        resetKscBoxSelected();
      </script>
    </c:otherwise>
  </c:choose>
  <div id="ksc-combo"></div>
</div>
