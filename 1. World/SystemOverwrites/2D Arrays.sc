+ Array2D {

	// fix out of bounds
	at { arg row, col;
		if (row<0) {^nil};
		if (col<0) {^nil};
		if (row>=rows) {^nil};
		if (col>=cols) {^nil};
		^array.at(row*cols + col)
	}

}