
fact(0, 1).

+fact(0, _) : true <- start; +fact(1, 1).

+fact(X, Y) : X > 0 & X < 10000 <- +fact(X + 1, (X + 1) * Y).

+fact(X, Y) : X == 10000 <- stop.