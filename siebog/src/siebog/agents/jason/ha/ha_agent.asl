
list([0]).
addNextNum.

+addNextNum : true <- 
	?list([H | T]); 
	H1 = H + 1; 
	NewList = [H1, H | T]; 
	-list([H | T]);
	+list(NewList);
	printList(NewList);
	-addNextNum.
	
-addNextNum : true <- +addNextNum. 
