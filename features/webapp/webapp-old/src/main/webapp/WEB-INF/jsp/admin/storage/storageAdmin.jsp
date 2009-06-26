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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="storageAdmin" value="true" />
  <jsp:param name="title" value="Storage" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Admin Storage" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Admin Storage" />
</jsp:include>

   <script type="text/javascript" language="javascript">


   //-------------------------------------------------------------------------------
   function windowReload() {
     // nota: qui puoi probabilmente usare semplicemente questo:
     location.reload(true);
   }
   //-------------------------------------------------------------------------------


   //-------------------------------------------------------------------------------
   // THIS IS A SILLY EXAMPLE OF USER-DEFINED UPLOAD CALLBACK
   //
   // Arguments:
   //
   //	idUploadForm	the ID of the posting form
   //
   //	objResponse	a JavaScript object tree containing the RWS response
   //			(see below)
   //
   //
   // Description:
   //
   //	The callback is called 2 times:
   //
   //	- as soon the user press the "submit" button:
   //		idUploadForm == <ID of the posting form>
   //		objResponse  == null
   //
   //	- at the end of the upload (i.e. when a response is received):
   //		idUploadForm == <ID of the posting form>
   //		objResponse  == <response object>
   //
   //
   // Return value:
   //
   //	true	display the default alert-box
   //	false	do NOT display the default alert-box
   //
   //
   // Implementation note:
   //
   //	when the callback is called at the end of the upload (objResponse!=null),
   //	you MUST check that objResponse.RWS != null (as a null value means that
   //	error occurred while processing the response).


   function postFileCallback( idUploadForm, objResponse ) {

     var statusArea= document.getElementById(idUploadForm).UploadStatus;

     if ( !objResponse ) {

       statusArea.value= "Upload started, please wait...";

     } else {

       //alert("Received response for: " + idUploadForm);

       if ( !objResponse.RWS ) {

         alert("WARNING: server returned an invalid or malformed response");

         statusArea.value= "";

       } else {

         var rs= objResponse.RWS.ResponseStatus[0];
         //alert("Upload "+rs.Class[0]+": "+rs.Description[0]+" ["+rs.Code[0]+"]\n\n"+rs.ServiceMessage[0]+"\n");
         statusArea.value= "Upload "+rs.Class[0]+": "+rs.Description[0]+" ["+rs.Code[0]+"]\n\n"+rs.ServiceMessage[0];

         if ((rs.Code[0] == "Updated") && (fn= objResponse.ResponseContent[0].ResourceEntity[0].StoredFileName[0])) {
           statusArea.value+= '\nThe file was stored with the name: "'+fn+'"';
         }

       }

       statusArea.value+= "\n\nPlease wait for the window to reload...";
       setTimeout(windowReload, 5000);

     }

     // return false to prevent the default alert box to appear

     return false;

   }
   //-------------------------------------------------------------------------------


   function createBucket() {
	  document.createBucketForm.action="admin/storage/storageCreateBucket.htm?node=${model.db_id}";
	  document.createBucketForm.submit();	
   }

   function deleteBucket() {
  	  if(confirm('Do you really want to delete all image files?')==true) {
	  	document.deleteBucketForm.action="admin/storage/storageDeleteBucket.htm?node=${model.db_id}";
	  	document.deleteBucketForm.submit();	
      }
   }
      
</script>


<div class="TwoColLeft">
    <!-- general info box -->
    <h3>General (Status: ${model.status_general})</h3>
    <table class="o-box">
		<tr>
			<th width="50%">Node</th>
	  		<td><a href="element/node.jsp?node=${model.db_id}">${model.id}</a></td>
	  	</tr>
		<tr>
	  		<th>Foreign Source</th>
	  		<td>${model.foreignSource}</td>
	  	</tr>
		<tr>
	  		<th>RWS status</th>
	  		<td>${model.RWSStatus}</td>
	  	</tr>
	</table>

	<h3>Software Images Stored</h3>
	
	<table class="o-box">
	<tr>
		<th>Name</th>
		<th>Size</th>
		<th>Last Modified</th>
	</tr>
	
	<c:forEach items="${model.bucketitems}" var="swimgelem">
		<tr>
			<td>${swimgelem.name}
<a href="${model.url}/storage/buckets/${model.id}?filename=${swimgelem.name}">(download)</a>
<a href="admin/storage/storageDeleteBucketItem.htm?node=${model.db_id}&bucket=${model.id}&filename=${swimgelem.name}">(delete)</a>
			</td>
			<td>${swimgelem.size}</td>
			<td>${swimgelem.lastModified}</td>
		</tr>
	</c:forEach>
	</table>

	<h3>Manage Images Stored</h3>
	<table class="o-box">
	<c:choose>		
	<c:when test="${model.bucketexist}">
	<tr>
	<th>
	 <form id="FormUpload1" method="POST" enctype="multipart/form-data"
          action="${model.url}/storage/buckets/${model.id}?responsetype=text"
          onsubmit="RWS_ProcessInFrameResponse(this, postFileCallback)">
      <input type="file" name="rws-storage-upload" />
      <input type="submit" name="Action" value="Upload" />
      <textarea name="UploadStatus"></textarea>
    </form>
	</th>
	</tr>
	<c:if test="${model.bucketlistsize == 0 }">
	<tr>
	<th >
	<form id="deleteBucketForm" method="post" name="deleteBucketForm">	
	<input name="delStatus" id="doDelete" type="submit" value="Delete" onClick="deleteBucket()">
	<INPUT TYPE="hidden" NAME="bucket" VALUE="${model.id}">
		</form>
		</th>
	</tr>
	</c:if>
	</c:when>
	<c:otherwise>
	<tr>
	<th >
	<form id="createBucketForm" method="post" name="createBucketForm">	
	<input name="createStatus" id="doCreate" type="submit" value="Create" onClick="createBucket()">
	<INPUT TYPE="hidden" NAME="bucket" VALUE="${model.id}">
		</form>
		</th>
	
	</tr>
	
	</c:otherwise>
	
	</c:choose>		
	</table>
</div>

  <div class="TwoColRight">
      <h3>Descriptions</h3>
      <div class="boxWrapper">
      <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
      </p>
        <p><b>(Delete) Bucket Item</b>: delete the specified image file from <em>bucket</em></p>
      
       <p><b>Upload Bucket</b>:  add a file to <b>bucket</b>. 
        </p>
        
       <p><b>Create Bucket</b>:  Create a <b>bucket</b> for storing image files. 
        </p>

       <p><b>Delete Bucket</b>:  delete <b>bucket</b> with all image files. 
        </p>
                

 
      </div>
  </div>
  <hr />

<jsp:include page="/includes/footer.jsp" flush="false" />
