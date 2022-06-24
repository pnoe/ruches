/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals tasks, ruchesurl, Gantt */
"use strict";
function eveGantt() {
	const gantt = new Gantt("#gantt", tasks, {
		view_mode: 'Day',
		language: 'fr',
		custom_popup_html: function(task) {
			return `<div class="details-container">
				    <a href="${ruchesurl}evenement/${task.id}">${task.name}<a>
					  <p>Fin&nbsp;:&nbsp;${task.end}</p>
					  <p>${task.obj}</p><p>${task.com}</p></div>`;
		}
	});
	$(".btn-group").on("click", "button", function() {
		const btn = $(this);
		gantt.change_view_mode(btn.attr('data-sel'));
		btn.parent().find('button').removeClass('active');
		btn.addClass('active');
	});
}