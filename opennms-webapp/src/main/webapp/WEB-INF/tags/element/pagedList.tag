      <%@ tag import="org.springframework.util.StringUtils" %> 
      <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
      <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
      <%@ attribute name="pagedListHolder" required="true" type="org.springframework.beans.support.PagedListHolder" %> 
      <%@ attribute name="pagedLink" required="true" type="java.lang.String" %>
      
      <ul class="pagination">
      
      <% String marker = "%7E"; %>
      <c:if test="${pagedListHolder.pageCount > 1}"> 
          <c:choose>
            <c:when test="${!pagedListHolder.firstPage}"> 
              <li><a href="<%= StringUtils.replace(pagedLink, marker, "0") %>">First</a></li>
              <li><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPage()-1)) %>">Previous</a></li>
            </c:when> 
            <c:otherwise> 
              <li class="disabled"><a href="<%= StringUtils.replace(pagedLink, marker, "0") %>">First</a></li>
              <li class="disabled"><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPage()-1)) %>">Previous</a></li>
            </c:otherwise> 
          </c:choose>
          <c:if test="${pagedListHolder.firstLinkedPage > 0}"> 
              <li><a href="<%= StringUtils.replace(pagedLink, marker, "0") %>">1</a></li>
          </c:if> 
          <c:forEach begin="${pagedListHolder.firstLinkedPage}" end="${pagedListHolder.lastLinkedPage}" var="i"> 
              <c:choose> 
                  <c:when test="${pagedListHolder.page == i}"> 
                      <li class="active"><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(jspContext.getAttribute("i"))) %>">${i+1}</a></li>
                  </c:when> 
                  <c:otherwise> 
                      <li><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(jspContext.getAttribute("i"))) %>">${i+1}</a></li>
                  </c:otherwise> 
              </c:choose> 
          </c:forEach>  
          <c:if test="${pagedListHolder.lastLinkedPage < pagedListHolder.pageCount - 1}"> 
              <li><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPageCount()-1)) %>">${pagedListHolder.pageCount}</a></li>
          </c:if> 
          <c:choose>
            <c:when test="${!pagedListHolder.lastPage}"> 
              <li><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPage()+1)) %>">Next</a></li>
              <li><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPageCount())) %>">Last</a></li>
            </c:when>
            <c:otherwise> 
              <li class="disabled"><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPage()+1)) %>">Next</a></li>
              <li class="disabled"><a href="<%= StringUtils.replace(pagedLink, marker, String.valueOf(pagedListHolder.getPageCount())) %>">Last</a></li>
            </c:otherwise> 
          </c:choose> 
      </c:if>
      
      </ul>
