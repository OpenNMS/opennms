function confirmAction(group, action, confirmation) {
	if (confirmation != null) {
		var answer = confirm(confirmation);
		if (!answer) {
			return false;
		}
	}
	document.takeAction.groupName.value = group;
	document.takeAction.action.value = action;
	document.takeAction.submit();
	return true;
}

function doAction(group, action) {
	document.takeAction.groupName.value = group;
	document.takeAction.action.value = action;
	document.takeAction.submit();
	return true;
}

function editRequisition(group) {
	document.editRequisition.groupName.value = group;
	document.editRequisition.submit();
	return true;
}

function editForeignSource(foreignSourceName) {
	document.editForeignSource.foreignSourceName.value = foreignSourceName;
	document.editForeignSource.submit();
	return true;
}
