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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.categories.*,
		java.util.*
	"
%>

<%
	Catinfo catconfig  = null;
	Calendar now = Calendar.getInstance();
		
        CatFactory catFactory;
        try
        {
                CategoryFactory.init();
                catFactory = CategoryFactory.getInstance();
                catconfig =  catFactory.getConfig();
        }
        catch(Exception e)
        {
                throw new ServletException (e);
        }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Availability" />
  <jsp:param name="headTitle" value="Availability" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="Availability" />
</jsp:include>


<script type="text/javascript">
	function openwindow()
	{
		var url = "<%=org.opennms.web.Util.calculateUrlBase(request)%>availability/availability?";
		url += "format=" + escape(document.avail.formatvalue.value);
		url += "&category="+ escape(document.avail.categoryvalue.value);
		url += "&view="+ escape(document.avail.view.value);
		//window.open(url, "", "fullscreen=yes,toolbar=no,status=no,menubar=no,resizable=yes,directories=no,location=no");
		window.open(url);
	}
</script>

  <div class="TwoColLeft">
  <h3>Network Availability Reporting</h3>
    <div class="boxWrapper">
      <form name="avail" method="GET" action="availability/availability" >
        <% boolean checked = true; %>
          <input type="hidden" name="view" value="Availability" />
	<p><B>Choose the format of report.</B><br/>
          <input type="radio" name="format" value="<%="SVG"%>" checked="checked" onClick="avail.formatvalue.value='SVG'"/> Graphical Reports in PDF Format <br/>
          <input type="radio" name="format" value="<%="PDF"%>" onClick="avail.formatvalue.value='PDF'" /> Numeric Reports in PDF Format<br/>
          <input type="radio" name="format" value="<%="HTML"%>" onClick="avail.formatvalue.value='HTML'"/> Numeric Reports in HTML Format<br/>
          <input type="hidden" name="formatvalue" value="SVG"/> </p>
    <p><B>Choose the format of the monthly report sections.</B><br/>
          <input type="radio" name="monthformat" value="<%="classic"%>" checked="checked" onClick="avail.monthformatvalue.value='classic'"/> Classic Format <br/>
          <input type="radio" name="monthformat" value="<%="calendar"%>" onClick="avail.monthformatvalue.value='calendar'" /> Calendar Format<br/>
          <input type="hidden" name="monthformatvalue" value="classic"/> </p>
	<p><B>Choose the category.</B></p>
	<p>
	  <% 
                boolean checkCategory = true;
                String catval = null;

                Enumeration enumCG = catconfig.enumerateCategorygroup();
                if(enumCG != null)
                {
                        while(enumCG.hasMoreElements())
                        {
                                Categorygroup cg = (Categorygroup)enumCG.nextElement();

                                // go through the categories
                                org.opennms.netmgt.config.categories.Categories cats = cg.getCategories();

                                Enumeration enumCat = cats.enumerateCategory();
                                while(enumCat.hasMoreElements())
                                {
                                        org.opennms.netmgt.config.categories.Category cat = (org.opennms.netmgt.config.categories.Category)enumCat.nextElement();
%>
                                        <input type="radio" name="category" value="<%=cat.getLabel() %>" <% if(checkCategory) {  %> checked="checked" <% } %> onClick="avail.categoryvalue.value = '<%= cat.getLabel()%>'" /> <%= cat.getLabel() %><br />

<%
                                          if(checkCategory)
                                                  catval = cat.getLabel();
                                          checkCategory = false;
                                }
                        }
                }
	  %>
	  </p>
	<input type="hidden" name="categoryvalue" value="<%= catval %>" /> <br />
        <p><input type="submit" value="Generate"  class="button" /></p>
        <p><b>Choose the date to use for this report.</b></p>

        <p>
        <select name="startMonth" size="1">
              <% for( int i = 0; i < 12; i++ ) { %>
         <option value="<%=i%>" <% if( now.get( Calendar.MONTH ) == i ) out.print("selected=\"selected\" ");%>><%=months[i]%></option>
              <% } %>
            </select>

            <input type="text" name="startDate" size="4" maxlength="2" value="<%=now.get( Calendar.DATE )%>" />
            <input type="text" name="startYear" size="6" maxlength="4" value="<%=now.get( Calendar.YEAR )%>" />
         </p>
      </form>
    </div>
  </div>

  <div class="TwoColRight">
  <h3>Notes</h3>
    <div class="boxWrapper">
        <p>Generating the availability reports may take a few minutes, especially 
        for large networks, so please do not press the stop or reload buttons 
        until it has finished.  Thank you for your patience.         
	</p>
	<p>The SVG and PDF report formats can be viewed using Adobe Acrobat Reader. 
	If you do not have Adobe Acrobat Reader and wish to download it, please click on the following link:</p>
	<p><a href="http://www.adobe.com/products/acrobat/readstep2.html" target="_new"><img src="images/getacro.gif" border="0" hspace="0" vspace="0" alt="Get Acrobat Reader"/></a></p>
	<p>Acrobat is a registered trademark of Adobe Systems Incorporated.</p>
	</div>
  </div>
  
  <%!
    //note these run from 0-11, this is because of java.util.Calendar!
    public static String[] months = new String[] {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };
%>

<hr />
<jsp:include page="/includes/footer.jsp" flush="false" />
