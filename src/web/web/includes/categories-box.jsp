<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%-- 
  This page is included by other JSPs to create a box containing a
  table of categories and their outage and availability status.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.category.*,java.util.*,org.opennms.netmgt.config.viewsdisplay.*,org.opennms.netmgt.config.ViewsDisplayFactory" %>

<%!
  protected static final String WEB_CONSOLE_VIEW = "WebConsoleView";

  protected CategoryModel model;  
  
  /** 
   * Display rules from viewsdisplay.xml.  If null, then just show all known
   * categories under the header "Category".  (See the getSections method.)
   */
  protected Section[] sections; 
  
  
  public void init() throws ServletException {    
      try {
          this.model = CategoryModel.getInstance();
      } 
      catch (Exception e) { 
          this.log("failed to instantiate the category model", e);
          throw new ServletException("failed to instantiate the category model", e);
      }
  
      try {
          ViewsDisplayFactory.init();    
          ViewsDisplayFactory viewsDisplayFactory = ViewsDisplayFactory.getInstance();
          
          View view = viewsDisplayFactory.getView(WEB_CONSOLE_VIEW);
          
          if( view != null ) {
              this.sections = view.getSection();
              this.log( "DEBUG: found display rules from viewsdisplay.xml" );
          }
          else {
              this.log( "DEBUG: did not find display rules from viewsdisplay.xml" );        
          }
      }
      catch( Exception e ) {
          this.log( "Couldn't open viewsdisplay factory on categories box.", e );
      }
  }
%>

<%
    int notFoundCategoryCount = 0;    
    Map categoryMap = this.model.getCategoryMap();
    List sectionList = this.getSections( categoryMap );
    int sectionCount = sectionList.size();
%>


<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" bgcolor="#cccccc">

  <% for( int i=0; i < sectionCount; i++ ) { %>
      <% Section section = (Section)sectionList.get(i); %>

      <tr bgcolor="#999999"> 
        <td width="50%"><b><%=section.getSectionName()%></b></td>
        <td width="20%" align="right"><b>Outages</b></td>
        <td width="30%" align="right"><b>24hr Avail</b></td>
      </tr>
    
      <% String[] categoryNames = section.getCategory(); %> 
    
      <% for( int j=0; j < categoryNames.length; j++ ) {
            String categoryName = categoryNames[j]; 
            Category category = (Category)categoryMap.get(categoryName);
            String title = this.model.getCategoryComment(categoryName);

            if( category != null ) {
                long serviceCount = category.getServiceCount();                       
                long serviceDownCount = category.getServiceDownCount();
                double servicePercentage = 100.0;
                
                if( serviceCount > 0 ) {
                    servicePercentage = ((double)(serviceCount-serviceDownCount))/(double)serviceCount*100.0;
                }
                
                String color = CategoryUtil.getCategoryColor( category );
                String outageColor = CategoryUtil.getCategoryColor( category, servicePercentage );
            %>
                <tr>
                  <td><a href="rtc/category.jsp?category=<%= java.net.URLEncoder.encode(categoryName) %>" title="<%=(title == null) ? categoryName : title%>"><%=categoryName%></a></td>
                  <td bgcolor="<%=outageColor%>" align="right" title="Updated: <%=category.getLastUpdated()%>"><%=serviceDownCount%> of <%=serviceCount%></td>
                  <td bgcolor="<%=color%>" align="right" title="Updated: <%=category.getLastUpdated()%>"><b><%=CategoryUtil.valueFormat.format( category.getValue() )%>%</b></td>
                  <!-- Last updated <%=category.getLastUpdated()%> -->
                  <!-- Epoch time:  <%=category.getLastUpdated().getTime()%> -->
                </tr>
            <% } else { %>
              <% notFoundCategoryCount++; %>            
              <tr>              
                <td><a href="rtc/category.jsp?category=<%= java.net.URLEncoder.encode(categoryName) %>" title="<%=(title == null) ? categoryName : title%>"><%=categoryName%></a></td>
                <td bgcolor="lightblue" align="right">Calculating...</td>
                <td bgcolor="lightblue" align="right">Calculating...</td>
              </tr>            
            <% } %>                                
      <% } %>
  <% } %>
    
  <tr bgcolor="#999999">
    <td colspan="3">Percentage over last 24 hours</td> <%-- next iteration, read this from same properties file that sets up for RTCVCM --%>
  </tr>   
</table>

<%
    if( notFoundCategoryCount > 0 ) {
        try {
            RTCPostSubscriber.subscribeAll(WEB_CONSOLE_VIEW);
        }
        catch(java.net.ConnectException e) {
            this.log("Couldn't subscribe to RTC", e );
        }
    }
%>


<%!
    /**
     * For the given map of category names to Category objects, organize
     * the categories into the currently active display rules.
     *
     * <p>If there are no display rules, a single section named 
     * <em>Category</em> will be returned.  It will include all the categories
     * in the category map, in alphabetical order by category name.</p>
     */
    public List getSections( Map categoryMap ) {
        List sectionList = null;
    
        if( this.sections != null ) {
            //just return the display rules as a list
            sectionList = Arrays.asList(this.sections);
        }
        else {                            
            Section section = new Section();
            section.setSectionName("Category");

            //put the categories in a TreeMap to sort them alphabetically                        
            TreeMap orderedMap = new TreeMap(categoryMap);
            
            //get an iterator
            Set categorySet = orderedMap.entrySet();
            Iterator iterator = categorySet.iterator();

            //iterate over the categories, adding each to the name list            
            while( iterator.hasNext() ) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Category category = (Category)entry.getValue();
                
                section.addCategory(category.getName());
            }

            //add our one section to the sections list
            sectionList = new ArrayList();
            sectionList.add(section);
        }            
        
        return( sectionList ); 
    }
    
%>
