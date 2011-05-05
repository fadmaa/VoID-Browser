function TreeDrawer(data,canvas){
	for(var i=0;i<data.rootResources.length;i++){
		var mainTR = canvas.insertRow(canvas.rows.length);
		TreeDrawer.drawNode(data.rootResources[i],mainTR);
	}
}

TreeDrawer.drawNode = function(node,tr){
//	var table = $('<table></table>').addClass('schema-alignment-table-layout')[0];
//	var tr = table.insertRow(table.rows.length);
	var tdMain = tr.insertCell(0);
	var tdToggle = tr.insertCell(1);
	var tdDetails = tr.insertCell(2);
//	$(canvasTD).append(table);
	
	$(tdMain).addClass('schema-alignment-node-main').addClass('padded');
	$(tdToggle).addClass('schema-alignment-node-toggle').css('width','1%');
	$(tdDetails).addClass('schema-alignment-node-details').css('width','96%');
	
	var html = $(
	    	'<table>' +
	    	  '<tr>' +
	    	    '<td bind="nodeLabel">' +
	    	    '</td>' +
	    	  '</tr>' +
	    	'</table>'
	    	  ).appendTo(tdMain)
	    	;
	var elmts = DOM.bind(html);
	var tdNodeLabel = elmts.nodeLabel;
	
	var a = TreeDrawer.getNode(node,tdNodeLabel); 
	
	var collapse =function(toHide,toShow){
		return function(e){
			toHide.hide();
			toShow.show();
			if(toShow.attr('class')==='schema-alignment-detail-container'){
				$(this).attr('src','imgs/expanded.png').click(collapse(toShow,toHide));
			}else{
				$(this).attr('src','imgs/collapsed.png').click(collapse(toShow,toHide));	
			}
		};
	};
	
	if(node.neighbours){
		var details_div = $('<div></div>').addClass('schema-alignment-detail-container');
		var padding_div = $('<div></div>').text('...').hide().appendTo(tdDetails);
		if(node.neighbours.length>0){
			var img = $('<img/>').attr('src','imgs/expanded.png').click(
					collapse(details_div,padding_div)
				).appendTo(tdToggle);
		}
		var details_table = $('<table></table>').addClass('schema-alignment-table-layout').appendTo(details_div)[0];
		$(tdDetails).append(details_div);
		for(var i=0;i<node.neighbours.length;i++){
			var details_tr = details_table.insertRow(details_table.rows.length);
			var edge_td = details_tr.insertCell(0);
			var edge_toggle_td = details_tr.insertCell(1);
			var details_td = details_tr.insertCell(2);
			$(edge_td).addClass('schema-alignment-link-main').addClass('padded');
			$(details_td).addClass('schema-alignment-link-details').attr('width','96%');
			
			var edge = node.neighbours[i];
			TreeDrawer.drawEdge(edge.properties,edge_td);
			
			var new_node_table = $('<table></table>').addClass('schema-alignment-table-layout')[0];
			var new_node_table_tr = new_node_table.insertRow(new_node_table.rows.length);
			var edge_details_div = $('<div></div>').addClass('schema-alignment-detail-container').append($('<div></div>').addClass('padded').css('display','none')).append(new_node_table).appendTo(details_td);
			var edge_padding_div = $('<div></div>').text('...').appendTo(details_td).hide();
			
			
			$(edge_toggle_td).addClass('schema-alignment-link-toggle').attr('width','1%')
			if(TreeDrawer.isExpandableEdge(edge)){
				var img = $('<img/>').attr('src','imgs/expanded.png').click(
					collapse(edge_details_div,edge_padding_div)
				).appendTo(edge_toggle_td);		
			}
			TreeDrawer.drawNode(edge.target,new_node_table_tr);
		}
	}

};

TreeDrawer.drawEdge = function(ps,canvasTD){
	var table = $('<table style="vertical-align:top;"></table>')[0];
	for(var i=0;i<ps.length;i++){
		var tr = table.insertRow(table.rows.length);
		var td = tr.insertCell(0);
		$('<a></a>').attr('href','javascript:{}').attr('title',ps[i].uri).addClass('schema-alignment-link-tag').quicktip({
			speed:300
		}).text(ps[i].curie).appendTo(td);
	}
	var a = $(canvasTD).attr('valign','top')
		.append($('<img/>').attr('src','imgs/arrow-start.png'))
		.append($('<div></div>').css('display','inline-block').append(table))
		.append($('<img/>').attr('src','imgs/arrow-end.png'));
};

TreeDrawer.getLabelForLiterals = function(literals){
	if(literals.length==1){
		return TreeDrawer.shorten(literals[0]);
	}else{
		return 'literals...';
	}
};

TreeDrawer.getLabelForResources = function(uris){
	if(uris.length==1){
		return uris[0].curie;
	}else{
		return 'resources...';
	}
};

TreeDrawer.getTitleForResources = function(uris){
	if(uris.length==1){
		return uris[0].uri;
	}else{
		//show first *limit* resources
		var limit = 10;
		var l = Math.min(limit,uris.length);
		var str = '';
		for(var i=0;i<l;i++){
			str += uris[i].uri + ' <br/>';
		}
		if(uris.length>limit){
			str += ' ... (more than ' + limit + ' resources)';
		}
		return str;
	}
};

TreeDrawer.getTitleForLiterals = function(literals){
	if(literals.length==1){
		return literals[0];
	}else{
		//show first *limit* resources
		var limit = 10;
		var l = Math.min(limit,literals.length);
		var str = '';
		for(var i=0;i<l;i++){
			str += literals[i] + ' <br/>';
		}
		if(literals.length>limit){
			str += ' ... (more than ' + limit + ' literals)';
		}
		return str;
	}
};

TreeDrawer.shorten = function(s){
	if(s.length>80){
		return s.substring(0,77) + "...";
	}
	return s;
};

TreeDrawer.getLabelForMixed = function(values){
	return 'Mixed: URIs & literals...';
};

TreeDrawer.getNode = function(node,tdNodeLabel){
	if(node.type==='resources'){
		if(node.uris && node.uris.length>1){
			$(tdNodeLabel).addClass('multiresources');
		}
		
		var a = $('<a></a>').attr('href','javascript:{}').addClass('schema-alignment-node-tag resources').appendTo(tdNodeLabel)
				.attr('title',TreeDrawer.getTitleForResources(node.uris))
				.click(function(){
					TreeDrawer.pivot(node.uris);
				})
				.quicktip();
		$('<span></span>').addClass('schema-alignment-node-column')
			.html('&nbsp;')
			.appendTo(a);
		return a;
	}else if(node.type==='mixed'){
		var a = $('<a></a>').attr('href','javascript:{}').addClass('schema-alignment-node-tag mixed').appendTo(tdNodeLabel)
				;
		$('<span></span>').addClass('schema-alignment-node-column')
			.text(TreeDrawer.getLabelForMixed(node.values))
			.appendTo(a);	
		return a;
	}else{
		if(node.literals && node.literals.length>1){
			$(tdNodeLabel).addClass('multiliterals');
		}
		var a = $('<a></a>').attr('href','javascript:{}').addClass('schema-alignment-node-tag literals').appendTo(tdNodeLabel)
				.attr('title',TreeDrawer.getTitleForLiterals(node.literals))
				.quicktip();
		$('<span></span>').addClass('schema-alignment-node-column')
			.html('&nbsp;')
			.appendTo(a);	
		return a;
	}
	
};

TreeDrawer.isExpandableEdge = function(edge){
	return edge.target && edge.target.type!=='literals' && edge.target.neighbours && edge.target.neighbours.length>1 ;
};

TreeDrawer.pivot = function(uris){
	var s = window.location.href;
	s = s.substring(0,s.indexOf("?")+1);
	var params = TreeDrawer.getParams(); 
	var depth = params["depth"]?params["depth"]:3;
	window.location.href = s + $.param({sparqlEndpoint:params["sparqlEndpoint"],resource:TreeDrawer.spaceSeparateUris(uris),depth:depth});
};

TreeDrawer.getParams = function(){
	var params = window.location.search;
	var r = {};
	if (params.length > 1) {
        params = params.substr(1).split("&");
        $.each(params, function() {
            pair = this.split("=");
            r[pair[0]] = unescape(pair[1]);
        });
    }
	return r;
};

TreeDrawer.spaceSeparateUris = function(uris){
	var s = "";
	for(var i=0;i<uris.length;i++){
		s += uris[i].uri + " ";
	}
	return s;
};