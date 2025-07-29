$(function(){
	$('#resetExcelUpload').attr('disabled', true);
	$('#resetFileUpload').attr('disabled', true);
	$('#reset_result').css('display', 'none');
});
var dropzone,
    dropzonePreviewNode = document.querySelector("#dropzone-preview-list"),
    inputMultipleElements =
        ((dropzonePreviewNode.id = ""),
        dropzonePreviewNode &&
            ((previewTemplate = dropzonePreviewNode.parentNode.innerHTML),
            dropzonePreviewNode.parentNode.removeChild(dropzonePreviewNode),
            (dropzone = new Dropzone(".dropzone", 
            { 
            url: "/admin/productCenter/resetExcelUpload", 
            method: "post", 
            previewTemplate: previewTemplate, 
            previewsContainer: "#dropzone-preview",
            autoProcessQueue: false, // 자동으로 보내기. true : 파일 업로드 되자마자 서버로 요청,
										// false : 서버에는 올라가지 않은 상태. 따로
										// this.processQueue() 호출시 전송
		    clickable: true, // 클릭 가능 여부
		    autoQueue: true, // 드래그 드랍 후 바로 서버로 전송
// createImageThumbnails: true, //파일 업로드 썸네일 생성
		
// thumbnailHeight: 120, // Upload icon size
// thumbnailWidth: 120, // Upload icon size
		
		   maxFiles: 1, // 업로드 파일수
		   maxFilesize: 10000, // 최대업로드용량 : 100MB
		   paramName: 'file', // 서버에서 사용할 formdata 이름 설정 (default는 file)
		   parallelUploads: 1, // 동시파일업로드 수(이걸 지정한 수 만큼 여러파일을 한번에 넘긴다.)
		   uploadMultiple: false, // 다중업로드 기능
		   timeout: 300000000, // 커넥션 타임아웃 설정 -> 데이터가 클 경우 꼭 넉넉히 설정해주자
		
		   addRemoveLinks: true, // 업로드 후 파일 삭제버튼 표시 여부
// dictRemoveFile: '삭제', // 삭제버튼 표시 텍스트
		   acceptedFiles: '.xlsx', // ZIP 파일 포맷만 허용
		   
		   init: function () {
		      // 최초 dropzone 설정시 init을 통해 호출
		      console.log('최초 실행');
		      let dropzone = this; // closure 변수 (화살표 함수 쓰지않게 주의)
		
		      // 서버에 제출 submit 버튼 이벤트 등록
		      document.querySelector('#resetExcelUpload').addEventListener('click', function () {
		         console.log('업로드');
		
		         // 거부된 파일이 있다면
		         if (dropzone.getRejectedFiles().length > 0) {
		            let files = dropzone.getRejectedFiles();
		            console.log('거부된 파일이 있습니다.', files);
		            return;
		         }
		
		         dropzone.processQueue(); // autoProcessQueue: false로 해주었기 때문에, 메소드 api로 파일을 서버로 제출
		      });
		
		      // 파일이 업로드되면 실행
		      this.on('addedfile', function (file) {
				  $('#resetExcelUpload').attr('disabled', false);
		         // 중복된 파일의 제거
		      });
		
		      // 업로드한 파일을 서버에 요청하는 동안 호출 실행
		      this.on('sending', function (file, xhr, formData) {
		         console.log('파일을 업로드 하고 있습니다');
		      });
		
		      // 서버로 파일이 성공적으로 전송되면 실행
		      this.on('success', function (file, responseText) {
		         console.log(responseText[0]);
		         if(responseText[0] == 'success'){
					 alert('업로드가 성공 하였습니다.');
					 location.reload();
				 }else{
					 $('#reset_result').css('display', 'block');
					 for(x=0; x<responseText.length;x++){
						 $('#result_list').append('<p># ' + responseText[x] + '</p>');
					 }
				 }
		      });
		
		      // 업로드 에러 처리
		      this.on('error', function (file, errorMessage) {
		         console.log(errorMessage);
		      });
		   },
		}))),FilePond.registerPlugin(FilePondPluginFileEncode, FilePondPluginFileValidateSize, FilePondPluginImageExifOrientation, FilePondPluginImagePreview),
        document.querySelectorAll("input.filepond-input-multiple"));
var secdropzone,
	secdropzonePreviewNode = document.querySelector("#sec-dropzone-preview-list"),
    inputMultipleElements =
        ((secdropzonePreviewNode.id = ""),
        secdropzonePreviewNode &&
            ((previewTemplate = secdropzonePreviewNode.parentNode.innerHTML),
            secdropzonePreviewNode.parentNode.removeChild(secdropzonePreviewNode),
            (secdropzone = new Dropzone(".sec.dropzone", 
            		{ 
            	url: "/admin/productCenter/resetZipUpload", 
	            method: "post", 
	            previewTemplate: previewTemplate, 
	            previewsContainer: "#sec-dropzone-preview",
	            autoProcessQueue: false, // 자동으로 보내기. true : 파일 업로드 되자마자 서버로 요청,
											// false : 서버에는 올라가지 않은 상태. 따로
											// this.processQueue() 호출시 전송
			    clickable: true, // 클릭 가능 여부
			    autoQueue: true, // 드래그 드랍 후 바로 서버로 전송
	// createImageThumbnails: true, //파일 업로드 썸네일 생성
			
	// thumbnailHeight: 120, // Upload icon size
	// thumbnailWidth: 120, // Upload icon size
			
			   maxFiles: 1, // 업로드 파일수
			   maxFilesize: 10000, // 최대업로드용량 : 100MB
			   paramName: 'file', // 서버에서 사용할 formdata 이름 설정 (default는 file)
			   parallelUploads: 1, // 동시파일업로드 수(이걸 지정한 수 만큼 여러파일을 한번에 넘긴다.)
			   uploadMultiple: false, // 다중업로드 기능
			   timeout: 300000000, // 커넥션 타임아웃 설정 -> 데이터가 클 경우 꼭 넉넉히 설정해주자
			
			   addRemoveLinks: true, // 업로드 후 파일 삭제버튼 표시 여부
	// dictRemoveFile: '삭제', // 삭제버튼 표시 텍스트
			   acceptedFiles: '.zip', // ZIP 파일 포맷만 허용
			   
			   init: function () {
			      // 최초 dropzone 설정시 init을 통해 호출
			      console.log('최초 실행');
			      let secdropzone = this; // closure 변수 (화살표 함수 쓰지않게 주의)
			
			      // 서버에 제출 submit 버튼 이벤트 등록
			      document.querySelector('#resetFileUpload').addEventListener('click', function () {
			         console.log('업로드');
			
			         // 거부된 파일이 있다면
			         if (secdropzone.getRejectedFiles().length > 0) {
			            let files = secdropzone.getRejectedFiles();
			            console.log('거부된 파일이 있습니다.', files);
			            return;
			         }
			
			         secdropzone.processQueue(); // autoProcessQueue: false로 해주었기 때문에, 메소드 api로 파일을 서버로 제출
			      });
			
			      // 파일이 업로드되면 실행
			      this.on('addedfile', function (file) {
					  $('#resetFileUpload').attr('disabled', false);
			         // 중복된 파일의 제거
			      });
			
			      // 업로드한 파일을 서버에 요청하는 동안 호출 실행
			      this.on('sending', function (file, xhr, formData) {
			         console.log('파일을 업로드 하고 있습니다');
			      });
			
			      // 서버로 파일이 성공적으로 전송되면 실행
			      this.on('success', function (file, responseText) {
			         console.log(responseText[0]);
			         if(responseText[0] == 'success'){
						 alert('업로드가 성공 하였습니다.');
						 location.reload();
					 }else{
						 $('#reset_result').css('display', 'block');
						 for(x=0; x<responseText.length;x++){
							 $('#result_list').append('<p># ' + responseText[x] + '</p>');
						 }
					 }
			      });
			
			      // 업로드 에러 처리
			      this.on('error', function (file, errorMessage) {
			         console.log(errorMessage);
			      });
			   },
    		}))),
        FilePond.registerPlugin(FilePondPluginFileEncode, FilePondPluginFileValidateSize, FilePondPluginImageExifOrientation, FilePondPluginImagePreview),
        document.querySelectorAll("input.filepond-input-multiple"));





















