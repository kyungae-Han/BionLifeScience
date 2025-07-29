
function insertNoticeSubject(){
	var text = $('#text').val();
	if(text === ''){
		alert('공지사항 분류를 입력 해 주세요.');
	}else{
		$.ajax({
			cache:false,
			type:'POST',
			url:'/admin/noticeSubjectInsert',
			data:{
				text : text
			}
		}).done(function(fragment){
			$('#textForm').replaceWith(fragment);
			alert('추가 되었습니다.');
			location.reload();
		});
	}
}

$(function(){
	$('#textDelBtn').attr('disabled', true);
	
	$('#textDelSelect').change(function(){
		$('#textDelBtn').attr('disabled', false);
		$('#textDelBtn').on('click', function(){
			var arr = new Array();
			arr =  $('#textDelSelect').val();
			$.ajax({
				cache:false,
				type:'POST',
				url:'/admin/noticeSubjectDelete',
				data:{
					text : arr
				},error:function(error){
					console.log(error)
				}
				
			}).done(function(fragment){
				if(fragment != 'fail'){
					$('#textForm').replaceWith(fragment);
					alert('삭제 되었습니다.');
					location.reload();	
				}else{
					alert('해당 분류가 적용된 공지사항들을 삭제 후 시도해 주세요');
					location.reload();
				}
			});
		});
	});
	
});
























