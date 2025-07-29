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
					console.log(error)
				}

			}).done(function(fragment) {
				if (fragment != 'fail') {
					$('#middlePanelMiddleSort').replaceWith(fragment);
					alert('삭제 되었습니다.');
					location.reload();
				} else {
					alert('해당 분류가 적용된 소분류를 삭제 후 시도해 주세요');
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
					console.log(error)
				}

			}).done(function(fragment) {
				if (fragment != 'fail') {
					$('#bigPanelBigSort').replaceWith(fragment);
					alert('삭제 되었습니다.');
					location.reload();
				} else {
					alert('해당 분류가 적용된 중분류를 삭제 후 시도해 주세요');
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
					$('#smallPanelSmallSortSelect').append("<option value=''> === 소 분류 선택 === </option>");
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#smallPanelSmallSortSelect').append(option);
					}
				}

			});
			$('#smallPanelSmallSortSelect').on('change', function() {
				$('#smallSortDeleteBtn').attr('disabled', false);
				$('#smallSortDeleteBtn').on('click', function() {

					var arr = new Array();
					arr = $('#smallPanelSmallSortSelect').val();
					$.ajax({
						cache: false,
						type: 'POST',
						url: '/admin/smallSortDelete',
						data: {
							text: arr,
						}, error: function(error) {
							console.log(error)
						}

					}).done(function(fragment) {
						if (fragment != 'fail') {
							alert('삭제 되었습니다.');
							location.reload();
						} else {
							alert('해당 분류가 적용된 제품을 삭제 후 시도해 주세요');
							location.reload();
						}
					});
				});
			});
		});
	});
	$("#productFile").on('change', function (e) {
    	if(e.target.files.length>5){
    		alert('파일은 최대 5까지 업로드 가능합니다.');
    		$(this).val('');
    		return;
    	}
    	var size = 0;
    	for(var i=0;i<e.target.files.length;i++){
    		size += e.target.files[i].size;
    	}
    	if(size > 20000000){
    		alert('1회 업로드 가능 용량은 20MB입니다.');
    		$(this).val('');
    		return;
    	}
    });
	
	
	$('#spec-plus-button').on('click',function(){
		var specDiv = $('<div class="spec-wrap">'
		+'<input type="text" name="spec" required="required" placeholder="제품 스펙을 입력 해 주세요. 예) 80mm*80mm*100mm" class="form-control" style="width:80%;">'+
		'</div>');
		$(specDiv).appendTo('#spec-wrap');
	});
	$('#spec-del-button').on('click',function(){
		if($('#spec-wrap div').length < 3){
			alert('1개 이하로 삭제할 수 없습니다.');			
		}else{
			$('#spec-wrap').find('div:last').remove();
		}
	});
	
	$('#info-plus-button').on('click',function(){
		var infoDiv = $('<div class="spec-wrap">'
		+'<input type="text" class="form-control" name="infoQ" required="required" placeholder="주제를 입력 해 주세요. 예) 제조자" style="width:40%;margin-right:4px;">'+
		'<input type="text" class="form-control" name="infoA" required="required" placeholder="답변을 입력 해 주세요. 예) 바이온라이프사이언스" style="width:40%;">'+
		'</div>');
		$(infoDiv).appendTo('#info-wrap');
	});
	$('#info-del-button').on('click',function(){
		if($('#info-wrap div').length < 3){
			alert('1개 이하로 삭제할 수 없습니다.');			
		}else{
			$('#info-wrap').find('div:last').remove();
		}
	});
	$("#sighCheck").on('change', function() {
		alert('gd')
	  if ($(this).is(':checked')) {
		alert('gd111')
	    $(this).attr('value', true);
	  } else {
		alert('gd222')
	    $(this).attr('value', false);
	  }
	  
	});

	

});


function imgReset(id) {
	var target = document.getElementById(id);
	target.value = "";
	$('#sampleSection').empty();
}
var sel_files = [];
$(function() {
	$('#deImage').on("change", handleImgFileSelect);
});

function handleImgFileSelect(e) {
	sel_files = [];
	$('#sampleSection').empty();
	var files = e.target.files;
	var filesArr = Array.prototype.slice.call(files);
	var index = 0;
	try{
		filesArr.forEach(function(f) {
			if (!f.type.match("image*")) {
				
				$('#deImage').val('');
				$('#sampleSection').empty();
				throw new Error("Stop");
			}
			sel_files.push(f);
			var reader = new FileReader();
			reader.onload = function(e) {
				var html = "<a href=\"javascript:void(0);\" id=\"img_id_" + index
					+ "\"><img width='200' src=\"" + e.target.result + "\" data-file = '" + f.name + "' class='sample-img' title='Click to Remove'></a>";
				$('#sampleSection').append(html);
				index++;
			}
			reader.readAsDataURL(f);
		});
	}catch(e){
		alert('확장자는 이미지 확장자만 가능합니다');
	}
}



















