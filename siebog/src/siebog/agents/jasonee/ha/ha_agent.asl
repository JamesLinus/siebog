
numbers([0]).
addNextNum.

+addNextNum : true <- 
	?numbers([OldHead | OldTail]); 
	NewHead = OldHead + 1; 
	NewList = [NewHead, OldHead | OldTail]; 
	-numbers([OldHead | OldTail]);
	+numbers(NewList);
	printList(NewList);
	-addNextNum;
	+addNextNum. 
