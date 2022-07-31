////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                             The Editor                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
toggle on/off = §, Inc & Dec tile type = 1 & 2, print map = ±, reset map = shift + del, zoom in & Out = - & +, ~ max framerate
mouse to draw & remove tiles (+shift to replace)

*/

// change to tile map

+ World_Tile_Map {

	*initTileEditor{
		if (verbose) { "World_Scene:initTileEditor".postln };
		tileEditorState = IdentityDictionary[];
		tileEditorState[\isOn    ] = false;
		tileEditorState[\tileType] = 0;
		tileEditorState[\zoom    ] = 1;
	}

	*drawTileEditorHUD{ tileMap.drawTileEditorHUD }

	drawTileEditorHUD{
		var image, indexOfImage = 8; // tiles
		if (tileSet[ tileEditorState[\tileType] ][0].isItem) { indexOfImage = 3 }; // items
		if (tileSet[ tileEditorState[\tileType] ][0].isNPC ) { indexOfImage = 2 }; // npcs
		image = images[ tileSet[ tileEditorState[\tileType] ][1][indexOfImage] ];  // the image
		Pen.use{
			Pen.scale(1,1);
			Pen.width_(16);
			Pen.fillColor_(black.copy);
			Pen.fillRect(Rect(screenWidth-tileWidth-24,16,tileWidth+8,tileHeight+8));
			Pen.width_(4);
			Pen.strokeColor_(white.copy);
			Pen.strokeRect   ( Rect(screenWidth-tileWidth-24,16,tileWidth+8,tileHeight+8));
			Pen.fastDrawImage( Rect(screenWidth-tileWidth-20,20,tileWidth  ,tileHeight), image );
		};
	}

	*drawMapEditor{
		Pen.width_(2);
		Pen.strokeColor_(Color.red);
		entities.do{|entity|
			var boundingBox = entity.boundingBox;
			if ((boundingBox.left   < actualRightEdge  ) and:
			    {boundingBox.right  > actualLeftEdge   } and:
			    {boundingBox.top    < actualBottomEdge } and:
			    {boundingBox.bottom > actualTopEdge    }) {
				    Pen.strokeRect(boundingBox);
			};
		};
		if (selectedTile.notNil) { Pen.strokeColor_(Color.green); Pen.strokeRect(selectedTile.boundingBox) };
	}

	deleteTileFromMap{|tile, x, y|
		var type = builtMap[x,y];
		if (tile.isTile) {
			var dx = tile.dx;
			var dy = tile.dy;
			dx.do{|x1|
				dy.do{|y1|
					builtMap[x+x1,y+y1] = -1;
					tiles[x+x1,y+y1].free;
					tiles[x+x1,y+y1] = nil;
				};
			};
			^[type, x, y, dx, dy];
		}{
			builtMap[x,y] = -1;
			tiles[x,y].free;
			tiles[x,y] = nil;
			^[type, x, y, 1, 1];
		};
	}

	deleteArea{|x1,x2,y1,y2|
		for(x1,x2,{|x|
			for(y1,y2,{|y|
				var tile = tiles[x,y];
				if (tile.notNil) {
					this.deleteTileFromMap(tile,x,y);
				};
			})
		})
	}

	addTileToMap{|type,x,y,dx,dy|
		var up = -3, right = -2;
		dx.do{|x1| builtMap[x+x1, y] = right };
		dy.do{|y1| builtMap[x, y+y1] = up };
		builtMap[x,y] = type;
	}

	// UI

	*tick{
		if (players[0].notNil) {
			players[0].moveBy(
				(controllerState[0,14] ? 0.5).map(0,1,-15.0,15.0) / (zoom.mix(1,0.5)),
				(controllerState[0,15] ? 0.5).map(0,1,15.0,-15.0) / (zoom.mix(1,0.5))
			);
			World_Camera.tick;
		};
	}

	*tileEditorKeyDown{|me, char, mod, unicode, keycode, key|
		if (tileMap.isNil) {^this};
		tileMap.tileEditorKeyDown(me, char, mod, unicode, keycode, key);
	}

	*keyDown{|key|
		if (tileMap.isNil) {^this};
		tileMap.keyDown(key);
	}

	keyDown{|key|
		var x, y;
		if (key.isTileEditorOnOff) { tileEditorState[\isOn] = tileEditorState[\isOn].not; ^this };
		if (tileEditorState[\isOn].not) { ^this };
		if (key.isZoomIn      ) { World_Camera.logZoom_(World_Camera.logZoom+0.2); tileEditorState[\zoom] = zoom };
		if (key.isZoomOut     ) { World_Camera.logZoom_(World_Camera.logZoom-0.2); tileEditorState[\zoom] = zoom };
		if (key.isDecTileType ) { tileEditorState[\tileType] = (tileEditorState[\tileType]-1).wrap(0, tileSet.size -1)};
		if (key.isIncTileType ) { tileEditorState[\tileType] = (tileEditorState[\tileType]+1).wrap(0, tileSet.size -1)};
		// below can only be done if we have something selected
		if (selectedTile.isNil) {^this};
		// delete
		if (key.isDeleteTile) {
			this.deleteTileFromMap(selectedTile, selectedXY.x, selectedXY.y );
			selectedTile = nil;
			^this;
		};
		// below can only be done on tiles
		if (selectedTile.isTile.not) { ^this };
		x  = selectedXY.x;
		y  = selectedXY.y;
		// right (inc x)
		if (key.isIncTileWidth) {
			var dx = selectedTile.dx + x;
			if (dx < mapWidth) {
				var args = this.deleteTileFromMap(selectedTile,x,y); // ^[type, x, y, dx, dy]
				this.deleteArea(x,dx,y, y - 1 + selectedTile.dy);    // TODO there are still big tiles that might get missed
				args[3] = args[3]+1;
				this.addTileToMap(*args);
				selectedTile = this.addTile(builtMap, args[1], args[2], args[0] );
			};
		};
		// down (inc y)
		if (key.isIncTileHeight) {
			var dy = selectedTile.dy + y;
			if (dy < mapHeight) {
				var args = this.deleteTileFromMap(selectedTile,x,y);  // TODO there are still big tiles that might get missed
				this.deleteArea(x,x - 1 + selectedTile.dx,y,dy);
				args[4] = args[4]+1;
				this.addTileToMap(*args);
				selectedTile = this.addTile(builtMap, args[1], args[2], args[0] );
			};
		};
		// left (dec x)
		if (key.isDecTileWidth) {
			var dx = selectedTile.dx;
			if (dx > 1) {
				var args = this.deleteTileFromMap(selectedTile,x,y);
				args[3] = args[3]-1;
				this.addTileToMap(*args);
				selectedTile = this.addTile(builtMap, args[1], args[2], args[0] );
			};
		};
		// up (dec y)
		if (key.isDecTileHeight) {
			var dy = selectedTile.dy;
			if (dy > 1) {
				var args = this.deleteTileFromMap(selectedTile,x,y);
				args[4] = args[4]-1;
				this.addTileToMap(*args);
				selectedTile = this.addTile(builtMap, args[1],args[2], args[0] );
			};
		};
	}

	*mouseDown{|screenPoint,worldPoint,mod,button,clickCount|
		if (tileMap.isNil) {^this};
		tileMap.mouseDown(screenPoint,worldPoint,mod,button,clickCount);
	}

	mouseDown{|screenPoint,worldPoint,mod,button,clickCount|
		var x = (worldPoint.x / tileWidth).asInt.clip(0,mapWidth-1);
		var y = (worldPoint.y / tileHeight).asInt.clip(0,mapHeight-1);
		// exit if off
		if (tileEditorState[\isOn].not) { ^this };
		// pick tile from map
		if (button==1) {
			entities.do{|entity|
				if  (( allEntitiesInTileSet.includes(entity.class) )and:{ entity.boundingBox.containsPoint(worldPoint) }) {
					mapWidth.do{|x|
						mapHeight.do{|y|
							if (tiles[x,y] === entity) {
								tileEditorState[\tileType] = builtMap[x,y]
								^this;
							};
						}
					};
				};
			};
			^this;
		};
		selectedTile = nil;
		selectedXY   = nil;
		// if a tile and is bigger than 1x1 then just select the tile
		entities.do{|entity|
			if  (( allEntitiesInTileSet.includes(entity.class) )and:{ entity.boundingBox.containsPoint(worldPoint) }) {
				if (entity.isTile) {
					if ((entity.dx>1) || (entity.dy>1)) {
						mapWidth.do{|x|
							mapHeight.do{|y|
								if (tiles[x,y] === entity) {
									selectedTile = entity;
									selectedXY   = x @ y;
									^this;
								};
							}
						};
					};
				};
			};
		};
		// if shift pressed then deleted whats below
		if (mod==131072) {
			if (builtMap[x,y] == tileEditorState[\tileType] ) { ^this };
			if (tiles[x,y].notNil) { this.deleteTileFromMap(tiles[x,y],x,y) };
			this.addTile(builtMap, x, y, tileEditorState[\tileType] );
			builtMap[x,y] = tileEditorState[\tileType];
			tileEditorState[\lastEditCord] = [x,y];
			^this;
		};
		// if tile is empty then add a tile
		if (tiles[x,y].isNil) {
			selectedTile = this.addTile(builtMap, x, y, tileEditorState[\tileType] );
			selectedXY   = x @ y;
			builtMap[x,y] = tileEditorState[\tileType];
			tileEditorState[\lastEditCord] = [x,y];
			^this;
		}{
			// if tile is taken then remove it
			if (tiles[x,y].notNil) { this.deleteTileFromMap(tiles[x,y],x,y) };
			tileEditorState[\lastEditCord] = nil;
			^this;
		};
		// if tile is 1x1 then select it
		entities.do{|entity|
			if  (( allEntitiesInTileSet.includes(entity.class) )and:{ entity.boundingBox.containsPoint(worldPoint) }) {
				selectedTile = entity;
				selectedXY = x @ y;
				^this;
			};
		};
	}

	*mouseMove{|screenPoint, worldPoint, mod|
		if (tileMap.isNil) {^this};
		tileMap.mouseMove(screenPoint, worldPoint, mod);
	}

	mouseMove{|screenPoint, worldPoint, mod|
		var x = (worldPoint.x / tileWidth).asInt.clip(0,mapWidth-1);
		var y = (worldPoint.y / tileHeight).asInt.clip(0,mapHeight-1);
		// exit if off
		if (tileEditorState[\isOn].not) { ^this };
		// if selected tile dx>1 then do nothing
		if (selectedTile.notNil) {
			if (selectedTile.isTile)  {
				if ((selectedTile.dx>1) && (selectedTile.dy>1)) {^this}
			};
			entities.do{|entity|
				if  ((entity.isTile)and:{entity.boundingBox.containsPoint(worldPoint)}) {
					if ((entity.dx>1) || (entity.dy>1)) {
						^this;
					};
				};
			};
		};
		// if shift then replace tile with this.
		if (mod==131072) {
			if (builtMap[x,y] == tileEditorState[\tileType] ) {^this };
			if (tiles[x,y].notNil) { this.deleteTileFromMap(tiles[x,y],x,y) };

			selectedTile  = this.addTile(builtMap, x, y, tileEditorState[\tileType] );
			selectedXY    = x @ y;
			builtMap[x,y] = tileEditorState[\tileType];
			tileEditorState[\lastEditCord] = [x,y];
			^this;
		};
		// if no lsast cord then delete tiles
		if (tileEditorState[\lastEditCord].isNil) {
			if (tiles[x,y].notNil) { this.deleteTileFromMap(tiles[x,y],x,y) };
			selectedTile  = nil;
		}{
			// if cell empty then add a tile
			if (tiles[x,y].isNil) {
			    selectedTile  = this.addTile(builtMap, x, y, tileEditorState[\tileType] );
				selectedXY    = x @ y;
				builtMap[x,y] = tileEditorState[\tileType];
				tileEditorState[\lastEditCord] = [x,y];
			}
		};
	}

	*mouseUp{|screenPoint,worldPoint,mod,button|
		if (tileMap.isNil) {^this};
		tileMap.mouseUp(screenPoint,worldPoint,mod,button);
	}

	mouseUp{|screenPoint,worldPoint,mod,button|}

}
