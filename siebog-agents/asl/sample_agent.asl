// Agent sample_agent in project jason_examples

/* Initial beliefs and rules */

value(5).

/* Initial goals */

+hello(N) : true <- doPrint(N).
-hello(_) : true <- doPrint(no_more_hello).

+value(0) : true <- doPrint(done).
+value(N) : N > 0 <- doPrint(N); -value(N); +value(N - 1).

/* Plans */

