function getTypes(sparqlEndpoint){
	$("#cloud").empty().html('<img src="imgs/large-spinner.gif"/><div>Loading classes from ' + sparqlEndpoint + ' </div>');
	$.get('../types',{'sparql':sparqlEndpoint, 'source':'sparql'},function(data){
		 if(data.code == 'ok'){
			 $("#cloud").empty();
			 for(var i=0; i< data.classes.length; i+=1){
				 $("<a />").attr('href','#').attr("rel", data.classes[i].count).text(data.classes[i].curie).appendTo("#cloud");
				 
				 /*.click(function(dataset_url, typeUri){
						 return function(e){
					 		e.preventDefault();
					 		showItemOfType(voidUrl, dataset_url, typeUri);
						 };
				 }(datasetUri, data.classes[i].uri))*/
				
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