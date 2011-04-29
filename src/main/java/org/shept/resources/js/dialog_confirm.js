
	var dlgConf_frm;
	var dlgConf_eleName;

$(function() {
//	confirmDialogInitialize("Hi");
	$("#dialog-confirm").dialog({
		autoOpen: false
	});
});

function openConfirmDialog(title, element, submissionName, okLabel, cancelLabel) {
	dlgConf_frm = $(element).closest("form");
	dlgConf_eleName = submissionName;

	var buttons = {};
	buttons[okLabel] = function() {
				$(this).dialog('close');			// close form

				// create a new hidden input for delete submission
				var input = document.createElement("input");
				input.setAttribute("type", "hidden");
				input.setAttribute("name", dlgConf_eleName);
				input.setAttribute("value", "dummySubmitValue");
				$(dlgConf_frm).append(input);
				$(dlgConf_frm).submit();
			};
	buttons[cancelLabel] = function() {
				$(this).dialog('close');
			};
		
	$("#dialog-confirm").dialog({
		autoOpen: true,
		resizable: true,
		height:150,
		width: 350,
		modal: true,
		title: title,
		buttons: buttons
	});
		
}

/**
 * You can use an initialization method instead of tag inclusion
 * but there is less control about style and text messages
 */
function confirmDialogInitialize(dlgText) {
	var html = "<div id='dialog-confirm'> " +
			"<span class='ui-icon ui-icon-alert' > </span>" +
			dlgText + "</div>";
	$(html).appendTo(document.body);
}
