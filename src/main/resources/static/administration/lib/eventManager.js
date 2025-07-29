function eventInsert(){
	var subject=$('#subject').val();
	var content = $('#content').val();
	var link = $('#link').val();
	
	if(subject ==='' || content === '' || link === ''){
		alert('이벤트 정보를 모두 입력 해 주시기 바랍니다.');
	}else{
		$.ajax({
			cache:false,
			url:'/admin/eventInsert',
			type:'POST',
			data:{
				subject : subject,
				content : content,
				link : link,
			},
		}).done(function(fragment){
			$('#eventForm').replaceWith(fragment);
			alert('적용 되었습니다.');
		})
	}
	
}
























