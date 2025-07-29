$(function() {
	$('#brandProductBrandId').on('change',function(){
		$.ajax({
			cache: false,
			type: 'POST',
			url: '/admin/brandBigSortSearch',
			data: {
				brandId: $('#brandProductBrandId :selected').val()
			}, success: function(result) {
				$('#brandProductBigSortId').find('option').remove();
				$('#brandProductBigSortId').append("<option> === 대분류 선택 === </option>");
				for (var i = 0; i < result.length; i++) {
					var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
					$('#brandProductBigSortId').append(option);
				}
			}

		});
		$('#brandProductBigSortId').on('change',function(){
			$.ajax({
				cache: false,
				type: 'POST',
				url: '/admin/brandMiddleSortSearch',
				data: {
					brandBigSortId: $('#brandProductBigSortId :selected').val()
				}, success: function(result) {
					$('#brandProductMiddleSortId').find('option').remove();
					$('#brandProductMiddleSortId').append("<option> === 중분류 선택 === </option>");
					for (var i = 0; i < result.length; i++) {
						var option = $("<option value=" + result[i].id + ">" + result[i].name + "</option>");
						$('#brandProductMiddleSortId').append(option);
					}
				}
	
			});
			
			$('#brandProductMiddleSortId').on('change', function(){
				
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



















