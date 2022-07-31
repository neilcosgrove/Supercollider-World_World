////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                  Building Scenes - Alien Maze Game                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// A scene is the space, containers & scripts in which a level or game and anything in it can be built and run.
// This is where we build the scenes, its entities & define their assets, tile sets, scripts, gameLoops, HUD etc..
// What to do when the engine starts, what happens when you start a new game, a new level, or what to do when its game over
// A Scene doesn't need to have Assets, Tile Sets, Light Maps, Scripts or a HUD but typically does.
//
///////////////////////
// Alien Maze Assets //
///////////////////////

+ World_Assets {

	// All asset method names must end with "_ASSET_DEF". Any methods with this prefix get called during Asset class init
	// This is because the assets are needed before the scene is created and before the engine is allowed to start
	alienMaze_ASSET_DEF{

		imagePaths = IdentityDictionary[
			// tiles
			\topLeft         -> "maze tiles/TL.png",
			\top             -> "maze tiles/edge top.png",
			\topRight        -> "maze tiles/TR.png",
			\left            -> "maze tiles/edge left.png",
			\middle          -> "maze tiles/middlle.png",
			\right           -> "maze tiles/edge right.png",
			\b1              -> "maze tiles/b1.png",
			\b2              -> "maze tiles/b2.png",
			\b3              -> "maze tiles/b3.png",
			\b4              -> "maze tiles/b4.png",
			\bottomLeft      -> "maze tiles/bl.png",
			\bottomRight     -> "maze tiles/br.png",
			\mTL             -> "maze tiles/middle top left.png",
			\mTR             -> "maze tiles/middle top right.png",
			\mBR             -> "maze tiles/middle bottom right.png",
			\mBL             -> "maze tiles/middle bottom left.png",
			// sprites
			\redAlien        -> "sprites/red ship.png",
			\redAlienHit     -> "sprites/red ship hit.png",
			\redAlienB1      -> "sprites/red ship blink 1.png",
			\redAlienB2      -> "sprites/red ship blink 2.png",
			\redAlienB3      -> "sprites/red ship blink 3.png",
			\redAlienShadow  -> "sprites/red ship shadow.png",
			\greenAlien      -> "sprites/green ship.png",
			\greenAlienHit   -> "sprites/green ship hit.png",
			\greenAlienB1    -> "sprites/green ship blink 1.png",
			\greenAlienShdow -> "sprites/green ship shadow.png",
			\star            -> "sprites/Yellow sphere.png",
			\ship1           -> "sprites/ship 1.png",
			\ship2           -> "sprites/ship 1 fire.png",
			\shipHit         -> "sprites/ship 1 hit.png",
			\shipShadow      -> "sprites/ship 1 shadow.png",
			\playerBullet    -> "sprites/bullet.png",
			// backgrounds
			\big             -> "backgrounds/floor.jpg",
		];

		bufferPaths = IdentityDictionary[
			\hit      -> "hit.ogg",
			\pop      -> "squish.ogg",
			\explode  -> "exp2.ogg",
			\tom      -> "EB_KIT06_TOM.ogg",
			\pop2     -> "pop short.ogg",
			\pickup   -> "pick up.ogg",
			\wind     -> "beneath the caves.ogg",
			\song1    -> "space song.ogg",
			\song2    -> "cave songs.ogg",
			\thruster -> "gas loop.ogg",
			\laser    -> "lazer.ogg",
			\text     -> "beep3.ogg",
			\kickVerb -> "kick verb.ogg",
			\ent1     -> "entomology2_t.ogg",
			\ent2     -> "entomology5_t.ogg",
			\ent3     -> "entomology6_t.ogg",
		];
	}

}

/////////////////////////
// Alien Maze Tile Set //
/////////////////////////

+ World_Tile_Map {

	// define your tiles & other entities for the tile map to use here
	alienMaze_TILE_SET_DEF{

		tileSetDefinition = [
			\middle      -> [World_Rect_Tile,  [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \middle      ]],
			\mTL         -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \mTL         ]],
			\mTR         -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \mTR         ]],
			\mBR         -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \mBR         ]],
			\mBL         -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \mBL         ]],
			\topLeft     -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \topLeft     ]],
			\top         -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \top         ]],
			\topRight    -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \topRight    ]],
			\left        -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \left        ]],
			\middle      -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \middle      ]],
			\right       -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \right       ]],
			\bottomLeft  -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \bottomLeft  ]],
			\bottomRight -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \bottomRight ]],
			\b1          -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \b1          ]],
			\b2          -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \b2          ]],
			\b3          -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \b3          ]],
			\b4          -> [World_Rect_Tile2, [0, 0, tileWidth, tileHeight, 1, 1, 1, true,  \b4          ]],
			\greenNPC    -> [World_Kamikaze_Type2_NPC, [Point(0,0), 11, \greenAlien  , \greenAlienHit, \pop, \pop2,
				[\greenAlienB1], \greenAlienShdow, Color.new255(132, 191, 8) ]],
			\redNPC      -> [World_Ranged_Type2_NPC, [Point(0,0), 11, \redAlien, \redAlienHit, \pop, \pop2,
				[\redAlienB1, \redAlienB2, \redAlienB3], \redAlienShadow, \laser, Color.new255(239, 58, 101) ]],
		];

	}

}

//////////////////////////////
// Alien Maze Scene Builder //
//////////////////////////////

+ World_Scene {

	*buildAlienMazeScene{|seed|
		var enemies = [World_Kamikaze_Type2_NPC, World_Ranged_Type2_NPC, World_Ranged_NPC_Type2_Bullet];

		World_Scene.setupScene(110, 110, 127, 127, 1, \alienMaze_ASSET_DEF, \alienMaze_TILE_SET_DEF); // << name of methods above

		// maps ///////////////////////////////////////

		tileMap.addMazeWithBorder(\middle, 2, \middle, 2, randomGap:0.2); // add a maze
		// add some courtyard spaces
		sceneState[\mapArea].div(500).do{
			tileMap.addRect(\empty,
				2+((sceneState[\mapWidth ].div(8)-1).rand*8),
				2+((sceneState[\mapHeight].div(8)-1).rand*8)
				,5 ,5)
		};
		// format the maze cells using our tile set and these rules
		tileMap.formatCells(IdentityDictionary[
				[0,0,0,0,1,1,0,1,1] -> \topLeft,
				[0,0,0,1,1,1,1,1,1] -> \top,
				[1,0,0,1,1,1,1,1,1] -> \top,
				[0,0,1,1,1,1,1,1,1] -> \top,
				[0,0,0,1,1,0,1,1,0] -> \topRight,
				[0,1,1,0,1,1,0,1,1] -> \left,
				[1,1,1,0,1,1,0,1,1] -> \left,
				[0,1,1,0,1,1,1,1,1] -> \left,
				[1,1,0,1,1,0,1,1,0] -> \right,
				[1,1,1,1,1,0,1,1,0] -> \right,
				[1,1,0,1,1,0,1,1,1] -> \right,
				[0,1,1,0,1,1,0,0,0] -> \bottomLeft,
			    [1,1,1,1,1,1,0,0,0] -> [\b1,\b2,\b3,\b4],
			    [1,1,1,1,1,1,1,0,0] -> [\b1,\b2,\b3,\b4],
			    [1,1,1,1,1,1,0,0,1] -> [\b1,\b2,\b3,\b4],
				[1,1,0,1,1,0,0,0,0] -> \bottomRight,
				[0,1,1,1,1,1,1,1,1] -> \mTL,
				[1,1,0,1,1,1,1,1,1] -> \mTR,
				[1,1,1,1,1,1,1,1,0] -> \mBR,
				[1,1,1,1,1,1,0,1,1] -> \mBL,
		    ])
		    .makeEmptyLists // this must be after terrain and before trees
	    	.addRandNPCs([\greenNPC, \redNPC], sceneState[\mapArea].div(20))
		    .replaceInRect(sceneState[\mapWidth].div(2)-8, sceneState[\mapHeight].div(2)-8, 16, 16,
			     [\greenNPC,\redNPC], \empty)
			.renderMap;

		// misc ///////////////////////////////////////

		World_Top_Down_Player(0, centrePoint.copy, 9, enemies,
			\ship1, \ship2, \shipHit, \shipShadow,\playerBullet, \explode, \hit, \tom);

		// you don't always have to use tiles maps for everything, here is an example of adding items to scene
		sceneState[\totalStars] = 0;
		(sceneState[\mapWidth]).div(4).do{|x|
			x = x * 4 + 3 * sceneState[\tileWidth];
			(sceneState[\mapWidth]).div(4).do{|y|
				y = y * 4 + 3 * sceneState[\tileHeight];
				if ((players[0].origin.distance(x@y) >= 300) && (0.5.coin)) {
					World_Item.newXY(x, y, 10, \star, \pickup, World_Star_Property);
					sceneState[\totalStars] = sceneState[\totalStars] + 1;
				};
			};
		};

		World_Scene.addDust(75,0.4,2, Color(0.8,0.9,1));      // add dust

		sceneState[\background    ] = \big;
		sceneState[\backgroundMode] = \followCamera;
		sceneState[\hud           ] = \alienMaze_HUD;         // name of hud method below
		sceneState[\overlay       ] = true;                   // light map
		sceneState[\overlayStart  ] = (248+20);
		sceneState[\overlayFade   ] = (150+20);
		sceneState[\overlayLight  ] = Color.clear;
		sceneState[\overlayDark   ] = Color.black;
		sceneState[\overlayOrigin ] = { World_World.cameraPos };

		// Scene Scripts //////////////////////////////////////////////
		// when the game starts
		sceneScripts[\startGame] = {
			// start music playlist
			World_Audio.newPlaylist(\music, 0.5, 20, 20, 2);
			[1,3/4,2/3,4/3].do{|rate|
				World_Audio.addToPlaylist(\music, \song1, 1, rate:rate/2 );
				World_Audio.addToPlaylist(\music, \song2, 1.4, rate:rate );
			};
			World_Audio.startPlaylist(\music, 1);
			// start background creatures playlist
			World_Audio.newPlaylist(\ambience, 80, 30, 60, 2);
			[\ent2,\ent1,\ent3].do{|sound| [1,3/4,2/3,4/3].do{|rate| World_Audio.addToPlaylist(\ambience,sound,0.03,rate:rate)}};
			World_Audio.startPlaylist(\ambience);
			// start the wind & the thruster
			World_Audio.play(\wind, 0.15, 3, 4, 1, 0, true, \ambience);
			World_Audio.play(\thruster, 0.15, 2, 2, 1, 0, true, \fx);
			worldState[\lives] = 3;
			// intro fade, zoom, text & player inactive
			if (skipIntro) { World_Camera.setToBlack.fadeToClear }{
				World_Camera.setToBlack.fadeToClear(3);
				World_Camera.logZoom = -2.45;
				players[0][\controller].active = false;
				{
					0.5.wait;
					300.do{|i|
						(1/60).wait;
						World_Camera.logZoom = i.mapSine(0,299,-2.45,0);
					};
					2.wait;
					players[0][\controller].active = true;
				}.forkInScene;
				{ ["All was quiet in...", "The\nAlien\nMaze"].message(3,[0.1,0],[\text,\kickVerb]); }.deferInScene(2);
			};
		};
		// scene starts
		sceneScripts[\startScene] = {
			sceneState[\stars] = 0;
			worldState[\lightZoom] = 1;
			World_Camera.snapTo( players[0] );
			World_Camera.motionBlur = 0.75;
		};
		// light moves like a flame & change volume of thruster
		sceneScripts[\tick] = {
			sceneState[\overlayStart] = (worldTime*6     ).heatMap.map(0,1,243,253)/(zoom ** 1.1) * 1.5 * worldState[\lightZoom] * globalZoom;
			sceneState[\overlayFade ] = (worldTime*6+1000).heatMap.map(0,1,145,155)/(zoom ** 1.1) * 1.6 * worldState[\lightZoom] * globalZoom;
			if ( players[0].notNil) {
				World_Audio.amp(\thruster, players[0].isAlive.if {players[0][\mechanics].velocity.rho * 0.0125 } {0} );
			};
			// player has won?
			if (sceneState[\stars] == sceneState[\totalStars]) { World_World.gameWin; players[0].invulnerable_(true) };
		};
		// key down press
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) { World_Audio.stopEverything; World_World.quitGame }; // quit
		};
		// mouse move
		sceneScripts[\mouseMove] = {|screenPoint,worldPoint,mod,button|
			if (tileEditorState[\isOn].not && (releaseMode.not)  && (players[0][\controller].active) ) {
				World_Ranged_Type2_NPC(worldPoint,5,\redAlien,\redAlien,\pop,\pop2,
					[\redAlienB1, \redAlienB2, \redAlienB3], \redAlienShadow, \laser, Color.new255(239, 58, 101)) };
		};
		// controller input
		sceneScripts[\controllerIn] = {|device, index, value|
			if ([index,value] == [9,1]) { World_World.tooglePause };  // option toogles pause
		};
		// revisit scene (called when the scene is opened for a 2nd or more time)
		sceneScripts[\revisitScene] = { World_Camera.snapTo( players[0] ); };
		// the player has died
		sceneScripts[\playerDeath] = {|id|
			{
				worldState[\lives] = worldState[\lives] - 1;
				(2/60).wait;
				sceneState[\stars].do{
					World_Moving_Item.newXY(players[0].origin.x, players[0].origin.y, 9, \star, \pickup, World_Star_Property);
				};
				sceneState[\stars] = 0;
				200.do{|i|
					timeDilation = timeDilation * 0.99; // slow down time
					0.025.wait;
				};
				if (worldState[\lives] > 0) {
					World_Top_Down_Player(0, centrePoint.copy, 9, enemies,
						\ship1, \ship2, \shipHit, \shipShadow, \playerBullet, \explode, \hit, \tom).doRetrigger;
					World_Camera.panTo(players[0],4);
					World_Camera.motionBlur_(0.75);
					timeDilation = 1;
				}{
					World_World.gameOver; // also triggers sceneScripts[\gameOver] below
				};
			}.forkInScene(false);
		};
		// game over
		sceneScripts[\gameOver] = {
			World_Audio.releaseEverything;
			{
				(60*5).do{|i|
					worldState[\lightZoom] = i.map(0,60*5-1,1,0.00001);
					(1/60).wait;
				};
				1.wait;
				World_World.quitGame;
			}.forkInScene(false);
		};
		/////////////////////////////////////////
	}

}

////////////////////
// Alien Maze HUD //
////////////////////

+ World_HUD {

	// we only scale with globalZoom here
	*alienMaze_HUD{
		Pen.use{
			var string = (sceneState[\stars] ? 0).asString+"/"+(sceneState[\totalStars]?0);
			var len    = string.bounds(hudFont).width * globalZoom;
			Pen.use{
				Pen.scale(globalZoom,globalZoom);
				Pen.prDrawImage(Point( (screenWidth - 80 - len) / globalZoom,  21 / globalZoom), images[\star], nil, 0, 1.0);
				Pen.stringLeftJustIn(string,
					tempRect.replaceLTWB( (screenWidth - 20 - len) / globalZoom, 10, len / globalZoom, 60 / globalZoom),
					hudFont, white
				);
				Pen.scale(0.45,0.45);
				worldState[\lives].do{|i|
					Pen.prDrawImage(Point( (42 + (100*i) ),  (12 * 3) / globalZoom), images[\ship2], nil, 0, 1.0);
				};
			};
		};
	}

}
