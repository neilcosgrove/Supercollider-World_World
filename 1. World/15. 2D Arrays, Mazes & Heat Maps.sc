////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                         2D Arrays                                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// expansions on Array2D

Int8_2D   : Base2D { arrayClass{ ^Int8Array   } }
Int16_2D  : Base2D { arrayClass{ ^Int16Array  } }
Int32_2D  : Base2D { arrayClass{ ^Int32Array  } }
Float_2D  : Base2D { arrayClass{ ^FloatArray  } }
Double_2D : Base2D { arrayClass{ ^DoubleArray } }
Symbol_2D : Base2D { arrayClass{ ^SymbolArray }
	init { arg argRows, argCols;
		rows = argRows;
		cols = argCols;
		array = SymbolArray(rows * cols);
		(rows * cols).do{ array = array.add(\nil) };
	}
}

Base2D : Array2D {

	arrayClass{^Array}

	init{|argRows, argCols|
		rows  = argRows;
		cols  = argCols;
		array = this.arrayClass.newClear(rows * cols);
	}

	colsDo{|func|
		cols.do{|ci|
			func.value( this.arrayClass.fill(rows,{|ri|
				this.at(ri,ci) }), ci )
		}
	}

	rowsDo{|func|
		rows.do{|ri|
			func.value( this.arrayClass.fill(cols,{|ci|
				this.at(ri,ci)
			}), ri )
		}
	}

	colAt{|ci|
		^this.arrayClass.fill(rows,{|ri|
			this.at(ri,ci)
		})
	}

}

+ Array2D {

	fill{|value| array.fill(value) }

	doXY{|func|
		cols.do{|y|
			rows.do{|x|
				func.value(x,y,this[x,y])
			}
		}
	}

	areaEquals{|x1, x2, y1, y2, value|
		for(y1,y2,{|y|
			for(x1,x2,{|x|
				if (this[x,y]!=value) {^false};
			});
		});
		^true
	}

	flop{} // TODO

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          2D Mazes                                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
Array2D.mazeRBT(20,20);
*/

+ Array2D {
	// a recursive back-tracker maze
	*mazeRBT{|w = 50, h = 50|
		var width = w.div(2) + (w.odd.binaryValue);
		var height = h.div(2) + (h.odd.binaryValue);
		var mazeVisited = this.new(width,height).fill(0); // all 0 at start
		var maze = this.new(w,h).fill(1); // all 1s at start
		var totalVisted = 1;
		var size = width * height;
		var stack = [ Point(0,0) ];
		mazeVisited[0,0] = 1;
		maze[0,0] = 0;
		if (w.even) {
			h.div(2).do{|y|
				0.66.coin.if{ maze[w-1,y*2] = 0 }
			};
		};
		if (h.even) {
			w.div(2).do{|x|
				0.66.coin.if{ maze[x*2,h-1] = 0}
			};
		};
		while {totalVisted < size} {
			var nextPos;
			var currentPos = stack.last;
			var x = currentPos.x;
			var y = currentPos.y;
			var possibilities = [];
			if (mazeVisited[x-1,y]==0) { possibilities = possibilities.add(Point(x-1,y)) };
			if (mazeVisited[x,y-1]==0) { possibilities = possibilities.add(Point(x,y-1)) };
			if (mazeVisited[x+1,y]==0) { possibilities = possibilities.add(Point(x+1,y)) };
			if (mazeVisited[x,y+1]==0) { possibilities = possibilities.add(Point(x,y+1)) };
			nextPos = possibilities.choose;
			if(nextPos.isNil) {
				stack.pop
			} {
				stack = stack.add(nextPos);
				mazeVisited[nextPos.x,nextPos.y] = 1;
				totalVisted = totalVisted + 1;
				maze[ nextPos.x * 2, nextPos.y * 2] = 0;
				maze[ currentPos.x + nextPos.x, currentPos.y + nextPos.y] = 0;
			};
		};
		^maze;
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          Heat Maps                                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// heat maps use hash to create random values that can be interp
// for example
/*
(0,   0.1 .. 10).collect(_.heatMap).plot;
(10, 10.1 .. 20).collect(_.heatMapSine).plot;
*/

+ Number {

	heatMap{
		var float = this.asFloat;
		var floor = float.floor.hash.wrap(0.0,1073741788)/1073741788;
		^floor + (this.frac * (float.ceil.hash.wrap(0.0,1073741788) / 1073741788 - floor))
	}

	heatMapSine{
		var float = this.asFloat;
		var floor = float.floor.hash.wrap(0.0,1073741788)/1073741788;
		^floor + ( (this.frac - 0.5 * pi).sin + 1 * 0.5 * (float.ceil.hash.wrap(0.0,1073741788)/1073741788 - floor))
	}

	heatMap2D{|y|
		var xFloor      = this.floor.asFloat;
		var yFloor      = y.floor.asFloat;
		var topLeft     = ((yFloor  )*10000+xFloor  ).hash.wrap(0.0,1073741788)/1073741788;
		var topRight    = ((yFloor  )*10000+xFloor+1).hash.wrap(0.0,1073741788)/1073741788;
		var bottomLeft  = ((yFloor+1)*10000+xFloor  ).hash.wrap(0.0,1073741788)/1073741788;
		var bottomRight = ((yFloor+1)*10000+xFloor+1).hash.wrap(0.0,1073741788)/1073741788;
		var xFrac       = this.frac;
		var yFrac       = y.frac;
		var top         = topLeft    + (xFrac * (topRight    - topLeft   ));
		var bottom      = bottomLeft + (xFrac * (bottomRight - bottomLeft));
		^top + (yFrac * (bottom - top))
	}

	heatMapSine2D{|y|
		var xFloor      = this.floor.asFloat;
		var yFloor      = y.floor.asFloat;
		var topLeft     = ((yFloor  )*10000+xFloor  ).hash.wrap(0.0,1073741788)/1073741788;
		var topRight    = ((yFloor  )*10000+xFloor+1).hash.wrap(0.0,1073741788)/1073741788;
		var bottomLeft  = ((yFloor+1)*10000+xFloor  ).hash.wrap(0.0,1073741788)/1073741788;
		var bottomRight = ((yFloor+1)*10000+xFloor+1).hash.wrap(0.0,1073741788)/1073741788;
		var xFrac       = (this.frac - 0.5 * pi).sin + 1 * 0.5;
		var yFrac       = (y.frac - 0.5 * pi).sin + 1 * 0.5;
		var top         = topLeft    + (xFrac * (topRight    - topLeft   ));
		var bottom      = bottomLeft + (xFrac * (bottomRight - bottomLeft));
		^top + (yFrac * (bottom - top))
	}

}

+ Function {

	heatMap2D{|xSize, ySize, xFreq=1, yFreq=1, minThresh=0, maxThresh=1, xSeed, ySeed|
		xSeed = xSeed ? (32768.rand);
		ySeed = ySeed ? (32768.rand);
		xSize.asInteger.do{|x|
			ySize.asInteger.do{|y|
				var value = (x * xFreq + xSeed).heatMap2D(y * yFreq + ySeed);
				if ((value>=minThresh) && (value<=maxThresh)) { this.value(x,y,value) };
			}
		}
	}

	heatMapSine2D{|xSize, ySize, xFreq=1, yFreq=1, minThresh=0, maxThresh=1, xSeed, ySeed|
		xSeed = xSeed ? (32768.rand);
		ySeed = ySeed ? (32768.rand);
		xSize.asInteger.do{|x|
			ySize.asInteger.do{|y|
				var value = (x * xFreq + xSeed).heatMapSine2D(y * yFreq + ySeed);
				if ((value>=minThresh) && (value<=maxThresh)) { this.value(x,y,value) };
			}
		}
	}

}

