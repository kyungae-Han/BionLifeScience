$(function(){
	if(!searchType || searchType == 'none'){
		$('#codeSearch').hide();
		$('#sortSearch').hide();
		$('#periodSearch').hide();
	}else if(searchType==='period'){
		$('#codeSearch').hide();
		$('#sortSearch').hide();
		$('#periodSearch').show();
	}else if(searchType === 'sort'){
		$('#codeSearch').hide();
		$('#periodSearch').hide();
		$('#sortSearch').show();
	}else if(searchType === 'code'){
		$('#sortSearch').hide();
		$('#periodSearch').hide();
		$('#codeSearch').show();
	}
	$('#searchType').on('change',function(){
		if($('#searchType option:selected').attr('id')==='searchBasic'){
			$('#periodSearch').hide();
			$('#sortSearch').hide();
			$('#codeSearch').hide();
		}else if($('#searchType option:selected').attr('id')==='searchCode'){
			$('#periodSearch').hide();
			$('#sortSearch').hide();
			$('#codeSearch').show();
		}else if($('#searchType option:selected').attr('id')==='searchSort'){
			$('#periodSearch').hide();
			$('#sortSearch').show();
			$('#codeSearch').hide();
		}else if($('#searchType option:selected').attr('id')==='searchPeriod'){
			$('#sortSearch').hide();
			$('#codeSearch').hide();
			$('#periodSearch').show();
			$('#startDate').val(new Date().toISOString().slice(0, 10));
			$('#endDate').val(new Date().toISOString().slice(0, 10));
		}
	});
	
});