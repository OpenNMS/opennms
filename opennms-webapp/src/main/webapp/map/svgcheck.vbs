' Copyright 1999-2000 Adobe Systems Inc. All rights reserved. Permission to redistribute
' granted provided that this file is not modified in any way. This file is provided with
' absolutely no warranties of any kind.
Function isSVGControlInstalled()
	on error resume next
	isSVGControlInstalled = IsObject(CreateObject("Adobe.SVGCtl"))
end Function
