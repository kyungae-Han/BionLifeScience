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
	
	// ── 허용 TLD 관리 ──
	$('#tldInsertBtn').on('click', function(){
		var tld = $('#tldInput').val().trim();
		if(!tld){ alert('TLD를 입력해 주세요.'); return; }
		$.ajax({
			type:'POST', url:'/admin/tldInsert',
			data:{ tld: tld }
		}).done(function(){ alert('추가되었습니다.'); location.reload(); })
		  .fail(function(xhr){ alert(xhr.responseText || '오류가 발생했습니다.'); });
	});

	$('#tldDelBtn').on('click', function(){
		var ids = $('#tldDelSelect').val();
		if(!ids || ids.length === 0){ alert('삭제할 TLD를 선택해 주세요.'); return; }
		$.ajax({
			type:'POST', url:'/admin/deleteTld',
			data:{ ids: ids }
		}).done(function(){ alert('삭제되었습니다.'); location.reload(); });
	});

	// ── 허용 이메일 주소 관리 ──
	$('#allowedEmailInsertBtn').on('click', function(){
		var email = $('#allowedEmailInput').val().trim();
		if(!email){ alert('이메일을 입력해 주세요.'); return; }
		$.ajax({
			type:'POST', url:'/admin/allowedEmailInsert',
			data:{ email: email }
		}).done(function(){ alert('추가되었습니다.'); location.reload(); })
		  .fail(function(xhr){ alert(xhr.responseText || '오류가 발생했습니다.'); });
	});

	$('#allowedEmailDelBtn').on('click', function(){
		var ids = $('#allowedEmailDelSelect').val();
		if(!ids || ids.length === 0){ alert('삭제할 이메일을 선택해 주세요.'); return; }
		$.ajax({
			type:'POST', url:'/admin/deleteAllowedEmail',
			data:{ ids: ids }
		}).done(function(){ alert('삭제되었습니다.'); location.reload(); });
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
























