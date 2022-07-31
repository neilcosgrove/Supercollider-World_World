////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                Tile Map                                            //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
t = World_Tile_Map(30,30); t.addTerrain; t.postMap;
t = World_Tile_Map(30,30); t.addBorder(2,1,true,false); t.postMap;
t = World_Tile_Map(100,30, defaultValue:1); t.addHorizontalTunnel(5); t.postMap;
Int16_2D(4,5).doXY{|x,y,value| [x,y,value].postln}
World_Scene.tileMap.postMap
*/

World_Tile_Map : World_World {

	classvar <mapToTextKey, <specialMapToTextKey, <>selectedTile, <>selectedXY;

	var <>map,           <mapWidth,      <mapHeight,         <tileWidth,   <tileHeight,   <mapTileSetMethod,  <defaultValue;
	var <flatSurfaceKey, <optimizedMap,  <emptyTiles,        <builtMap,    <>optimize = true;
	var <tileSet,        <keyToIndex,    <tileSetDefinition, <makeEmptyListsHasRun = false;
	var <tiles,          <allEntitiesInTileSet; // used in editor

	*init{
		if (verbose) { "World_Map:init".postln };
		// for text file coversion = 90 pos tiles
		mapToTextKey = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!£$%^&*()_+{}:~<>?=[];'\`,./";
		specialMapToTextKey = " -|@"; // neg -1 = empty, -2 = extendRight, -3 = extendDown, -4 = playerSpawn
	}

	*new{|mapWidth=12, mapHeight=8, tileWidth=50, tileHeight=50, mapTileSetMethod=\platformerTileSet, defaultValue = -1|
		^super.new.init(mapWidth, mapHeight, tileWidth, tileHeight, mapTileSetMethod, defaultValue)
	}

	init{|argMapWidth, argMapHeight, argTileWidth, argTileHeight, argMapTileSetMethod, argDefaultValue|
		mapWidth              = argMapWidth.asInteger;
		mapHeight             = argMapHeight.asInteger;
		tileWidth             = argTileWidth;
		tileHeight            = argTileHeight;
		mapTileSetMethod      = argMapTileSetMethod;
		defaultValue          = argDefaultValue;
		map                   = Int16_2D(mapWidth, mapHeight).fill(defaultValue);
		if (verbose) { "World_Tile_Map:%".format(mapTileSetMethod).postln };
		this.perform(mapTileSetMethod);
		tileSet               = tileSetDefinition.collect(_.value);
		keyToIndex            = IdentityDictionary[ \empty -> -1 ]; // empty is in all keys
		tileSetDefinition.do{|item,index| keyToIndex[item.key] = index };

		allEntitiesInTileSet = tileSetDefinition.collect{|association| association.value[0] }.asSet;

		this.makeFlatSurfaceKey;
	}

	// no tiles at all
	noTiles{
		if (verbose) { "World_Tile_Map:noTiles".postln};
		tileSetDefinition = [];
	}

	// make list of which entities are World_Rect_Tile and solid
	makeFlatSurfaceKey{
		flatSurfaceKey = [];
		tileSet.do{|list,index|
			// this needs to be done better £
			if ([ World_Rect_Tile, World_Rect_Tile2, World_Color_Rect_Tile].includes(list[0]) and: {list[1][7]}) {
				flatSurfaceKey = flatSurfaceKey.add(index) };
		};
	}

	// tools //////////////////////////////////////////////////////////////////////////////////////////

	addMaze{|value, blocks=1, l=0, t=0, r, b, randomGap=0, emptyCell|
		var maze;
		if (verbose) { "World_Tile_Map:addMaze".postln};
		value = value.asArray.collect{|key| keyToIndex[key] };
		if (emptyCell.notNil) { emptyCell = emptyCell.asArray.collect{|key| keyToIndex[key] } };
		r = r ? (mapWidth  - 1);
		b = b ? (mapHeight - 1);
		maze = Int8_2D.mazeRBT((r-l+1).div(blocks),(b-t+1).div(blocks));
		maze.doXY{|x,y,val|
			if ((randomGap.coin.not)or:{(x.odd) && (y.odd)}) {
				if (val==1) {
					blocks.do{|dx|
						blocks.do{|dy|
							var val2 = value.choose;
							map[x*blocks+l+dx, y*blocks+t+dy] = val2;
						}
					}
				}{
					if (emptyCell.notNil) {
						blocks.do{|dx|
							blocks.do{|dy|
								var val2 = emptyCell.choose;
								map[x*blocks+l+dx, y*blocks+t+dy] = val2;
							}
						};
					};
				};
			};
		};
	}

	addMazeWithBorder{|value, blocks=1, borderValue, border=1, l=0, t=0, r, b, randomGap=0, emptyCell|
		if (verbose) { "World_Tile_Map:addMazeWithBorder".postln};
		r = r ? (mapWidth  - 1);
		b = b ? (mapHeight - 1);
		this.addMaze(value, blocks, l + border, t + border, r-border, b-border, randomGap, emptyCell);
		this.addBorder(borderValue, border, border, border, border);
	}

	formatCells{|rules, l=1, t=1, r, b|
		var rules2 = IdentityDictionary[];
		if (verbose) { "World_Tile_Map:formatCells".postln};
		rules.keysValuesDo{|key,value| rules2[key.convertDigits(2)] = value };
		r = r ? (mapWidth  - 2);
		b = b ? (mapHeight - 2);
		for(l.clip(1,mapWidth-2), r.clip(1,mapWidth-2), {|x|
			for(t.clip(1,mapHeight-2), b.clip(1,mapHeight-2),{|y|
				var val = [
					(map[x-1,y-1]+1).clip(0,1),(map[x+0,y-1]+1).clip(0,1),(map[x+1,y-1]+1).clip(0,1),
					(map[x-1,y+0]+1).clip(0,1),(map[x+0,y+0]+1).clip(0,1),(map[x+1,y+0]+1).clip(0,1),
					(map[x-1,y+1]+1).clip(0,1),(map[x+0,y+1]+1).clip(0,1),(map[x+1,y+1]+1).clip(0,1)
				].convertDigits(2);
				val = rules2[val];
				if (val.notNil) { map[x,y] = keyToIndex[ ([] ++ val).choose ] } ;
			})
		})
	}

	addRect{|value, left, top, width, height|
		value = keyToIndex[value];
		for(left.clip(0,inf), (left + width).clip(0,mapWidth-1), {|x|
			for(top.clip(0,inf), (top + height).clip(0,mapHeight-1),{|y|
					map[x,y] = value;
			})
		})
	}

	// add a border to the outer edges.
	addBorder{|value, left = 1, top = 1, right = 1, bottom = 1|
		if (verbose) { "World_Tile_Map:addBorder".postln};
		value = keyToIndex[value];
		left.do{|i|	  this.fillX(i, value) };
		top.do{|i|	  this.fillY(i, value) };
		right.do{|i|  this.fillX(mapWidth  - 1 - i, value) };
		bottom.do{|i| this.fillY(mapHeight - 1 - i, value) };
	}

	// add random tiles inside the rectangle
	addRandomInRect{|value, n=1, l=0, t=0, r, b|
		if (verbose) { "World_Tile_Map:addRandomInRect".postln};
		value = value.asArray.collect{|key| keyToIndex[key] };
		r = r ? (mapWidth  - 1);
		b = b ? (mapHeight - 1);
		n.do{ map[l.rrand(r), t.rrand(b)] = value.choose };
	}

	// heat maps are easy ways of adding terrain
	addHeatMap{|tile, xFreq=1, yFreq=1, minThresh=0, maxThresh=1, xSeed, ySeed|
		if (verbose) { "World_Tile_Map:addHeatMap".postln};
		tile = keyToIndex[tile];
		{|x,y,value| map[x,y] = tile }.heatMapSine2D(mapWidth, mapHeight, xFreq, yFreq , minThresh, maxThresh, xSeed, ySeed);
	}

	// heat maps are easy ways of adding terrain
	addHeatMapWithDepth{|tile1, tile2, depthThres=0.8, xFreq=1, yFreq=1, minThresh=0, maxThresh=1, xSeed, ySeed|
		if (verbose) { "World_Tile_Map:addHeatMapWithDepth".postln};
		tile1 = keyToIndex[tile1];
		tile2 = keyToIndex[tile2];
		{|x,y,value|
			map[x,y] = (value>depthThres).if(tile1,tile2)
		}.heatMapSine2D(mapWidth, mapHeight, xFreq, yFreq , minThresh, maxThresh, xSeed, ySeed);
	}

	// i can add alot more to this (start @, end @, max slope)
	addHorizontalTunnel{|value = \empty, width = 4|
		var seed, oddAdd;
		if (verbose) { "World_Tile_Map:addHorizontalTunnel".postln };
		value = keyToIndex[value];
		seed   = 10737.rand;
		width  = (width - 1).clip(0,inf).asInteger;
		oddAdd = width.odd.if(1,0);
		width  = width.div(2);
		mapWidth.do{|x|
			var y = 1.mixClip ( (x*0.055 + seed).heatMapSine, x/30 ) * (mapHeight - width - width) + width;
			for( (y - width - oddAdd).clip(0,mapHeight - 1), (y + width).clip(0,mapHeight - 1), {|y2| map[x,y2] = value });
		};
	}


	// TODO
	addVerticalTunnel{}

	// add triangles to smooth the corners
	addCorners{|solid, empty, bL, bR, tL, tR, iTL, iTR|
		if (verbose) { "World_Tile_Map:addCorners".postln };
		solid = solid.asArray.collect{|key| keyToIndex[key] }; // solid tiles
		empty = empty.asArray.collect{|key| keyToIndex[key] }; // empty tiles
		bL    = keyToIndex[ bL ];
		bR    = keyToIndex[ bR ];
		tL    = keyToIndex[ tL ];
		tR    = keyToIndex[ tR ];
		iTL   = keyToIndex[ iTL ];
		iTR   = keyToIndex[ iTR ];
		(mapHeight-2).do{|y|
			y = y + 1;
			(mapWidth-2).do{|x|
				x = x + 1;
				if (
	             // this is broken, some how bottom row doesn't always work. is it always bottom row?
					(empty.includes(map[x+0, y+0])) &&   // EE.
					(empty.includes(map[x+0, y-1])) &&   // EES
					(empty.includes(map[x-1, y-1])) &&   // .S.
					(empty.includes(map[x-1, y+0])) &&
					(solid.includes(map[x+1, y+0])) &&
					(solid.includes(map[x+0, y+1]))
				){
					map[x,y] = bR; // bottom right
					map[x,y+1] = iTL;
				};
				if (
					(empty.includes(map[x+0, y+0])) &&   // .EE
					(empty.includes(map[x+0, y-1])) &&   // SEE
					(empty.includes(map[x+1, y-1])) &&   // .S.
					(empty.includes(map[x+1, y+0])) &&
					(solid.includes(map[x-1, y+0])) &&
					(solid.includes(map[x+0, y+1]))
				){
					map[x,y] = bL; // bottom left
					map[x,y+1] = iTR;
				};
			}
		};
		(mapWidth-2).do{|x|
			x = x + 1;
			(mapHeight-2).do{|y|
				y = y + 1;
				if (
					(empty.includes(map[x+0, y+0])) &&
					(empty.includes(map[x+0, y+1])) &&
					(empty.includes(map[x+1, y+1])) && //
					(empty.includes(map[x+1, y+0])) &&
					(solid.includes(map[x-1, y+0])) &&
					(solid.includes(map[x+0, y-1]))
				){
					map[x,y] = tL; // top left
				};
				if (
					(empty.includes(map[x+0, y+0])) &&
					(empty.includes(map[x+0, y+1])) &&
					(empty.includes(map[x-1, y+1])) && //
					(empty.includes(map[x-1, y+0])) &&
					(solid.includes(map[x+1, y+0])) &&
					(solid.includes(map[x+0, y-1]))
				){
					map[x,y] = tR; // top right
				};
			};
		};
	}

	// replace the surface layer of floor of the terrain
	replaceFloor{|replaceThis, withThis, empty|
		if (verbose) { "World_Tile_Map:replaceFloor".postln };
		replaceThis = replaceThis.asArray.collect{|key| keyToIndex[key] };
		withThis = withThis.asArray.collect{|key| keyToIndex[key] };
		empty = empty.asArray.collect{|key| keyToIndex[key] }; // empty tiles
		(mapHeight-1).do{|y|
			mapWidth.do{|x|
				if (empty.includes(map[x,y])) {
					var indexOf = replaceThis.indexOf(map[x, y+1]);
					if (indexOf.isNumber) { map[x, y+1] = withThis[indexOf] };
				};
			};
		};
	}

	// add trees & decor
	addDecor{|items, weights, n=1|
		if (verbose) { "World_Tile_Map:addDecor".postln };
		this.checkEmptyListHasRun;
		items = items.asArray.collect{|key| keyToIndex[key] };
		n.do{|i|
			var j     = emptyTiles[\floor2x1].size.rand;
			var tile  = emptyTiles[\floor2x1][j];
			var x     = tile[0];
			var y     = tile[1];
			var value = items.wchooseNorm(weights);
			if (map[x,y] == (-1)) { map[x,y] = value };
		};
	}

	// add n number of NPC to the map
	addRandNPCs{|items, n=1|
		if (verbose) { "World_Tile_Map:addRandNPCs".postln };
		this.checkEmptyListHasRun;
		items = items.asArray.collect{|key| keyToIndex[key] };
		n.do{|i|
			var emptyTile  = emptyTiles[\allTiles][ (1.0.rand ** 0.75 * (emptyTiles[\allTiles].size-1)).asInteger ];
			if ( map[ emptyTile[0], emptyTile[1] ] == (-1)) {                // we may loose some
				map[ emptyTile[0], emptyTile[1] ] = items.wrapAt(i); // alternate blue yellow
			};
		};
	}

	// add n objects to the map
	addItem{|tileName,noItems|
		var objectIndex = keyToIndex[tileName];
		if (verbose) { "World_Tile_Map:addItem".postln };
		this.checkEmptyListHasRun;
		noItems.do{|i|
			var x = (mapWidth - 4 / noItems * i + 2).asInteger;
			if (emptyTiles[\floorTileColums][x].size>0) {
				var y    = emptyTiles[\floorTileColums][x].size.rand;
				var list = emptyTiles[\floorTileColums][x][y];
				 if ( map[ list[0], list[1] ] == (-1)) {                // we may loose some
				 	map[ list[0], list[1] ] = objectIndex;
					// do i want this? how to handle light maps?
				 	lightMap.addStaticLightSource( list[0] + 0.5 * tileWidth, list[1] + 0.5 * tileHeight, 1, 6);
				 };
			};
		}
	}

	// replace a set of tiles in a rect with another set of tiles
	replaceInRect{|left, top, width, height, what, with|
		what = what.asArray.collect{|key| keyToIndex[key] };
		with = with.asArray.collect{|key| keyToIndex[key] };
		for(left.clip(0,inf), (left + width).clip(0,mapWidth-1), {|x|
			for(top.clip(0,inf), (top + height).clip(0,mapHeight-1),{|y|
				var index = what.indexOf(map[x,y]);
				if (index.notNil) { map[x,y] = with.wrapAt(index) };
			})
		})
	}

	// add a static light source to all empty cells
	addStaticLightToEmptyTiles{|scale=1|
		if (verbose) { "World_Tile_Map:addStaticLightToEmptyTiles".postln };
		this.checkEmptyListHasRun;
		emptyTiles[\allTiles].do{|list|
			lightMap.addStaticLightSource( list[0] + 0.75 * tileWidth, list[1] + 0.75 * tileHeight, 0.2 * scale, 3);
		};
	}

	// building //////////////////////////////////////////////////////////////////////////////////////////

	// make all tiles in the map
	renderMap{|argOptimize|
		var mapToBuild;
		if (verbose) { "World_Tile_Map:renderMap".postln };
		tiles = MultiLevelIdentityDictionary[];
		optimize = argOptimize ? optimize;
		if (optimize) {
			this.makeOptimizedMap;
			mapToBuild = optimizedMap;
		}{
			mapToBuild = map;
		};
		builtMap = mapToBuild; // for tile editor

		mapToBuild.doXY{|x,y,value|
			if (value>=0) {
				this.addTile(mapToBuild, x, y, value);
			}{
				if (value == -4) {
					sceneState[\spawn] = Point( x*tileWidth+25, y*tileHeight+30); // << offset here?
				};
			};
		};
	}

	// add a tile to the map
	addTile{|mapToBuild, x, y, objectIndex|
		if (objectIndex.notNil){
			var object = tileSet[objectIndex][0];
			var args   = tileSet[objectIndex][1].copy;
			case { object.isTile } {
				args[0] = args[0] + (x*tileWidth);
				args[1] = args[1] + (y*tileHeight);
				args[4] = this.dx(mapToBuild, x, y);
				args[5] = this.dy(mapToBuild, x, y);
				tiles[x,y] = object.new(*args); // for tile editor
			}
			{ object.isItem } {
				args[0] = x;
				args[1] = y;
				tiles[x,y] = object.new(*args);
			}
			{ object.isNPC } {
				var origin = Point( (x + 0.5) * tileWidth, (y + 0.5) * tileHeight );
				args[0] = origin;
				tiles[x,y] = object.new(*args);
			};
		};
		^tiles[x,y]
	}

	// optimized the map so repeating cells are made into bigger tiles, this reduces entity count
	makeOptimizedMap{|iterations = 10, flop=false|
		var up = -3, right = -2, space = -1;
		if (verbose) { "World_Tile_Map:makeOptimizedMap".postln };
		optimizedMap = map.deepCopy;
		//if (flop) { map = map.collect(_.ascii).flop.collect(_.asciiToString) }; // swap vert & horz
		mapWidth.do{|xx|
			mapHeight.do{|yy|
				var x = flop.if(yy,xx);
				var y = flop.if(xx,yy);
				var value = optimizedMap[x,y];
				if (value>=0) {
					var objectList = tileSet[value];
					var object = objectList[0];
					// this needs to be done better £
					if ([ World_Rect_Tile, World_Rect_Tile2, World_Color_Rect_Tile].includes(object)) {
						var tileWidth  = 1;
						var tileHeight = 1;
						var dx         = 1;
						var dy         = 1;
						var avalibleWidth  = iterations.clip(0, mapWidth - x );
						var avalibleHeight = iterations.clip(0, mapHeight - y );
						// check x+n cells
						{dx<avalibleWidth}.while{
							if (optimizedMap[x + dx, y] == value) {
								dx = dx + 1;
								tileWidth = dx;
							}{
								dx = avalibleWidth; // this just terminates the while loop
							};
						};
						// check y+n rows
						{dy<avalibleHeight}.while{
							if(	optimizedMap.areaEquals(x, x+tileWidth-1, y + dy, y + dy, value) ) {
								dy = dy + 1;
								tileHeight = dy;
							}{
								dy = avalibleHeight; // this just terminates the while loop
							};
						};
						// now replace
						if ((tileWidth>1)||(tileHeight>1)) {
							tileWidth.do{|x1|
								var x2 = x + x1;
								tileHeight.do{|y1|
									var y2 = y + y1;
									case
									{(x1==0)&&(y1==0)} {      }
									{(y1==0)}          { optimizedMap[x2, y2] = flop.if(up,right) }
									{(x1==0)}          { optimizedMap[x2, y2] = flop.if(right,up) }
									{ optimizedMap[x2, y2] = space             };
								};
							};
						};
					}
				}
			}
		};
		//	if (flop) { map = map.collect(_.ascii).flop.collect(_.asciiToString) }; // swap back vert & horz
	}

	// width of extended tiles ( extend right )
	dx{|mapToBuild, x,y|
		var size = 1;
		var x1 = x + 1;
		if (x1 >= mapWidth) {^1};
		while ( { (x1 < mapWidth) and: { mapToBuild[x1,y] == -2 } }, {
			x1 = x1 + 1;
			size = size + 1;
		});
		^size;
	}

	// height of extended tiles ( extend down )
	dy{|mapToBuild, x,y|
		var size = 1;
		var y1 = y + 1;
		if (y1 >= mapHeight) {^1};
		while ( { (y1 < mapHeight) and: { mapToBuild[x,y1] == -3 } }, {
			y1 = y1 + 1;
			size = size + 1;
		});
		^size;
	}

	checkEmptyListHasRun{ if (makeEmptyListsHasRun.not) { this.makeEmptyLists } }

	makeEmptyLists{
		if (verbose) { "World_Tile_Map:makeEmptyLists".postln };
		// maybe move these to init ?
		makeEmptyListsHasRun         = true;
		emptyTiles                   = IdentityDictionary[];
		emptyTiles[\allTiles       ] = [];
		emptyTiles[\allRows        ] = {[]} ! mapHeight;
		emptyTiles[\allColumns     ] = {[]} ! mapWidth;
		emptyTiles[\floorTiles     ] = [];
		emptyTiles[\floorTileRows  ] = {[]} ! mapHeight;
		emptyTiles[\floorTileColums] = {[]} ! mapWidth;
		emptyTiles[\floor2x1       ] = [];
		map.doXY{|x,y,value|
			if (value == (-1) ) {
				emptyTiles[\allTiles]       = emptyTiles[\allTiles].add([x,y]);
				emptyTiles[\allRows][y]     = emptyTiles[\allRows][y].add([x,y]);
				emptyTiles[\allColumns][x]  = emptyTiles[\allColumns][x].add([x,y]);
				if (y<(mapHeight-1)) {
					if (flatSurfaceKey.includes(map[x,y+1])) {
						emptyTiles[\floorTiles]         = emptyTiles[\floorTiles].add([x,y]);
						emptyTiles[\floorTileRows][y]   = emptyTiles[\floorTileRows][y].add([x,y]);
						emptyTiles[\floorTileColums][x] = emptyTiles[\floorTileColums][x].add([x,y]);
					};
				};
				if ( (y > 0) && (y < (mapHeight-1)) && (x < (mapWidth - 1)) ) {
					if ((map[x+1, y] == -1) &&(flatSurfaceKey.includes(map[x,y+1])) &&(flatSurfaceKey.includes(map[x+1, y+1]))){
						emptyTiles[\floor2x1] = emptyTiles[\floor2x1].add([x,y]);
					};
				};
			};
		};
	}

	// more tools (these only support methods above because of value )//////////////////////////////////////////////////////

	clear{ this.fill(defaultValue) }
	fill{|value| map.fill(value) }
	fillY{|y, value|  mapWidth.do{|x| map[x,y] = value } }
	fillX{|x, value| mapHeight.do{|y| map[x,y] = value } }

	setSpawn{|x,y| map[x,y] = -4 }
	clearCell{|x,y| map[x,y] = -1 }
	putTile{|key,x,y| map[x,y] = keyToIndex[key] }

	fillRect{|left, top, width, height, value|
		for(left.clip(0,inf), (left + width).clip(0,mapWidth-1), {|x|
			for(top.clip(0,inf), (top + height).clip(0,mapHeight-1),{|y|
					map[x,y] = value;
			})
		})
	}

	strokeRect{|left, top, width, height, value| } // TODO
	fillOuterRect{|left, top, width, height, value| } // TODO
	fillOval{|left, top, width, height, value|
		var origin = Point( left + (width  / 2), top  + (height / 2));
		var radius = width / 2;
		var ratio  = width / height;
		for(left, left + width, {|x|
			for(top, top + height, {|y|
				// test for array bounds
				if ((x>=0)&&(x<mapWidth)&&(y>=0)&&(y<mapHeight)) {
					if ( (x@(y - top - (height/2) * ratio + top + (height/2) )).hypot(origin) < radius ) {
						map[x,y] = value;
					};
				};
			})
		})
	}
	fillOuterOval{|left, top, width, height, value|
		var origin = Point( left + (width  / 2), top  + (height / 2));
		var radius = width / 2;      // of a circle
		var ratio  = width / height; // and use ratio to swash it
		for(left, left + width, {|x|
			for(top, top + height, {|y|
				// test for array bounds
				if ((x>=0)&&(x<mapWidth)&&(y>=0)&&(y<mapHeight)) {
					if ( (x@(y - top - (height/2) * ratio + top + (height/2) )).hypot(origin) > radius ) {
						map[x,y] = value;
					}
				};
			})
		})
	}
	strokeOval{|left, top, width, height, penWidth, value|
		var origin = Point( left + (width  / 2), top  + (height / 2));
		var radius = width / 2;
		var ratio  = width / height;
		for(left, left + width, {|x|
			for(top, top + height, {|y|
				// test for array bounds
				if ((x>=0)&&(x<mapWidth)&&(y>=0)&&(y<mapHeight)) {
					var dist = (x@(y - top - (height/2) * ratio + top + (height/2) )).hypot(origin);
					if (( dist < radius ) && ( dist > (radius - penWidth) )) {
						map[x,y] = value;
					};
				};
			})
		})
	}


	getAsString{|optimized=false, built=true|
		var string = "";
		built.if(builtMap ? map, optimized.if(optimizedMap,map)).doXY{|x,y,value|
			if (value>=0) { string = string ++ mapToTextKey[ value ] } { string = string ++ specialMapToTextKey[value.neg - 1] };
			if (x==(mapWidth-1)) { string = string ++ "\n" };
		};
		^string;
	}

	*mapExists{|filename| ^File.exists( World_Assets.mapsDirectory +/+ filename ) }

	saveMap{|filename, optimized=false, built=true|
		var saveMap = built.if(builtMap ? map, optimized.if(optimizedMap,map));
		var saveState = IdentityDictionary[];
		[\asset_DEF_Method,\tileSet_DEF_Method,\maxResponderSize,\mapWidth,\mapHeight,\tileWidth,\tileHeight].do{|key|
			saveState[key] = sceneState[key];
		};
		saveState[\map] = saveMap;
		saveState.saveFile( World_Assets.mapsDirectory +/+ filename );
	}

	// this just gets the IdentityDictionary
	*loadMap{|filename|
		^(World_Assets.mapsDirectory +/+ filename).loadFile;
	}

	/*
	World_Tile_Map.tileMap.getAsString(false,false);
	World_Tile_Map.tileMap.saveMap("my 1st map");
	a = World_Tile_Map.loadMap("my 1st map");
	a[\map]
	a[\mapWidth].class
	*/

	at   {|x, y| ^map[x,y] }
	put  {|x, y, value| map[x,y] = keyToIndex[value] }
	putAt{|x, y, value| map[x,y] = keyToIndex[value] }

}
