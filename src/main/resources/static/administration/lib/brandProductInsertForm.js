$(function() {
	$('#smallPanelBrandSort').on('change',function(){
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $('#smallPanelBrandSort :selected').val()
			}, success: function(result) {
				$('#smallPanelBigSort').find('option').remove();
				$('#smallPanelBigSort').append("<option value=''> === 대분류 선택 === </option>");
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
					$('#smallPanelMiddleSort').append("<option value=''> === 중분류 선택 === </option>");
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
						$('#smallPanelSmallSort').append("<option value=''> === 소분류 선택 === </option>");
						for (var i = 0; i < result.length; i++) {
							var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
							$('#smallPanelSmallSort').append(option);
						}
					}
		
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
		// 칸 너비와 간격은 bion-admin.css 의 격자가 잡는다. 여기서 적지 않는다
		var specDiv = $('<div class="spec-wrap">'
		+'<input type="text" name="spec" required="required" placeholder="제품 스펙을 입력 해 주세요. 예) 80mm*80mm*100mm" class="form-control">'+
		'<input type="text" name="specEn" placeholder="English (optional)" class="form-control">'+
		'</div>');
		$(specDiv).appendTo('#spec-wrap');
	});
	$('#spec-del-button').on('click',function(){
		if($('#spec-wrap div').length < 2){
			alert('1개 이하로 삭제할 수 없습니다.');			
		}else{
			$('#spec-wrap').find('div:last').remove();
		}
	});
	
	$('#info-plus-button').on('click',function(){
		// 칸 차례는 수정 화면과 같다. 한 줄에 주제와 값, 윗줄 한글 / 아랫줄 영문.
		// 너비와 간격은 bion-admin.css 의 격자가 잡는다.
		var infoDiv = $('<div class="spec-wrap spec-item">'
		+'<input type="text" class="form-control" name="infoQ" required="required" placeholder="주제 (한글) 예) 제조자">'+
		'<input type="text" class="form-control" name="infoA" required="required" placeholder="값 (한글) 예) 바이온라이프사이언스">'+
		'<input type="text" class="form-control" name="infoQEn" placeholder="Subject in English (optional)">'+
		'<input type="text" class="form-control" name="infoAEn" placeholder="Value in English (optional)">'+
		'</div>');
		// 단추가 맨 아래에 있으므로 새 줄은 그 앞에 끼운다
		$(infoDiv).insertBefore('#info-foot');
	});
	$('#info-del-button').on('click',function(){
		// div:last 로 지우면 맨 아래 단추 줄이 지워진다. 값이 든 줄만 센다
		var $rows = $('#info-wrap').find('.spec-item');
		if($rows.length < 2){
			alert('1개 이하로 삭제할 수 없습니다.');			
		}else{
			$rows.last().remove();
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



















