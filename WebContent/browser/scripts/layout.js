var sparqlEndpoint = "http://dbpedia.org/sparql";
var rootResources = "";
$(function(){
	var params = window.location.search;
	var r = {};
	if (params.length > 1) {
        params = params.substr(1).split("&");
        $.each(params, function() {
            pair = this.split("=");
            r[pair[0]] = unescape(pair[1]);
        });
    }
	sparqlEndpoint = r['endpoint'];
	getTypes(sparqlEndpoint);
	$("#collapse").click(function(){
		$("#page-header").toggle();
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
		$("#typesCloud").attr("class", "active");
	});
	$("#resourcesCloud").click(function(e){
		e.preventDefault();
		getResources(sparqlEndpoint);
		$("#resourcesCloud").attr("class", "active");
		$("#typesCloud").attr("class", "inactive");
	});
	$("#tree-depth").change(function(){
		if(rootResources){
			drawTree(rootResources, $("#tree-depth").val());
		}
	});
});