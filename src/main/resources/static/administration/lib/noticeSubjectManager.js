/* 공지사항 분류 관리.
   인증서 분류 관리(certificationSortManager)와 같은 방식이다.
   저장 주소는 /admin/noticeSubject* 다. */

$(function () {
	// 줄을 끌어서 순서를 바꾼다. 손잡이를 잡아야만 끌린다.
	// 표 전체를 잡히게 하면 입력칸을 누르다가도 끌려서 못 쓴다.
	$('#subjectRows').sortable({
		handle: '.bn-grip',
		axis: 'y',
		placeholder: 'bn-placeholder',
		helper: function (e, tr) {
			// 끌 때 칸 너비가 무너지지 않게 원래 너비를 박아 둔다
			var originals = tr.children();
			var helper = tr.clone();
			helper.children().each(function (i) { $(this).width(originals.eq(i).width()); });
			return helper;
		},
		update: function () {
			var ids = $('#subjectRows tr[data-id]').map(function () {
				return $(this).data('id');
			}).get();

			$.post('/admin/noticeSubjectOrder', { ids: ids })
				.done(function () {
					// 화면의 순서 번호도 다시 매긴다. 새로고침하지 않아도 맞게 보인다
					$('#subjectRows tr[data-id] .bn-index').each(function (i) {
						$(this).text(i + 1);
					});
				})
				.fail(function () {
					alert('순서를 저장하지 못했습니다. 새로고침하고 다시 해 주세요.');
					location.reload();
				});
		}
	});

	// 한글 이름 칸에서 엔터를 치면 바로 등록한다
	$('#newText, #newTextEn').on('keydown', function (e) {
		if (e.key === 'Enter') {
			e.preventDefault();
			noticeSubjectInsert();
		}
	});
});

function noticeSubjectInsert() {
	var ko = $('#newText').val().trim();
	var en = $('#newTextEn').val().trim();

	if (!ko) {
		alert('한글 이름을 입력해 주세요.');
		$('#newText').focus();
		return;
	}

	$.post('/admin/noticeSubjectInsert', { text: ko, textEn: en })
		.done(function (res) {
			if (res === 'DUP') {
				alert('같은 이름의 분류가 이미 있습니다.');
				return;
			}
			if (res !== 'ok') {
				alert('등록하지 못했습니다.');
				return;
			}
			location.reload();
		})
		.fail(function () {
			alert('등록하지 못했습니다.');
		});
}

function noticeSubjectUpdate(id) {
	var row = $('#subjectRows tr[data-id="' + id + '"]');
	var ko = row.find('.js-ko').val().trim();
	var en = row.find('.js-en').val().trim();

	if (!ko) {
		alert('한글 이름은 비울 수 없습니다.');
		row.find('.js-ko').focus();
		return;
	}

	$.post('/admin/noticeSubjectUpdate', { id: id, text: ko, textEn: en })
		.done(function (res) {
			if (res === 'DUP') {
				alert('같은 이름의 분류가 이미 있습니다.');
				return;
			}
			if (res !== 'ok') {
				alert('저장하지 못했습니다.');
				return;
			}
			alert('저장했습니다.');
		})
		.fail(function () {
			alert('저장하지 못했습니다.');
		});
}

function noticeSubjectDelete(id) {
	var row = $('#subjectRows tr[data-id="' + id + '"]');
	var used = parseInt(row.attr('data-used'), 10) || 0;

	// 서버에서도 막지만, 눌러 보기 전에 알려 주는 편이 낫다
	if (used > 0) {
		alert('이 분류에 속한 공지사항이 ' + used + '건 있어 삭제할 수 없습니다.\n'
			+ '먼저 해당 공지사항의 분류를 다른 것으로 옮겨 주세요.');
		return;
	}

	if (!confirm('[' + row.find('.js-ko').val() + '] 분류를 삭제하시겠습니까?')) {
		return;
	}

	$.post('/admin/noticeSubjectDelete', { id: id })
		.done(function (res) {
			if (typeof res === 'string' && res.indexOf('USED:') === 0) {
				alert('이 분류에 속한 공지사항이 ' + res.split(':')[1] + '건 있어 삭제할 수 없습니다.');
				return;
			}
			if (res !== 'ok') {
				alert('삭제하지 못했습니다.');
				return;
			}
			location.reload();
		})
		.fail(function () {
			alert('삭제하지 못했습니다.');
		});
}
