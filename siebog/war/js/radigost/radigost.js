/**
 * Defines an agent listener. The object is used to receive information about the running agent,
 * e.g. "execution started", "computational step completed", etc.
 * 
 * @constructor
 */
function AgentListener() {
};

/**
 * Invoked right after the agent has been started. Called automatically, by the system.
 * 
 * @param {AID} aid Agent ID.
 */
AgentListener.prototype.onStart = function(aid) {
};

/**
 * Optional, invoked by the agent itself once it completes a single "computational step".
 * 
 * @param {AID} aid Agent ID.
 * @param {Object} msg Agent-specific data.
 */
AgentListener.prototype.onStep = function(aid, msg) {
};

/**
 * Invoked once the agent has been stopped. Called automatically, by the system.
 * 
 * @param {AID} aid Agent ID.
 */
AgentListener.prototype.onStop = function(aid) {
};

(function(global) {
	if ((typeof console === "undefined") || (console == null))
		console = {
			log : function(msg) {
			}
		};

	var radigost = new function() {
		this.device = uuid.v4();

		// running agents, AID -> worker, callback
		var running = new Object();
		
		/**
		 * Runs a new agent.
		 * 
		 * @param {string} name Locally-unique agent name.
		 * @param {string} url URL to the agent's source code.
		 * @param {AgentListener} callback Optional, AgentListener object.
		 * @param {object} args Optional, arguments to be passed to the agent.
		 * @returns {AID} AID of the running agent (either newly created or already existing).
		 */
		this.runAgent = function(name, url, callback, args) {
			var aid = new AID(name, this.device);
			if (get(aid) != null)
				return aid; // already running
			var ragigost_ = this;
			var obj = {};
			obj.callback = typeof callback === "object" ? callback : null;
			obj.worker = new Worker(url);
			obj.worker.onmessage = function(e) {
				var msg = e.data;
				// is this a message directed to me or to another agent?
				if (typeof msg.op === "number") {
					var obj = get(msg.aid);
					if ((obj != null) && (obj.callback != null))
						handleSysMsg(msg, obj);
				} else
					ragigost_.broadcast(msg);
			};
			obj.worker.onerror = function(e) {
				console.log(e.data);
			};
			put(aid, obj);
			// start the agent
			var msg = {};
			msg.aid = aid;
			msg.args = args;
			obj.worker.postMessage(msg);
			// inform the server
			ws.command("ag+", aid.value);
			return aid;
		};

		/**
		 * Posts an ACL message locally, i.e. to agents running within the same page. The method is
		 * faster than {@link #broadcast} and should be used when only local communication is
		 * needed.
		 * 
		 * @param {ACLMessage} msg ACLMessage object.
		 */
		this.post = function(msg) {
			for ( var i in msg.receivers) {
				var obj = get(msg.receivers[i]);
				if (obj != null)
					obj.worker.postMessage(msg);
			}
		};

		/**
		 * Posts an ACL message to agents that can be local (i.e. running within the same page) or
		 * server-side. The method is generally slower than {@link #post} and should be used in
		 * application that require both local and global agent communication.
		 * 
		 * @param {ACLMessage} msg ACLMessage object.
		 */
		this.broadcast = function(msg) {
			// the message needs to be sent through the socket only once
			var postedToSocket = false;
			for ( var i in msg.receivers) {
				var aid = msg.receivers[i];
				// to server?
				if (aid.server) {
					if (!postedToSocket) {
						postedToSocket = true;
						ws.send(JSON.stringify(msg));
					}
				} else {
					// look for the agent locally
					var obj = get(aid);
					if (obj != null)
						obj.worker.postMessage(msg);
				}
			}
		};

		var get = function(aid) {
			var obj = running[aid.value];
			return typeof obj === "undefined" ? null : obj;
		};

		var put = function(aid, obj) {
			running[aid.value] = obj;
		};

		var handleSysMsg = function(msg, obj) {
			switch (msg.op) {
			case 1:
				obj.callback.onStart(msg.aid);
				break;
			case 2:
				obj.callback.onStep(msg.aid, msg.msg);
				break;
			}
		};

		this.stopAgent = function(aid) {
			// TODO : implement stopAgent(aid)
			ws.command("ag-", aid.value);
		};

		this.setDeviceId = function(id) {
			if ((typeof id !== "undefined") && (id != null) && /[A-z0-9]+/.test(id))
				this.device = id;
		};
	};

	// WebSocket support
	var ws = new function() {
		this.connected = false;
		this.socket = null;
		this.cmdHdr = String.fromCharCode(0xff);

		/**
		 * Initializes the WebSocket object.
		 */
		this.initialize = function() {
			// create the WebSocket
			var url = "ws://" + window.location.host + "/radigost/websocket";
			if ("WebSocket" in global)
				this.socket = new WebSocket(url);
			else if ("MozWebSocket" in global)
				this.socket = new MozWebSocket(url);

			// event handlers
			if (this.socket != null) {
				var me = this;

				this.socket.onopen = function() {
					me.connected = true;
				};

				this.socket.onclose = function() {
					me.connected = false;
					me.socket = null;
					console.log("websocket connection closed");
				};

				this.socket.onmessage = function(e) {
					// assume the message is for local agents
					radigost.post(JSON.parse(e.data));
				};

				this.socket.onerror = function() {
					console.log("websocket connection error");
				};
			}
		};

		this.command = function(name, value) {
			this.send(this.cmdHdr + name + "=" + value);
		};

		this.send = function(str) {
			if (this.connected)
				this.socket.send(str);
		};
	};

	global.radigost = radigost;
	ws.initialize();
	// TODO : here we send the default radigost.device, but what if the client changes it?
	ws.command("run", radigost.device);
})(typeof window === "undefined" ? this : window);