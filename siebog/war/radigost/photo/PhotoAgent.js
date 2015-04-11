importScripts("/siebog/radigost.js");

function PhotoAgent() {
};

PhotoAgent.prototype = new Agent();

PhotoAgent.prototype.onInit = function(args) {
	this.image = args.image;
	this.targetClient = args.targetClient;
	this.sessionId = args.sessionId;
	this.moveToServer();
};

PhotoAgent.prototype.onArrived = function(host, isServer) {
	if (isServer) {
		var helper = this.getRadigostHelper();
		helper.persist(this.sessionId, this.image);
		helper.moveToClient(this.aid.str, this.targetClient);
	}
	else 
		this.onStep(this.image);
};

setAgentInstance(new PhotoAgent());