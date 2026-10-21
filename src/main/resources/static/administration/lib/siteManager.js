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

























/* ── 회사소개 상단 배너 ────────────────────────────────────────────
 * 파일을 보내야 해서 FormData 로 올린다.
 * 보통 ajax 처럼 data 에 객체를 주면 파일이 안 실린다.
 * processData/contentType 을 false 로 두어야 브라우저가 경계 문자열을 만들어 준다. */
function aboutBannerUpload(){
	var ko = document.getElementById('aboutBannerFile');
	var en = document.getElementById('aboutBannerEnFile');
	var hasKo = ko && ko.files && ko.files.length > 0;
	var hasEn = en && en.files && en.files.length > 0;

	if(!hasKo && !hasEn){
		alert('올릴 그림을 고른 뒤 저장을 눌러 주세요.');
		return;
	}

	var fd = new FormData();
	if(hasKo) fd.append('aboutBannerFile', ko.files[0]);
	if(hasEn) fd.append('aboutBannerEnFile', en.files[0]);

	$.ajax({
		cache: false,
		url: '/admin/aboutBannerUpload',
		type: 'POST',
		data: fd,
		processData: false,
		contentType: false
	}).done(function(fragment){
		$('#aboutBannerForm').replaceWith(fragment);
		alert('배너를 올렸습니다.');
	}).fail(function(xhr){
		alert('올리지 못했습니다. ' + (xhr.responseText || '').slice(0, 200));
	});
}

/** 올린 배너를 치운다. 기본 그림으로 돌아간다. 파일 자체는 남는다 */
function aboutBannerClear(english){
	if(!confirm(english ? '영문 배너를 치울까요? 한글 배너가 대신 나갑니다.'
	                    : '한글 배너를 치울까요? 기본 그림이 나갑니다.')) return;

	$.ajax({
		cache: false,
		url: '/admin/aboutBannerClear',
		type: 'POST',
		data: { english: english }
	}).done(function(fragment){
		$('#aboutBannerForm').replaceWith(fragment);
	}).fail(function(){
		alert('치우지 못했습니다.');
	});
}
