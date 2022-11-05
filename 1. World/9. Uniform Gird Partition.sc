////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Uniform Grid Partition (UGP)                                    //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// UGP is the way World_World optimises collision detection
// the world is split into cells of equal size
// maxResponderSize is the largest size a responder can be in num cells
// type names for a group of both sources and responders. responders of a type detect sources of the same type

World_UGP : World_World {

	// names of collisions groups, you can add more
	classvar <>collisionTypes = #[\tiles, \solids, \npcs, \items, \npcBullets, \rayCast];

	var <cellWidth, <cellHeight, <gridWidth, <gridHeight, <cells, <responders, <maxResponderSize;

	// maxResponderSize is the largest size a responder can be in num cells
	*new{|cellWidth, cellHeight, maxResponderSize = 1| ^super.new.init(cellWidth, cellHeight, maxResponderSize) }

	init{|width, height, argMaxResponderSize|
		if (verbose) { "World_UGP:init".postln };
		maxResponderSize = argMaxResponderSize;
		responders       = IdentitySet[];
		cellWidth        = width;
		cellHeight       = height;
		gridWidth        = (worldWidth  / cellWidth ).ceil.asInteger;
		gridHeight       = (worldHeight / cellHeight).ceil.asInteger;
		// make a list of cells which are Dictionaries of each collision type and the entities in that cell
		cells = {
			var dict = IdentityDictionary[];
			collisionTypes.do{|symbol| dict[symbol] = [] }; // NEEDS TO BE A LIST FOR \addToHead
			dict;
		} ! (gridWidth * gridHeight);
	}

	// test & run all collisions
	doCollisions{
		responders.do{|collider1|
			if (collider1.active) {
				collider1.collisionResponderTypes.do{|type|
					cells[collider1.cell][type].do{|collider2|
						if (collider1.testForCollision(collider2)) { collider1.onCollision(collider2) }
					};
				};
			};
		};
	}

	// used in wall grab
	doTileCollisionsFunc{|cell, rect, func, type = \tiles|
		cells[cell][type].do{|collider|
			if (((collider.isTile) && (collider.isRect))||(collider.isSolid)) {
				if (collider.testForCollision(rect)) { func.value };
			};
		};
	}

	// add an object as a responder
	addResponder{|object| responders = responders.add(object) }

	// remove an object from the responders
	removeResponder{|object| responders.remove(object) }

	// world space to ugp cell number
	getCell{|point|
		^((point.x / cellWidth ).round.clip(0, gridWidth  - 1) )
		+((point.y / cellHeight).round.clip(0, gridHeight - 1) * gridWidth);
	}

	// move an object from a previousCell to a newCell
	moveCell{|object, collisionSourceTypes, previousCell, newCell, newBoundingBox, oldBoundingBox|
		this.removeRectFromCells(object, oldBoundingBox, collisionSourceTypes);
		this.addTypesToCells(object, newBoundingBox, collisionSourceTypes);
	}

	// add an object to a group of cells using a bounding box, this means you can have sources which are bigger than 1 cell
	addRectToCells{|object, boundingBox, type, addAction=\addToTail|
		var l = ( (boundingBox.left   / cellWidth ).floor - maxResponderSize).clip(0,gridWidth -1);
		var r = ( (boundingBox.right  / cellWidth ).ceil  + maxResponderSize).clip(0,gridWidth -1);
		var t = ( (boundingBox.top    / cellHeight).floor - maxResponderSize).clip(0,gridHeight-1);
		var b = ( (boundingBox.bottom / cellHeight).ceil  + maxResponderSize).clip(0,gridHeight-1);
		for (l, r, {|x|
			for (t, b, {|y|
				var i = x + (y * gridWidth);
				switch (addAction,
					\addToTail, { cells[i][type] = cells[i][type].add(object) },
					\addToHead, { cells[i][type] = cells[i][type].insert(0,object) }
				);
			})
		});
	}

	// add an object to a group of cells using a bounding box, this means you can have sources which are bigger than 1 cell
	// can add to multiple types here
	addTypesToCells{|object, boundingBox, types, addAction=\addToTail|
		var l = ( (boundingBox.left   / cellWidth ).floor - maxResponderSize).clip(0,gridWidth -1);
		var r = ( (boundingBox.right  / cellWidth ).ceil  + maxResponderSize).clip(0,gridWidth -1);
		var t = ( (boundingBox.top    / cellHeight).floor - maxResponderSize).clip(0,gridHeight-1);
		var b = ( (boundingBox.bottom / cellHeight).ceil  + maxResponderSize).clip(0,gridHeight-1);
		for (l, r, {|x|
			for (t, b, {|y|
				var i = x + (y * gridWidth);
				switch (addAction,
					\addToTail, { types.do{|type| cells[i][type] = cells[i][type].add(object) } },
					\addToHead, { types.do{|type| cells[i][type] = cells[i][type].insert(0,object) } }
				);
			})
		});
	}

	// add an object to a group of cells using a bounding box, this means you can have sources which are bigger than 1 cell
	removeRectFromCells{|object, boundingBox, types|
		var l = ( (boundingBox.left   / cellWidth ).floor - maxResponderSize).clip(0,gridWidth -1);
		var r = ( (boundingBox.right  / cellWidth ).ceil  + maxResponderSize).clip(0,gridWidth -1);
		var t = ( (boundingBox.top    / cellHeight).floor - maxResponderSize).clip(0,gridHeight-1);
		var b = ( (boundingBox.bottom / cellHeight).ceil  + maxResponderSize).clip(0,gridHeight-1);
		for (l, r, {|x|
			for (t, b, {|y|
				var i = x + (y * gridWidth);
				types.do{|type| cells[i][type].remove(object) };
			})
		});
	}

	// careful : this is a lot of cells to remove from
	removeFromAllCells{|object, type| cells.do{|cell| cell[type].remove(object) } }

	// careful : this is a lot of cells to remove from
	removeTypesFromAllCells{|object, types| types.do{|type| cells.do{|cell| cell[type].remove(object) } } }

	// remove all types from previous cells
	freeTypesFromCell{|object, types, boundingBox|
		var l = ( (boundingBox.left   / cellWidth ).floor - maxResponderSize).clip(0,gridWidth -1);
		var r = ( (boundingBox.right  / cellWidth ).ceil  + maxResponderSize).clip(0,gridWidth -1);
		var t = ( (boundingBox.top    / cellHeight).floor - maxResponderSize).clip(0,gridHeight-1);
		var b = ( (boundingBox.bottom / cellHeight).ceil  + maxResponderSize).clip(0,gridHeight-1);
		for (l, r, {|x|
			for (t, b, {|y|
				var i = x + (y * gridWidth);
				types.do{|type| cells[i][type].remove(object)};
			})
		});
	}

}
