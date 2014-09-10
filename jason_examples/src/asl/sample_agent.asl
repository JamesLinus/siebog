// Agent sample_agent in project jason_examples

/* Initial beliefs and rules */

value(5).

/* Initial goals */

+value(0) : true <- .print("done").
+value(N) : N > 0 <- .print(N); -value(N); +value(N - 1).

/* Plans */

