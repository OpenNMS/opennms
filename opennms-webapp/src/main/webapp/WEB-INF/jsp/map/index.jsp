<%--
 
//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2003 Networked Knowledge Systems, Inc.  All rights reserved.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Map" />
  <jsp:param name="headTitle" value="Map" />
  <jsp:param name="location" value="map" />  
  <jsp:param name="breadcrumb" value="Maps" />
</jsp:include>
<!-- Body -->
<script language="Javascript" type="text/javascript" >
	
	function viewMap()
		{
		var vmf = document.getElementById("viewMapForm");
	        //alert(vmf);
	    var fullscreen=vmf.fullscreen.value;
 		var refresh=vmf.refresh.value;	
 		var dimension=vmf.dim.value;
		
		var scrollBarOffset=0;
		var marginOffset=50;  		    	  
		var width,height;
		if(dimension=="auto")
		{

			if (navigator.appName=="Netscape") {
				  scrollBarOffset=16;
			}
			if (navigator.appName.indexOf("Microsoft")!=-1) {
				  scrollBarOffset=20;
			}
			//available pixels - 200(=menu width) - scrollbar offset (depending on browser)
            var availWidth=screen.availWidth-200-scrollBarOffset-marginOffset;
			//var availHeight=screen.availableHeight;
			if(availWidth>=1600){
					width=1600;
					height=1200;
			}else if(availWidth>=1280){
					width=1280;
					height=1024;
			}else if(availWidth>=1024){
					width=1024;
					height=768;
			}else if(availWidth>=800){											
					width=800;
					height=600;
			}else if(availWidth>=640){											
					width=640;
					height=480;			
			}else{
			      width=400;
			      height=300;
			}
			dimension=width+"x"+height;
		}
		vmf.action="Map.map?dimension="+dimension;				
    	vmf.submit();
	}
</script>


  <div class="TwoColLeft">
      <h3>Mapping</h3>    
    <div class="boxWrapper">
          <form method="POST" id="viewMapForm">
            <p align="right">View "fullscreen":
            <select name="fullscreen">
                  <option value="true">Yes</option>
                  <option value="false" selected>No</option>
                </select>
            </p>
            <p align="right">Auto-refresh:
            <select name="refresh">
                  <option value="60">1 minute</option>
                  <option value="120">2 minutes</option>
                  <option value="180">3 minutes</option>
                  <option value="300" selected>5 minutes</option>
                  <option value="600">10 minutes</option>
                  <option value="900">15 minutes</option>
                </select>
            </p>
            <p align="right">Dimension:
            <select name="dim">
		    <option value="auto" selected>Auto</option>
                  <option value="640x480">640x480</option>
                  <option value="800x600">800x600</option>
                  <option value="1024x768">1024x768</option>
                  <option value="1280x1024">1280x1024</option>
                  <option value="1600x1200">1600x1200</option>
                </select>
            </p>
       <c:choose>
		   <c:when test="${manager.allMapMenus!=null}">
	            <p align="right">Open Map:
		    <select name="mapToOpen">
			  <option value="<c:out value="${mapsConstants.MAP_NOT_OPENED}"/>" selected></option>
			   <c:forEach items="${manager.allMapMenus}" var="mapInfo">
					<option value="${mapInfo.id}">${mapInfo.name}</option>		   	
			   </c:forEach>
	        </select>
		   </p>
		   </c:when>
		   <c:otherwise>
		   	<input type="hidden" name="mapToOpen" value="${mapsConstants.MAP_NOT_OPENED}" />
		   </c:otherwise>
	   </c:choose>
            <p align="right">
            <input type="button" value="View" onclick="viewMap()"/>
 	   </p>
          </form>
      </div>      
   </div>      


  <div class="TwoColRight">
  <h3>Mapping</h3>
     <div class="boxWrapper">
      <p>
         Mapping provides the management of maps representing the status (links beetwen nodes, status,
         availability ecc.) of a subset of nodes monitored by the system.
         You can create your personal view of the system, just creating a map containing the nodes you
         are interested to. 
       </p>
       
       <p>  
         Also, you can include one or more <i>map nodes</i> into your map. A <i>map node</i> is
         a map (previousely created) containing another subset (not compulsorily disjoined from your map)
         of nodes of system. By this instrument, you can monitor your system grouping nodes in costumized 
         way and easily to jump from a map to another with a double-click.
        </p>
        
        <p>
         At last, you can change the layout of your map, modifying its background, icons of the nodes 
         (by default, the icon corrensponding to the asset category of the nodes) etc.
         </p>
         
         <p>
         To view Maps, choose if view in fullscreen mode, the interval for refreshing node informations and the
         dimensions of the map frame (with <i>Auto</i> the system choose best dimensions for your browser). 
         Note that choosing wrong dimensions you could not visualize correctly maps.
         </p>
    </div>

  </div>
                                     
<hr />

    <jsp:include page="/includes/footer.jsp" flush="false" >
      <jsp:param name="location" value="map" />
    </jsp:include>

