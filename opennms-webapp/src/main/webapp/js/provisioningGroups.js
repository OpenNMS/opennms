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
	document.editRequisitionForm.groupName.value = group;
	document.editRequisitionForm.submit();
	return true;
}

function editForeignSource(foreignSourceName) {
	document.editForeignSourceForm.foreignSourceName.value = foreignSourceName;
	document.editForeignSourceForm.submit();
	return true;
}
