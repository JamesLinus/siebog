(function() {
	'use strict';

	angular
		.module('siebog.radigost')
		.factory('radigost', radigost);

	radigost.$inject = ['$websocket', 'xjaf', 'webClientOpCode', 'opCode', 'aid'];
	function radigost($websocket, xjaf, webClientOpCode, opCode, aid) {
		return {
			createRadigost: createRadigost
		};

		function createRadigost(host, autoCreateStubs) {
			var radigost = {
				host: host,
				running: {},
				autoCreateStubs: autoCreateStubs,
				socket: $websocket("ws://" + window.location.host + "/siebog/webclient"),
				onWorkerMessage: onWorkerMessage,
				start: start,
				postToServer: postToServer,
				postToClient: postToClient,
				post: post,
				getAgent: getAgent,
				putAgent: putAgent
			};

			radigost.socket.onMessage(function(message) {
				var msg = JSON.parse(message.data);
				if (typeof msg.sender === "string") {
					msg.sender = JSON.parse(msg.sender);
				}
				if (typeof msg.replyTo === "string") {
					msg.replyTo = JSON.parse(msg.replyTo);
				}
				for (var i = 0, len = msg.receivers.length; i < len; i++) {
					if (typeof msg.receivers[i] === "string") {
						msg.receivers[i] = JSON.parse(msg.receivers[i]);
					}
				}
				postToClient(msg);
			});

			radigost.socket.onOpen(function() {
				radigost.socket.send(webClientOpCode.REGISTER + radigost.host);
			});

			radigost.socket.onClose(function(e) {
				console.log("WebSocket connection closed.");
			});

			radigost.socket.onError(function(e) {
				console.log("WebSocket connection error: " + e.data);
			});

			return radigost;

			function onWorkerMessage(ev) {
				var msg = ev.data;
				if (typeof msg.opcode === "undefined") // a regular message
					post(msg);
				else {
					switch (msg.opcode) {
					case opCode.INIT:
						var ag = getAgent(msg.aid);
						if (ag !== null && ag.observer !== null)
							ag.observer.onStart(msg.aid);
						break;
					case opCode.STEP:
						var ag = getAgent(msg.aid);
						if (ag !== null && ag.observer !== null)
							ag.observer.onStep(msg.aid, msg.info);
						break;
					case opCode.MOVE_TO_SERVER:
						var ag = getAgent(msg.aid);
						if (ag !== null && ag.url !== null)
							xjaf.accept(ag.url, msg.aid, msg.state);
						break;
					default:
						throw new Error("Unrecognized OpCode: " + JSON.stringify(msg));
					}
				}
			};

			function start(url, name, agentObserver, agentInitArgs, recreate) {
				var newAid = aid.createAid(name, radigost.host);
				//if (getAgent(newAid) == null) {
					var agent = {};
					agent.url = url;
					agent.observer = agentObserver;
					
					agent.worker = new Worker(url);
					agent.worker.onmessage = onWorkerMessage;
					
					putAgent(newAid, agent);
					// initialize it
					var msg = {
						opcode: opCode.INIT,
						aid: newAid,
						args: agentInitArgs
					};
					agent.worker.postMessage(msg);
					if (radigost.autoCreateStubs) {
						// create the server-side stub
						var radigostStubAgent = {
			                agClass: {
			                    ejbName: "RadigostStub",
			                    module: "siebog",
			                    path: "/siebog/agents/xjaf"
			                },
			                name: name,
			                host: radigost.host
			            };
						if(!recreate) {
							xjaf.startAgent(radigostStubAgent);
						}
					}
				//}
				return newAid;
			};

			function postToServer(message) {
				xjaf.sendMessage(message);
			};

			function postToClient(message) {
				for (var i = 0, len = message.receivers.length; i < len; i++) {
					var agent = getAgent(message.receivers[i]);
					if (agent != null && agent.worker != null) {
						agent.worker.postMessage(message);
					}
				}
			};

			// uses the 'radigost' field of each receiver aid to determine if the agent
			// is on the client (true) or on the server (false)
			function post(message) {
				var server = [];
				for (var i = 0, len = message.receivers.length; i < len; i++) {
					var aid = message.receivers[i];
					if (aid.radigost) {
						var agent = getAgent(aid);
						if (agent !== null && agent.worker !== null)
							agent.worker.postMessage(message);
					} else {
						server.push(aid);
					}
				}
				// send to server?
				if (server.length > 0) {
					message.receivers = server;
					xjaf.post(message);
				}
			};

			function getAgent(aid) {
				if (radigost.running.hasOwnProperty(aid.str))
					return radigost.running[aid.str];
				return null;
			};

			function putAgent(aid, agent) {
				radigost.running[aid.str] = agent;
			};
		}
	}
})();