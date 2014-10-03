
+cfp(N)[source(Initiator)] : true <- 
	print(1);
	analyzeCfp(N); 
	Num = 42;
	.send(Initiator, tell, propose(Num)).
	
+acceptProposal(N)[source(Initiator)] : true <-
	processTask(N);
	Num = 42;
	.send(Initiator, tell, done(Num)).