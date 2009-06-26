      <%@ tag import="org.springframework.util.StringUtils" %> 
      <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
      <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
      <%@ attribute name="pagedListHolder" required="true" type="org.springframework.beans.support.PagedListHolder" %> 
      <%@ attribute name="pagedLink" required="true" type="java.lang.String" %>
      
      <div class="pagination">
      
      <c:if test="${pagedListHolder.pageCount > 1}"> 
          <c:if test="${!pagedListHolder.firstPage}"> 
              <span><a href="<%= StringUtils.replace(pagedLink, "~", "0") %>">First</a></span>
              <span><a href="<%= StringUtils.replace(pagedLink, "~", String.valueOf(pagedListHolder.getPage()-1)) %>">Previous</a></span> 
          </c:if> 
          <c:if test="${pagedListHolder.firstLinkedPage > 0}"> 
              <span><a href="<%= StringUtils.replace(pagedLink, "~", "0") %>">1</a></span> 
          </c:if> 
          <c:forEach begin="${pagedListHolder.firstLinkedPage}" end="${pagedListHolder.lastLinkedPage}" var="i"> 
              <c:choose> 
                  <c:when test="${pagedListHolder.page == i}"> 
                      <span><strong>${i+1}</strong></span> 
                  </c:when> 
                  <c:otherwise> 
                      <span><a href="<%= StringUtils.replace(pagedLink, "~", String.valueOf(jspContext.getAttribute("i"))) %>">${i+1}</a></span> 
                  </c:otherwise> 
              </c:choose> 
          </c:forEach>  
          <c:if test="${pagedListHolder.lastLinkedPage < pagedListHolder.pageCount - 1}"> 
              <span><a href="<%= StringUtils.replace(pagedLink, "~", String.valueOf(pagedListHolder.getPageCount()-1)) %>">${pagedListHolder.pageCount}</a></span> 
          </c:if> 
          <c:if test="${!pagedListHolder.lastPage}"> 
              <span><a href="<%= StringUtils.replace(pagedLink, "~", String.valueOf(pagedListHolder.getPage()+1)) %>">Next</a></span> 
              <span><a href="<%= StringUtils.replace(pagedLink, "~", String.valueOf(pagedListHolder.getPageCount())) %>">Last</a></span>
          </c:if>
      </c:if>  
      </span>
      
      </div>