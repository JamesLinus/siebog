<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Contractor Agent - ContractNet Protocol</title>
<script src="../../client/jquery.min.js"></script>
<script src="../../client/uuid.js"></script>
<script type="text/javascript" src="../../client/fipaacl.js"></script>
<script type="text/javascript" src="../../client/radigost.js"></script>
<script type="text/javascript">
	var started = 0;

	$(document).ready(function() {
		$("#compId").focus();
		$("#jade").val(window.location.hostname + ":1099");
	});

	function ContractorCB() { }

	ContractorCB.prototype = new AgentListener();

	ContractorCB.prototype.onStart = function(aid) {
		++started;
		$("#log").text("Started " + started + " contractors");
	};
	
	var numRuns = 0;
	var resultAid = null; 
	
	function ResultCB() { }
	ResultCB.prototype = new AgentListener();
	ResultCB.prototype.onStep = function(aid, msg) {
		++numRuns;
		$("#result").append("<span>" + numRuns + ": " + msg + " ms</span>");
		$("#result").append("<br />");
	};
	
	function runJadeBridge() {
		$("#btnJade").attr("disabled", "disabled");
		$.get("/radigost/websocket?op=bridge&name=JadeBridge&host=" + $("#jade").val(), function(data) {
			$("#jadeStatus").text(data);
		});
	}
	
	function send2Manager(content, sender) {
		var aid = new AID("manager", $("#jade").val() + "/JADE", true);
		var msg = new ACLMessage(ACLPerformative.REQUEST);
		msg.receivers.push(aid);
		msg.content = content;
		if (typeof sender !== "undefined")
			msg.sender = sender;
		radigost.broadcast(msg);
	}
	
	function resetManager() {
		send2Manager('r');
	}

	function runContractors() {
		$("#btnCont").attr("disabled", "disabled");

		radigost.setDeviceId($("#device").val());

		// run agents
		var cb = new ContractorCB();
		var cid = $("#contId").val();
		var n = cid.indexOf("-");
		var cl = parseInt(cid.substring(0, n));
		var ch = parseInt(cid.substring(n + 1));
		for ( var i = cl; i < ch; i++)
			radigost.runAgent("Contractor" + i, "/radigost/samples/cnet/Contractor.js", cb);
		
		var resCb = new ResultCB();
		resultAid = radigost.runAgent("Result", "/radigost/samples/cnet/Result.js", resCb);
		
		send2Manager("a" + cid + "@" + $("#device").val());
	}
	
	function runManager() {
		send2Manager("x" + $("#msgLen").val(), resultAid);
	}
</script>
<style type="text/css">
body {
	margin: 32px;
}

label {
}

input {
	width: 140px;
}
</style>
</head>
<body>
	<div>
		<label for="device">Device ID:</label>
		<input type="text" id="device" value="<%= request.getRemoteAddr() %>">
	</div>
	<hr />
	<div>
		<label for="jade">JADE address:</label>
		<input type="text" id="jade" value="192.168.229.1:1099">
		<button id="btnJade" onclick="runJadeBridge()">Run JADE bridge</button>
		<span id="jadeStatus"></span>
	</div>
	<hr />
	<div>
		<button onclick="resetManager()">Reset Manager</button>
	</div>
	<hr />
	<div>
		<label for="contLow">Contractors (e.g. 0-4 for [0..4)):</label> 
		<input type="text" id="contId" value="0-1"><br />
		<button id="btnCont" onclick="runContractors()">Run contractors</button>
	</div>
	<div id="log"></div>
	<hr />
	<div>
		<label for="msgLen">Message content length (in chars):</label>
		<input type="text" id="msgLen" value="65536"><br />
		<button onclick="runManager()">Go!</button>
		<div id="result"></div>
	</div>
</body>
</html>