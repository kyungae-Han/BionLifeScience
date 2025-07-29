
function deleteNotice(id){
	
	var result=confirm("공지사항을 삭제 하시겠습니까?");
	if(result){
		location.href="/admin/noticeDelete/" + id;
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
				}
				
			}).done(function(fragment){
				$('#textForm').replaceWith(fragment);
				alert('삭제 되었습니다.');
			})
		});
	});
	
});
























