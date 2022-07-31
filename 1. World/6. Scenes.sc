////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                Scene                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// A scene is the space, containers & functions in which a level and anything in it can be built and run.

World_Scene : World_World {

	classvar <scenes, <sceneIndex = 0, <initNumScenes = 1, <setterMethods, <getterMethods;
	var <>sceneState, <>tileMap, <>lightMap, <>ugp,     <>timeDilation;
	var <>entities,   <>layers,  <>dynamics, <>players, <>inputs, <>foregroundLayer, <>sceneTimers;
	var <>images,     <>imageBounds, <>sceneScripts;
	var <>buffers;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Scene methods / utilities                                                                      //
	////////////////////////////////////////////////////////////////////////////////////////////////////

	*init{
		if (verbose) { "World_Scene:init".postln };
		getterMethods = #[ \sceneState, \tileMap, \lightMap, \ugp, \entities, \layers, \dynamics, \players, \inputs,
			\foregroundLayer, \sceneTimers, \timeDilation, \images, \imageBounds, \sceneScripts, \buffers];
		setterMethods = getterMethods.collect{|method| (method++"\_").asSymbol };
	}

	// at the moment instances are only used as storage i.e to save and load the world state
	*new{ ^super.new }

	// load a scene as the current scene
	*loadFromScene{|scene|
		if (verbose) { "World_Scene:loadFromScene".postln };
		World_Scene.setWorldSize(
			scene.sceneState[\mapWidth ] * scene.sceneState[\tileWidth ],
			scene.sceneState[\mapHeight] * scene.sceneState[\tileHeight]
		);
		setterMethods.do{|method,i| this.perform(method, scene.perform( getterMethods[i] )) };
	}

	// save the current scene to a scene
	*saveToScene{|scene|
		if (verbose) { "World_Scene:saveToScene".postln };
		setterMethods.do{|method,i| scene.perform(method, this.perform( getterMethods[i] )) };
	}

	// build initNumScenes on start up
	*buildScenes{
		if (verbose) { "World_Scene:buildScenes".postln };
		scenes = [];
		if (titleScreen) { World_Scene.buildScene(titleScreenBuildMethod) } { World_Scene.buildScene(defaultBuildMethod) };
	}

	*startFirstScene{
		if (verbose) { "World_Scene:startFirstScene".postln };
		World_Scene.loadFromScene(scenes[0]); // and open 1st scene
		if (verbose) { "World_Scene:sceneScripts[\\startGame]".postln };
		sceneScripts[\startGame].value;     // game start func
		if (verbose) { "World_Scene:sceneScripts[\\startScene]".postln };
		sceneScripts[\startScene].value;    // scene start func
		sceneState[\playerVisits] = sceneState[\playerVisits] + 1;
		game_Loop   = sceneState[\gameLoop];
		render_Loop = sceneState[\renderLoop];
	}

	// add a single scene, called from controller dpad up and world edge response
	*addScene{|buildMethod, startGame = false|
		if (verbose) { "World_Scene:addScene".postln };
		isPlaying = false; // we need to stop the engine during level creation because the SystemClockLoop plays catch up
		if (sceneState[\playerVisits]>0) {
			if (verbose) { "World_Scene:sceneScripts[\\leaveScene]".postln };
			sceneScripts[\leaveScene].value;
		};
		World_Scene.saveToScene(scenes[sceneIndex]);
		World_Scene.buildScene(buildMethod);
		sceneIndex = scenes.size - 1;
		if (startGame) {
			if (verbose) { "World_Scene:sceneScripts[\\startGame]".postln };
			sceneScripts[\startGame].value;     // game start func
		};
		if (sceneState[\playerVisits]==0) {
			if (verbose) { "World_Scene:sceneScripts[\\startScene]".postln };
			sceneScripts[\startScene].value
		} {
			if (verbose) { "World_Scene:sceneScripts[\\revisitScene]".postln };
			sceneScripts[\revisitScene].value
		};
		sceneState[\playerVisits] = sceneState[\playerVisits] + 1;
		game_Loop   = sceneState[\gameLoop];
		render_Loop = sceneState[\renderLoop];
		{
			isPlaying = true;
			guiFrame = worldFrame; // so we can spot dropped frames on a per scene basis
			World_Controller_Base.doRetrigger;
		}.defer(0.01); // we can start it again now. The AppClock will allow time to slip by
	}

	// build a single scene, called from buildScenes & addScene
	*buildScene{|buildMethod|
		var timeToBuild, scene = World_Scene();
		if (verbose  ) { "Building scene: [%]".format(scenes.size).postln; timeToBuild = Main.elapsedTime };
		if (debugMode) { World_Scene.buildDebugScene } {
			var seed = Date.seed;
			thisThread.randSeed = seed;
			if (verbose) { "World_Scene:%(%)".format(buildMethod ? defaultBuildMethod, seed).postln };
			World_Scene.perform(buildMethod ? defaultBuildMethod, seed);
		};
		if (verbose  ) { "Time to build scene: ".post; (Main.elapsedTime - timeToBuild).post; " sec(s)".postln;};
		World_Scene.saveToScene(scene);
		scenes = scenes ++ [scene];
	}

	// saves current scene and then opens scene @ scene[index]
	*openScene{|index,runGameStartFunc = false|
		if (verbose) { "World_Scene:openScene(%)".format(index.wrap(0,scenes.size-1)).postln };
		if (sceneState[\playerVisits]>0) {
			if (verbose) { "World_Scene:sceneScripts[\\leaveScene]".postln };
			sceneScripts[\leaveScene].value;
		};
		World_Scene.saveToScene(scenes[sceneIndex]);      // 1st save the current scene
		sceneIndex = index.wrap(0, scenes.size-1);
		World_Scene.loadFromScene(scenes[sceneIndex]);    // and then open the scene @ scenes[index]
		if (runGameStartFunc) {
			if (verbose) { "World_Scene:sceneScripts[\\startGame]".postln };
			sceneScripts[\startGame].value;
		};
		if (sceneState[\playerVisits]==0) {
			if (verbose) { "World_Scene:sceneScripts[\\playerVisits]".postln };
			sceneScripts[\startScene].value
		} {
			if (verbose) { "World_Scene:sceneScripts[\\revisitScene]".postln };
			sceneScripts[\revisitScene].value
		};
		sceneState[\playerVisits] = sceneState[\playerVisits] + 1;
		game_Loop   = sceneState[\gameLoop];
		render_Loop = sceneState[\renderLoop];
		{World_Controller_Base.doRetrigger}.defer(0.01);
	}

	// move up and down the scenes
	*incScene{
		if ((titleScreen) && (sceneIndex == 0)) { ^this };
		if (titleScreen) {
			World_Scene.openScene( (sceneIndex + 1).wrap(1, scenes.size-1) );
		}{
			World_Scene.openScene(sceneIndex + 1)
		};
	}

	// move down the scenes
	*decScene{
		if ((titleScreen) && (sceneIndex == 0)) { ^this };
		if (titleScreen) {
			World_Scene.openScene( (sceneIndex - 1).wrap(1, scenes.size-1) );
		}{
			World_Scene.openScene(sceneIndex - 1)
		};
	}

	// this gets done at the end of the frame
	*worldEdgeResponse{|id,edge| worldEdgeResponses = worldEdgeResponses.add([id,edge]) }

	// // create or move to next map TODO
	// *nextScene{
	// 	World_Scene.addScene;
	// 	timeDilation = 1;
	// }

	// create or move to next map TODO
	*createScene{|method, startGame=false|
		if (verbose) { "World_Scene:createScene".postln };
		timeDilation = 1;
		World_Scene.addScene(method, startGame);
	}

	*mapExists{|filename| ^World_Tile_Map.mapExists(filename) }

	*saveMap{|filename, optimized=false, built=true| if (tileMap.notNil) { tileMap.saveMap(filename, optimized, built) } }

	// create a scene from a saved tile map
	*loadMap{|path, optimize = false|
		var loadState          = World_Tile_Map.loadMap(path);
		var asset_DEF_Method   = loadState[\asset_DEF_Method];
		var tileSet_DEF_Method = loadState[\tileSet_DEF_Method];
		var maxResponderSize   = loadState[\maxResponderSize];
		var mapWidth           = loadState[\mapWidth];
		var mapHeight          = loadState[\mapHeight];
		var tileWidth          = loadState[\tileWidth];
		var tileHeight         = loadState[\tileHeight];
		this.setupScene(mapWidth, mapHeight, tileWidth, tileHeight, maxResponderSize, asset_DEF_Method, tileSet_DEF_Method);
		tileMap.map = loadState[\map];
		tileMap.renderMap(optimize);
	}

	// setup everything for the scene
	*setupScene{|mapWidth, mapHeight, tileWidth, tileHeight, maxResponderSize=1, asset_DEF_Method, tileSet_DEF_Method|
		World_World.resetAll;
		World_Assets.open(asset_DEF_Method);
		sceneScripts                 = IdentityDictionary[];
		sceneState                   = IdentityDictionary[];
		sceneState[\asset_DEF_Method   ] = asset_DEF_Method;
		sceneState[\tileSet_DEF_Method ] = tileSet_DEF_Method;
		sceneState[\maxResponderSize   ] = maxResponderSize;
		sceneState[\mapWidth       ] = mapWidth;
		sceneState[\mapHeight      ] = mapHeight;
		sceneState[\mapArea        ] = mapWidth * mapHeight;
		sceneState[\tileWidth      ] = tileWidth;
		sceneState[\tileHeight     ] = tileHeight;
		sceneState[\background     ] = Color.black;   // by default
		sceneState[\backgroundMode ] = [\fixedNoZoom, \fixed, \strechToFit, \parallax, \followCamera][3];
		sceneState[\gameLoop       ] = \gameLoop;     // by default
		sceneState[\renderLoop     ] = \renderLoop;   // by default
		sceneState[\pauseLoop      ] = \pauseLoop;    // by default
		sceneState[\gameOverLoop   ] = \gameOverLoop; // by default
		sceneState[\gameWinLoop    ] = \gameWinLoop;  // by default
		sceneState[\playerVisits   ] = 0;
		sceneState[\overlay        ] = false;
		sceneState[\backgroundY    ] = 0;
		sceneState[\backgroundDY   ] = 1;
		World_Scene.setWorldSize  (mapWidth * tileWidth,   mapHeight * tileHeight);
		lightMap = World_Light_Map(mapWidth,  mapHeight,   tileWidth,  tileHeight);
		ugp      = World_UGP      (tileWidth, tileHeight, maxResponderSize);
		if (tileSet_DEF_Method.notNil) {
			tileMap  = World_Tile_Map(mapWidth,  mapHeight,   tileWidth,  tileHeight, tileSet_DEF_Method);
		}{
			tileMap = nil;
		};
	}

	// setup everything for the scene using pixels as units
	*setupScenePX{|pxWidth, pxHeight, tileWidth, tileHeight, maxResponderSize=1, asset_DEF_Method, tileSet_DEF_Method|
		var mapWidth  = (pxWidth / tileWidth).ceil.asInteger;
		var mapHeight = (pxHeight / tileHeight).ceil.asInteger;
		World_World.resetAll;
		World_Assets.open(asset_DEF_Method);
		sceneScripts                 = IdentityDictionary[];
		sceneState                   = IdentityDictionary[];
		sceneState[\asset_DEF_Method   ] = asset_DEF_Method;
		sceneState[\tileSet_DEF_Method ] = tileSet_DEF_Method;
		sceneState[\maxResponderSize   ] = maxResponderSize;
		sceneState[\mapWidth       ] = mapWidth;
		sceneState[\mapHeight      ] = mapHeight;
		sceneState[\mapArea        ] = mapWidth * mapHeight;
		sceneState[\tileWidth      ] = tileWidth;
		sceneState[\tileHeight     ] = tileHeight;
		sceneState[\background     ] = Color.black;   // by default
		sceneState[\backgroundMode ] = [\fixedNoZoom, \fixed, \strechToFit, \parallax, \followCamera][3];
		sceneState[\gameLoop       ] = \gameLoop;     // by default
		sceneState[\renderLoop     ] = \renderLoop;   // by default
		sceneState[\pauseLoop      ] = \pauseLoop;    // by default
		sceneState[\gameOverLoop   ] = \gameOverLoop; // by default
		sceneState[\gameWinLoop    ] = \gameWinLoop;  // by default
		sceneState[\playerVisits   ] = 0;
		sceneState[\overlay        ] = false;
		sceneState[\backgroundY    ] = 0;
		sceneState[\backgroundDY   ] = 1;
		World_Scene.setWorldSize  (pxWidth, pxHeight);
		lightMap = World_Light_Map(mapWidth,  mapHeight,   tileWidth,  tileHeight);
		ugp      = World_UGP      (tileWidth, tileHeight, maxResponderSize);
		if (tileSet_DEF_Method.notNil) {
			tileMap  = World_Tile_Map(mapWidth,  mapHeight,   tileWidth,  tileHeight, tileSet_DEF_Method);
		}{
			tileMap = nil;
		};
	}

	// set the world size and commonly used variables
	*setWorldSize{|width,height|
		if (verbose) { "World_Scene:setWorldSize".postln };
		worldRight       = width;                              // right edge of world
		worldBottom      = height;                             // bottom edge of world
		worldWidth       = width;                              // width of world
		worldHeight      = height;                             // height of world
		worldRect        = Rect(0,0,width,height);             // world bounds
		worldTopLeft     = Point(0,0);                         // world top left corner is always Point(0,0);
		worldBottomRight = Point(worldRight,worldBottom);      // world bottom left corner
		centreX          = width/2;                            // centre X of world
		centreY          = height/2;                           // centre Y of world
		centrePoint      = Point(centreX,centreY);             // centre point of world
		transX           = 0;                                  // x transpose
		transY           = 0;                                  // y transpose
		rotBoundY        = 0;                                  // rotational bound increase X axis
		rotBoundX        = 0;                                  // and y
		leftEdge         = 0;                                  // offscreen edges
		topEdge          = 0;                                  // used in draw method to avoid drawing entities
		rightEdge        = screenWidth;                        // that aren't on the screen
		bottomEdge       = screenHeight;
		actualLeftEdge   = 0;                                  // actual offscreen edges, adjusted for rotation of camera
		actualTopEdge    = 0;
		actualRightEdge  = screenWidth;
		actualBottomEdge = screenHeight;
		World_Camera.reset;                                    // reset zoom, angle and motionblur
		World_Camera.setCameraMaximumBounds;                   // area camera can move
		World_Camera.snapTo(centrePoint.copy);                 // camera default to centre of world
	}

	// add foreground particles
	*addSnow{|n=150, speed=5|
		n.do{
			var size  = 0.5.rrand(1);
			var dy    = 1.0.rrand(2) * size * speed;
			var dx    = dy * 0.5 * (0.5.rrand(1.5));
			var color = Color(size,size,size,0.5);
			World_Snow_Foreground_Particle(worldWidth.rand, worldHeight.rand, size, size.map(0.5,1,2,4), dx.neg, dy, color);
		};
	}

	// add foreground particles
	*addSparklyDust{|n=150, speed=1, sizeAdj=3|
		n.do{
			var size  = 0.3.rrand(1);
			var vel   = Polar(0.5,2pi.rand).asPoint;
			var dy    = vel.x * size * speed;
			var dx    = vel.y * size * speed;
			var color = Color(size,size,size,0.25);
			World_Sparkly_Dust_Foreground_Particle(worldWidth.rand, worldHeight.rand, size.map(0.5,1,0.1,1)**2,
				size.map(0.5,1,0,1)**2*sizeAdj+2, dx, dy, color);
		};
	}

	// add foreground particles
	*addDust{|n=150, speed=1, sizeAdj=2, color|
		n.do{
			var size  = 0.3.rrand(1);
			var vel   = Polar(0.5,2pi.rand).asPoint;
			var dy    = vel.x * size * speed;
			var dx    = vel.y * size * speed;
			World_Dust_Foreground_Particle(worldWidth.rand, worldHeight.rand, size.map(0.5,1,0.75,1),
				size.map(0.5,1,0.25,1)*sizeAdj+1, dx, dy, color);
		};
	}

	// add background particles
	*addStars{|n=150, speed=0.66, sizeAdj=2, color|
		n.do{
			var size = 0.3.rrand(1);
			var vel  = Point(0,2.rrand(10.0));
			var dx   = vel.x * size * speed;
			var dy   = vel.y * size * speed;
			color    = Color.grey(size*0.5+0.125);
			World_Star_Background_Particle(worldWidth.rand, worldHeight.rand, size.map(0.5,1,0.75,1),
				size.map(0.5,1,0.25,1)*sizeAdj+1, dx, dy, color);
		};
	}

	// scene tick
	*tick{ if (tileEditorState[\isOn].not) { sceneScripts[\tick].value } }

	free{
		sceneState = nil;  tileMap = nil; lightMap = nil;    ugp = nil;          timeDilation = nil;
		entities = nil;    layers = nil;  dynamics = nil;    players = nil;      inputs = nil;
		sceneTimers = nil; images = nil;  imageBounds = nil; sceneScripts = nil; buffers = nil;
		foregroundLayer = nil;
	}

}
