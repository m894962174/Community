$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// //将CSRF令牌值存入到消息头中
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, options) {
	// 	xhr.setRequestHeader(header, token);
	// });

	var title=$("#recipient-name").val();
	var content=$("#message-text").val();

	$.post(
		 "/community/addDiscussPost",
		{"title":title,"content":content},
		function (data) {
			data=$.parseJSON(data);
			// 在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后,自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	);

}