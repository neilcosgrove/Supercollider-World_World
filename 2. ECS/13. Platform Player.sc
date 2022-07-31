////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                   Platform Player                                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Platform_Player : World_Player_Base {

	*new{|id, origin, layer, enemies, image, grabImage, jumpSound, doubleJumpSound, deathSound, damageSound, fireSound|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(origin,radius).initPlayer(id)
		.init(layer,radius,enemies,image,grabImage,jumpSound, doubleJumpSound, deathSound, damageSound, fireSound)
	}

	init{|layer,radius,enemies,image,grabImage, jumpSound, doubleJumpSound, deathSound, damageSound, fireSound|
		components[\controller ] = World_Platform_Player_Controller(this, id);
		components[\mechanics  ] = World_Platform_Player_Mechanics (this, id, jumpSound, doubleJumpSound);
		components[\collider   ] = World_Platform_Player_Collider  (this, radius, enemies);
		components[\health     ] = World_Platform_Player_Health    (this, 100, 100, damageSound);
		components[\damage     ] = World_Damage_Profile            (this, 100);
		components[\amour      ] = World_Armour_Profile            (this, 1, 1, 1, 1);
		components[\death      ] = World_Platform_Player_Death     (this, Color(1,0,1,0.5), deathSound);
		components[\weapon     ] = World_Platformer_Weapon         (this, fireSound: fireSound);
		components[\hitTimer   ] = World_Player_Hit_Timer          (this, 0.15, false);
		components[\drawFunc   ] = World_Platform_Player_DrawFunc  (this, layer, image, grabImage);
	}

}

World_Platform_Player_Controller : World_Controller_Base {

	retrigger{ ^[14, 7] } // L&R + fire get retriggered

	controllerIn{|device, index, value|
		// left joy L&R to acceleration X
		if ((index== 14) and: {components[\mechanics].wallGrabOn.not}) {
			components[\mechanics].acceleration.x = value.map(0,1,-1.2,1.2)
		};
		// R1 to fire
	    if (index==7) { if (value==1) { components[\weapon].start } { components[\weapon].stop } };
		// x to jump
		if (index==1) { components[\mechanics].fromController_jump(value) };
		// l1 wall grab
		if (index==4) { components[\mechanics].fromController_wallGrab(value) };
	}

}

// this is quite a big component, I found it hard to split up or make easier

World_Platform_Player_Mechanics : World_Classical_Mechanics_Base {

	var <>defaultGravity, <>isJumping, <>jumpPeriod, <>jumpTimer, <>jumpAcceleration, <>releaseDamp, <>coyoteTime, <>coyoteTimer;
	var <>inputBufferPeriod, <>inputBufferTimer, <>doulbeJumps, <>doubleJumpCounter, <>jump2Period, <>jump2Acceleration;
	var <>jump2VelJump, <>wallGrabOn, <>wallSlide, <>wallJumpSpeed, <>wallJumpAcc, <>onSlope, <>slopeTheta, <>left, <>right;
	var <>top, <>bottom, <>jumpSound, <>doubleJumpSound;
	var <>id;

	*new{|parent, id, jumpSound, doubleJumpSound| ^super.new.initComponent(parent).init(id,jumpSound, doubleJumpSound) }

	// there are so many parameters that it might be worth subclassing this method for your own platformers
	init{|argID, argJumpSound, argDoubleJumpSound|
		origin            = parent.origin;
		boundingBox       = parent.boundingBox;
		dynamics          = dynamics.add(this);
		velocity          = Vector(0,0);              // start stationary
		maxVelocity       = Vector(25,10);            // max x velocity & max y velocity are different
		defaultGravity    = Vector(0,0.75);           // default gravity, (gravity changes on wall slide)
		gravity           = defaultGravity.copy;      // set gravity to default
		friction          = Vector(0.9,1);            // friction is only horizontal here
		acceleration      = Vector(0,0);              // controller acceleration
		isJumping         = false;                    // is player jumping?
		jumpPeriod        = 0.2;                      // duration of jump
		jumpTimer         = jumpPeriod;               // jump timer
		jumpAcceleration  = -2;                       // acceleration applied during a jump period
		releaseDamp       = 0.5;                      // damping applied if button let go during the jumpPeriod
		coyoteTime        = 5/60;                     // falling off edge jump grace (5 frames @ 60 fps = 0.083 sec)
		coyoteTimer       = 0;                        // timer for grace period
		inputBufferPeriod = 7/60;                     // jump button input buffer (7 frames @ 60 fps = 0.117 sec)
		inputBufferTimer  = 0;                        // timer for grace period
		doulbeJumps       = 1;                        // number of double jumps allowed
		doubleJumpCounter = 0;                        // double jump counter
		jump2Period       = 0.15;                     // duration of double jump
		jump2Acceleration = -2.375;                   // acceleration applied during double jump
		jump2VelJump      = 0;                        // jump in horizontal velocity when doing a double jump
		wallGrabOn        = false;                    // player is grabbing a wall
		wallSlide         = Vector(0,0.6);            // velocity while grabbing a wall
		wallJumpSpeed     = 12.5;                     // speed when jumping off a wall
		wallJumpAcc       = -2.375;                   // acceleration applied during a wall jump
		onSlope           = false;                    // is player on a slope
		slopeTheta        = 0;                        // angle of slope
		left              = false;
		right             = false;
		top               = false;
		bottom            = false;
		jumpSound         = argJumpSound;
		doubleJumpSound   = argDoubleJumpSound;
		id                = argID;
	}

	// core tick adjusted for slopes. gravity and velocity are rotated by the angle of the slope
	tick{
		if (active) {
			if (acceleration.notNil) { velocity.addMul  (acceleration, timeDilation)}; // apply acceleration
			if (    friction.notNil) { velocity.mulPower(friction,     timeDilation)}; // apply friction
			if (onSlope) {
				tempVector.rotateReplace(gravity, slopeTheta);                         // adjustments for slope,
				velocity.addMul(tempVector, timeDilation * 0.00001);                   // gravity towards slope
			}{
				if ( gravity.notNil) { velocity.addMul(gravity, timeDilation)};        // apply gravity
			};
			if ( maxVelocity.notNil) { velocity.clipVelocity(maxVelocity)};            // clip velocity to maximum
			this.myTick;

			velocity.clipXYToZero; // stop moving below a threshold

			if (onSlope) {
				tempVector.rotateReplace(velocity, slopeTheta);                        // adjustments for slope
				origin.addMul(tempVector, timeDilation);
			}{
				origin.addMul(velocity, timeDilation);                                 // apply velocity
			};
			this.worldEdge;                                                            // apply world edge response
			parent.updateBoundingBox;                                                  // update pos
			onSlope = false;
		}
	}

	myTick{
		// jumping
		if (isJumping) {
			if (jumpTimer>0) {
				jumpTimer = jumpTimer - frameLength;
			}{
				isJumping = false;
				acceleration.y = 0;
			};
		};
		// direction facing set to only left or right
		if ( velocity.x > 0 )  { angle = 0  };
		if ( velocity.x < -0 ) { angle = pi };
		// wall grab
		if (wallGrabOn) {
			// am i still grabbing a wall?
			var test = false;
			var rect = boundingBox.copy;
			var cell = components[\collider].cell;
			rect.top = rect.top + (rect.height/4);
			rect.height = rect.height/2;
			rect.left = rect.left + (Point.fromPolar(2,angle).x);  // only adjust left because angle is direct we are facing
			ugp.doTileCollisionsFunc(cell,rect, { test = true } );
			if (test.not) { this.stopWallGrab };
		}{
			// can i start a grab (picked up from collision detection)
			if ((controllerState[id, 4].notNil) and: {controllerState[id, 4]>0.75}) {
				if (left || right) {
					this.startWallGrab;
					if (left) { angle = pi } { angle = 0  };
				}
			};
		};
		// dec the various timers
		coyoteTimer      = coyoteTimer      - frameLength;
		inputBufferTimer = inputBufferTimer - frameLength;
		this.resetSurfaces; // this will be update in time for next frame
		if (lightMap.isOn) { lightMap.addDynamicLightSource(origin.x, origin.y, 2.25, 7, 0.35) };
	}

	// set a surface that we are currently touching
	setSurface{|edge|
		switch (edge)
		    {\left   } { left   = true }
		    {\right  } { right  = true }
		    {\top    } { top    = true }
		    {\bottom } { bottom = true };
	}

	// reset surface touching
	resetSurfaces{
		left   = false;
		right  = false;
		top    = false;
		bottom = false;
	}

	// jump input from controller
	fromController_jump{|value|
		if (value==1) { // button down
			if (wallGrabOn) { this.doJump; ^this };
			if (bottom) {
				this.doJump; // do a normal jump
			}{
				if (coyoteTimer>0) {  // if i'm in the grace period for falling off an edge
					this.doJump;
				}{
					inputBufferTimer = inputBufferPeriod;           // set timer for input buffer
					if (doubleJumpCounter>0) {                      // if i have any double jumps left, then do them
						doubleJumpCounter = doubleJumpCounter - 1;
						this.doDoubleJump;
					};
				};
			};
		}{ // button up
			if (isJumping) {
				isJumping = false;
				acceleration.y = 0;
				velocity.y = velocity.y * releaseDamp;              // release jump button for smaller jumps
			};
		};
	}

	// do a jump
	doJump{
		isJumping = true;
		if (wallGrabOn) {                                           // do a wall jump
			angle = angle + pi;                                     // flip direction
			acceleration.y = wallJumpAcc;                           // acceleration applied during a wall jump
			velocity += (Point.fromPolar(wallJumpSpeed,angle));     // velocity gained from jump
		}{
			acceleration.y = jumpAcceleration;                      // acceleration applied during a normal jump
		};
		jumpTimer = jumpPeriod;                                     // duration of jump
		World_Audio.play(doubleJumpSound, 0.2,0.01);
		3.do{|i| World_Rect_Particle(origin.copy.y_(boundingBox.bottom),11,6.rrand(10),Color(1,1,1,0.5),i-1,3,0.25) };
		// and do above when landing
	}

	// do a double jump
	doDoubleJump{
		isJumping = true;
		if (wallGrabOn) { angle = angle + pi };                     // if on a wall flip direction
		jumpTimer = jump2Period;                                    // duration of jump
		acceleration.y    = jump2Acceleration;                      // acceleration applied during this period
		velocity += (Point.fromPolar(jump2VelJump,angle));          // a dash like element
		World_Audio.play(jumpSound, 0.33, 0.02);
		3.do{|i| World_Circle_Particle(origin.copy.y_(boundingBox.bottom),11,6.rrand(10),Color(1,1,1,0.5),i-1,3,0.25) };
	}

	// wall grab input from controller
	fromController_wallGrab{|value|
		var cell = components[\collider   ].cell;
		if (value>0.75) {
			if (wallGrabOn.not) {
				var test = false;
				var rect = boundingBox.copy;
				rect.top = rect.top + (rect.height/4);
				rect.height = rect.height/2;
				rect.left = rect.left + (Point.fromPolar(2,angle).x);
				ugp.doTileCollisionsFunc(cell,rect, { test = true } );
				if (test) { this.startWallGrab };
			}
		}{
			this.stopWallGrab;
		};
	}

	// grad a wall
	startWallGrab{
		wallGrabOn  = true;
		isJumping = false;
		components[\drawFunc].wallGrabOn_(wallGrabOn);
		acceleration.replaceXY(0,0);
		gravity.replaceY(0);
		velocity.replace(wallSlide);
	}

	// let go of wall
	stopWallGrab{
		wallGrabOn = false;
		components[\drawFunc].wallGrabOn_(wallGrabOn);
		gravity.replace(defaultGravity);
		if (controllerState[id, 14].notNil) { acceleration.x = controllerState[id, 14].map(0,1,-1.6,1.6) / 2 };
	}

	// what happens when the entity hits the edge of the world?
	worldEdge{
		if (origin.x < 0          ) {
			origin.x       = origin.x.clip(0,worldRight );
		    velocity.x     = 0;
			acceleration.x = 0;
			World_Scene.worldEdgeResponse(parent.id, \left);
		};
		if (origin.x > worldRight ) {
			origin.x       = origin.x.clip(0,worldRight );
			velocity.x     = 0;
			acceleration.x = 0;
			World_Scene.worldEdgeResponse(parent.id, \right);
		};
		if (origin.y < 0          ) {
			origin.y = origin.y.clip(0,worldBottom);
			velocity.y = 0;
			World_Scene.worldEdgeResponse(parent.id, \top);
		};
		if (origin.y > worldBottom) {
			origin.y = origin.y.clip(0,worldBottom);
			velocity.y = 0;
			bottom = true;
			World_Scene.worldEdgeResponse(parent.id, \bottom);
		};
	}

	// what to do when hitting a triangular tile
	triangleTileResponse{|edge, collisionOnSlope, theta|
		if (collisionOnSlope) {
			if (edge==\bottom) {
				onSlope = true;
				slopeTheta = theta;
				doubleJumpCounter = doulbeJumps;
				if (inputBufferTimer>0) {
					this.doJump;
					inputBufferTimer = 0;
				}{
					coyoteTimer = coyoteTime;
				};
				velocity.y = velocity.y.clipNeg;
			}{
				velocity.y = velocity.y.clipNeg; // else edge is top
			};
		}{
			if (edge==\top) { velocity.y = velocity.y.clipNeg; };
			if (collisionOnSlope) { ^this};
			this.setSurface(edge);
			if (edge==\bottom) {
				doubleJumpCounter = doulbeJumps;
				if (inputBufferTimer>0) {
					this.doJump;
					inputBufferTimer = 0;
				}{
					coyoteTimer = coyoteTime;
				};
			};
		};
	}

	// what to do when hitting a rectanglar tile
	rectTileResponse{|edge|
		if (onSlope) {^this};
		this.setSurface(edge);
		if (edge==\bottom) {
			doubleJumpCounter = doulbeJumps;
			if (inputBufferTimer>0) {
				this.doJump;
				inputBufferTimer = 0;
			}{
				coyoteTimer = coyoteTime;
			};
		};
	}

	// what to do when hitting a line
	lineResponse{|edge, theta|
		if (edge==\bottom) {
			onSlope = true;
			slopeTheta = theta;
			doubleJumpCounter = doulbeJumps;
			if (inputBufferTimer>0) {
				this.doJump;
				inputBufferTimer = 0;
			}{
				coyoteTimer = coyoteTime;
			};
			velocity.y = velocity.y.clipNeg;
		}{
			velocity.y = velocity.y.clipNeg; // else edge is top
		};
	}

	// what to do when hitting a cirlce
	circleResponse{|edge|
		if (onSlope) {^this};
		this.setSurface(edge);
		if (edge==\bottom) {
			doubleJumpCounter = doulbeJumps;
			if (inputBufferTimer>0) {
				this.doJump;
				inputBufferTimer = 0;
			}{
				coyoteTimer = coyoteTime;
			};
		};
	}

}

World_Platform_Player_Collider : World_Circle_Collider_Base {

	var <>enemies;

	isPlayer { ^true }

	*new{|parent,radius,enemies| ^super.new.initComponent(parent).initUGP.initShape(radius).init(enemies) }

	init{|argEnemies|
		enemies = argEnemies;
		this.collisionResponder_(\tiles,      true);
		this.collisionResponder_(\solids,     true);
		this.collisionResponder_(\npcs,       true);
		this.collisionResponder_(\items,      true);
		this.collisionResponder_(\npcBullets, true);
	}

	// player has collided with something, what do we do now?
	onCollision{|collider|
		// World Tile or Solid response
		if ((collider.isTile) or: {collider.isSolid}) {
			// rect response
			if (collider.isRect) {
				var edge = this.rectResponse_SolidWallPlatformer(collider, components[\mechanics].onSlope, 1);
				 components[\mechanics].rectTileResponse(edge);
				^this
			};
			// triangle response
			if (collider.isTriangle) {
				var edge, onSlope, theta;
				# edge, onSlope, theta = this.triangleResponse_SolidWallPlatformer(collider);
				 components[\mechanics].triangleTileResponse(edge, onSlope, theta);
				^this;
			};
			// line response
			if (collider.isLine) {
				var edge, theta;
				# edge, theta = this.lineResponse_SolidWallPlatformer(collider,1);
				 components[\mechanics].lineResponse(edge,theta);
				^this
			};
			// circle response
			if (collider.isCircle) {
				var edge = this.circleResponse_SolidWallPlatformer(collider,0@(-2)); // £ using a point here to test an idea
				components[\mechanics].circleResponse(edge);
				^this
			}
		};
		// Item response
		if (collider.isItem) { collider.collected(this); ^this; };
		// if player hits enemies then destroy enemies + to do: take damage
		if ( enemies.includes(collider.parent.class) ) {
			collider.takeDamage(this);
			this.takeDamage(collider);
			^this
		};
	}

	takeDamage{|collider| components[\health].takeDamage(collider) }

}

World_Platform_Player_Health : World_Health {

	takeDamage{|collider|
		var damage = 0;
		case {collider.isBullet} {
			damage = collider[\damage].standard * components[\amour].standard;
			collider.parent.kill;
			if (invulnerable.not) {
				components[\hitTimer].reset;
				components[\death].takeDamage;
				World_Camera.shake(25,0.9);
				World_Audio.play(damageSound, 1, 0.02);
			}
		} {collider.isNPC} {
			damage = collider[\damage].standard * components[\amour].standard;
			if (invulnerable.not) {
				components[\hitTimer].reset;
				components[\death].takeDamage;
				World_Camera.shake(25,0.9);
				World_Audio.play(damageSound, 1, 0.02);
			};
		};
		if (invulnerable.not) { this.decHealth(damage) };
	}

}

World_Platform_Player_Death : World_Death {

	var <>fillColor, <>deathSound;

	*new{|parent, fillColor, deathSound| ^super.new.initComponent(parent).init(fillColor, deathSound) }

	init{|argFillColor, argDeathSound| fillColor = argFillColor; deathSound = argDeathSound }

	takeDamage{
		World_World.makeExplosion(parent.origin, 0.5,noSmall: 12, noLarge: 0, lifeTime: 0.66, smallColor: fillColor);
	}

	doDeath{
		World_Camera.shake(75,0.96);
		World_Camera.setToWhite;
		World_Camera.fadeToClear(1,6);
		World_World.makeExplosion(parent.origin, 1, nil, 3, 600, 200, type: \circle, largeColor: fillColor );
		World_Audio.play(deathSound);
		sceneScripts[\playerDeath].value(parent.id);
	}

}

World_Player_Hit_Timer : World_Clock {

	resetAction { components[\drawFunc].hitTimerOn_(true ) }

	tickAction  { World_Camera.angle_( this.frac * (0.05.rand2), 15) } // this needs to change to a heat map

	endAction   { components[\drawFunc].hitTimerOn_(false) }

}

World_Platform_Player_DrawFunc : World_DrawFunc {

	var <>image, <>grabImage, <>opacity=1.0, <>wallGrabOn = false, <>hitTimerOn = false;

 	*new{|parent,layer,image,grabImage| ^super.new.initComponent(parent).initDrawFunc(layer).init(image,grabImage) }

	init{|argImage,argGrabImage| image = images[argImage]; grabImage = images[argGrabImage]; }

	drawFunc{
		if (hitTimerOn) {
			Pen.fillColor_(white).addOval(boundingBox).draw(0)
		}{
			Pen.use{
				var x = origin.x;
				if (wallGrabOn) {
					Pen.rotate( (x * 0.041).wrap(0,2pi).round(halfpi), x, origin.y)
					.prDrawImage(tempPoint.leftTop(boundingBox), grabImage, nil, 0, opacity);
				}{
					Pen.rotate( (x * 0.041).wrap(0,2pi), x, origin.y)
					.prDrawImage(tempPoint.leftTop(boundingBox), image, nil, 0, opacity);
				};
			}
		}
	}

}
