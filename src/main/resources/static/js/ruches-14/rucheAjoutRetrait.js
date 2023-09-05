/* globals _csrf_token, urlRucheOdreHause, rucheId, DataTable
 */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const tableRetrait = new DataTable('#retraitHausses', {
		paging: false,
		searching: false,
		info: false,
		ordering: true,
		rowReorder: {
			selector: 'tr'
		},
		columns: [
			{ orderable: false },
			{ orderable: false }
		]
	});
	tableRetrait.on('row-reordered', function(e, diff) {
		if (diff.length === 0) { return; }
		const hausses = [];
		const aElements = document.querySelectorAll('#retraitHausses tbody tr a');
		aElements.forEach(function(aElement) {
			const href = aElement.getAttribute('href');
			hausses.push(href.substring(href.lastIndexOf('/') + 1));
		});
		const xhr = new XMLHttpRequest();
		xhr.open('POST', urlRucheOdreHause + rucheId, true);
		xhr.setRequestHeader('Content-Type', 'application/json');
		xhr.setRequestHeader('x-csrf-token', _csrf_token);
		xhr.onreadystatechange = () => {
			if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
				if (xhr.responseText !== 'OK') {
					alert(xhr.responseText);
				}
			}
		};
		xhr.send(JSON.stringify(hausses));
	});
	new DataTable('#ajoutHausses');
});