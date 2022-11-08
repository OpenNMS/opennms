<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
		contentType="text/html"
		session="true"
		import="java.util.*,
                javax.servlet.jsp.JspWriter,
                org.opennms.netmgt.model.OnmsNode,
                org.opennms.netmgt.model.OnmsHwEntity,
                org.opennms.netmgt.model.OnmsHwEntityAttribute,
                org.opennms.netmgt.dao.api.HwEntityDao,
                org.opennms.web.element.ElementUtil,
                org.apache.commons.lang.StringUtils,
                org.springframework.web.context.WebApplicationContext,
                org.springframework.web.context.support.WebApplicationContextUtils,
                org.opennms.netmgt.model.OnmsMetaData,
                org.opennms.netmgt.model.snmpmetadata.SnmpMetadataEntry,
                org.opennms.netmgt.model.snmpmetadata.SnmpMetadataObject,
                org.opennms.netmgt.model.snmpmetadata.SnmpMetadataTable,
                org.opennms.netmgt.model.snmpmetadata.SnmpMetadataValue"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!

    public void printSnmpMetadataTable(final JspWriter out, final SnmpMetadataTable snmpMetadataTable) throws Exception {
        final String parentCls = snmpMetadataTable.getParent() == null ? "" : " treegrid-parent-" + snmpMetadataTable.getParent().getId();
        out.println("<tr class='treegrid-" + snmpMetadataTable.getId() + parentCls + "'>");
        out.println("<td>" + snmpMetadataTable.getName() + "</td><td><table class=\"table table-sm table-bordered\">");
        out.println("</table></td></tr>");

        for (final SnmpMetadataEntry snmpMetadataEntry : snmpMetadataTable.getEntries()) {
            printSnmpMetadataEntry(out, snmpMetadataEntry);
        }
    }

    public void printSnmpMetadataValue(final JspWriter out, final SnmpMetadataValue snmpMetadataValue) throws Exception {
        out.println("<td width='50%'>" + snmpMetadataValue.getValue() + "&nbsp;</td>");
    }

    public void printSnmpMetadataEntry(final JspWriter out, final SnmpMetadataEntry snmpMetadataEntry) throws Exception {
        final String parentCls = snmpMetadataEntry.getParent() == null ? "" : " treegrid-parent-" + snmpMetadataEntry.getParent().getId();
        out.println("<tr class='treegrid-" + snmpMetadataEntry.getId() + parentCls + "'>");
        out.println("<td>" + ((SnmpMetadataTable)snmpMetadataEntry.getParent()).getName() +"[" + snmpMetadataEntry.getIndex() + "]" + "</td><td><table class=\"table table-sm table-bordered\">");
        for(final SnmpMetadataValue snmpMetadataValue : snmpMetadataEntry.getValues()) {
            out.println("<tr><th>" + snmpMetadataValue.getName() + "</th>");
            printSnmpMetadataValue(out, snmpMetadataValue);
            out.println("</tr>");
        }
        out.println("</table></td></tr>");
    }

    public void printSnmpMetadataObject(final JspWriter out, final SnmpMetadataObject snmpMetadataObject) throws Exception {
        final String parentCls = snmpMetadataObject.getParent() == null ? "" : " treegrid-parent-" + snmpMetadataObject.getParent().getId();
        out.println("<tr class='treegrid-" + snmpMetadataObject.getId() + parentCls + "'>");
        out.println("<td>" + snmpMetadataObject.getName() + "</td><td>");
        out.println("<table class=\"table table-sm table-bordered\">");
        for (final SnmpMetadataValue snmpMetadataValue : snmpMetadataObject.getValues()) {
            out.println("<tr>");
            printSnmpMetadataValue(out, snmpMetadataValue);
            out.println("</tr>");
        }
        out.println("</table></td></tr>");
        for (final SnmpMetadataObject nextSnmpMetadataObject : snmpMetadataObject.getObjects()) {
            printSnmpMetadataObject(out, nextSnmpMetadataObject);
        }
        for (final SnmpMetadataTable nextSnmpMetadataTable : snmpMetadataObject.getTables()) {
            printSnmpMetadataTable(out, nextSnmpMetadataTable);
        }
    }

    private HwEntityDao hwEntityDao;

    public void init() throws ServletException {
        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        hwEntityDao = webAppContext.getBean("hwEntityDao", HwEntityDao.class);
    }

    public String getTitle(OnmsHwEntity entity) {
        String id = entity.getEntPhysicalName() == null ? "[Unknown]" : entity.getEntPhysicalName();
        return id + " (entPhysicalIndex=" + entity.getEntPhysicalIndex() + ")";
    }

    public String getContent(OnmsHwEntity entity) {
        StringBuffer sb = new StringBuffer();
        sb.append("<table class=\"table table-sm table-bordered\">");
        sb.append("<tr><th>Description</th><td>" + entity.getEntPhysicalDescr() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalVendorType()))
            sb.append("<tr><th>Vendor Type</th><td>" + entity.getEntPhysicalVendorType() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalClass()))
            sb.append("<tr><th>Class</th><td>" + entity.getEntPhysicalClass() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalHardwareRev()))
            sb.append("<tr><th>Hardware Revision</th><td>" + entity.getEntPhysicalHardwareRev() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalFirmwareRev()))
            sb.append("<tr><th>Firmware Revision</th><td>" + entity.getEntPhysicalFirmwareRev() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalSoftwareRev()))
            sb.append("<tr><th>Software Revision</th><td>" + entity.getEntPhysicalSoftwareRev() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalSerialNum()))
            sb.append("<tr><th>Serial Number</th><td>" + entity.getEntPhysicalSerialNum() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalMfgName()))
            sb.append("<tr><th>Manufacturer Name</th><td>" + entity.getEntPhysicalMfgName() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalModelName()))
            sb.append("<tr><th>Model Name</th><td>" + entity.getEntPhysicalModelName() + "</td></tr>");
        if (StringUtils.isNotBlank(entity.getEntPhysicalAlias()))
            sb.append("<tr><th>Alias</th><td>" + entity.getEntPhysicalAlias() + "</td></tr>");
        for (OnmsHwEntityAttribute a : entity.getHwEntityAttributes()) {
            sb.append("<tr><th>" +  a.getTypeName() + "</th><td>" + a.getValue() + "</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public void printTree(JspWriter out, OnmsHwEntity entity) throws Exception {
        String parentCls = entity.getParent() == null ? "" : " treegrid-parent-" + entity.getParent().getId();
        out.println("<tr class='treegrid-" + entity.getId() + parentCls + "'>");
        out.println("<td>" + getTitle(entity) + "</td><td>" + getContent(entity) + "</td>");
        out.println("</tr>");
        for (OnmsHwEntity child : entity.getChildren()) {
            printTree(out, child);
        }
    }
%>

<%
    OnmsNode node = ElementUtil.getNodeByParams(request, getServletContext());
    OnmsHwEntity root = hwEntityDao.findRootByNodeId(node.getId());
    List<OnmsMetaData> onmsMetaDataList = node.getMetaData();
    boolean snmpHardware = false;

    for(final OnmsMetaData onmsMetaData : onmsMetaDataList) {
        if ("snmp".equals(onmsMetaData.getContext())) {
            snmpHardware = true;
            break;
        }
    }

    String nodeBreadCrumb = "<a href='element/node.jsp?node=" + node.getId()  + "'>Node</a>";
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Hardware Inventory" />
  <jsp:param name="headTitle" value="Hardware Inventory" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="Hardware Inventory" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-treegrid-js" />
</jsp:include>

<br/>
<table class="table table-sm table-hover tree">
  <% if (root == null) { %>
    <% if (!snmpHardware) { %>
      <br/>
      <div class="jumbotron"><h3>The node <%= WebSecurityUtils.sanitizeString(node.getLabel()) %> doesn't have hardware information on the database.</h3></div>
    <% } else {
        SnmpMetadataObject snmpMetadataObject = (SnmpMetadataObject) SnmpMetadataObject.fromOnmsMetadata(onmsMetaDataList, "snmp");
        printSnmpMetadataObject(out, snmpMetadataObject);
       }
    %>
  <% } else {
       printTree(out, root);
     }
  %>
</table>

<script type="text/javascript">
  $('.tree').treegrid();
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
