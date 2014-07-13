function getTypes(sparqlEndpoint){
	$("#cloud").empty().html('<img src="imgs/large-spinner.gif"/><div>Loading classes from ' + sparqlEndpoint + ' </div>');
	$.get('../types',{'sparql':sparqlEndpoint, 'source':'sparql'},function(data){
		 if(data.code == 'ok'){
			 $("#cloud").empty();
			 for(var i=0; i< data.classes.length; i+=1){
				 $("<a />").attr('href','#').attr("rel", data.classes[i].count).text(data.classes[i].curie)
				 		.appendTo("#cloud").click(function(typeUri){
						 return function(e){
					 		e.preventDefault();
					 		showItemOfType(typeUri);
						 };
				 }(data.classes[i].uri));
				
			 }
			 $("#cloud a").tagcloud({
			     size: {
			           start: 8, 
			           end: 24, 
			           unit: 'pt'
			         }, 
			         color: {
			           start: "#BBBBDD",
			           end: "#5D27CA"
			         }
				   });
			 
		 } else{
			 alert(data.msg);
		 }
	 },'json');
}

function getResources(sparqlEndpoint){
	$("#cloud").empty().html('<img src="imgs/large-spinner.gif"/><div>Loading resources from ' + sparqlEndpoint + ' </div>');
	$.get('../resources',{'sparql':sparqlEndpoint},function(data){
		 if(data.code == 'ok'){
			 $("#cloud").empty();
			 for(var i=0; i< data.resources.length; i+=1){
				 $("<a />").attr('href','#').attr("rel", data.resources[i].count).text(data.resources[i].curie)
				 		.appendTo("#cloud").click(function(resourceUri){
				 			return function(e){
				 				e.preventDefault();
				 				drawTree(resourceUri);
				 			};
				 		}(data.resources[i].uri));
			 }
			 $("#cloud a").tagcloud({
			     size: {
			           start: 8, 
			           end: 24, 
			           unit: 'pt'
			         }, 
			         color: {
			           start: "#BBBBDD",
			           end: "#5D27CA"
			         }
				   });
			 
		 } else{
			 alert(data.msg);
		 }
	 },'json');
}

function showItemOfType(typeUri){
	//get resources
	$("#cloud").empty().html('<img src="imgs/large-spinner.gif"/><div>Loading resources of type ' +  typeUri + ' from ' + sparqlEndpoint + ' </div>');
	$.get("../resourcesOfType",{'sparql':sparqlEndpoint,'type':typeUri},function(data){
		//populate the resources tab
		if(data.code == 'ok'){
			 $("#cloud").empty();
			 for(var i=0; i< data.resources.length; i+=1){
				 $("<a />").attr('href','#').attr("rel", data.resources[i].count).text(data.resources[i].curie)
				 		.appendTo("#cloud").click(function(resourceUri){
				 			return function(e){
				 				e.preventDefault();
				 				drawTree(resourceUri);
				 			};
				 		}(data.resources[i].uri));
			 }
			 $("#cloud a").tagcloud({
			     size: {
			           start: 8, 
			           end: 24, 
			           unit: 'pt'
			         }, 
			         color: {
			           start: "#BBBBDD",
			           end: "#5D27CA"
			         }
				   });
			 
		 } else{
			 alert(data.msg);
		 }
		
		$("#resourcesCloud").attr("class", "active");
		$("#typesCloud").attr("class", "inactive");
		//draw tree
		drawTree(data.resources[0].uri);
	},"json");
}