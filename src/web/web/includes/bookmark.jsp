<script language="Javascript" type="text/javascript">
<!--
function addbookmark()
{
	if((navigator.appName == "Microsoft Internet Explorer" && (parseInt(navigator.appVersion) >= 4)))
	{
		var url = window.location
		var title = String(window.document.title)
		var title1  = title
		while( title1.indexOf(' | ') != -1  ){
			title1 = title1.replace(' | ' , ' - ');
		}
		javascript:window.external.AddFavorite(url, title1);
	}
	else if(!document.all)
	{
		var msg = "Netscape users must bookmark the pages manually by hitting"
		if(navigator.appName == "Netscape") 
		{
			msg += " <CTRL-D>";
		}
		document.write(msg);
	}
}
//-->
</script>

<form NAME="bookmark" ACTION="javascript:addbookmark()">
	<table>
	<tr><td><input type="SUBMIT" value="Bookmark the results"/></td></tr>
	</table>
</form>
