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
	
    const opt = $.extend(true, {
      height: 570,
      lang: 'ko-KR',
      placeholder: '',
      uploadUrl: '/admin/notice/image'
    }, options || {});

	
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

    // 저장할 때 편집기 내용을 원래 입력칸으로 옮긴다.
    //
    // 코드 보기 상태에서는 편집기가 입력칸을 갱신하지 않는다.
    // 그래서 코드 보기로 붙여 넣고 바로 저장하면 예전 내용이 저장됐다.
    // 저장이 됐다 안 됐다 하던 원인이다.
    //
    // 폼을 보내기 직전에 코드 보기를 끄고 내용을 한 번 더 넣어 준다.
    // 편집기를 쓰는 모든 화면에 같이 걸린다.
    // 한 화면에 편집기가 둘 이상일 때가 있다 (한글칸·영문칸).
    // 이름이 같으면 나중에 붙는 쪽이 앞의 것을 지워 버리므로 칸마다 다른 이름을 쓴다.
    var syncNs = 'submit.summernoteSync_' + ($editor.attr('id') || Math.random().toString(36).slice(2));

    $editor.closest('form').off(syncNs).on(syncNs, function () {
      try {
        var $note = $editor.next('.note-editor');
        var $codable = $note.find('.note-codable');

        // 코드 보기 상태면 거기 적힌 글자를 그대로 가져간다.
        // 편집 화면으로 되돌리면 편집기가 HTML 을 자기 방식대로 다시 써서
        // 붙여 넣은 태그가 바뀌거나 잘린다. 그래서 되돌리지 않고 원문을 쓴다.
        if ($codable.length && $codable.is(':visible')) {
          $editor.val($codable.val());
          return;
        }

        $editor.val($editor.summernote('code'));
      } catch (e) {
        // 편집기가 이미 사라진 경우는 그냥 넘어간다
      }
    });
  };

})(window, window.jQuery);
