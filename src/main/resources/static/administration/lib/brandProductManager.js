$(function() {


	$('#brandProductBrandId').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $('#brandProductBrandId :selected').val()
			}, success: function(result) {
				$('#brandProductBigSortId').find('option').remove();
				$('#brandProductBigSortId').append("<option> === 대분류 선택 === </option>");
				$('#brandProductMiddleSortId').find('option').remove();
				$('#brandProductMiddleSortId').append("<option> === 중분류 선택 === </option>");
				$('#brandProductSmallSortId').find('option').remove();
				$('#brandProductSmallSortId').append("<option> === 소분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#brandProductBigSortId').append(option);
				}
			}

		});

	});
	$('#brandProductBigSortId').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandMiddleSortSearch',
			data: {
				brandBigSortId: $('#brandProductBigSortId :selected').val()
			}, success: function(result) {
				$('#brandProductMiddleSortId').find('option').remove();
				$('#brandProductMiddleSortId').append("<option> === 중분류 선택 === </option>");
				$('#brandProductSmallSortId').find('option').remove();
				$('#brandProductSmallSortId').append("<option> === 소분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#brandProductMiddleSortId').append(option);
				}
			}

		});


	});
	$('#brandProductMiddleSortId').on('change', function() {

		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandSmallSortSearch',
			data: {
				brandMiddleSortId: $('#brandProductMiddleSortId :selected').val()
			}, success: function(result) {
				$('#brandProductSmallSortId').find('option').remove();
				$('#brandProductSmallSortId').append("<option> === 소분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#brandProductSmallSortId').append(option);
				}
			}

		});

	});
	$('#smallPanelSmallSort').on('change', function() {
		$('#brandSmallSortDeleteBtn').attr('disabled', false);
		$('#brandSmallSortDeleteBtn').on('click', function() {
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandSmallSortDelete',
				data: {
					brandSmallSortId: $('#smallPanelSmallSort :selected').val()
				}, success: function() {
					alert('쇼분류가 삭제 되었습니다.');
					location.reload();
				}, error: function(error) {
					alert('해당 분류가 적용된 소분류를 삭제 후 시도해 주세요');
					location.reload();
				}
			});
		});
	});
});



function brandProductDelete(id) {
	var result = confirm('해당 제품을 삭제 하시겠습니까?');
	if (result) {
		location.href = '/admin/brandProductDelete/' + id;
	}
}











