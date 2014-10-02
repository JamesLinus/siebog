
!start.

+!start : true <-
	?primeLimit(PrimeLimit);
	cfpStarted;
	.broadcast(tell, cfp(PrimeLimit));
	.print("CFPs sent.").
	
+propose(Res)[source(Participant)] : true <-
	?primeLimit(PrimeLimit);
	.send(Participant, tell, acceptProposal(PrimeLimit)).
	
+done(Res)[source(Participant)] : true <-
	taskCompleted(Res).
