////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                           Debug                                                    //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
World_World.debugMode = World_World.debugMode.not;
*/

World_Debug : World_World {

	classvar <debugState;

	*init{
		debugState = IdentityDictionary[];
		debugState[\inDebugScene] = false;
	}

	*mouseDown{|screenPoint,worldPoint,mod,button,clickCount|
		if (debugMode.not) {^this};
		debugState[\selectedEntity] = nil;
		entities.do{|entity|
			if (entity.boundingBox.containsPoint(worldPoint)) {
				debugState[\selectedEntity] = entity;
				debugState[\offset] = debugState[\selectedEntity].origin - worldPoint;
			};
		};
	}

	*mouseMove{|screenPoint,worldPoint,mod|
		if (debugMode.not) {^this};
		if (debugState[\selectedEntity].notNil) { debugState[\selectedEntity].origin_(
			worldPoint + debugState[\offset]
		) };
	}

	*mouseUp{|screenPoint,worldPoint,mod,button|}

	*mouseWheel{|screenPoint,worldPoint,dx,dy,mod|
		if (debugMode.not) {^this};
		debugState[\wheel] = ((debugState[\wheel] ? (0@0)) + ((dx@dy)*3) ).clip(-inf,0);
	}

	*keyDown{|key|
		if (debugMode.not) {^this};
		if (key.isIncTimeDilation) { timeDilation = (timeDilation * 2).clip(0.0078125,4) };      // inc time dilation
		if (key.isDecTimeDilation) { timeDilation = timeDilation / 2;
			if (timeDilation < 0.0078125) { timeDilation = 0 } };                                // dec time dilation
		if (key.isIncMotionBlur  ) { World_Camera.motionBlur_(World_Camera.motionBlur - 0.05) }; // dec motion blur
		if (key.isDecMotionBlur  ) { World_Camera.motionBlur_(World_Camera.motionBlur + 0.05) }; // inc motion blur
	}

	*drawGUI{
		var string, y=0, x=0, rect;
		if (debugState[\selectedEntity].notNil) {
			var ent     = debugState[\selectedEntity];
			var instVar = ent.class.instVarNames.select{|varName| ent.respondsTo(varName) };
			var objs    = instVar.collect{|varName| ent.perform(varName) };
			string  = "\n" ++ ent.class.asString ++ "()\n\n";
			instVar.do{|i,j| if (i!=\components) { string = string ++ i ++ " : " ++ objs[j].asCompileString + "\n" } };
			string = string ++ "\n";
			(objs[instVar.indexOf(\components)] ? ()).keysValuesDo{|key,comp|
				var objs, instVar = comp.class.instVarNames.select{|varName| comp.respondsTo(varName) };
				instVar.remove(\origin);
				instVar.remove(\boundingBox);
				instVar.remove(\parent);
				// check all these ok ^^
				objs = instVar.collect{|varName| comp.perform(varName) };
				string = string ++ "components[\\" ++ key ++ "] = " ++ comp.class ++"()\n";
				instVar.do{|i,j|
					if (i!=\components) { string = string ++ "  " ++ i ++ " : " ++ objs[j].asCompileString + "\n"; };
				};
				string = string ++ "\n";
			};
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
			string = "\nWorld World v"++ engineVersionMajor ++ "." ++ engineVersionMinor ++"\n\n"
			++fps.asFormatedString(1,1) +"fps"
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
			+antiAliasing;
		};
		if (debugState[\wheel].notNil) {
			var realBounds = string.realBounds(World_HUD.statFontMedium);
			debugState[\wheel].y = debugState[\wheel].y.clip( (realBounds.height - screenHeight + 100).clip(0,inf).neg, 0);
			y = y - debugState[\wheel].y;
			debugState[\wheel].x = debugState[\wheel].x.clip(
				(realBounds.width - (screenWidth / (debugState[\inDebugScene].if(2,10))) + 50).clip(0,inf).neg, 0);
			x = x - debugState[\wheel].x ;
		};
		if (debugState[\inDebugScene]) {
			rect = Rect(screenWidth * 0.5 + 10 - x, y.neg, screenWidth * 0.5 - 10 + x, screenHeight+y);
			Pen.fillColor_(Color.grey(0.2)).fillRect( rect );
		}{
			rect = Rect(screenWidth * 9 / 10 + 10 - x, y.neg, screenWidth / 10 - 10 + x, screenHeight+y);
			Pen.fillColor_(Color(0,0,0,0.75)).fillRect( rect );
		};
		Pen.fillColor_(white).stringInRect(string, rect.moveBy(10,0), World_HUD.statFontMedium);
	}

	*draw{
		this.userDraw;
		Pen.width_(2);
		entities.do{|entity|
			var boundingBox = entity.boundingBox;
			if ((boundingBox.left   < actualRightEdge  ) and:
				{boundingBox.right  > actualLeftEdge   } and:
				{boundingBox.top    < actualBottomEdge } and:
				{boundingBox.bottom > actualTopEdge    }) {
				var origin = entity.origin;
				if (entity === debugState[\selectedEntity]) {
					Pen.use{
						Pen.width_(3);
						Pen.strokeColor_(Color(1,1,0,1));
						Pen.lineDash_(FloatArray[2,2]);
						Pen.strokeRect(boundingBox);
					};
				}{
					Pen.strokeColor_(Color(1,0,0,0.35));
					Pen.strokeRect(boundingBox);
				};
				Pen.fillColor_(Color.red  );
				Pen.fillOval( Rect.aboutPoint(origin,3,3) );
				if (entity[\mechanics].notNil){
					var velocity = entity[\mechanics].velocity;
					Pen.strokeColor_(Color.green).line(origin, origin + (velocity*10)).stroke };
			};
		};
	}

}

