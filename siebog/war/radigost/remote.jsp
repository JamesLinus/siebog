<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Remote Examples</title>
<script type="text/javascript" src="/siebog/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="/siebog/radigost/radigost.js"></script>
<script type="text/javascript">
	var radigost = new Radigost("${pageContext.request.remoteAddr}", true);
	// Observer of received messages //
	function Observer() {
	}

	Observer.prototype = new AgentObserver();
	Observer.prototype.onStep = function(aid, msg) {
		$("<li>" + new Date() + " " + msg + "</li>").appendTo($("#remote-messages"));
	}

	function runAgent(name) {
		radigost.start("RemoteAgent.js", name, new Observer());
		setTimeout(reloadAgentList, 1500);
	}

	function reloadAgentList() {
		XJAF.getRunning(function(list) {
			var divList = $("#agent-list");
			divList.empty();
			for (var i = 0, len = list.length; i < len; i++) {
				var aid = list[i];
				var a = $("<a href='#'></a>").text(aid.str);
				askContentAndSendMessage(a, aid.name, aid.host);
				$("<p></p>").append(a).appendTo(divList);
			}
		});
	}
	
	function askContentAndSendMessage(a, targetName, targetHost) {
		a.click(function(e) {
			e.preventDefault();
			var content = prompt("Specify optional content to send:", "");
			if (content != null) {
				sendMessage(targetName, targetHost, content);
			}
		});		
	}
	
	function sendMessage(targetName, targetHost, content) {
		var msg = new ACLMessage(ACLPerformative.INFORM);
		msg.receivers = [ { name: targetName, host: targetHost, radigost: true } ];
		msg.content = "Radigost@" + radigost.host + " says: " + content;
		radigost.postToServer(msg);
	}
	
	$(function() {
		reloadAgentList();
	});
</script>
</head>
<body>
	<h3>An example which demonstrates how agents located in physically
		distributed, heterogeneous devices can communicate with each other.</h3>
	<div>
		<label for="this-name">To run an agent on this device, please
			specify its name:</label> <input id="this-name" type="text"> <a
			href="#"
			onclick="event.preventDefault(); runAgent($('#this-name').val()); $('#this-name').val('')">Start</a>
	</div>
	<div>
		<p>The following is a list of available agents. Click on any agent to sent it a message.</p>
		<div id="agent-list"></div>
		<p><a href="#" onclick="event.preventDefault(); reloadAgentList()">Reload
			agent list</a></p>
	</div>
	<div id="received">
		<p>Messages received from remote agents:</p>
		<ul id="remote-messages"></ul>
	</div>
</body>
</html>