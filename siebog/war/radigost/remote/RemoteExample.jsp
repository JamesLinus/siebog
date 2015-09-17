<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Remote Examples</title>
<script type="text/javascript" src="/siebog/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="/siebog/radigost/radigost.js"></script>
<script type="text/javascript">
	var radigost = new Radigost("<%= request.getRemoteAddr() %>", true);
	var myAid = null;

	function runAgent() {
		var name = $("#this-name").val();
		myAid = radigost.start("RemoteAgent.js", name, new Observer());
	}

	function sendMessage() {
		var msg = new ACLMessage(ACLPerformative.REQUEST);
		var aid = new AID($("#remote-name").val(), radigost.host);
		msg.sender = myAid;
		msg.receivers = [ aid ];
		msg.content = $("#remote-content").val();
		radigost.postToServer(msg);
	}

	// Observer of received messages //
	function Observer() {
	}

	Observer.prototype = new AgentObserver();
	Observer.prototype.onStep = function(aid, msg) {
		$("<p>" + msg + "</p>").appendTo($("#remote-messages"));
	}
</script>
</head>
<body>
	<p>An example which demonstrates how agents located in physically
		distributed, heterogeneous devices can communicate with each other. To
		test it, first start an agent on this device. Then, you may send
		messages running on different devices.</p>
	<div>
		<p>&nbsp;</p>
		<form>
			<label for="this-name">Please provide a name for the agent
				running on this device:</label> <input id="this-name" type="text"> <a
				href="#" onclick="event.preventDefault(); runAgent(); $('#send-form').show(); $('#received').show()">Start</a>
		</form>
	</div>
	<p>&nbsp;</p>
	<div id="send-form" style="display: none">
		<form>
			<label for="remote-name">Name of the remote agent:</label> <input
				id="remote-name" type="text"> <label for="remote-content">Content
				to send:</label> <input id="remote-content" type="text"> <a href="#"
				onclick="event.preventDefault(); sendMessage()">Send</a>
		</form>
	</div>
	<div id="received" style="display: none">
		<p>&nbsp;</p>
		<p>Messages received from remote agents:</p>
		<div id="remote-messages"></div>
	</div>
</body>
</html>