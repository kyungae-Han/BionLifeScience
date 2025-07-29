$(function() {
	
	$('#brandBigSortDelBtn').attr('disabled', true);

	$('#bigPanelBrandId').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $(this).val()
			}, success: function(result) {
				$('#bigPanelBigSort').find('option').remove();
				$('#bigPanelBigSort').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#bigPanelBigSort').append(option);
				}
			}

		});
		
	});
	$('#bigPanelBigSort').on('change', function(){
		$('#brandBigSortDelBtn').attr('disabled', false);
		$('#brandBigSortDelBtn').on('click', function() {
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandBigSortDelete',
				data: {
					text: $('#bigPanelBigSort :selected').val(),
					brandId: $('#bigPanelBrandId :selected').val()
				},success:function(){
					alert('대분류가 삭제 되었습니다.');
					location.reload();
				}, error: function(error) {
					alert('해당 분류가 적용된 중분류를 삭제 후 시도해 주세요');
					location.reload();
				}
			});
		});
	});
	

	$('#middleSortBrandId').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $(this).val()
			}, success: function(result) {
				$('#middleSortBigSort').find('option').remove();
				$('#middleSortBigSort').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#middleSortBigSort').append(option);
				}
			}

		});
		
	});
	$('#brandMiddleSortDeleteBtn').attr('disabled', true);
	$('#middlePanelBrandSort').on('change', function() {
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $(this).val()
			}, success: function(result) {
				$('#middlePanelBigSortId').find('option').remove();
				$('#middlePanelBigSortId').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#middlePanelBigSortId').append(option);
				}
			}

		});
		$('#middlePanelBigSortId').on('change',function(){
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandMiddleSortSearch',
				data: {
					brandBigSortId: $('#middlePanelBigSortId :selected').val()
				}, success: function(result) {
					$('#middlePanelMiddleId').find('option').remove();
					$('#middlePanelMiddleId').append("<option> === 중분류 선택 === </option>");
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#middlePanelMiddleId').append(option);
					}
				}
	
			});
			$('#middlePanelMiddleId').on('change', function(){
				$('#brandMiddleSortDeleteBtn').attr('disabled', false);
				$('#brandMiddleSortDeleteBtn').on('click',function(){
					$.ajax({
						cache: false,
						type: 'POST',
						url: '/admin/brandMiddleSortDelete',
						data: {
							brandMiddleSortId: $('#middlePanelMiddleId :selected').val()
						},success:function(){
							alert('중분류가 삭제 되었습니다.');
							location.reload();
						}, error: function(error) {
							alert('해당 분류가 적용된 소분류를 삭제 후 시도해 주세요');
							location.reload();
						}
					});
				});
			});
			
		});
	});

	$('#smallPanelBrandId').on('change',function(){
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $('#smallPanelBrandId :selected').val()
			}, success: function(result) {
				$('#smallPanelBigSortId').find('option').remove();
				$('#smallPanelBigSortId').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#smallPanelBigSortId').append(option);
				}
			}

		});
		$('#smallPanelBigSortId').on('change',function(){
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandMiddleSortSearch',
				data: {
					brandBigSortId: $('#smallPanelBigSortId :selected').val()
				}, success: function(result) {
					$('#smallPanelMiddleSortId').find('option').remove();
					$('#smallPanelMiddleSortId').append("<option> === 중분류 선택 === </option>");
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#smallPanelMiddleSortId').append(option);
					}
				}
	
			});
		});
	});
	$('#brandSmallSortDeleteBtn').attr('disabled', true);
	$('#smallPanelBrandSort').on('change',function(){
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $('#smallPanelBrandSort :selected').val()
			}, success: function(result) {
				$('#smallPanelBigSort').find('option').remove();
				$('#smallPanelBigSort').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#smallPanelBigSort').append(option);
				}
			}

		});
		$('#smallPanelBigSort').on('change',function(){
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandMiddleSortSearch',
				data: {
					brandBigSortId: $('#smallPanelBigSort :selected').val()
				}, success: function(result) {
					$('#smallPanelMiddleSort').find('option').remove();
					$('#smallPanelMiddleSort').append("<option> === 중분류 선택 === </option>");
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#smallPanelMiddleSort').append(option);
					}
				}
	
			});
			
			$('#smallPanelMiddleSort').on('change', function(){
				
				$.ajax({
					cache: false,
					type: 'POST',
					url: '/admin/brandSmallSortSearch',
					data: {
						brandMiddleSortId: $('#smallPanelMiddleSort :selected').val()
					}, success: function(result) {
						$('#smallPanelSmallSort').find('option').remove();
						$('#smallPanelSmallSort').append("<option> === 소분류 선택 === </option>");
						for (var i = 0; i < result.length; i++) {
							var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
							$('#smallPanelSmallSort').append(option);
						}
					}
		
				});
				$('#smallPanelSmallSort').on('change', function(){
					$('#brandSmallSortDeleteBtn').attr('disabled', false);
					$('#brandSmallSortDeleteBtn').on('click', function(){
						$.ajax({
							cache: false,
							type: 'POST',
							url: '/admin/brandSmallSortDelete',
							data: {
								brandSmallSortId: $('#smallPanelSmallSort :selected').val()
							},success:function(){
								alert('소분류가 삭제 되었습니다.');
								location.reload();
							}, error: function(error) {
								alert('해당 분류가 적용된 소분류를 삭제 후 시도해 주세요');
								location.reload();
							}
						});
					});
				});
			});
		});
	});

});





















