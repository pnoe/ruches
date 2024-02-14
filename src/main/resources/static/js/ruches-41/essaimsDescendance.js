/* globals buttontextprint, buttontextcol, DataTable */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	new DataTable('#descendance', {
		dom: 'lftip',
		scrollX: true,
	});
});