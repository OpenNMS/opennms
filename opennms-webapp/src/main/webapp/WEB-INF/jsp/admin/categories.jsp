<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.api.Util" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Categories")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Categories")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">

   var surveillanceCategories = {
		   <c:set var="first" value="true"/>
		   <c:forEach var="surveillanceCat" items="${surveillanceCategories}" varStatus="count">
		     <c:choose>
		       <c:when test="${first == true}">
		         <c:set var="first" value="false" />
		         "${surveillanceCat}":"${surveillanceCat}"
		       </c:when>
		       <c:otherwise>
		         ,"${surveillanceCat}":"${surveillanceCat}"
		       </c:otherwise>
		     </c:choose>
		   </c:forEach>
   };

   function deleteCategory(categoryName, categoryId){
	   if(surveillanceCategories.hasOwnProperty(categoryName)){
           if(confirm("This Surveillance Category is also in your surveillance-views.xml config.\nPlease edit surveillance-views.xml to reflect changes.")){
               location = "<%= Util.calculateUrlBase(request, "admin/categories.htm") %>?removeCategoryId=" + categoryId;
           }
       }else{
           location = "<%= Util.calculateUrlBase(request, "admin/categories.htm") %>?removeCategoryId=" + categoryId;
       }
   }
</script>

<div class="card">
  <div class="card-header">
    <span>Surveillance Categories</span>
  </div>
  <table class="table table-sm table-responsive">
    <tr>
      <th>Delete</th>
      <th>Edit</th>
      <th>Category</th>
    </tr>
    <c:forEach items="${categories}" var="category">
      <tr>
        <td><a href="javascript:void(0);" onclick="deleteCategory('${e:forJavaScript(category.name)}', ${category.id})" ><i class="fas fa-trash-can fa-2x"></i></a></td>
        <td><a href="admin/categories.htm?categoryid=${category.id}&edit"><i class="fas fa-pen-to-square fa-2x"></i></a></td>
        <td><a href="admin/categories.htm?categoryid=${category.id}">${fn:escapeXml(category.name)}</a></td>
      </tr>
    </c:forEach>
    <tr>
      <td colspan="3">
        <form role="form" class="form-inline mt-4" action="admin/categories.htm">
          <input type="textfield" class="form-control" size="40" name="newCategoryName" placeholder="Category name"/>
          <button type="submit" class="btn btn-secondary ml-2"><i class="fas fa-plus"></i> Add New Category</button>
        </form>
    </tr>
  </table>
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
