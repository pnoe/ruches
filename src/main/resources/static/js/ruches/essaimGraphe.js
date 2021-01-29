/* jshint  esversion: 6, browser: true, jquery: true */
/* globals d3,
	data, essaimnom, essaimtxt,	ruchetxt, ruchertxt,
	MielKgtxt, MielDescKgtxt, urlessaim	*/

function essaimGraphe() {
	const dataMap = data.reduce(function(map, node) {
		map[node.name] = node;
		return map;
	}, {});
	const treeData = [];
	data.forEach(function(node) {
		let parent = dataMap[node.parent];
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
	const width = 1300 - margin.left - margin.right;
	const height = 700 - margin.top - margin.bottom;
	const treemap = d3.tree()
		.size([width, height]);
	let nodes = d3.hierarchy(treeData[0]);
	nodes = treemap(nodes);
	let svg = d3.select("#graph").append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom),
		g = svg.append("g")
			.attr("transform",
				"translate(" + margin.left + "," + margin.top + ")");
	g.selectAll(".link")
		.data(nodes.descendants().slice(1))
		.enter().append("path")
		.attr("class", "link")
		.attr("d", function(d) {
			return "M" + d.x + "," + d.y +
				"C" + d.x + "," + (d.y + d.parent.y) / 2 +
				" " + d.parent.x + "," + (d.y + d.parent.y) / 2 +
				" " + d.parent.x + "," + d.parent.y;
		});
	let node = g.selectAll(".node")
		.data(nodes.descendants())
		.enter().append("g")
		.attr("class", function(d) {
			return "node" +
				(d.children ? " node--internal" : " node--leaf");
		})
		.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
	node.append("a")
		.attr("xlink:href", function(d) {
			return urlessaim + d.data.id;
		})
		.append("circle")
		.attr("r", 10)
		.style("fill", function(d) {
			return d.data.couleurReine;
		})
		.style("stroke", function(d) {
			// return d.data.fillColor;
			if (d.data.name == essaimnom) {
				return "black";
			}
		});
	node.append("text")
		.attr("dy", ".35em")
		.attr("y", function(d) {
			return d.children ? -87 : 10;
		})
		.style("text-anchor", "middle")
		.html(function(d) {
			const l1 = "<tspan x='0' dy='1.2em'>" + ((d.data.parent == "null") ? essaimtxt + " " : "") + d.data.name + "</tspan>",
				l2 = "<tspan x='0' dy='1.2em'>" + ((d.data.parent == "null") ? ruchetxt + " " : "") +
					((d.data.nomRuche != 'null') ? d.data.nomRuche : "-") + "</tspan>",
				l3 = "<tspan x='0' dy='1.2em'>" + ((d.data.parent == "null") ? ruchertxt + " " : "") +
					((d.data.nomRucher != 'null') ? d.data.nomRucher : "-") + "</tspan>",
				l4 = "<tspan x='0' dy='1.2em'>" + ((d.data.parent == "null") ? MielKgtxt + " " : "") +
					((d.data.poidsMiel != 0) ? (d.data.poidsMiel / 1000.0).toFixed(2) : "") + "</tspan>",
				l5 = "<tspan x='0' dy='1.2em'>" + ((d.data.parent == "null") ? MielDescKgtxt + " " : "") +
					((d.data.poidsMielDescendance != 0) ? (d.data.poidsMielDescendance / 1000.0).toFixed(2) : "") + "</tspan>";
			return l1 + l2 + l3 + l4 + l5;
		});
}