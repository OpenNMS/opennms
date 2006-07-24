<%@page language="java" contentType="text/html" session="true"%>
<%@taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles" %>


<div id="graph">
<!-- XXX TO BE IMPLEMENTED --- DON'T DELETE DJ YOU IDIOT
     <div id="tab-bar">
     	  <ul>
		<li><a href="">Graph</a></li>
		<li><a href="">New</a></li>
	  </ul>
     </div>     
-->

  <span id="data-list">
		<tiles:insert name="graph-node" />
  </span>

  <div id="graph-right">

<!-- XXX TO BE IMPLEMENTED --- DON'T DELETE DJ YOU IDIOT
       <div id="graph-header">
     	  
       </div>
-->

     <div id="cart" class="cart" style="clear:left; margin-top:10px;">  
	   <tiles:insert name="graph-cart" />
     </div>

     <div id="wastebin">
			  Drop items here to remove them from the cart.
	 </div>

	<div class="updating_indicator">
		<p id="indicator" style="display:none;margin-top:0px;">
		  <img alt="Indicator" src="images/indicator.gif" /> Updating cart...
		</p>
	</div>

<%--
	<script type="text/javascript">Droppables.add('cart', {accept:'products', onDrop:function(element){new Ajax.Updater('cart', 'graph-cart.htm', {onLoading:function(request){Element.show('indicator')}, onComplete:function(request){Element.hide('indicator')}, parameters:'add=' + encodeURIComponent(element.id), evalScripts:true, asynchronous:true})}, hoverclass:'cart-active'})</script>
	<script type="text/javascript">Droppables.add('wastebin', {accept:'cart-items', onDrop:function(element){Element.hide(element); new Ajax.Updater('cart', 'graph-cart.htm', {onLoading:function(request){Element.show('indicator')}, onComplete:function(request){Element.hide('indicator')}, parameters:'remove=' + encodeURIComponent(element.id), evalScripts:true, asynchronous:true})}, hoverclass:'wastebin-active'})</script>
--%>
	<script type="text/javascript"><!--
		Droppables.add('cart', {accept:'products', onDrop:function(element){Element.show('indicator'); new Ajax.Updater('cart', 'graph-cart.htm', {onComplete:function(request){Element.hide('indicator')}, parameters:'add=' + encodeURIComponent(element.id), evalScripts:true, asynchronous:true})}, hoverclass:'cart-active'});
		Droppables.add('wastebin', {accept:'cart-items', onDrop:function(element){Element.hide(element); Element.show('indicator'); new Ajax.Updater('cart', 'graph-cart.htm', {onComplete:function(request){Element.hide('indicator')}, parameters:'remove=' + encodeURIComponent(element.id), evalScripts:true, asynchronous:true})}, hoverclass:'wastebin-active'});
	--></script>

   </div>
</div>
