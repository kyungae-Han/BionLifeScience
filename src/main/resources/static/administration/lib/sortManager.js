$(function() {
	$('#middleSortDeleteBtn').attr('disabled', true);

	$('#middlePanelMiddleSort').on('change', function() {
		$('#middleSortDeleteBtn').attr('disabled', false);
		$('#middleSortDeleteBtn').on('click', function() {
			var arr = new Array();
			arr = $('#middlePanelMiddleSort').val();
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/middleSortDelete',
				data: {
					text: arr,
					bigId: $('#middlePanelBigSort').val()
				}, error: function(error) {
					alert('해당 분류가 적용된 소분류를 삭제 후 시도해 주세요');
				}

			}).done(function(fragment) {
				if (fragment != 'fail') {
					$('#middlePanelMiddleSort').replaceWith(fragment);
					alert('삭제 되었습니다.');
					location.reload();
				} else {
					location.reload();
				}
			});
		});

	});

	$('#middlePanelBigSort').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/searchMiddleSort',
			data: {
				bigId: $(this).val()
			}, success: function(result) {
				$('#middlePanelMiddleSort').find('option').remove();
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#middlePanelMiddleSort').append(option);
				}
			}

		});
	});

	$('#bigSortDeleteBtn').attr('disabled', true);
	$('#bigPanelBigSort').on('change', function() {
		$('#bigSortDeleteBtn').attr('disabled', false);
		$('#bigSortDeleteBtn').on('click', function() {
			var arr = new Array();
			arr = $('#bigPanelBigSort').val();
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/bigSortDelete',
				data: {
					text: arr,
				}, error: function(error) {
					// console.log(error);
					alert('해당 분류가 적용된 중분류를 삭제 후 시도해 주세요');
				}

			}).done(function(fragment) {
				// console.log('frag' + fragment);
				if (fragment != 'fail') {
					$('#bigPanelBigSort').replaceWith(fragment);
					alert('삭제 되었습니다.');
					location.reload();
				} else {
					location.reload();
				}
			});
		});
	});

	$('#smallSortInsertForm').attr('disabled', true);
	$('#smallPanelBigSort').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/searchMiddleSort',
			data: {
				bigId: $(this).val()
			}, success: function(result) {
				$('#smallPanelMiddleSort').find('option').remove();
				$('#smallPanelMiddleSort').append("<option> === 중분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#smallPanelMiddleSort').append(option);
				}
			}
		});
		$('#smallPanelMiddleSort').on('change', function() {
			$('#smallSortInsertForm').attr('disabled', false);
		});
	});
	$('#smallSortDeleteBtn').attr('disabled', true);
	$('#smallPanelBigSortSelect').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/searchMiddleSort',
			data: {
				bigId: $(this).val()
			}, success: function(result) {
				$('#smallPanelMiddleSortSelect').find('option').remove();
				$('#smallPanelMiddleSortSelect').append("<option> === 중분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#smallPanelMiddleSortSelect').append(option);
				}
			}
		});

		$('#smallPanelMiddleSortSelect').on('change', function() {
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/searchSmallSort',
				data: {
					middleId: $(this).val()
				}, success: function(result) {
					$('#smallPanelSmallSortSelect').find('option').remove();
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#smallPanelSmallSortSelect').append(option);
					}
				}

			});
			$('#smallPanelSmallSortSelect').on('change', function() {
				$('#smallSortDeleteBtn').attr('disabled', false);
				$('#smallSortDeleteBtn').on('click',function(){
					
					var arr = new Array();
					arr = $('#smallPanelSmallSortSelect').val();
					$.ajax({
						cache: false,
						type: 'POST',
						url: '/admin/smallSortDelete',
						data: {
							text: arr,
						}, error: function(error) {
							// console.log(error);
							alert('해당 분류가 적용된 제품을 삭제 후 시도해 주세요');
						}
	
					}).done(function(fragment) {
						if (fragment != 'fail') {
							alert('삭제 되었습니다.');
							location.reload();
						} else {
							location.reload();
						}
					});
				});
			});
		});
	});

});





















