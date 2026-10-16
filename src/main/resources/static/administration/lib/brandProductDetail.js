// 줄을 지우거나 자리를 바꾼 뒤 specs[i] 번호를 0부터 다시 매긴다.
// 예전에는 이 함수가 수정 화면의 DOMContentLoaded 안에 들어 있어서
// 이 파일의 삭제 처리에서 부르면 ReferenceError 가 났다. 그러면 번호에 구멍이
// 남고(specs[0], specs[2]), 서버가 빈 줄을 하나 새로 만들려다 DB 에 걸려 fail 이 떴다.
// 어디서나 부를 수 있도록 여기 둔다.
function reindexSpecs() {
	var wrap = document.querySelector('#info-wrap');
	if (!wrap) return;
	wrap.querySelectorAll('.spec-item').forEach(function (item, idx) {
		item.querySelectorAll('input').forEach(function (input) {
			if (input.name) {
				input.name = input.name.replace(/specs\[\d+\]/, 'specs[' + idx + ']');
			}
		});
		// 저장된 줄은 specs[i].specOrder, 새로 더한 줄은 그냥 specOrder 다.
		// 예전에는 점이 붙은 쪽만 찾아서, 새 줄을 위로 올려도 순서가 안 바뀌었다.
		// 그래서 저장하면 늘 맨 끝으로 돌아갔다.
		var orderInput = item.querySelector('input[name="specOrder"], input[name$=".specOrder"]');
		if (orderInput) orderInput.value = idx + 1;
	});
}

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
				$('#brandProductBigSortId').append("<option value=''> === 대분류 선택 === </option>");
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
					$('#brandProductMiddleSortId').append("<option value=''> === 중분류 선택 === </option>");
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
						$('#brandProductSmallSortId').append("<option value=''> === 소분류 선택 === </option>");
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
		// 칸 너비와 간격은 bion-admin.css 의 격자가 잡는다. 여기서 적지 않는다
		var specDiv = $('<div class="spec-wrap">'
		+'<input type="text" name="spec" required="required" placeholder="제품 스펙을 입력 해 주세요. 예) 80mm*80mm*100mm" class="form-control">'+
		'<input type="text" name="specEn" placeholder="English (optional)" class="form-control">'+
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
	
	$('#info-plus-button').on('click', function(){
	  const wrap = $('#info-wrap');
	  // 단추 줄(.spec-wrap#info-foot)은 세지 않는다. 값이 든 줄만 센다
	  const nextOrder = wrap.find('.spec-item').length + 1;

	  // 칸 차례는 저장된 줄과 같아야 한다. 한 줄에 주제와 값이 나란히 서고,
	  // 윗줄이 한글 아랫줄이 영문이다. 차례가 어긋나면 새로 더한 줄만 다르게 읽힌다.
	  // 너비와 간격은 bion-admin.css 의 격자가 잡는다.
	  const infoDiv = $(`
	    <div class="spec-wrap spec-item">
	      <input type="checkbox" class="spec-check">
	      <input type="text" class="form-control" name="infoQ" required="required" placeholder="주제 (한글) 예) 제조자">
	      <input type="text" class="form-control" name="infoA" required="required" placeholder="값 (한글) 예) 바이온라이프사이언스">
	      <input type="text" class="form-control" name="infoQEn" placeholder="Subject in English (optional)">
	      <input type="text" class="form-control" name="infoAEn" placeholder="Value in English (optional)">
	      <input type="hidden" name="specOrder" value="${nextOrder}">
	      <div class="btn-group-vertical">
	        <button type="button" class="btn btn-light btn-sm move-up">▲</button>
	        <button type="button" class="btn btn-light btn-sm move-down">▼</button>
	      </div>
	    </div>
	  `);
	  // 단추가 맨 아래에 있으므로 새 줄은 그 앞에 끼운다
	  infoDiv.insertBefore('#info-foot');
	});
	$('#info-del-button').on('click', function () {
	    const $wrap = $('#info-wrap');
	    const $checkedItems = $wrap.find('.spec-item input[type="checkbox"]:checked');
	    const totalItems = $wrap.find('.spec-item').length;

	    if ($checkedItems.length === 0) {
	        alert('삭제할 항목을 선택해주세요.');
	        return;
	    }

	    if ($checkedItems.length === totalItems) {
	        alert('1개 이하로는 삭제할 수 없습니다.');
	        return;
	    }

	    // ✅ 선택된 항목들만 순회
	    $checkedItems.each(function () {
	        const $specItem = $(this).closest('.spec-item');
	        const specIdInput = $specItem.find('input[name$=".id"]');
	        const specId = specIdInput.val();

	        if (specId) {
	            // ✅ DB에 저장된 항목이면 Ajax 삭제
	            if (confirm(`이 항목(ID: ${specId})을 삭제하시겠습니까?`)) {
	                $.ajax({
	                    url: `/admin/brandProductSpecDelete/${specId}`,
	                    type: 'DELETE',
	                    success: function () {
	                        console.log(`✅ DB 삭제 완료: ${specId}`);
	                        $specItem.remove();
	                        reindexSpecs();
	                    },
	                    error: function (xhr) {
	                        console.error('❌ 삭제 실패', xhr);
	                        alert('삭제 중 오류가 발생했습니다.');
	                    }
	                });
	            }
	        } else {
	            // ✅ DB에 없는 항목은 그냥 제거
	            $specItem.remove();
	            reindexSpecs();
	        }
	    });
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



















