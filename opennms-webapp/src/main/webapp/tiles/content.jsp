<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div id="content">
	<p>
	Click <a href="graph.htm">here</a> to graph something.
	</p>
	
	<p>
	Enter the name of your favorite OGP Member:
	</p>

	
	<input class="autocomplete_text" type="text" id="autocomplete" name="formtest"/>
	<div class="autocomplete_choices" id="autocomplete_choices"></div>
    <p>
	* Well maybe someday.
	</p>

	<p>
	Click <a href="addMember.htm">here</a> to add a new OGP Member.
	</p>

	<script type="text/javascript">
	  new Ajax.Autocompleter("autocomplete", "autocomplete_choices", "completions.htm", { paramName : 'searchKey' });
	</script>

</div>
