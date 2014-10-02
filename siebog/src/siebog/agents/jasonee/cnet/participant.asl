
+cfp(N)[source(Initiator)] : true <- 
	analyzeCfp(N); 
	Num = 42;
	.send(Initiator, tell, propose(Num)).
	
+acceptProposal(N)[source(Initiator)] : true <-
	processTask(N);
	Num = 42;
	.send(Initiator, tell, done(Num)).