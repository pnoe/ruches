/* globals DataTable */
'use strict';

// externalisé en .js à cause de [[...]] qui doit être écrit [ [...] ] dans un .html.
document.addEventListener('DOMContentLoaded', () => {
	new DataTable('#ruchers', { searching: false, paging: false, info: false });
	new DataTable('#essaims', {
		// Tri par rucher, puis par ruche.
		// https://datatables.net/reference/option/order
		// https://datatables.net/reference/option/stateSave
		// Si l'ordre est enregistré par un clic utilisateur, alors order n'est plus appliqué.
		stateSave: false,
		order: [[1, 'asc'], [0, 'asc']],
		scrollX: true,
	});
});