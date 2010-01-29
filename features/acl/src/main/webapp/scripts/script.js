function moveFromList(listSource, listDestination){
	var options_selected = new Array();
	var options_unselected = new Array();
	for(count = 0; count < listSource.options.length; count++)
	{
		if(listSource.options[count].selected){options_selected.push({text: listSource.options[count].text,id: listSource.options[count].value})}
		else{options_unselected.push({text: listSource.options[count].text,id: listSource.options[count].value})}
	}
	dwr.util.removeAllOptions(listSource);
	dwr.util.addOptions(listSource, options_unselected, "id", "text");
	dwr.util.addOptions(listDestination, options_selected, "id", "text");
}

function setInputList(list, eleInput){
	for(count = 0; count < list.options.length; count++)
	{
		if(eleInput.value == ""){eleInput.value = list.options[count].value;}
		else{eleInput.value = eleInput.value + "," + list.options[count].value;}
	}
}
/*
function setListaInInputBis(lista, eleInput){
	for(count = 0; count < lista.options.length; count++)
	{
		if(eleInput.value == ""){eleInput.value = lista.options[count].value;}
		else{eleInput.value = eleInput.value + "," + lista.options[count].value;}
	}
}*/