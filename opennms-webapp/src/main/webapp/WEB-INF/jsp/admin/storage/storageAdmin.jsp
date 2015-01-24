<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="storageAdmin" value="true" />
  <jsp:param name="title" value="Storage" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Admin Storage" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Admin Storage" />
</jsp:include>

   <script type="text/javascript">


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
		return true;
	}

	function deleteBucket() {
		if(confirm('Do you really want to delete all image files?')==true) {
			document.deleteBucketForm.action="admin/storage/storageDeleteBucket.htm?node=${model.db_id}";
			return true;
		} else {
			return false;
		}
	}
      
</script>


<div class="row">
  <div class="col-md-6">
    <!-- general info box -->
      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">General (Status: ${model.status_general})</h3>
        </div>
        <table class="table table-condensed">
		<tr>
			<th>Node</th>
	  		<td><a href="element/node.jsp?node=${model.db_id}">${model.id}</a></td>
	  	</tr>
		<tr>
	  		<th>Requisition Name</th>
	  		<td>${model.foreignSource}</td>
	  	</tr>
		<tr>
	  		<th>RWS status</th>
	  		<td>${model.RWSStatus}</td>
	  	</tr>
	</table>
      </div> <!-- panel -->

      <div class="panel panel-default">
        <div class="panel-heading">
	  <h3 class="panel-title">Software Images Stored</h3>
        </div>
	<table class="table table-condensed">
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
      </div> <!-- panel -->

      <div class="panel panel-default">
        <div class="panel-heading">
	  <h3 class="panel-title">Manage Images Stored</h3>
        </div>
	<table class="table table-condensed">
	<c:choose>		
	<c:when test="${model.bucketexist}">
	<tr>
	<th>
	 <form id="FormUpload1" method="post" enctype="multipart/form-data"
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
		<th>
			<form id="deleteBucketForm" method="post" name="deleteBucketForm" onsubmit="return deleteBucket();">
				<input name="delStatus" id="doDelete" type="submit" value="Delete"/>
				<input type="hidden" name="bucket" value="${model.id}"/>
			</form>
		</th>
	</tr>
	</c:if>
	</c:when>
	<c:otherwise>
	<tr>
		<th>
			<form id="createBucketForm" method="post" name="createBucketForm" onsubmit="return createBucket();">
				<input name="createStatus" class="btn btn-default" id="doCreate" type="submit" value="Create"/>
				<input type="hidden" name="bucket" value="${model.id}"/>
			</form>
		</th>
	</tr>
	
	</c:otherwise>
	
	</c:choose>		
	</table>
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Descriptions</h3>
      </div>
      <div class="panel-body">
        <p>Detailed Documentation on all options can be found on <a title="The OpenNMS Project wiki" href="http://www.opennms.org" target="new">the OpenNMS wiki</a>.
        </p>
          <p><b>(Delete) Bucket Item</b>: Delete the specified image file from <em>bucket</em>.</p>

         <p><b>Upload Bucket</b>:  Add a file to <em>bucket</em>.
          </p>

         <p><b>Create Bucket</b>:  Create a <em>bucket</em> for storing image files.
          </p>

         <p><b>Delete Bucket</b>:  Delete <em>bucket</em> with all image files.
          </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
