////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                         Simulation scene                                           //
////////////////////////////////////////////////////////////////////////////////////////////////////////

//ToTry: tie color to velocity

+ World_Scene {
	*buildSimScene{
		World_Scene.setupScenePX(1280, 800, 40, 40);
		sceneState[\background] = Color.white;
		// static objects
		[ [0,0,400,40],[400,0,400,40],[800,0,400,40],[1200,0,80,40],[1240,40,40,400],[1240,440,40,360],[840,760,400,40],
		  [440,760,400,40],[40,760,400,40],[0,440,40,360],[0,40,40,400] ].do{|args|
			World_Rect.newRect( Rect(*args), 7, Color.grey(0.4), Color.black);
		};
		World_Triangle(1140@660, 6, 100, \bottomRight, Color.grey(0.6), Color.black );
		World_Triangle(1140@140, 6, 100, \topRight,    Color.grey(0.6), Color.black );
		World_Triangle( 140@140, 6, 100, \topLeft,     Color.grey(0.6), Color.black );
		World_Triangle( 140@660, 6, 100, \bottomLeft,  Color.grey(0.6), Color.black );
		World_Rect    ( 450@380, 6, 100, Color.grey,   Color.black );
		World_Circle  ( 850@380, 6, 100, Color.grey,   Color.black );
		// moving circles
		200.do{|i|
			var size = (i<4).if(32, 6.exprand(20));
			World_Circle(Point(140.rrand(1140), 140.rrand(660)), 5, size, Color.grey(0.3.rrand(1.0)), Color.black, true, true)
			.velocity_(Vector.randRangePolar(1,1) ).mass_(size);
		};
		// called when the game starts
		sceneScripts[\startGame ] = {
			World_Camera.reset.setToBlack.fadeToClear(2);
			if (skipIntro.not) { ["Move objects with the mouse", "Change speed with the arrow keys"].subtitle(4,0) };
		};
		// mouse down
		sceneScripts[\mouseDown] = {|screenPoint,worldPoint|
			entities.do{|entity|
				if (entity.boundingBox.containsPoint(worldPoint)) {
					sceneState[\entity] = entity;
					sceneState[\offset] = entity.origin - worldPoint;
				};
			};
		};
		// mouse move
		sceneScripts[\mouseMove] = {|screenPoint,worldPoint|
			if (sceneState[\entity].notNil) { sceneState[\entity].origin_( worldPoint + sceneState[\offset] ) };
		};
		// key down
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) { World_Audio.stopEverything; World_World.quitGame }; // quit
			if (key.isIncTimeDilation) { timeDilation = (timeDilation * (2**0.5)).clip(0.0078125,4) };      // inc time dilation
			if (key.isDecTimeDilation) { timeDilation = timeDilation / (2**0.5);
				if (timeDilation < 0.0078125) { timeDilation = 0 } };                                 // dec time dilation
		};

	}
}
