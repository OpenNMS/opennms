var __debug__ = true;

function alertPrintNode(node)
{
	if (__debug__)
		alert(printNode(node));	
}

function alertDebug(str)
{
	if (__debug__)
		alert(str);
}

function printDebug(node, str)
{
	if (__debug__)
	{
		if (node != null )
			node.getFirstChild().setData(str);	
	}
}

function displayPropertyNames(obj) {
	if (__debug__)
	{
	    var names = "";
	    for(var name in obj) 
	    	names += name + "\n";
	   	alert(names);
   	}
} 