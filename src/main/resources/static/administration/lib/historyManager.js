
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
	// 영문은 선택이다. 비우면 영문 화면에서 한글이 그대로 나간다
	var contentEn = $('#content-en-' + subjectId).val();
	var date = $('#content-date-' + subjectId).val();
	if(content != '' && date != ''){

		$.ajax({
			cache:false,
			type:'POST',
			url:'/admin/historyContentInsert',
			data:{
				contentSubject : content,
				contentSubjectEn : contentEn,
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

/* ── 고치기 ────────────────────────────────────────────────────────
 * 보기 칸과 고치기 칸이 둘 다 마크업에 들어 있다. 여기서는 번갈아 보여 주기만 한다.
 * 한 번에 한 줄만 열어 둔다. 여러 줄을 열어 두면 어느 것을 고치는 중인지 헷갈린다. */
function historyEditOpen(contentId){
	// 열려 있던 다른 줄은 닫는다
	$('.history-edit').hide();
	$('[id^="history-view-"]').show();

	$('#history-view-' + contentId).hide();
	$('#history-edit-' + contentId).show();
	$('#hist-ko-' + contentId).focus();
}

function historyEditClose(contentId){
	$('#history-edit-' + contentId).hide();
	$('#history-view-' + contentId).show();
}

function historyContentUpdate(contentId){
	var ko = $('#hist-ko-' + contentId).val();
	var en = $('#hist-en-' + contentId).val();
	var date = $('#hist-date-' + contentId).val();

	if(ko == null || ko.trim() === ''){
		alert('연혁 내용을 입력 해 주세요.');
		$('#hist-ko-' + contentId).focus();
		return;
	}
	if(date == null || date === ''){
		alert('연혁에 해당하는 날을 입력 해 주세요.');
		$('#hist-date-' + contentId).focus();
		return;
	}

	$.ajax({
		cache:false,
		type:'POST',
		url:'/admin/historyContentUpdate',
		data:{
			id : contentId,
			contentSubject : ko,
			contentSubjectEn : en,
			dateString : date
		}
	}).done(function(fragment){
		$('#history-subject-wrap').replaceWith(fragment);
		// 날짜를 바꾸면 차례가 달라진다. 다시 읽어서 제자리에 놓는다
		location.reload();
	}).fail(function(){
		alert('저장하지 못했습니다. 잠시 뒤 다시 시도해 주세요.');
	});
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
