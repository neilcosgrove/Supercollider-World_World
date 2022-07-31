////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                            Debug scene                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the debug scene and is opened when World_World.debugMode = true
// Use this scene to test, try out and debug your code

+ World_Assets {
	debug_ASSET_DEF{
		imagePaths  = IdentityDictionary[
			\platformPlayer         -> "sprites/player1A.png",
			\platformPlayerWallGrab -> "sprites/player1B.png",
		];
		bufferPaths = IdentityDictionary[];
	}
}

+ World_Tile_Map {
	debug_TILE_SET_DEF{
		tileSetDefinition = [
			\bricks -> [ World_Color_Rect_Tile, [0, 0, tileWidth, tileHeight, 1, 1, 10, true, Color.grey(0.4), Color.black ] ]
		];
	}
}

+ World_Scene {

	*buildDebugScene{
		World_Scene.setupScene(1280/40, 800/40, 20, 20, 1, \debug_ASSET_DEF, \debug_TILE_SET_DEF); // << the 2 methods above
		tileMap.addBorder(\bricks).renderMap;
		sceneState[\background] = Color.black;      // can be either a Color or a key to an image in images[\key]
		sceneState[\hud       ] = \debugHUD;        // name of method below which draw the HUD

		World_Circle(200@200, 6, 30, Color.grey, Color.white, true, true).velocity_(Vector(1,2)).mass_(1);
		World_Triangle(520@280 , 5, 100, \bottomRight, Color.grey, Color.white )[\drawFunc];
		World_Platform_Player(0, 75@325, 7, [], \platformPlayer, \platformPlayerWallGrab);
		World_Vector_Top_Down_Player(1, 75@75 , 7, 24, []);

		// called when the game starts
		sceneScripts[\startGame ] = { centreOnScreen = false; debugMode = true; World_Debug.debugState[\inDebugScene] = true };
		// called when key pressed down
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) { World_Audio.stopEverything; World_World.quitGame }; // quit
		};
		// called when scene is closed,
		sceneScripts[\leaveScene] = {
			if (sceneIndex == 0) { World_World.quitSCLang } {
				debugMode = false; centreOnScreen = true; World_Debug.debugState[\inDebugScene] = false;
			};
		};
		sceneScripts[\startScene   ] = {};                          // called when the scene is 1st opened
		sceneScripts[\keyUp        ] = {};                          // key up action
		sceneScripts[\mouseDown    ] = {|screenPoint,worldPoint| }; // mouse down action
		sceneScripts[\mouseMove    ] = {|screenPoint,worldPoint| }; // mouse move action
		sceneScripts[\mouseUp      ] = {|screenPoint,worldPoint| }; // mouse up action
		sceneScripts[\mouseWheel   ] = {};
		sceneScripts[\tick         ] = {};                          // called every frame
		sceneScripts[\controllerIn ] = {|device, index, value| };   // controller input
		sceneScripts[\worldEdge    ] = {|id, edge|};                // edge = \left, \right, \top, \bottom
		sceneScripts[\revisitScene ] = {};                          // called when the scene is opened for a 2nd or more time
		sceneScripts[\playerDeath  ] = {|id|};                      // called when the player dies
		sceneScripts[\gameOver     ] = {};                          // called when the game finishes
		sceneScripts[\gameWin      ] = {};                          // called when the player wins
	}

}

+ World_Debug {
	*userDraw{
		// put your own gui highlights here
		[~p1, ~p2, ~p3, ~p4, ~p5, ~p6, ~p7, ~p8, ~p9, ~p10].do{|p|
			if (p.notNil) { Pen.fillColor_(Color.green); Pen.fillOval( Rect.aboutPoint(p,4,4) ) };
		};
	}
}

+ World_HUD {
	*debugHUD{}
}
