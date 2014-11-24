function confirmAction(group, action, confirmation) {
	if (confirmation != null) {
		var answer = confirm(confirmation);
		if (!answer) {
			return false;
		}
	}
	document.takeAction.groupName.value = group;
	document.takeAction.action.value = action;
	document.takeAction.actionTarget.value = "";
	document.takeAction.submit();
	return true;
}

function doAction(group, action) {
	document.takeAction.groupName.value = group;
	document.takeAction.action.value = action;
	document.takeAction.actionTarget.value = "";
	document.takeAction.submit();
	return true;
}

function cloneRequisition(group) {
	var answer = prompt("What do you wish to call the new requisition?");
	if (answer == null) {
		return false;
	} else {
		document.takeAction.groupName.value = group;
		document.takeAction.action.value = "cloneRequisition";
		document.takeAction.actionTarget.value = answer;
		document.takeAction.submit();
	}
	return true;
}

function editRequisition(group) {
	document.editRequisitionForm.groupName.value = group;
	document.editRequisitionForm.submit();
	return true;
}

function cloneForeignSource(group) {
	var answer = prompt("What do you wish to call the new foreign source?");
	if (answer == null) {
		return false;
	} else {
		document.takeAction.groupName.value = group;
		document.takeAction.action.value = "cloneForeignSource";
		document.takeAction.actionTarget.value = answer;
		document.takeAction.submit();
	}
	return true;
}

function editForeignSource(foreignSourceName) {
	document.editForeignSourceForm.foreignSourceName.value = foreignSourceName;
	document.editForeignSourceForm.submit();
	return true;
}

function resetDefaultForeignSource() {
	var really = confirm("Are you sure you want to reset the default foreign-source definition? This CANNOT be undone.");
	if (!really) {
		return false;
	}
	document.takeAction.groupName.value = "default";
	document.takeAction.action.value = "resetDefaultForeignSource";
	document.takeAction.submit();
	return true;
}
