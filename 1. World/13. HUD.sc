////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                 HUD                                                //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_HUD : World_World {

	classvar <>justFPS = false , <hudFont, <hudFontSmall, <hudFontMedium, <hudFontLarge, <statFont, <statFontMedium;

	*init{
		if (verbose) { "World_HUD:init".postln };
		hudFont        = Font("Geomancy", 35, true);
		hudFontSmall   = Font("Geomancy", 35 * 0.5, true);
		hudFontMedium  = Font("Geomancy", 35 * 0.7, true);
		hudFontLarge   = Font("Geomancy", 35 * 1.28, true);
		statFont       = Font("Monaco"  , 12 * globalZoom, true);
		statFontMedium = Font("Monaco"  , 14 * globalZoom, true);
	}

	*drawStats{
		if (frameRate==60) {
			if (sceneState[\background].isSymbol.not) {
				Pen.fillColor = sceneState[\background].complementary;
			}{
				Pen.fillColor = white
			};
		} { Pen.fillColor = Color.yellow };
		if (justFPS)
		    { Pen.stringInRect(this.stats, screenRect, statFontMedium)}
		    { Pen.stringInRect(this.stats, screenRect, statFontMedium)};
	}

	*stats{
		if (justFPS) {
			^fps.asFormatedString(1,1) +"fps " ++ ((worldFrame - guiFrame).clip(0,inf).asInteger)
		}{
			var ents = entities.size;
			var drawEnts = 0;
			var playerOrigin;
			World_World.layers.do{|i| drawEnts = drawEnts + i.size }; // this is no longer correct due to shadows
			drawEnts = drawEnts + (foregroundLayer.size);
			if(players.isEmpty){ playerOrigin = " nil" } {
				playerOrigin = " Point("
				++players[0].origin.x.asFormatedString(1,1)++","
				+players[0].origin.y.asFormatedString(1,1)
				++ ")"
			};
			^fps.asFormatedString(1,1) +"fps"
			++ "\n\nWorld Frame    :"
			+ worldFrame
			++ "\nDropped Frames :"
			+ (worldFrame - guiFrame).clip(0,inf).asInteger
			++ "\nWorld Time     :"
			+ worldTime.asFormatedString(1,1)
			++ "\nTime Dilation  :"
			+ timeDilation.asFormatedString(1,2)
			++ "\nFrame Length   :"
			+ frameLength.asFormatedString(1,4)
			++ "\nNum Entities   :"
			+ ents
			++ "\nNum Particles  :"
			+ (drawEnts - ents)
			++ "\nCamera Pos     : Point("
			++cameraPos.x.asFormatedString(1,1)++","
			+cameraPos.y.asFormatedString(1,1)
			++ ")\nCamera Angle   :"
			+cameraAngle.asFormatedString(1,3)
			++ "\nZoom           :"
			+zoom.asFormatedString(1,3)
			++ "\nGlobal Zoom    :"
			+globalZoom.asFormatedString(1,3)
			++ "\nMotion Blur    :"
			+motionBlur.asFormatedString(1,3)
			++ "\nPlayer Origin  :"
			++ playerOrigin
			++"\nScene Index    :"
			+World_Scene.sceneIndex
			++ "\nScreen Bounds  :"
			+screenRect
			++ "\nWorld Bounds   :"
			+ worldRect
			++ "\nMap Size       :"
			+((tileMap?()).mapWidth)+"x"
			+((tileMap?()).mapHeight)
			++ "\nTile Size      :"
			+sceneState[\tileWidth]++"px x"
			+sceneState[\tileHeight]
			++ "px\nGame Loop      : \\"
			++game_Loop
			++ "\nRender Loop    : \\"
			++render_Loop
			++ "\nHUD            : \\"
			++sceneState[\hud]
			++ "\nScale Mode     : \\"
			++scaleMode
			++ "\nAnti-aliasing  :"
			+antiAliasing
			++ "\n\nWorld World v"++ engineVersionMajor ++ "." ++ engineVersionMinor

		}
	}

}
