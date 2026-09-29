function eventInsert(){
	var subject=$('#subject').val();
	var content = $('#content').val();
	// 영문 칸도 같이 보낸다. 빠뜨리면 서버가 빈 값으로 받아 저장할 때마다 영문이 지워진다
	var subjectEn = $('#subjectEn').val();
	var contentEn = $('#contentEn').val();
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
				subjectEn : subjectEn,
				contentEn : contentEn,
				link : link,
			},
		}).done(function(fragment){
			$('#eventForm').replaceWith(fragment);
			alert('적용 되었습니다.');
		})
	}
	
}
























