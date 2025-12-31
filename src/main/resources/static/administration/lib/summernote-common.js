(function (w, $) {
  if (!$ || !$.summernote) return;

  // =========================
  // 1) 커스텀 버튼/플러그인 (1회 등록)
  // =========================
  $.summernote.plugins.imageSize100 = function (context) {
    context.memo('button.imageSize100', function () {
      return $.summernote.ui.button({
        contents: '100%',
        click: function () {
          const $img = $(context.invoke('editor.restoreTarget')).closest('img');
          $img.css('width', '100%');
        }
      }).render();
    });
  };

  $.summernote.plugins.imageSize75 = function (context) {
    context.memo('button.imageSize75', function () {
      return $.summernote.ui.button({
        contents: '75%',
        click: function () {
          const $img = $(context.invoke('editor.restoreTarget')).closest('img');
          $img.css('width', '75%');
        }
      }).render();
    });
  };

  $.summernote.plugins.imageSize50 = function (context) {
    context.memo('button.imageSize50', function () {
      return $.summernote.ui.button({
        contents: '50%',
        click: function () {
          const $img = $(context.invoke('editor.restoreTarget')).closest('img');
          $img.css('width', '50%');
        }
      }).render();
    });
  };

  $.summernote.plugins.imageSize25 = function (context) {
    context.memo('button.imageSize25', function () {
      return $.summernote.ui.button({
        contents: '25%',
        click: function () {
          const $img = $(context.invoke('editor.restoreTarget')).closest('img');
          $img.css('width', '25%');
        }
      }).render();
    });
  };

  $.extend($.summernote.plugins, {
    tableCellColor: function (context) {
      const ui = $.summernote.ui;

      function getSelectedCells() {
        const sel = window.getSelection();
        if (!sel.rangeCount) return [];
        const range = sel.getRangeAt(0);

        const fragment = range.cloneContents();
        const tempDiv = document.createElement('div');
        tempDiv.appendChild(fragment);
        const selectedCells = tempDiv.querySelectorAll('td,th');

        if (selectedCells.length > 0) {
          return Array.from(range.cloneContents().querySelectorAll('td,th'))
            .map(el => Array.from(document.querySelectorAll('td,th'))
              .find(c => c.textContent === el.textContent))
            .filter(Boolean);
        } else {
          const singleCell = $(range.startContainer).closest('td,th')[0];
          return singleCell ? [singleCell] : [];
        }
      }

      context.memo('button.tableBorderColor', function () {
        const $input = $('<input type="color" style="display:none;">').appendTo(document.body);

        $input.on('input', function () {
          const color = this.value;
          getSelectedCells().forEach(cell => cell.style.borderColor = color);
        });

        return ui.button({
          contents: '<i class="note-icon-magic"></i>',
          click: function () { $input.trigger('click'); }
        }).render();
      });

      context.memo('button.tableCellColor', function () {
        const $input = $('<input type="color" style="display:none;">').appendTo(document.body);

        $input.on('input', function () {
          const color = this.value;
          const sel = window.getSelection();
          if (!sel.rangeCount) return;

          context.invoke('editor.beforeCommand');

          const range = sel.getRangeAt(0);
          const container = range.commonAncestorContainer;

          let $cells = $(container).closest('td,th');
          if ($cells.length === 0 && container.querySelectorAll) {
            $cells = $(container).find('td,th');
          }

          if ($cells.length > 0) {
            $cells.each(function () { this.style.backgroundColor = color; });
          } else {
            const singleCell = $(range.startContainer).closest('td,th')[0];
            if (singleCell) singleCell.style.backgroundColor = color;
          }

          context.invoke('editor.afterCommand');
        });

        return ui.button({
          contents: '<i class="note-icon-pencil"></i>',
          click: function () { $input.trigger('click'); }
        }).render();
      });
    }
  });

  // =========================
  // 2) 업로드 AJAX (전역 함수)
  // =========================
  function uploadImageAjax(file, editor, opt) {
    const formData = new FormData();
    formData.append('file', file);

    $.ajax({
      url: opt.uploadUrl,
      type: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function (res) {
        if (res && res.url) {
          $(editor).summernote('insertImage', res.url);
        } else {
          alert('업로드 응답에 url이 없습니다.');
        }
      },
      error: function (xhr) {
        console.log(xhr.responseText);
        alert('이미지 업로드 실패: ' + xhr.status);
      }
    });
  }

  // =========================
  // 3) 에디터 초기화 함수 (페이지에서 호출)
  // =========================
  w.initSummernoteEditor = function (editorSelector, options) {
	
	const $editor = $(editorSelector);
	const dataUploadUrl = $editor.data('uploadUrl');
	
    const opt = $.extend(true, {
      height: 570,
      lang: 'ko-KR',
      placeholder: '',
      uploadUrl: null
    }, options || {});

	opt.uploadUrl = opt.uploadUrl || dataUploadUrl;
	
	
	if (!opt.uploadUrl) {
	   throw new Error('uploadUrl이 없습니다. options.uploadUrl 또는 data-upload-url을 지정하세요.');
	 }
	
    // 중복 초기화 방지
    if ($editor.data('summernote')) {
      $editor.summernote('destroy');
    }

    $editor.summernote({
      height: opt.height,
      focus: true,
      lang: opt.lang,
      placeholder: opt.placeholder,

      toolbar: [
        ['style', ['bold', 'italic', 'underline', 'strikethrough', 'clear']],
        ['font', ['fontsize', 'fontname', 'color']],
        ['height', ['height']],
        ['para', ['ul', 'ol', 'paragraph']],
        ['insert', ['link', 'picture', 'video', 'table', 'hr']],
        ['view', ['fullscreen', 'codeview', 'help']]
      ],

      popover: {
        image: [
          ['custom', ['imageSize100', 'imageSize75', 'imageSize50', 'imageSize25']],
          ['float', ['floatLeft', 'floatRight', 'floatNone']],
          ['remove', ['removeMedia']]
        ],
        table: [
          ['add', ['addRowDown', 'addRowUp', 'addColLeft', 'addColRight']],
          ['delete', ['deleteRow', 'deleteCol', 'deleteTable']],
          ['merge', ['mergeCell', 'splitCell']],
          ['custom', ['tableCellColor', 'tableBorderColor', 'tableBorderWidth']]
        ]
      },

      fontSizes: Array.from({ length: 65 }, (_, i) => (i + 8).toString()),
      fontNames: ['Arial', 'Verdana', 'Times New Roman', 'Noto Sans KR', 'IBM Plex Sans KR', '맑은 고딕', '궁서', '굴림'],
      styleTags: ['p', { title: 'Blockquote', tag: 'blockquote', className: 'blockquote', value: 'blockquote' }, 'pre', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'],
      lineHeights: ['0.8', '1.0', '1.2', '1.4', '1.5', '2.0', '3.0'],

      codeviewFilter: false,
      codeviewIframeFilter: false,

      callbacks: {
        onInit: function () {
          $editor.next('.note-editor').find('.note-editable').css({ 'background-color': '#fff' });
        },

        onChange: function () {
          const $cells = $editor.next('.note-editor').find('.note-editable table td');
          $cells.filter('.ui-resizable').resizable('destroy');
          $cells.resizable({
            handles: "e, s",
            minWidth: 50,
            minHeight: 30
          });
        },

        onImageUpload: function (files) {
          for (const f of files) uploadImageAjax(f, this, opt);
        },

        onPaste: function (e) {
          const cb = (e.originalEvent || e).clipboardData;
          if (!cb?.items) return;

          for (const item of cb.items) {
            if (item.type && item.type.startsWith('image/')) {
              e.preventDefault();
              uploadImageAjax(item.getAsFile(), this, opt);
              return;
            }
          }
        }
      }
    });
  };

})(window, window.jQuery);
