var depth = 2;
function drawTree(uri){
	var div = $('#tree');
	div.empty().html('<img src="imgs/large-spinner.gif"/><div>fetching info about ' + uri);
	$.get('../structure',{"resource":uri,"sparql":sparqlEndpoint,"depth":depth},function(data){
		if(data.code && data.code==='error'){
			alert(data.msg);
			div.empty().html('<div>Oops! something went wrong!</div>');
			return;
		}
		var mainTable = $('<table></table>').addClass('schema-alignment-dialog-canvas')[0];
		div.empty().append(mainTable);
		
		var update = function(filter_literals){
			div.empty().html('<img src="imgs/large-spinner.gif"/><div>hiding literals... </div>');
			var mainTable = $('<table></table>').addClass('schema-alignment-dialog-canvas')[0];
			div.empty().append(mainTable);
			var drawer = new TreeDrawer(data,mainTable,filter_literals);	
		};
		
		$('#hide_literals_tick').change(function(){
			update(this.checked);
		});
		var drawer = new TreeDrawer(data,mainTable,false);
	},"json");
}