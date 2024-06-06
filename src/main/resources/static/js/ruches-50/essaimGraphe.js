/* globals d3,
	data, essaimnom, essaimtxt,	ruchetxt, ruchertxt,
	MielKgtxt, MielDescKgtxt, Actif, Inactif, urlessaim, bootstrap	*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	const dataMap = data.reduce(function(map, node) {
		map[node.name] = node;
		return map;
	}, {});
	const treeData = [];
	data.forEach(function(node) {
		const parent = dataMap[node.parent];
		if (parent) {
			(parent.children || (parent.children = []))
				.push(node);
		} else {
			treeData.push(node);
		}
	});
	const margin = {
		top: 90,
		right: 90,
		bottom: 90,
		left: 90
	};
	const width = 1300;
	const height = 700;
	const treemap = d3.tree()
		.size([width - margin.left - margin.right, height - margin.top - margin.bottom]);
	let nodes = d3.hierarchy(treeData[0]);
	nodes = treemap(nodes);
	const svg = d3.select('#graph').append('svg')
		.attr('width', width)
		.attr('height', height);
	const g = svg.append('g')
		.attr('transform',
			'translate(' + margin.left + ',' + margin.top + ')');
	g.selectAll('.link')
		.data(nodes.descendants().slice(1))
		.enter().append('path')
		.attr('class', 'link')
		.attr('d', function(d) {
			return 'M' + d.x + ',' + d.y +
				'C' + d.x + ',' + (d.y + d.parent.y) / 2 +
				' ' + d.parent.x + ',' + (d.y + d.parent.y) / 2 +
				' ' + d.parent.x + ',' + d.parent.y;
		});
	const node = g.selectAll('.node')
		.data(nodes.descendants())
		.enter().append('g')
		.attr('class', function(d) {
			return 'node' +
				(d.children ? ' node--internal' : ' node--leaf');
		})
		.attr('transform', function(d) {
			return 'translate(' + d.x + ',' + d.y + ')';
		});
	node.append('text')
		.attr('dy', '.35em')
		.attr('y', function(d) {
			return d.children ? -87 : 10;
		})
		.attr('transform', function(d) {
			return 'rotate(' + ((d.data.parent === 'null') ? 0 : -50) + ')';
		})
		.style('text-anchor', 'middle')
		.style('fill', '#DF6C00')
		.html(function(d) {
			return '<tspan x="0" y="0.1em">' + ((d.data.parent === 'null') ? essaimtxt + ' ' : '') + d.data.name + '</tspan>';
		});
	node.append('a')
		.attr('xlink:href', function(d) {
			return urlessaim + d.data.id;
		})
		.attr('data-bs-toggle', 'popover')
		.attr('data-bs-trigger', 'hover')
		.attr('data-html', true)
		.attr('data-bs-content', function(d) {
			return essaimtxt + ' ' + d.data.name + '<br/>' +
				'&nbsp;&nbsp;&nbsp;' + (d.data.actif ? Actif : Inactif) + '<br/>' +
				'&nbsp;&nbsp;&nbsp;' + d.data.dateAcquisition + '<br/>' +
				ruchetxt + ' ' + ((d.data.nomRuche === 'null') ? '-' : d.data.nomRuche) + '<br/>' +
				ruchertxt + ' ' + ((d.data.nomRucher === 'null') ? '-' : d.data.nomRucher) + '<br/>' +
				MielKgtxt + ' ' +
				((d.data.poidsMiel === 0) ? '' : (d.data.poidsMiel / 1000.0).toLocaleString(lang, digits2))
				+ '<br/>' + MielDescKgtxt + ' ' +
				((d.data.poidsMielDescendance === 0) ? '' : (d.data.poidsMielDescendance / 1000.0).toLocaleString(lang, digits2))
				+ '<br/>';
		})
		.append('circle')
		.attr('r', 10)
		.style('fill', function(d) {
			return d.data.couleurReine;
		})
		.style('opacity', 0.6)
		.style('stroke', function(d) {
			if (d.data.name === essaimnom) {
				return 'black';
			}
		});
	// https://d3js.org/d3-zoom
	// https://observablehq.com/@d3/zoom?collection=@d3/d3-zoom
	svg.call(d3.zoom()
		.on('zoom', zoomed));
	function zoomed({ transform }) {
		g.attr('transform', transform);
	}
	document.querySelectorAll('[data-bs-toggle="popover"]').forEach(item => {
		new bootstrap.Popover(item, {
			html: true
		})
	});
});