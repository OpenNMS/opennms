/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
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