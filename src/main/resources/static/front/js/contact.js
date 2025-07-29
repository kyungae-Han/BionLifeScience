$(function(){
	
	$("#file").on('change', function (e) {
    	if(e.target.files.length>5){
    		alert('파일은 최대 5까지 업로드 가능합니다.');
    		$(this).val('');
    		return;
    	}
    	var size = 0;
    	for(var i=0;i<e.target.files.length;i++){
    		size += e.target.files[i].size;
    	}
    	if(size > 20000000){
    		alert('1회 업로드 가능 용량은 20MB입니다.');
    		$(this).val('');
    		return;
    	}
    });

	$('#contactBtn').attr('disabled', true);
	$('#policyCheck').on('change',function(){
		if($(this).is(':checked')){
			$('#contactBtn').attr('disabled', false);
		}else{
			$('#contactBtn').attr('disabled', true);
		}
	});
});
  