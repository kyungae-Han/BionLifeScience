$(function() {
	$('#brandDeleteBtn').attr('disabled', true);
	$('#brandPanel').on('change', function() {
	
		$('#brandDeleteBtn').attr('disabled', false);
		$('#brandDeleteBtn').on('click', function() {
			
			$.ajax({
				async: true,
				cache: false,
				type: 'POST',
				url: '/admin/brandDelete',
				data: {
					text: $('#brandPanel :selected').val(),
				},success:function(){
					alert('브랜드가 삭제 되었습니다.');
					location.reload();
				}, error: function(error) {
					location.reload();
					alert('해당 분류가 적용된 대분류를 삭제 후 시도해 주세요');
				}
			});
		});
	});

});
function brandDelete(id){
	var result = confirm("삭제 하시겠습니까?");
	if(result){
		$.ajax({
			async: true,
			cache: false,
			type: 'POST',
			url: '/admin/brandDelete',
			data: {
				text: id,
			},success:function(){
				alert('브랜드가 삭제 되었습니다.');
				location.reload();
			}, error: function(error) {
				location.reload();
				alert('해당 분류가 적용된 대분류를 삭제 후 시도해 주세요');
			}
		});
	}
}




















