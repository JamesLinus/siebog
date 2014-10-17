
numbers([0]).
!addNextNum.

+!addNextNum : true <- 
	?numbers([OldHead | Tail]); 
	NewHead = OldHead + 1; 
	NewList = [NewHead, OldHead | Tail]; 
	-+numbers(NewList);
	printList(NewList);
	!!addNextNum.
