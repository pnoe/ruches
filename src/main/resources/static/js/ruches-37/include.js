/* globals urlValMessFr, urlDatatMessFr, datatPageLength, tout */
'use strict';
function updateTheme() {
	document.querySelector('html').setAttribute('data-bs-theme',
		window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
}
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', updateTheme);
updateTheme();
if ((window.navigator.language.indexOf('fr') !== -1)) {
	const script = document.createElement('script');
	script.src = urlValMessFr;
	script.async = true;
	document.head.appendChild(script);
	$.extend(true, $.fn.dataTable.defaults, {
		language: {
			url: urlDatatMessFr
		},
	});
}
$.extend(true, $.fn.dataTable.defaults, {
	stateSave: true,
	// Attention au formattage, respecter l'espace entre les crochets pour qu'ils ne soient pas
	//  traités par Thymeleaf
	lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, tout]],
	pageLength: parseInt(datatPageLength),
});