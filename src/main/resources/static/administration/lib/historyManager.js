
function historySubjectInsert(){
	var start = $('#start').val();
	var end = $('#end').val();
	if(start != '' && end != ''){
		
		$.ajax({
			cache:false,
			url:'/admin/historySubjectInsert',
			type:'POST',
			data:{
				start:start,
				end:end,
			},
		
		}).done(function(fragment){
			$('#history-subject-wrap').replaceWith(fragment);
		});
	}else{
		alert('시작년도와 종료년도 모두 입력 해 주세요.');
	}
}

function historyContentInsert(subjectId){
	var content = $('#content-' + subjectId).val();
	var date = $('#content-date-' + subjectId).val();
	if(content != '' && date != ''){
		
		$.ajax({
			cache:false,
			type:'POST',
			url:'/admin/historyContentInsert',
			data:{
				contentSubject : content,
				dateString : date,
				subjectId : subjectId
			}
		}).done(function(fragment){
			$('#history-subject-wrap').replaceWith(fragment);
			location.reload();
		});
		
	}else {
		alert('날짜와 내용을 모두 입력 해 주세요.');
	}
		
}

function historyDelete(contentId , code){
	if(code === 1){
		var result = confirm('해당 년도에 속한 모든 연혁이 삭제됩니다. 진행하시겠습니까?');
		if(result){
				$.ajax({
				cache:false,
				type:'POST',
				url:'/admin/historyDelete',
				data:{
					code:code,
					id:contentId,
				},success:function(result){
					console.log(result);
				},error:function(error){
					console.log(error);
				}
			}).done(function(fragment){
				$('#history-subject-wrap').replaceWith(fragment);
				location.reload();
			});
		}
		
	}else if(code === 0){
		var result = confirm('해당 연혁을 삭제 하시겠습니까?');
		if(result){
			$.ajax({
				cache:false,
				type:'POST',
				url:'/admin/historyDelete',
				data:{
					code:code,
					id:contentId,
				},success:function(result){
					console.log(result);
				},error:function(error){
					console.log(error);
				}
			}).done(function(fragment){
				$('#history-subject-wrap').replaceWith(fragment);
			});
			
		}
	}
};


















