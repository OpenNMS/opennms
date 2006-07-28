function init(evt)
	{
		//mapSvgDocument=htmldocument.embeds[0].getSVGDocument();
		root=mapSvgDocument.documentElement;
	}
	
function zoom(k)
	{
		root=mapSvgDocument.firstChild();
		var old_value=root.currentScale;
		var x=root.currentTranslate.x;
		var y=root.currentTranslate.y;
		new_value=old_value * k;
		root.currentScale=new_value;

		setTimeout("zoom1("+(x/(old_value/new_value))+", "+old_value+" ,"+new_value+")", 500);
		//root.currentTranslate.x=x/(old_value/new_value);
		//root.currentTranslate.y=y/(old_value/new_value);
	}

function zoom1(xval, old_value, new_value){
		root=mapSvgDocument.firstChild();
		root.currentTranslate.x=xval;
		var y=root.currentTranslate.y;
		setTimeout("zoom2("+(y/(old_value/new_value)) +")", 500);
}

function zoom2(yval){
		root=mapSvgDocument.firstChild();
		root.currentTranslate.y=yval;
		
}


function pan(k1,k2)
	{
		
		root=mapSvgDocument.firstChild();		
		var old_value=root.currentScale;
		if (k1!=0)
		{
			old_x=root.currentTranslate.x;
			x=old_x + k1 * 20;
			root.currentTranslate.x=x;
		}
	else
		{
			old_y=root.currentTranslate.y;
			y=old_y + k2 * 20;
			root.currentTranslate.y=y;
		}
	}


function reset()
	{
		resetX();
//		root.currentTranslate.x=0; //original code... doesn't function cause maybe svg bug.
//		root.currentTranslate.y=0;
//		root.currentScale=1;
	}


function resetX(){
		root=mapSvgDocument.firstChild();
		root.currentTranslate.x=0;
		setTimeout("resetY()",200);

		
}

function resetY(){
		root=mapSvgDocument.firstChild();
		root.currentTranslate.y=0;
		setTimeout("resetScale()",200);

}

function resetScale(){
		root=mapSvgDocument.firstChild();
		root.currentScale=1;
}