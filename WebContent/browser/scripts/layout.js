var sparqlEndpoint = "http://dbpedia.org/sparql";
$(function(){
	$("#collapse").click(function(){
		$("#header").toggle();
		var direction = $("#collpase_pointer").attr("src");
		if(direction.indexOf("_up") == -1){
			$("#collpase_pointer").attr("src","imgs/triangle_up.png");
		} else{
			$("#collpase_pointer").attr("src","imgs/triangle_down.png");
		}
	
	});
	$("#typesCloud").click(function(e){
		e.preventDefault();
		getTypes(sparqlEndpoint);
		$("#resourcesCloud").attr("class", "inactive");
		$("#typeesCloud").attr("class", "active");
	});
	$("#resourcesCloud").click(function(e){
		e.preventDefault();
		getResources(sparqlEndpoint);
		$("#resourcesCloud").attr("class", "active");
		$("#typeesCloud").attr("class", "inactive");
	});
});