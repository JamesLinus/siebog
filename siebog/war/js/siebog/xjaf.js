(function(global) {
	var xjaf = new function() {

		this.getPerformatives = function(onSuccess) {
			$.ajax({
				type : "GET",
				url : "rest/messages/performatives",
				dataType : "json",
				success : onSuccess
			});
		};

		this.getDeployed = function(onSuccess) {
			$.ajax({
				type : "GET",
				url : "rest/agents/deployed",
				dataType : "json",
				success : onSuccess
			});
		};
		
		this.getRunning = function(onSuccess) {
			$.ajax({
				type : "GET",
				url : "rest/agents/running",
				dataType : "json",
				success : onSuccess
			});
		};
		
		this.start = function(moduleAndEjbName, runtimeName, onSuccess) {
			$.ajax({
				type : "PUT",
				url : "rest/agents/running/" + moduleAndEjbName + "/" + runtimeName,
				dataType : "multipart/form-data",
				success : onSuccess
			});
		};
	};
	global.xjaf = xjaf;
})(typeof window === "undefined" ? this : window);