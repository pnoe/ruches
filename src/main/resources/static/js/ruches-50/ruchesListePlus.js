/* globals
	Ruches, buttTxtPrint, buttTxtCol, dtListe
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	// dtListe est dans include.js
	dtListe('ruchesplus', Ruches, buttTxtPrint, buttTxtCol, 'landscape');
});