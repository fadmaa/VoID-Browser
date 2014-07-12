$(function(){
	var r = TreeDrawer.getParams();
	var sparql = r["sparqlEndpoint"];
	var resource = r["resource"]; 
	var depth = r["depth"];
	if(!depth){
		depth = 3;
	}
	if(!sparql || !resource){
		alert("sparqlEndpoint and resource parameters are required!");
	}else{
		$('#tree').empty().html('<img src="imgs/large-spinner.gif"/><div>fetching info about ' + resource + ' from ' + sparql);
		$.get('structure',{"resource":resource,"sparql":sparql,"depth":depth},function(data){
			if(data.code && data.code==='error'){
				alert(data.msg);
				$('#tree').empty().html('<div>Oops! something went wrong!</div>');
				return;
			}
			var mainTable = $('<table></table>').addClass('schema-alignment-dialog-canvas')[0];
			$('#tree').empty().append(mainTable);
			
			var update = function(filter_literals){
				$('#tree').empty().html('<img src="imgs/large-spinner.gif"/><div>hiding literals... </div>');
				var mainTable = $('<table></table>').addClass('schema-alignment-dialog-canvas')[0];
				$('#tree').empty().append(mainTable);
				var drawer = new TreeDrawer(data,mainTable,filter_literals);	
			};
			
			$('#hide_literals_tick').change(function(){
				update(this.checked);
			});
			var drawer = new TreeDrawer(data,mainTable,false);
		},"json");
	}
	
});