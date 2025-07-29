var nestableSorted = document.getElementById('nested-sortable');
var nestedSortables = [].slice.call(
	document.querySelectorAll(".nested-sortable")
);
$('#indexChangeBtn').attr('disabled', true);
var exItems = document.querySelectorAll('.sortable-items');
var sortableItems = document.querySelectorAll('.sortable-items');
nestedSortablesHandles =
	(nestedSortables &&
		Array.from(nestedSortables).forEach(function(e) {
			new Sortable(e, {
				group: "nested",
				animation: 150,
				fallbackOnBody: !0,
				swapThreshold: 0.65,
				invertSwap: true,
				onChange: function(e) {
				},
				onAdd: function(e) {
				},
				onSort: function(e) {
				},
				onEnd: function(evt) {
					sortableItems = document.querySelectorAll('.sortable-items');
					$('#indexChangeBtn').attr('disabled', false);
					evt.item;  // dragged HTMLElement
					evt.to;    // target list
					evt.from;  // previous list
					evt.oldIndex;  // element's old index within old parent
					evt.newIndex;  // element's new index within new parent
					evt.oldDraggableIndex; // element's old index within old parent, only counting draggable elements
					evt.newDraggableIndex; // element's new index within new parent, only counting draggable elements
					evt.clone // the clone element
					evt.pullMode = false;  // when item is in another sortable: `"clone"` if cloning, `true` if moving
				},
				onMove: function(e) {
				}
			});
		}));
$('#indexChangeBtn').on('click', function() {
	console.log($('#code').val());
	var before = new Array();
	var after = new Array();
	for(a=0;a<sortableItems.length;a++){
		after[a] = sortableItems[a].id;
	}
	for(a=0;a<exItems.length;a++){
		before[a] = exItems[a].id;
	}
	Array.from(sortableItems).forEach(function(item) {
		console.log(item.id);
	})
	Array.from(exItems).forEach(function(item) {
		console.log(item.id);
	})
	$.ajax({
		type: 'POST',
		url: '/admin/productCenter/changeIndex',
		async:true,
		data: {
			exIndex: before,
			sortableIndex: after,
			code : $('#code').val()
		},success:function(){
			alert('INDEX가 변경 되었습니다.');
			location.reload();
		}
	})
});