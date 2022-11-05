////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                         Raycaster  scene                                           //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_FPS_Player : World_Player_Base {

	*new{|id,origin,layer,radius,enemies| ^super.new.initEntityWithRadius(origin,radius).initPlayer(id).init(layer,radius,enemies) }

	init{|layer,radius,enemies|
		components[\mechanics  ] = World_FPS_Mechanics     (this, 0@0, 20@20, 0.8@0.8, 0@(0.25*0), 0@0).angle_(pi*1.5);
		components[\controller ] = World_FPS_Controller    (this,id).acceleration_(2);
		components[\collider   ] = World_TDP_Collider      (this, radius, enemies);
		components[\health     ] = World_TDP_Health        (this, 1, 1);
		components[\death      ] = World_TDP_Death         (this, Color(0.737, 0.71, 0.937, 0.7));
		components[\weapon     ] = World_Platformer_Weapon (this, 5,5,0.1,2);
		components[\damage     ] = World_Damage_Profile    (this, 100);
		components[\amour      ] = World_Armour_Profile    (this, 1, 1, 1, 1);
		components[\hitTimer   ] = World_Player_Hit_Timer  (this, 0.15, false);
		components[\drawFunc   ] = World_TDPVector_DrawFunc(this, layer, Color.red, Color.white);

		components[\rayCaster  ] = World_RayCaster_ViewPort(this);
	}

}


World_FPS_Mechanics : World_Classical_Mechanics {

	var <>exhaustOffset = 0;

	myTick{
		velocity.clipToZero; // stop moving below a threshold
		if (lightMap.isOn) { lightMap.addDynamicLightSource(origin.x, origin.y, 2, 11, 0.7) };
		// smoke
		if ((0.3 * timeDilation * (velocity.rho / 8)).coin) {
			if (velocity.isMoving) {
				World_Circle_Particle(origin.copy += tempPoint.fromPolar(exhaustOffset, angle)
					,11,6.rrand(10),Color(1,1,1,0.25),1.0.rand2,1.0.rand2,0.25);
			};
		};
	}

	// what happens when the entity hits the edge of the world?
	worldEdge{
		if (origin.x < 0          ) { origin.x = origin.x.clip(0,worldRight ); velocity.x = 0 };
		if (origin.x > worldRight ) { origin.x = origin.x.clip(0,worldRight ); velocity.x = 0 };
		if (origin.y < 0          ) { origin.y = origin.y.clip(0,worldBottom); velocity.y = 0 };
		if (origin.y > worldBottom) { origin.y = origin.y.clip(0,worldBottom); velocity.y = 0 };
	}

}

World_FPS_Controller : World_Controller_Base {

	var <>acceleration=1;

	retrigger{ ^[14, 15, 7] } // L,R,U,D + fire get retriggered

	// ps4 controller in
	controllerIn{|device, index, value|
		// left joy L&R to acceleration X
		if (index== 14) {

			components[\mechanics].angularVelocity = value.map(0,1,-0.075,0.075);


			components[\mechanics].acceleration.fromPolar(
				(controllerState[device][15]?0.5).map(0,1,-1,1.0) * acceleration, components[\mechanics].angle

			);

		};
		// left joy U&D to acceleration Y
		if (index== 15) {

			//components[\mechanics].acceleration.y = value.map(0,1,1.0,-1.0) * acceleration

			components[\mechanics].acceleration.fromPolar(value.map(0,1,-1,1.0) * acceleration, components[\mechanics].angle );
		};

		// R1 to fire
	    if (index==7) { if (value==1) { components[\weapon].start } { components[\weapon].stop } };
	}

}



World_RayCaster_ViewPort : World_Component {

	var <>origin, <>angle = 0, <>fov = 0.4, <>resolution = 150, <>dRho = 35, <>interations = 30;
	var <castPoint, <dd, <>rects, <>colors, <>zBuffer;

	*new{|parent|
		^super.new.initComponent(parent).init()
	}

	init{
		var sliceWidth = screenWidth / resolution;
		rects     = {|i| Rect(sliceWidth*i,0,sliceWidth,1) } ! resolution;
		colors    = { Color() } ! resolution;
		zBuffer   = 0 ! resolution;
		origin    = parent.origin;
		castPoint = Point();
		dd        = Point();
	}

	render{
		var dTheta = fov * 2 / resolution;
		var angle = components[\mechanics].angle; // temp
		resolution.do{|i| this.ray(angle - fov + (dTheta*i), i) }
	}

	ray{|rayAngle,index|

		castPoint.replace(origin);                     // start at origin
		dd.fromPolar(dRho, rayAngle);                   // distance between each interation
		interations.do{|i|
			var cell = ugp.getCell( castPoint += dd ); // add dd every interation and get the cell
			var distance = inf;                        // the shortest distance
			ugp.cells[cell][\rayCast].do{|collider|
				// test for collisions
				if (collider.testForCollision(castPoint)) {

					var cDist = collider.origin.dist(origin);

					if (cDist < distance) {
						var size = ((cDist * 0.01) + 1).reciprocal;
						var size2 = ((cDist * 0.002 **2) + 1).reciprocal;
						var col = collider.parent[\drawFunc].fillColor;
						distance       = cDist;
						zBuffer[index] = distance;

						colors[index].replaceMul(col,size2);

						size = size * halfScreenHeight;
						rects[index].top_( halfScreenHeight - size );
						rects[index].height_( size * 2);
						^this
					};

					//var heightRatio = (dist + 1).reciprocal
				};
			}
		};
		zBuffer[index] = inf;
	}

	draw{

		rects.do{|rect, index|
			if (zBuffer[index]< inf) {
				Pen.fillColor_(colors[index]).addRect(rect).draw(0);
				//Pen.fillColor_( colors[index] ).strokeColor_(black).addRect(rect).draw(3);
			};

		}

	}

}

World_Rect_Collider : World_Rect_Collider_Base {

	var <>restitution;

	*new{|parent, restitution = 1, solid = false|
		^super.new.initComponent(parent).initUGP.init(restitution, solid) // initShape not needed here, shape is a rect
	}

	init{|argRestitution, solid|
		isSolid = solid;

		this.collisionSource_(\rayCast, true); // £ temp

		if (isSolid) {
			// £
			this.collisionSource_(\tiles, true);
		}{
			this.collisionSource_(\npcs, true);
			this.collisionResponder_(\tiles, true);
		};
		restitution = argRestitution;
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile ) {
			if (collider.isRect    ) { this.    rectResponse_rigidBodyBounce(collider,restitution); ^this };
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider,restitution); ^this };
		};
	}

}

+ World_World {

	// the render func used to draw the world, deferred every frame from the System Clock
	*raycasterRenderLoop{
		var rayCaster = players[0][\rayCaster];

		//if (true) {this.renderLoop; ^this };

		rayCaster.render;
		Pen.smoothing_(antiAliasing);                                          // set anti-aliasing on/off
		if (World_Camera.isTransparent) {
			this.drawBackground;                                               // draw background
			Pen.use {
				rayCaster.draw;
			}
		};

	}

}


//ToTry: tie color to velocity


+ World_Scene {
	*buildRaycasterScene{
		World_Scene.setupScenePX(1280, 800, 40, 40, 1);
		sceneState[\background] = Color.black;
		sceneState[\renderLoop] = \raycasterRenderLoop;

		1280.div(40).do{|x|
			800.div(40).do{|y|
				if ((x==0) || (y==0) || (y== (800.div(40)-1)) || (x== (1280.div(40)-1))) {
					World_Rect.newRect( Rect(x*40,y*40,40,40), 7, Color.rand(), Color.black);
				};
			};
		};

		World_FPS_Player(0, 200@200 , 7, 24, []);

		sceneScripts[\startGame ] = { World_Camera.reset.setToBlack.fadeToClear };
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) { World_Audio.stopEverything; World_World.quitGame }; // quit
		};

	}
}




