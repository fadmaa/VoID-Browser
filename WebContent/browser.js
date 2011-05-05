$(function(){
	var r = TreeDrawer.getParams();
	var voidUri = r["void"];
	var depth = r["depth"];
	if(!depth){
		depth = 3;
	}
	if(!voidUri){
		alert("void URI is required!");
	}else{
		$('#tree').empty().html('<img src="imgs/large-spinner.gif"/><div>fetching info about ' + voidUri);
		$.get('voidbrowse',{"void":voidUri,"depth":depth},function(data){
			if(data.code && data.code==='error'){
				alert(data.msg);
				$('#tree').empty().html('<div>Oops! something went wrong!</div>');
				return;
			}
			var mainTable = $('<table></table>').addClass('schema-alignment-dialog-canvas')[0];
			$('#tree').empty().append(mainTable);
			var drawer = new TreeDrawer(data,mainTable);
		},"json");
	}
	
});