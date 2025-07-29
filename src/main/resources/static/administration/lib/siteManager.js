function companyInfoInsert(){
	var name=$('#companyName').val();
	var address = $('#companyAddress').val();
	var number = $('#companyNumber').val();
	var tel = $('#companyTelephone').val();
	var check = $('#companyEmailCheck').val();
	
	if(tel ==='' || number === '' || address === '' || name === ''){
		alert('회사 정보를 모두 입력 해 주시기 바랍니다.');
	}else{
		$.ajax({
			cache:false,
			url:'/admin/companyInfoInsert',
			type:'POST',
			data:{
				companyName : name,
				companyAddress : address,
				companyNumber : number,
				companyTelephone : tel,
				companyEmailCheck: check
			},
		}).done(function(fragment){
			$('#companyInfoForm').replaceWith(fragment);
			alert('변경 되었습니다.');
		})
	}
	
}

function insertEmail(){
	var email = $('#email').val();
	if(email === ''){
		alert('이메일을 입력 해 주세요.');
	}else{
		$.ajax({
			cache:false,
			type:'POST',
			url:'/admin/emailInsert',
			data:{
				email : email
			}
		}).done(function(fragment){
			$('#emailForm').replaceWith(fragment);
			alert('추가 되었습니다.');
			location.reload();
		});
	}
}

$(function(){
	
	$('#emailDelBtn').attr('disabled', true);
	
	$('#emailDelSelect').change(function(){
		$('#emailDelBtn').attr('disabled', false);
		$('#emailDelBtn').on('click', function(){
			var arr = new Array();
			arr =  $('#emailDelSelect').val();
			$.ajax({
				cache:false,
				type:'POST',
				url:'/admin/deleteEmail',
				data:{
					email : arr
				}
				
			}).done(function(fragment){
				$('#emailForm').replaceWith(fragment);
				alert('삭제 되었습니다.');
				location.reload();
			})
		});
	});
	
	$('#emailStatusBtn').attr('disabled', true);
	$('#companyEmailCheck').on('change', function(){
		$('#emailStatusBtn').attr('disabled', false);
		$('#emailStatusBtn').on('click', function(){
			var companyEmailCheck = false;
			if($('#companyEmailCheck').is(':checked')){
				companyEmailCheck = true;
			}
			
			$.ajax({
				cache:false,
				type:'POST',
				url:'/admin/changeEmailStatus',
				data:{
					companyEmailCheck : companyEmailCheck,
				}
			}).done(function(fragment){
				alert('상태가 변경 되었습니다.');
				location.reload();
			})
		});
	});
	
});
























