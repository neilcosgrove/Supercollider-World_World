////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 Building Scenes - Space Jumper Game                                //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// A scene is the space, containers & scripts in which a level or game and anything in it can be built and run.
// This is where we build the scenes, its entities & define their assets, tile sets, scripts, gameLoops, HUD etc..
// What to do when the engine starts, what happens when you start a new game, a new level, or what to do when its game over
// A Scene doesn't need to have Assets, Tile Sets, Light Maps, Scripts or a HUD but typically does.
//
/////////////////////////
// Space Jumper Assets //
/////////////////////////

+ World_Assets {

	// All asset method names must end with "_ASSET_DEF". Any methods with this prefix get called during Asset class init
	// This is because the assets are needed before the scene is created and before the engine is allowed to start
	spaceJumper_ASSET_DEF{

		imagePaths = IdentityDictionary[
			// tiles
			\bricks      -> "tiles/bricks.png",
			\soil        -> "tiles/soil.png",
			\bedrock     -> "tiles/bedrock.png",
			\bricksBL    -> "tiles/bricks tri 0.png",
			\bricksBR    -> "tiles/bricks tri 1.png",
			\bricksTR    -> "tiles/bricks tri 2.png",
			\bricksTL    -> "tiles/bricks tri 3.png",
			\bricksSnow  -> "tiles/bricks + grass.png",
			\soilSnow    -> "tiles/soil + grass.png",
			\bedrockSnow -> "tiles/bedrock + grass.png",
			\stone1      -> "tiles/stone1.png",
			\flower1     -> "tiles/flower.png",
			\flower2     -> "tiles/flower2.png",
			\tree1       -> "tiles/tree 1.png",
			\tree2       -> "tiles/tree 2.png",
			\tree3       -> "tiles/tree 3.png",
			\stone2      -> "tiles/rock.png",
			\darkBricks  -> "tiles/bricks dark.png",
			\bsTR        -> "tiles/bricks snow TR.png",
			\bsTL        -> "tiles/bricks snow TL.png",
			// sprites
			\yellowAlien            -> "sprites/yellow ship.png",
			\blueAlien              -> "sprites/black ship.png",
			\platformPlayer         -> "sprites/player1A.png",
			\platformPlayerWallGrab -> "sprites/player1B.png",
			\greenOrb               -> "sprites/Green sphere.png",
			\blueOrb                -> "sprites/Blue sphere.png",
			\blackOrb               -> "sprites/Black sphere.png",
			\yellowAlienHit         -> "sprites/yellow ship hit.png",
			\blueAlienHit           -> "sprites/black ship hit.png",
			// backgrounds
			\alienPlanet -> "backgrounds/alienDark.jpg",
		];
		bufferPaths = IdentityDictionary[
			\hit     -> "hit.ogg",
			\pop     -> "pop.ogg",
			\explode -> "exp.ogg",
			\tom     -> "EB_KIT06_TOM.ogg",
			\pop2    -> "pop short.ogg",
			\jump1   -> "nice jump.ogg",
            \jump2   -> "nice jump 2.ogg",
			\pickup  -> "pick up.ogg",
			\wind    -> "wind.ogg",
			\song1   -> "song 1 part 1.ogg",
			\song2   -> "song 1 part 2.ogg",
			\song3   -> "song 1 part 3.ogg",
			\song4   -> "song 1 part 4.ogg",
			\song5   -> "song 1 part 5.ogg",
			\song6   -> "song 1 part 6.ogg",
			\song7   -> "song 1 part 7.ogg",
			\song8   -> "song 1 part 8.ogg",
			\laser   -> "laser.ogg",
			\text    -> "text type.ogg",
			\ent1    -> "entomology1_p.ogg",
			\ent2    -> "entomology3_p.ogg",
		];
	}

}

///////////////////////////
// Space Jumper Tile Set //
///////////////////////////

+ World_Tile_Map {

	// define your tiles & other entities for the tile map to use here
	spaceJumper_TILE_SET_DEF{

		tileSetDefinition = [
			\bricks      -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricks      ]],
			\soil        -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \soil        ]],
			\bedrock     -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bedrock     ]],
			\bricksBL    -> [World_Triangle_Tile, [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricksBL,  0]],
			\bricksBR    -> [World_Triangle_Tile, [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricksBR,  1]],
			\bricksTR    -> [World_Triangle_Tile, [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricksTR,  2]],
			\bricksTL    -> [World_Triangle_Tile, [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricksTL,  3]],
			\bricksSnow  -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bricksSnow  ]],
			\soilSnow    -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \soilSnow    ]],
			\bedrockSnow -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bedrockSnow ]],
			\bsTR        -> [World_Rect_Tile2,    [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bsTR        ]],
			\bsTL        -> [World_Rect_Tile2,    [0, 0, tileWidth, tileHeight, 1, 1, 0, true,   \bsTL        ]],
			\stone1      -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1,  1, false, \stone1      ]],
			\flower1     -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1, 10, false, \flower1     ]],
			\flower2     -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1, 10, false, \flower2     ]],
			\tree1       -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1,  1, false, \tree1       ]],
			\tree2       -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1,  1, false, \tree2       ]],
			\stone2      -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1,  1, false, \stone2      ]],
			\tree3       -> [World_Decor_Tile,    [0, 0, tileWidth, tileHeight, 1, 1,  1, false, \tree3       ]],
			\darkBricks  -> [World_Rect_Tile,     [0, 0, tileWidth, tileHeight, 1, 1,  0, false, \darkBricks  ]],
			\health      -> [World_Item,          [0, 0, 6, \greenOrb, \pickup, World_Health_Property,     \moveUp ]],
			\rateOfFire  -> [World_Item,          [0, 0, 6, \blueOrb , \pickup, World_RateOfFire_Property, \moveUp ]],
			\clipSize    -> [World_Item,          [0, 0, 6, \blackOrb, \pickup, World_ClipSize_Property,   \moveUp ]],
			\blueNPC     -> [World_Kamikaze_NPC,  [Point(0,0), 5, \blueAlien, \blueAlienHit, \pop, \pop2, Color(0.07,0.4,0.67)]],
			\yellowNPC   -> [World_Ranged_NPC,    [Point(0,0), 5, \yellowAlien, \yellowAlienHit, \pop, \pop2, \laser,
				Color(0.96,0.77,0.08) ]],
		];
	}

}

////////////////////////////////
// Space Jumper Scene Builder //
////////////////////////////////

+ World_Scene {

	// if the map exists on disk then open it, otherwise create a random level
	*buildSpaceJumperScene{|seed|
		worldState[\sceneNum] = worldState[\sceneNum] ? 0 + 1;
		if (World_Scene.mapExists("space jumper map" + worldState[\sceneNum])) {
			this.loadMap("space jumper map" + worldState[\sceneNum], true); // load a saved map
		}{
			this.buildSpaceJumperSceneRand(seed); // generate a random map
		};
		this.buildSpaceJumperScenePart2;
	}

	// build a random map
	*buildSpaceJumperSceneRand{|seed|
		World_Scene.setupScene(222, 45, 50, 50, 1, \spaceJumper_ASSET_DEF, \spaceJumper_TILE_SET_DEF); // << name of methods above
		tileMap.addHeatMapWithDepth(\bricks, \soil,   0.8, 0.07  , 0.3 , 0.675, 1)
			.addHeatMapWithDepth(\soil,   \bricks, 0.8, 0.1225, 0.35, 0.675, 1)
			.addHeatMap(\bedrock, 0.098, 0.55 , 0.9)
		    .addRandomInRect([\bedrock, \soil, \bricks], sceneState[\mapArea].div(33))
		    .formatCells(IdentityDictionary[
			[0,0,0,1,0,1,1,1,1] -> \bricks,
			[0,0,0,1,0,1,0,1,1] -> \bricks,
			[0,0,0,1,0,1,1,1,0] -> \bricks,
			[0,0,0,1,0,1,0,1,0] -> \bricks,
			[0,0,0,1,0,1,0,0,0] -> \bricks,
			[1,0,0,1,0,1,1,1,1] -> \bricks,
			[1,0,0,1,0,1,0,1,1] -> \bricks,
			[1,0,0,1,0,1,1,1,0] -> \bricks,
			[0,0,1,1,0,1,1,1,1] -> \bricks,
			[0,0,1,1,0,1,0,1,1] -> \bricks,
			[0,0,1,1,0,1,1,1,0] -> \bricks,
		    ]) // cover up holes in the floor
		    .addRandomInRect(\darkBricks, sceneState[\mapArea].div(300))
			.addHorizontalTunnel(\empty, 8)
			.addHorizontalTunnel(\empty, 8)
		    .addBorder(\bricks, 0, 2, 0, 2)
		    .addCorners(  [\bricks, \soil, \bedrock, \bsTL, \bsTR],
			    [\empty, \darkBricks], \bricksBL, \bricksBR, \bricksTL, \bricksTR, \bsTL, \bsTR)
		    .replaceFloor([\bricks, \soil, \bedrock], [\bricksSnow, \soilSnow, \bedrockSnow],  [\empty, \darkBricks])
		    .makeEmptyLists // this must be after terrain and before trees
			.addDecor([\stone1, \flower1, \flower2, \tree1, \tree2, \stone2, \tree3],
			          [    0.5,        3,        3,   1.16,   1.16,       2,   1.16], sceneState[\mapArea].div(40) )
	    	.addRandNPCs([\blueNPC, \yellowNPC], sceneState[\mapArea].div(80))
		    .replaceInRect(0, sceneState[\mapHeight]-20, 20, 20, [\blueNPC,\yellowNPC], \empty) // remove NPCs near player spawn
		    .addItem(\health,     sceneState[\mapArea].div(336) )
			.addItem(\rateOfFire, sceneState[\mapArea].div(896) )
			.addItem(\clipSize,   sceneState[\mapArea].div(384) )
		    .setSpawn(2, sceneState[\mapHeight]-3)
		    .clearCell(2, sceneState[\mapHeight]-4)
		    .putTile(\bricksSnow, 2, sceneState[\mapHeight]-2)
			.renderMap;
	}

	// build the rest of the scene
	*buildSpaceJumperScenePart2{
		World_Platform_Player(0, sceneState[\spawn].copy, 7, [World_Kamikaze_NPC, World_Ranged_NPC, World_Ranged_NPC_Bullet],
			\platformPlayer, \platformPlayerWallGrab, \jump1, \jump2, \explode, \hit, \tom);

		World_Scene.addSnow(200,4);                        // add snow
		World_Camera.logZoom    = 1.5;                     // zoom
		World_Camera.motionBlur = 0.8;                     // motion blur

		sceneState[\background    ] = \alienPlanet;        // this can also be a color
		sceneState[\backgroundMode] = \parallax;           // scroll mode
		sceneState[\hud           ] = \spaceJumper_HUD;    // name of hud method below

		tileMap.addStaticLightToEmptyTiles(1.9);
		lightMap.isOn_(true).scaleAndClipStaticLightMap(0.65,0,1).contrastStaticLightMap(1,0,0.6);

		// Scene Scripts //////////////////////////////////////////////

		// when the game starts
		sceneScripts[\startGame] = {
			// start music playlist
			World_Audio.newPlaylist(\music, 4, 20, 40, 2); // 5 sec start, then a gap of 30-60 secs
			[1,3/4,2/3,4/3].do{|rate| #[\song1, \song2, \song3, \song4, \song5, \song6, \song7, \song8].do{|song|
				World_Audio.addToPlaylist(\music, song, (song==\song4).if(1,0.4), rate:rate )}
			};
			// start background creatures playlist
			World_Audio.startPlaylist(\music, 3); // start with songs[3] = \song4
			World_Audio.newPlaylist(\ambience, 60, 40, 60, 2);
			[\ent1,\ent2].do{|sound| [1,3/4,2/3,4/3].do{|rate| World_Audio.addToPlaylist(\ambience, sound, 0.03, rate:rate)}};
			World_Audio.startPlaylist(\ambience);
			// start wind
			World_Audio.play(\wind, 0.15, 3, 4, 1, 0, true, \ambience);
			worldState[\lives] = 1;
			worldState[\windSpeed] = {
				inf.do{|i|
					World_Audio.rate(\wind, (i/30).heatMap.map(0,1,-4.5,4.5).midiratio );
					0.2.wait;
				}
			}.forkInWorld(false);
			// intro fade, text, dialog & player inactive
			if (skipIntro) { World_Camera.setToBlack.fadeToClear }{
				World_Camera.setToBlack.fadeToClear(4);
				{ ["A really long time ago,", "on a planet a really long way away...",
					"Did you see that? They've turned off the secondary reactor.\nWe'll be obliterated for sure. This is crazy!"
				].message([3,3,5], [0.075, 0.075, 0.05], \text, [false, false, true], [nil, nil, "E3BO : "] ); }.deferInScene(4);
				players[0][\controller].active_(false);
				{
					(960).do{|i|
						players[0][\drawFunc].opacity_( i.map(0,959,-3.0,1.0).clip(0,1) );
						(1/60).wait;
					};
					players[0][\controller].active_(true);
				}.forkInScene;
			}
		};
		// scene starts
		sceneScripts[\startScene] = {
			World_Camera.snapTo( players[0] );
			if (sceneIndex>1) { World_Camera.setToBlack.fadeToClear };
		};
		// scene ticks (zoom out as me move right)
		sceneScripts[\tick] = {
			World_Camera.logZoom_( cameraPos.x.map(0,worldWidth,0.75,-1).clip(0,1) );
		};
		// key down press
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) {
				worldState[\windSpeed].stop; World_Audio.stopEverything; World_World.quitGame;
			}; // quit
			if (tileEditorState[\isOn]) {
				if (key.isSaveTileMap) {
					var filename = "space jumper map" + worldState[\sceneNum];
					tileMap.saveMap(filename);
					("Saving \"" ++ filename ++ "\"").subtitle(3,0);
				};
			}{
				// dev keys, up arrow makes a new scene
				if ((releaseMode.not) && (debugMode.not)) {
					if (key.isBuildNewScene) { {World_Scene.createScene(\buildSpaceJumperScene) }.defer };
					if (key.isIncScene) { World_Scene.decScene };              // left  : dec map
					if (key.isDecScene) { World_Scene.incScene };              // right : inc map
				};
			};
		};
		// controller input
		sceneScripts[\controllerIn] = {|device, index, value|
			if ([index,value] == [9,1]) { World_World.tooglePause }; // option toogles pause
		};
		// leave scene
		sceneScripts[\leaveScene] = { };
		// world edge response
		sceneScripts[\worldEdge] = {|id, edge|
			if (edge==\right) {
				players[id].moveBy( sceneState[\tileWidth].neg * 0.5, 0); // if we return
				World_Scene.createScene(\buildSpaceJumperScene);
			};
		};
		// revisit scene (called when the scene is opened for a 2nd or more time)
		sceneScripts[\revisitScene] = { World_Camera.snapTo( players[0] ); };
		// the player has died
		sceneScripts[\playerDeath] = {|id|
			{
				worldState[\lives] = worldState[\lives] - 1;
				200.do{|i|
					World_Camera.motionBlur_(i.map(0,199,0.2,0.5));
					World_Camera.angle_( (199-i/199) ** 3 * (0.025.rand2), 15); // use a heat map << put this in camera
					timeDilation = timeDilation * 0.99;                        // slow down time
					0.025.wait;
				};
				if (worldState[\lives] > 0) {
					World_Platform_Player(0,sceneState[\spawn].copy ? (50@50),7,
						[World_Kamikaze_NPC, World_Ranged_NPC, World_Ranged_NPC_Bullet],
						\platformPlayer, \platformPlayerWallGrab, \jump1, \jump2, \explode, \hit, \tom).doRetrigger;
					World_Camera.panTo(players[0],4);
					World_Camera.motionBlur_(0.8);
					timeDilation = 1;
				}{
					World_World.gameOver; // also triggers sceneScripts[\gameOver] below
				};
			}.forkInScene(false);
		};
		// game over
		sceneScripts[\gameOver] = {
			World_Audio.releaseEverything;
			World_Camera.fadeToBlack(4);
			worldState[\windSpeed].stop;
			{ 5.wait; World_World.quitGame }.forkInScene(false);
		};
		/////////////////////////////////////////
	}

}

//////////////////////
// Space Jumper HUD //
//////////////////////

+ World_HUD {

	// we only scale with globalZoom here
	*spaceJumper_HUD{
		if (players[0].notNil) {
			var w = (200 * globalZoom).asInteger;
			var h = (22  * globalZoom).asInteger;
			var health = players[0].components.notNil.if { players[0].components[\health].frac } { 0 };
			Pen.stringRightJustIn(
				String.fill( players[0].components.notNil.if {  players[0].components[\weapon].clipCount } { 0 }, $I) ++
				String.fill( players[0].components.notNil.if {  players[0].components[\weapon].noEmpty   } { 0 }, $.),
				tempRect.replaceLTWB( 10, screenHeight - 45, screenWidth-30, h),
				hudFont, white
			)
			// TODO : make this bar a class or method ?
			.fillColor_(tempColor.replaceRGBA(0,0,0,1))
			.strokeColor_(tempColor.replace(1,1,1))
			.width_(2)
			.addRect( tempRect.replaceLTWB( 18, screenHeight - 43, w+4, h+4) ).draw(3)
			.fillColor_( tempColor.replace( (2 - (health**1.5*2)).clip(0,1), (health**1.5*2).clip(0,1), 0))
			.addRect( tempRect.replaceLTWB( 21, screenHeight - 40 , (w-2)*(health),h-2)).draw(0)
		};
	}

}
