///////////////////////////////////////////////////////////////////////////////////////////////////////
//                                               World                                               //
///////////////////////////////////////////////////////////////////////////////////////////////////////
// Welcome to World_World 1.0 - The Supercollider 2D game engine
// made by Neil Cosgrove in 2021-2022 (GNU General Public License)
//
// all classes start with the prefix World_
// This is the superclass of everything in the engine, all other classes are subclasses of this
// it holds the current state of the runtime World and shares it with everything else
// see <link TODO> for a hierarchy graph and a short description of what each class does

World_World {

	classvar <projectName = "The Supercollider World_World Demo";
	classvar <releaseMode = false;

	// Start up flags (all these are overridden if releaseMode is true)

	classvar <>verbose    = false;   // post dev & debug info
	classvar <startEngine = true;   // start the game engine on launch
	classvar <fullScreen  = true;    // open in fullscreen mode
	classvar <titleScreen = true;    // open the title screen on launch
	classvar <skipIntro   = false;   // use to skip intro during dev
	classvar <>debugMode  = false;   // open the debug scene on launch & call World_World.debug in the game loop

	classvar titleScreenBuildMethod = \buildTitleScreenScene; // this scene will open on launch if titleScreen = true;
	classvar defaultBuildMethod     = \buildSpaceJumperScene; // this scene will open on launch if titleScreen = false;

	// Globals ///////////////////////////////////////////////////////////

	// scaleMode options are: \none, \diagonal, \width, \height, \nearestPixels, \furthestPixels, \smallestRatio, \largestRatio
	classvar <scaleMode      = \smallestRatio; // how to scale from the default screen to a different resolutions
	classvar <game_Loop      = \gameLoop;      // method name of current game loop
	classvar <render_Loop    = \renderLoop;    // method name of the current render loop
	classvar <hasStarted     = false;          // has the start up been run yet?
	classvar <isRunning      = false;          // is the engine on and gui open?
	classvar <isPlaying      = false;          // is the engine playing the core game play loop?
	classvar <defaultWidth   = 1280;           // put your current screen dimensions here so you can develop on your own screen
	classvar <defaultHeight  = 800;            // & the engine will adjust the globalZoom to other screen resolutions for you
	classvar <centreOnScreen = true;           // if the scene is smaller than the screen do you want it centred?
	classvar <>antiAliasing  = false;          // do you want Pen.smoothing_(true or false) ?
	classvar <interpolation  = \fast;          // image interpolation either \fast or \smooth
	classvar <numLayers      = 12;             // the number of layers in a scene. layers[0] is drawn 1st then layers[1] etc..
	classvar <motionBlur     = 0.8;            // background alpha used as a crude motion blur
	classvar <frameRate      = 60;             // frame rate of the World in fps, all frames events come from the userview
	classvar <frame          = 0;              // number of frames since gui creation
	classvar <worldFrame     = 0;              // total world frames since world epoch
	classvar <guiFrame       = 0;              // total gui frames since world epoch
	classvar <worldTime      = 0;              // time since epoch in seconds
	classvar <>timeDilation  = 1;              // timeDilation is used to slow down or speed up time
	classvar <numControllers = 4;              // number of controllers to support (only PS4 & PS5 controllers at the moment)
	classvar <frameLength;                     // the length of 1 frame in secs, this is set every frame
	classvar <fps;                             // actual fps of gui, can be lower than frameRate when drawing something demanding
	classvar <staticFrameLength;               // frameLength but not effected by timeDilation

	// Engine ////////////////////////////////////////////////////////////

	classvar <engineVersionMajor = 0;
	classvar <engineVersionMinor = 1;

	// screen
	classvar <window,         <userView,         <screenRect,          <screenWidth,           <screenHeight;
	classvar <aspectRatio,    <screenDiagonal,   <globalZoom,          <defaultScreenDiagonal, <scaledScreenRect;
	classvar <screenCentre;

	// world
	classvar <worldRect,      <worldWidth,       <worldHeight,         <worldBottom,           <worldRight;
	classvar <worldTopLeft,   <worldBottomRight, <halfScreenWidth,     <halfScreenHeight,      <centrePoint;
	classvar <centreX,        <centreY,          <worldState,          <garbage,               <controllerState;
	classvar <worldTimers,    <systemClockLoop,  <>keyboardMap;

	// camera
	classvar <>zoom,          <>transX,          <>transY,             <backX,                 <backY;
	classvar <cameraPos,      <cameraAngle,      <offScreenDistance,   <rotBoundX,             <rotBoundY;
	classvar <leftEdge,       <rightEdge,        <topEdge,             <bottomEdge;
	classvar <actualLeftEdge, <actualRightEdge,  <actualTopEdge,       <actualBottomEdge;

	// scene state
	classvar <>sceneState,    <>sceneTimers,     <>tileMap,            <>lightMap,             <>ugp,        <>sceneScripts;

	// ECS system
	classvar <>entities,      <>layers,          <>dynamics,           <>players,              <>inputs,     <>foregroundLayer;

	// assets
	classvar <>images,        <>imageBounds,     <>buffers;

	// end of frame script tiggers
	classvar <isGameOver,     <triggerGameOver,  <worldEdgeResponses;

	// temps & misc
	classvar tempPoint, tempPoint2, offset, tempRect, tempColor, tempVector, p1_2, p2_3, p1_3, white, black, grey;
	classvar halfpi, sqrtHalfPi;
    classvar guiRepeat, <tileEditorState, previousFrameRate, <capsLock = 0;

	// ****************************** ENGINE ************************************ //

	// world init
	*initClass{
		if (verbose) { "World_World:initClass".postln };
		if (releaseMode) {
			verbose = false; startEngine = true; fullScreen = true; titleScreen = true; debugMode = false; skipIntro = false;
		};
		Class.initClassTree(HID);
		if (startEngine.not) { ^this };
		hasStarted = true;
		this.initAllServices;
		StartUp.add{ World_World.startGameEngine };
	}

	// manual start (use this to start if startEngine = false)
	*start{
		if (verbose) { "World_World:start".postln };
		if (hasStarted) {
			"World_World has already started".warn;
			if (isRunning.not) { this.cmdPeriod };
			^this
		};
		hasStarted = true;
		this.initAllServices;
		World_Controller_Patch.restartAll;
		this.startGameEngine;
	}

	*initAllServices{
		if (verbose) { "World_World:initAllServices".postln };
		World_Camera.init;
		World_World.init;
		World_World.initScreenSize;
		World_World.initEntityContainers;
		World_Tile_Map.init;
		World_Assets.init;
		World_Archive.init;
		World_Debug.init;
		World_Audio.init;
		World_Scene.init;
		World_Tile_Map.initTileEditor;
		World_HUD.init;
		World_Controller_Patch.init;
		World_Message.init;
	}

	// garbage, constants & temp vars used to help speed up calculations
	*init{
		if (verbose) { "World_World:init".postln };
		#tempPoint, tempPoint2, offset, tempVector, p1_2, p2_3, p1_3 = {Point()} ! 7;
		garbage     = IdentitySet[];
		worldTimers = IdentitySet[]; // maybe here?
		halfpi      = pi * 0.5 ;
		sqrtHalfPi  = sqrt(pi/2);
		tempRect    = Rect();  // a temporary rect for use in calculations
		tempColor   = Color(0,0,0,1);
		white       = Color.white;
		black       = Color.black;
		grey        = Color.grey;
		staticFrameLength = 1 / frameRate; // used in clocks during init
	}

	// the screen size and commonly used variables
	// there are different resize options. for example... scale to screenDiagonal,
	*initScreenSize{
		if (verbose) { "World_World:initScreenSize".postln };
		defaultScreenDiagonal = defaultWidth.hypot(defaultHeight);              // these are the defaults for my mac
		screenWidth           = Window.screenBounds.width.asInteger;            // width of the screen
		screenHeight          = Window.screenBounds.height.asInteger;           // height of the screen
		screenDiagonal        = screenWidth.hypot(screenHeight);                // length of screen diagonal
		// how to scale to fit different sized screens compared to the default dev one
		switch (scaleMode,
			\none,     { globalZoom = 1 },                                      // don't scale
			\diagonal, { globalZoom = screenDiagonal / defaultScreenDiagonal }, // scale to fit the diagonal
			\width,    { globalZoom = screenWidth    / defaultWidth  },         // scale to fit the width
			\height,   { globalZoom = screenHeight   / defaultHeight },         // scale to fit the height
			\nearestPixels, {
				// scale to fit the sides which are nearest in pixels to each other.
				if (((screenWidth - defaultWidth).abs) < ((screenHeight - defaultHeight).abs)) {
					globalZoom = screenWidth / defaultWidth }{ globalZoom = screenHeight / defaultHeight } },
			\furthestPixels, {
				// scale to fit the sides which are furthest in pixels from each other.
				if (((screenWidth - defaultWidth).abs) > ((screenHeight - defaultHeight).abs)){
					globalZoom = screenWidth / defaultWidth }{globalZoom = screenHeight / defaultHeight } },
			\smallestRatio, {
				// scale to fit the sides who's ratios are closest to each other.
				if ((screenWidth / defaultWidth) < (screenHeight / defaultHeight)) {
					globalZoom = screenWidth / defaultWidth }{ globalZoom = screenHeight / defaultHeight } },
			\largestRatio, {
				// scale to fit the sides who's ratios are furthest from each other.
				if ((screenWidth / defaultWidth) > (screenHeight / defaultHeight)) {
					globalZoom = screenWidth / defaultWidth }{ globalZoom = screenHeight / defaultHeight } }
		);
		aspectRatio           = screenWidth / screenHeight;                // aspect ratio
		screenRect            = Rect(0,0,screenWidth,screenHeight);        // used to create gui & draw func to clear background
		scaledScreenRect      = Rect(0,0,screenWidth / globalZoom, screenHeight / globalZoom); // scaled by global zoom
		halfScreenWidth       = screenWidth    * 0.5;                      // used by camera
		halfScreenHeight      = screenHeight   * 0.5;                      // used by camera
		screenCentre          = halfScreenWidth @ halfScreenHeight;        // used by messages
		offScreenDistance     = screenDiagonal * 0.5;                      // distance to a corner from the centre of the screen
		World_Camera.zoom_(1);
		if (verbose) {
			"=================================".postln;˚
" screenWidth    : %\n screenHeight   : %\n screenDiagonal : %\n aspectRatio    : %\n scaleMode      : %\n globalZoom     : %"
			.format(screenWidth,screenHeight,screenDiagonal,
				(aspectRatio.asFraction[0]).asString ++ ":" ++ (aspectRatio.asFraction[1]) + "or" +aspectRatio.round(0.02)
				,scaleMode,globalZoom).postln;
			"=================================".postln;
		};
	}

	// called during initClass and creating new maps
	*initEntityContainers{
		if (verbose) { "World_World:initEntityContainers".postln };
		layers          = {OrderedIdentitySet[]} ! numLayers;
		foregroundLayer = OrderedIdentitySet[];
		entities        = IdentitySet[];
		inputs          = IdentitySet[];
		dynamics        = IdentitySet[];
		sceneTimers     = IdentitySet[];
		players         = IdentityDictionary[];
	}

	// start the engine
	*startGameEngine{
		if (verbose) { "World_World:startGameEngine".postln };
		isPlaying = true;
		// add controllers
		numControllers.do{|deviceNo|
			World_Controller_Patch.addAction( deviceNo, {|index, value|
				World_World.controllerIn(deviceNo, index, value);
				if (sceneScripts.notNil) { sceneScripts[\controllerIn].value(deviceNo, index, value) };
				controllerState[deviceNo, index] = value;
				if (isPlaying) { inputs.do{|object| object.controllerInput(deviceNo, index, value)} };
			});
		};
		CmdPeriod.add(World_World);
		World_World.initWorld;
		World_Audio.bootServer({ this.postServerBoot });
	}

	// init world state
	*initWorld{
		var highScore;
		if (verbose) { "World_World:initWorld".postln };
		isGameOver             = false;
		triggerGameOver        = false;
		worldEdgeResponses     = nil;
		worldTime              = 0;
		worldState             = IdentityDictionary[];
	}

	// once the server has booted (TODO : what if server quits?)
	*postServerBoot{
		if (verbose) {"World_World:postServerBoot".postln };
		World_Scene.buildScenes;
		World_Scene.startFirstScene;
		if (userView.isNil) { World_World.createGUI } { if ( userView.isClosed ) { World_World.createGUI } };
		"*** World_World v1.0 ready ***".postln;
	}

	// called on cmd period
	*cmdPeriod{
		if (verbose) { "World_World:cmdPeriod".postln };
		if (releaseMode) {
			this.startSystemClockLoop;
			World_Audio.resume;
		}{
			var now =  Main.elapsedTime;
			guiRepeat = guiRepeat ? (now - 0.125);
			// for some reason cmd period gets called twice per key press, this test stops it
			if (now - guiRepeat > 0.0625) {
				if (window.isClosed) {
					World_World.createGUI;
					if ((titleScreen) && (World_Scene.sceneIndex == 0)) {World_Audio.resume };
				}{
					window.close;
					if ((titleScreen) && (World_Scene.sceneIndex == 0)) { World_Audio.pause } { this.isPlaying = false};
				};
				guiRepeat = now;
			};
		};
	}

	// toggle pause on & off
	*tooglePause{ if (isGameOver.not) {this.isPlaying = isPlaying.not} }

	*isPlaying_{|bool|
		isPlaying = bool;
		if(isPlaying) {
			render_Loop = sceneState[\renderLoop]; World_Audio.resume; World_Controller_Base.doRetrigger;
		} {
			render_Loop = sceneState[\pauseLoop]; World_Audio.pause;
		};
	}

	// create gui window & user view
	*createGUI{
		if (verbose) { "World_World:createGUI".postln };
		window = Window(projectName, Rect(0, 0, screenWidth*0.5, screenHeight*0.5));
		if (releaseMode) { window.userCanClose_(false) };
		userView = UserView(window, screenRect).frameRate_(frameRate).animate_(false).clearOnRefresh_(false)
		.drawFunc_{|me|
			frame = me.frame;                                              // number of frames since gui creation
			if (isPlaying) {guiFrame = guiFrame + 1};                      // used to spot frame drops
			this.perform(render_Loop);                                     // perform the current render loop
			if (verbose) { World_HUD.drawStats };                          // dev info
			if (tileEditorState[\isOn]) { World_Tile_Map.drawTileEditorHUD }; // tile editor
		}
		.onClose_{
			isRunning = false;
			World_Audio.pause;
		}
		.mouseDownAction_{|me,x,y,mod,button,clickCount|
			var screenPoint = Point(x,y);
			var worldPoint  = World_Camera.screenSpaceToWorldSpace(screenPoint);
			World_Scene.sceneScripts[\mouseDown].value(screenPoint,worldPoint,mod,button,clickCount);
			World_Tile_Map.mouseDown(screenPoint,worldPoint,mod,button,clickCount); // tile editor here
			World_Debug.mouseDown(screenPoint,worldPoint,mod,button,clickCount);
			if (releaseMode.not) {
				// TODO tile editor here
			};
		}
		.mouseUpAction_{|me,x,y,mod,button|
			var screenPoint = Point(x,y);
			var worldPoint  = World_Camera.screenSpaceToWorldSpace(screenPoint);
			World_Scene.sceneScripts[\mouseUp].value(screenPoint,worldPoint,mod,button);
			World_Tile_Map.mouseUp(screenPoint,worldPoint,mod,button); // tile editor here
			World_Debug.mouseUp(screenPoint,worldPoint,mod,button);
			if (releaseMode.not) {
				// TODO tile editor here
			};
		}
		.mouseMoveAction_{|me,x,y,mod,button|
			var screenPoint = Point(x,y);
			var worldPoint  = World_Camera.screenSpaceToWorldSpace(screenPoint);
			World_Scene.sceneScripts[\mouseMove].value(screenPoint,worldPoint,mod,button);
			World_Tile_Map.mouseMove(screenPoint,worldPoint,mod); // tile editor here
			World_Debug.mouseMove(screenPoint,worldPoint,mod);
			if (releaseMode.not) {
				// TODO tile editor here
			};
		}.mouseWheelAction_{|me,x,y,mod,dx,dy|
			var screenPoint = Point(x,y);
			var worldPoint  = World_Camera.screenSpaceToWorldSpace(screenPoint);
			World_Scene.sceneScripts[\mouseWheel].value(screenPoint,worldPoint,dx,dy,mod);
			World_Debug.mouseWheel(screenPoint,worldPoint,dx,dy,mod);
		};
		World_World.addKeyboardControls;
		{window.front}.defer(0.1);
		World_Camera.motionBlur_(0.8);
		if (fullScreen) {window.fullScreen};
		isRunning = true;
		this.startSystemClockLoop;
	}

	// this is the SystemClock loop that calls the gameLoop & defers the renderLoop
	*startSystemClockLoop{
		if (verbose) { "World_World:startSystemClockLoop".postln };
		systemClockLoop = {
			var currentTime, previousTime = AppClock.seconds;
			var currentFrameRate, currentFrame, previousFrame = 0;
			previousFrameRate = frameRate;
			fps = frameRate;
			inf.do{
				frameRate.do{
					if (tileEditorState[\isOn]) { World_Tile_Map.tick }{
						if (isPlaying && isRunning) {
							worldFrame        = worldFrame + 1;                   // frames since world creation
							staticFrameLength = 1 / frameRate ;                   // length of a single frame in seconds no tD
							frameLength       = staticFrameLength * timeDilation; // length with timeDilation
							worldTime         = worldTime + frameLength;          // time in seconds since world creation
							this.perform(game_Loop)
						}; // do game loop
					};
					// i dont know if changing these values will alter your performance, these seem to help on a 2012 Macbook pro
					(frameRate.reciprocal*0.25).wait;          // wait 25% of a frame length
					{userView.refresh}.defer;                  // do render loop
					(frameRate.reciprocal*0.75).wait;          // wait 75% of a frame length
				};
				if (debugMode || verbose) {
					// update fps for the dev info
					currentFrameRate  = frameRate;
					currentFrame      = guiFrame;
					currentTime       = AppClock.seconds;
					if (currentFrameRate==previousFrameRate) { fps = (currentFrame-previousFrame)/(currentTime-previousTime) };
					previousFrameRate = currentFrameRate;
					previousTime      = currentTime;
					previousFrame     = currentFrame;
				};
			};
		}.fork(SystemClock);
	}

	// you can't free an entity halfway through a frame because its still referenced by other entities, components & containers
	// garbage can also contain timers & clocks
	// garbage is collected at the end of a frame
	*collectGarbage{ if (garbage.notEmpty) { garbage.do(_.free).clear } }

	// free all entities in the world
	*freeAll{
		if (verbose) { "World_World:freeAll;".postln };
		entities.copy.do(_.free);
		World_Tile_Map.selectedTile = nil;
	}

	*resetAll{
		if (verbose) { "World_World:resetAll;".postln };
		World_World.initEntityContainers;
		World_Tile_Map.selectedTile = nil;
	}

	*keyDown{|key|
		// toggle fullscreen
		if (key.isFullScreen) {
			{
				if (window.isClosed.not) {
					guiRepeat = (guiRepeat ? (fullScreen.asInteger) ).asInteger + 1;
					if (guiRepeat.odd) { window.fullScreen }{ window.endFullScreen};
				};
			}.defer;
		};
		if (tileEditorState[\isOn]) { ^this };
		// screen shot (png) (I have to limit how often we save screens shots else my SCApp crashes)
		if (key.isScreenShot_png) {
			if ((~previousTime.isNil) or: { Main.elapsedTime - ~previousTime > 5 }) {
				Image.fromWindow(World_World.window).write( ("~/Desktop/World_World_" ++ (~i = (~i ? 0) + 1) +
					(Date.localtime.stamp) ++ ".png").standardizePath);
				~previousTime = Main.elapsedTime;
			};
		};
		// screen shot (jpg) (I have to limit how often we save screens shots else my SCApp crashes)
		if (key.isScreenShot_jpg) {
			if ((~previousTime.isNil) or: { Main.elapsedTime - ~previousTime > 2.5 }) {
				Image.fromWindow(World_World.window).write( ("~/Desktop/World_World_" ++ (~i = (~i ? 0) + 1) +
					(Date.localtime.stamp) ++ ".jpg").standardizePath);
				~previousTime = Main.elapsedTime;
			};
		};
		// dev keys
		if (releaseMode.not) {
			if (key.isAntiAliasing) { antiAliasing = antiAliasing.not }; // toggles antiAliasing on & off
			if (key.isDebugMode   ) { debugMode    = debugMode.not    }; // toggles debugMode on & off
			if (key.isFPS         ) { frameRate    = 180 - frameRate  }; // toggles fps between 60 & 120
			// verbose mode
			if (key.isVerbose) {
				if (verbose) {
					if (World_HUD.justFPS.not) { World_HUD.justFPS = true }{ verbose = false }
				}{
					verbose = true;
					World_HUD.justFPS = false;
				};
				previousFrameRate = 0; // quick hack to stop fps meter from giving wrong values
			};
		};
	}

	// keyboard keyup input
	*keyUp{|key|}

	// controller in
	*controllerIn{|device, index, value|}

	// for global events ?
	*tick{}

	// start the game, usually called from the title screen
	*startGame{|method|
		World_World.initWorld;
		World_Scene.createScene(method, true);
	}

	// this sets up a trigger that does the game over which happens at the end of the current frame
	*gameOver{
		render_Loop = sceneState[\gameOverLoop];
		triggerGameOver = true;
	}

	// this sets up a trigger that does the game over which happens at the end of the current frame
	*gameWin{
		render_Loop = sceneState[\gameWinLoop];
		triggerGameOver = true;
	}


	// end a game, called from game pause + q
	*quitGame{
		if (titleScreen) {
			tileEditorState[\isOn] = false;
			this.isPlaying = true;
			World_Scene.openScene(0);
			guiFrame = worldFrame; // so we can spot dropped frames on a per scene basis
		};
	}

	// need to close HID before quitting this app
	*quitSCLang{
		{
			Server.quitAll;
			0.25.wait;
			HID.closeAll; // there is a 50% 50% chance this crashes on my mac when controllers added
			0.25.wait;
			0.exit
		}.fork;
	}

}
