///////////////////////////////////////////////////////////////////////////////////////////////////////
//                                        Game & Render Loops                                        //
///////////////////////////////////////////////////////////////////////////////////////////////////////
// the game loop is called once every frame from the systemClockLoop. this is where we loop the core mechanics of the game.
// the render loop is deferred once every frame from the systemClockLoop. this is how we draw the scene.
// gui frames might get dropped vs game loop frames if the scene is too demanding to draw in time.
// you can make your own game or render loops, e.g. for an inventory page.
// rename the variables gameLoop & renderLoop to the name of yours methods, (use symbols not strings)
// or set them up in the scene state under sceneState. by default sceneState[\gameLoop] = \gameLoop;
// sceneState[\renderLoop] = \renderLoop;  sceneState[\pauseLoop] = \pauseLoop; sceneState[\gameOverLoop] = \gameOverLoop;
// the pauseLoop & gameOverLoop are used when you pause the game or a Gave Over state is playing out.

+ World_World {

	// the games mechanics loop called every frame on the System clock
	*gameLoop{
		// core mechanics
		if (lightMap.isOn) {lightMap.clearDynamicLightMap}; // clear lightmap for new frame
		World_World.tick;                       // global world tick
		World_Scene.tick;                       // current scene tick
		World_Foreground_Particle.tick;         // global tick for World_Foreground_Particle
		worldTimers.do(_.tick);                 // timers that run outside of all scenes
		sceneTimers.do(_.tick);                 // timers that run inside a scene
		dynamics.do(_.tick);                    // core movement mechanics
		ugp.doCollisions;                       // test and run all collisions
		World_Camera.tick;                      // move camera
		World_World.collectGarbage;             // free any garbage (could be entities, timers or clocks)
		if (triggerGameOver) {
			triggerGameOver = false;
			isGameOver      = true;
			sceneScripts[\gameOver].value;      // this happen at the end of the frame because we may change scenes here
		}{
			if (worldEdgeResponses.notNil) {
				worldEdgeResponses.do{|list| sceneScripts[\worldEdge].value(*list) }; // also this
				worldEdgeResponses = nil;
			};
		};
	}

	// the render func used to draw the world, deferred every frame from the System Clock
	*renderLoop{
		Pen.smoothing_(antiAliasing);                                          // set anti-aliasing on/off
		if (World_Camera.isTransparent) {
			this.drawBackground;                                               // draw background
			Pen.use {
				Pen.translate(transX, transY);                                 // transpose camera
				Pen.scale(zoom, zoom);                                         // zoom camera in & out
				Pen.rotate(cameraAngle, cameraPos.x, cameraPos.y);             // rotate camera
				layers.do{|layer| layer.do(_.draw) };                          // draw all layers & entities
				if (tileEditorState[\isOn]) { World_Tile_Map.drawMapEditor } { // tile editor
					if (lightMap.isOn) { lightMap.draw };                      // draw lightmap
					if (sceneState[\overlay]) { lightMap.drawOverlay };        // draw lightmap overlay
				};
				if (debugMode) {World_Debug.draw};                             // used for debugging
			};
			if (debugMode) {World_Debug.drawGUI};                              // used for debugging
			if (tileEditorState[\isOn].not) { foregroundLayer.do(_.draw) };    // draw foregroundLayer of dust & weather effects
			if (sceneState[\hud].notNil){World_HUD.perform(sceneState[\hud])}; // draw the HUD
		};
		World_Camera.draw;                                                     // draw camera overlay
		World_Message.draw;                                                    // draw any messages or subtitles
	}

	// a slightly different order of drawing here as an example
	*renderLoopShooter{
		Pen.smoothing_(antiAliasing);                                          // set anti-aliasing on/off
		if (World_Camera.isTransparent) {
			this.drawBackground;                                               // draw background
			if (tileEditorState[\isOn].not) { foregroundLayer.do(_.draw) };    // draw foregroundLayer of dust & weather effects
			if (sceneState[\hud].notNil){World_HUD.perform(sceneState[\hud])}; // draw the HUD
			Pen.use {
				Pen.translate(transX, transY);                                 // transpose camera
				Pen.scale(zoom, zoom);                                         // zoom camera in & out
				Pen.rotate(cameraAngle, cameraPos.x, cameraPos.y);             // rotate camera
				layers.do{|layer| layer.do(_.draw) };                          // draw all layers & entities
				if (tileEditorState[\isOn]) { World_Tile_Map.drawMapEditor } { // tile editor
					if (lightMap.isOn) { lightMap.draw };                      // draw lightmap
					if (sceneState[\overlay]) { lightMap.drawOverlay };        // draw lightmap overlay
				};
				if (debugMode) {World_Debug.draw};                             // used for debugging
			};
			if (debugMode) {World_Debug.drawGUI};                              // used for debugging
		};
		World_Camera.draw;                                                     // draw camera overlay
		World_Message.draw;                                                    // draw any messages or subtitles
	}

	// the render loop used while the game is paused
	*pauseLoop{
		this.perform(sceneState[\renderLoop]);
		Pen.fillColor_(Color.black.alpha_(0.5)).addRect(screenRect).draw(0);
		Pen.stringCenteredIn( projectName ++ "\n\nEsc to Resume\n\nQ to Quit", screenRect.moveBy(2,2), World_HUD.hudFont, black);
		Pen.stringCenteredIn( projectName ++ "\n\nEsc to Resume\n\nQ to Quit", screenRect, World_HUD.hudFont, white);
	}

	// the render loop used while the game is over
	*gameOverLoop{
		this.perform(sceneState[\renderLoop]);
		Pen.stringCenteredIn( "Game Over", screenRect.moveBy(2,2), World_HUD.hudFontLarge, black);
		Pen.stringCenteredIn( "Game Over", screenRect, World_HUD.hudFontLarge, white);
	}

	// the render loop used while the game is over
	*gameOverLoopAltVersion{
		this.perform(sceneState[\renderLoop]);
		Pen.stringCenteredIn( "Game Over", screenRect.moveBy(2,2), World_HUD.hudFontLarge, black);
		Pen.stringCenteredIn( "Game Over", screenRect, World_HUD.hudFontLarge, white);
		World_Camera.draw;
	}

	// the render loop used once you have won
	*gameWinLoop{
		this.perform(sceneState[\renderLoop]);
		Pen.stringCenteredIn( "Congratulations\nyou have collected all the sacred stars",
			screenRect.moveBy(2,2), World_HUD.hudFontLarge, black);
		Pen.stringCenteredIn( "Congratulations\nyou have collected all the sacred stars",
			screenRect, World_HUD.hudFontLarge, white);
	}

	// draw background
	*drawBackground{
		if (sceneState[\background].isSymbol) {
			switch (sceneState[\backgroundMode],
				\fixedNoZoom,{
					Pen.fastTileImageSource(screenRect, images[sceneState[\background]], nil, motionBlur);
				},
				\fixed,{
					Pen.use{
						Pen.scale(globalZoom, globalZoom);
						Pen.fastTileImageSource(scaledScreenRect, images[sceneState[\background]], nil, motionBlur);
					};
				},
				\strechToFit,{
					var bounds = images[sceneState[\background]].bounds;
					var zoomX  = bounds.width  / screenWidth;
					var zoomY  = bounds.height / screenHeight;
					Pen.use{
						Pen.scale(1/zoomX, 1/zoomY);
						Pen.fastTileImageSource(tempRect.replaceLTWB(0,0,screenWidth * zoomX, screenHeight * zoomY),
							images[sceneState[\background]], nil, motionBlur);
					};
				},
				\parallax,{
					Pen.use{
						Pen.translate(backX, backY);
						Pen.scale(globalZoom, globalZoom);
						if (sceneState[\rotateBackground]==true) {
							Pen.rotate(cameraAngle, cameraPos.x * zoom + transX - backX, cameraPos.y * zoom + transY - backY);
							// cpu!
						};
						Pen.fastTileImage(imageBounds[sceneState[\background]],
							images[sceneState[\background]], motionBlur);
					};
				},
				\followCamera,{
					Pen.use{
						Pen.translate(transX, transY);
						Pen.scale(zoom, zoom);
						if (sceneState[\rotateBackground]==true) {
							Pen.rotate(cameraAngle, cameraPos.x*zoom, cameraPos.y * zoom); // cpu!
						};
						Pen.fastTileImageSource(worldRect, images[sceneState[\background]], nil, motionBlur);
					};
				},
				\scroll,{
					sceneState[\backgroundY] = sceneState[\backgroundY] + (sceneState[\backgroundDY] * timeDilation);
					Pen.use{
						var newBounds    = Rect(actualLeftEdge, actualTopEdge, actualRightEdge-actualLeftEdge,
							actualBottomEdge - actualTopEdge);
						var bounds       = imageBounds[ sceneState[\background] ];
						var extend       = bounds.height;
						newBounds.top    = newBounds.top - extend +  sceneState[\backgroundY].wrap(0,extend);
						newBounds.height = newBounds.height + extend;
						Pen.translate(transX, transY);
						Pen.scale(zoom, zoom);
						Pen.fastTileImageSource(newBounds, images[sceneState[\background]], nil, motionBlur);
					};
				}
			);
		}{
			Pen.fillColor_(sceneState[\background].alpha_(motionBlur)).addRect(screenRect).draw(0);
		};

	}
}
