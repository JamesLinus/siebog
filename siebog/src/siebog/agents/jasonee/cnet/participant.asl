
!start.

+!start : true <- .send(i, tell, participantReady(.my_name)).

+cfp(N)[source(Initiator)] : true <-
	analyzeCfp(N); 
	.send(Initiator, tell, propose(Num)).
	
+acceptProposal(N)[source(Initiator)] : true <-
	processTask(N);
	.send(Initiator, tell, done(Num)).