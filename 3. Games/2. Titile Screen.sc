////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Building Scenes - Title screen                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// A scene is the space, containers & scripts in which a level or game and anything in it can be built and run.
// This is where we build the scenes, its entities & define their assets, tile sets, scripts, gameLoops, HUD etc..
// What to do when the engine starts, what happens when you start a new game, a new level, or what to do when its game over
// A Scene doesn't need to have Assets, Tile Sets, Light Maps, Scripts or a HUD but typically does.
//
/////////////////////////
// Title Screen Assets //
/////////////////////////

+ World_Assets {
	// All asset method names must end with "_ASSET_DEF". Any methods with this prefix get called during Asset class init
	// This is because the assets are needed before the scene is created and before the engine is allowed to start
	titleScreen_ASSET_DEF{
		imagePaths  = IdentityDictionary[ \fantasy2     -> "backgrounds/fantasy 2.jpg" ];
		bufferPaths = IdentityDictionary[ \openingMusic -> "opening music.ogg"        ];
	}
}

////////////////////////////////////////////////////////////////
// Title Screen scene (opens at launch if titleScreen = true) //
////////////////////////////////////////////////////////////////

+ World_Scene {
	*buildTitleScreenScene{
		World_Scene.setupScene(26, 17, 50, 50, 1, \titleScreen_ASSET_DEF);
		World_Scene.addSparklyDust(600);
		World_Text(centrePoint+(0@ -210), 10, projectName                         , World_HUD.hudFontLarge);
		World_Rounded_Rect.newLTWH(centrePoint.x-220,centrePoint.y-120-35,440,405,10,Color.black,Color.black);
		World_Text(centrePoint+(0@ -130), 10, "Press"                             , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@ -80 ), 10, "1 to Play Space Jumper"            , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@ -30 ), 10, "2 to Play Alien Maze"              , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@20   ), 10, "3 to Play Astro Attack"            , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@70   ), 10, "4 to Enter Simulation #1"          , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@120  ), 10, "5 to Open Debug Scene"             , World_HUD.hudFontMedium);
		World_Text(centrePoint+(0@180  ), 10, "CMD + F to toggle Fullscreen"      , World_HUD.hudFontSmall);
		World_Text(centrePoint+(0@220  ), 10, "CMD + Q to Quit"                   , World_HUD.hudFontSmall);
		World_Text(centrePoint+(0@340  ), 10, releaseMode.if( "made by LNX_Games" , "? for Dev Info" ), Font.default);
		sceneState[\background    ] = \fantasy2;
		sceneState[\backgroundMode] = \strechToFit;
		// Scene Scripts ///////////////////////////////////////
		// called when the scene is 1st opened
		sceneScripts[\startScene] = {
			World_Camera.setToBlack;
			{
				0.5.wait;
				World_Audio.play(\openingMusic,1,5,1,1,0,true,\music);
				1.0.wait;
				World_Camera.fadeToClear(7,0.125); // i need a better fade
			}.forkInScene;
		};
		// called every frame
		sceneScripts[\tick] = {
			var alpha = (1 - World_Camera.alpha)**10; // grab the alpha of the camera fade and use it here
			entities.do{|text| text.alpha_(alpha) };  // just the text entities in the entire scene
		};
		// key down press
		sceneScripts[\keyDown] = {|key|
			if ((key.key>=49)&&(key.key<=54)) {
				World_Camera.setToBlack;
				{
					World_World.startGame([
						\buildSpaceJumperScene, \buildAlienMazeScene, \buildAstroAttackScene,
						\buildSimScene,         \buildDebugScene,     \buildRaycasterScene
					][key.key-49]);
				}.deferInWorld;
			};
			if (key.isQuitSCLang) { World_World.quitSCLang };
		};
		// called when scene is closed
		sceneScripts[\leaveScene] = { World_Audio.release(\openingMusic) };
		// revisit scene (called when the scene is opened for a 2nd or more time)
		sceneScripts[\revisitScene] = {
			World_Audio.releaseEverything;
			World_Camera.setToBlack;
			World_Camera.fadeToClear(2);
			World_Message.clearAll;
			World_Message.reset;
			World_Audio.play(\openingMusic,1,2.5,1,1,0,true,\music);
			scenes[1..].do(_.free);
			scenes = scenes[..0];   // remove old scenes
		};
	}
}
