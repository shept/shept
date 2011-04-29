
function AllOptions(field, ctrl) {
	if (null == field) {
		return;
	}
	// exactly one element ?
	if (typeof field[0] == 'undefined') {
		field.checked = ctrl.checked;
		return;
	} else {
	var loop;
		for (loop = 0; loop < field.length; loop++) {
			field[loop].checked = ctrl.checked;
		}
	}
}

