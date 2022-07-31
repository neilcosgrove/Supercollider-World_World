////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                              NPC                                                   //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_NPC_Base : World_Entity {
	*isNPC { ^true }
}

World_Kamikaze_NPC : World_NPC_Base {

	*new{|origin, layer, image, imageHit, deathSound, damageSound, deathColor|
		^super.new.init(origin, layer, image, imageHit, deathSound, damageSound, deathColor)
	}

	init{|origin, layer, image, imageHit, deathSound, damageSound, deathColor|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin, radius);
		components[\mechanics ] = World_Kamikaze_NPC_Mechanics(this, Point.rand * 4 - 2, 2@2, acceleration: 0@0);
		components[\collider  ] = World_NPC_Collider          (this, radius);
		components[\health    ] = World_NPC_Health            (this, 5, 5);
		components[\hitTimer  ] = World_Hit_Timer             (this, 0.15, false);
		components[\death     ] = World_NPC_Death             (this, Color(1,1,1), deathColor, \circle,deathSound,damageSound);
		components[\damage    ] = World_Damage_Profile        (this, 10);
		components[\amour     ] = World_Armour_Profile        (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_DrawFunc          (this, layer, image).hitImage_(imageHit);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}

}

World_Kamikaze_Type2_NPC : World_NPC_Base {

	*new{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, shadowImage, deathColor|
		^super.new.init(origin, layer, image, imageHit, deathSound, damageSound, blinkImages, shadowImage, deathColor)
	}

	init{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, shadowImage, deathColor|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin,radius);
		components[\mechanics ] = World_Kamikaze_NPC_Mechanics(this, Point.rand * 4 - 2, 2@2, acceleration: 0@0);
		components[\collider  ] = World_NPC_Collider          (this, radius);
		components[\health    ] = World_NPC_Health            (this, 5, 5);
		components[\hitTimer  ] = World_Hit_Timer             (this, 0.15, false);
		components[\death     ] = World_NPC_Death             (this, Color(1,1,1), deathColor, \circle,deathSound,damageSound);
		components[\damage    ] = World_Damage_Profile        (this, 10);
		components[\amour     ] = World_Armour_Profile        (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_Blink_DrawFunc    (this,layer,image).hitImage_(imageHit).blinkImages_(blinkImages);
        components[\drawFunc2 ] = World_NPC_Shadow_DrawFunc   (this, 0,shadowImage);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}

}

World_Ranged_NPC : World_NPC_Base {

	*new{|origin, layer, image, imageHit, deathSound, damageSound, bulletSound, deathColor|
		^super.new.init(origin, layer, image, imageHit, deathSound, damageSound, bulletSound, deathColor)
	}

	init{|origin, layer, image, imageHit, deathSound, damageSound, bulletSound, deathColor|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin,radius);
		components[\mechanics ] = World_Ranged_NPC_Mechanics(this, Point.rand * 4 - 2, 5@5, acceleration: 0@0);
		components[\collider  ] = World_NPC_Collider        (this, radius);
		components[\health    ] = World_NPC_Health          (this, 10, 10);
		components[\hitTimer  ] = World_Hit_Timer           (this, 0.15, false);
		components[\fireTimer ] = World_Fire_Timer          (this, 2, 4, false, true).accuracy_(0.15).bulletSound_(bulletSound);
		components[\death     ] = World_NPC_Death           (this, Color(1,1,1), deathColor, \circle,deathSound,damageSound);
		components[\damage    ] = World_Damage_Profile      (this, 10);
		components[\amour     ] = World_Armour_Profile      (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_DrawFunc        (this, layer, image).hitImage_(imageHit);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}

}

World_Ranged_Type2_NPC : World_NPC_Base {

	*new{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, shadowImage, bulletSound, deathColor|
		^super.new.init(origin, layer, image, imageHit, deathSound, damageSound, blinkImages,shadowImage,bulletSound,deathColor)
	}

	init{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, shadowImage, bulletSound, deathColor|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin,radius);
		components[\mechanics ] = World_Ranged_NPC_Mechanics(this, Point.rand * 4 - 2, 5@5, acceleration: 0@0);
		components[\collider  ] = World_NPC_Collider        (this, radius);
		components[\health    ] = World_NPC_Health          (this, 10, 10);
		components[\hitTimer  ] = World_Hit_Timer           (this, 0.15, false);
		components[\fireTimer ] = World_Fire_Type2_Timer    (this, 2, 4, false, true).accuracy_(0.15).bulletSound_(bulletSound);
		components[\death     ] = World_NPC_Death           (this, Color(1,1,1), deathColor, \circle,deathSound,damageSound);
		components[\damage    ] = World_Damage_Profile      (this, 10);
		components[\amour     ] = World_Armour_Profile      (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_Blink_DrawFunc  (this,layer,image).hitImage_(imageHit).blinkImages_(blinkImages);
        components[\drawFunc2 ] = World_NPC_Shadow_DrawFunc (this,0,shadowImage);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}

}

// NPC Mechanics ///////////////////////////////////////////////////////////////////////////////////////

World_Kamikaze_NPC_Mechanics : World_Classical_Mechanics {

	var lastAngle=0;

	// AI for the NPC
	myTick{
		var playerOrigin     = players[0].origin;
		var distanceToPlayer = origin.distance(playerOrigin);
		var angleToPlayer    = playerOrigin.thetaFromPoint(origin);
		if (players[0].isAlive) {
			// if the player is alive move towards them
			if (distanceToPlayer<400) {
				tempVector.fromPolar(((400-distanceToPlayer).clip(0,5)*0.022), angleToPlayer);
				velocity.addMul(tempVector,timeDilation).clipVelocityValue(5);
				tempVector.fromPolar(((400-distanceToPlayer/150).clip(0,15)), angleToPlayer);
				origin.addMul(tempVector, timeDilation); // this might be a problem with npc vs npc
			};
		}{
			// if player dead
			if (distanceToPlayer<500) {
				tempVector.fromPolar(((500-distanceToPlayer).clip(0,5)*0.0045), angleToPlayer);
				velocity.addMul(tempVector,timeDilation).clipVelocityValue(5);
			};
		};
		// lag angle of sprite to velocity
		angle = (velocity.x*0.2).clip(-1,1).mix(lastAngle,0.95**timeDilation); // point in direction of travel
		lastAngle = angle;
	}

}

World_Ranged_NPC_Mechanics : World_Classical_Mechanics {

	var lastAngle=0, <inRangeOfPlayer = false;

	// Classical Mechanics, can be overridden if needed
	// all operations are in place to avoid any new objects, which increases performance
	// for example... += -= *= /= addMul mulPower wrapInPlace clipInPlace aboutPoint clipVelocity are all in place operations
	tick{
		var previouslyActive = active;
		if (activeDistance.notNil) {
			distanceToCamera = origin.distance(cameraPos) * zoom; // distance from object to camera
			active = distanceToCamera < activeDistance;           // only active when in range of camera
		};
		if (active) {
			if (angularVelocity.notNil) { angle = angle + (angularVelocity * timeDilation)}; // rotate
			if (   acceleration.notNil) { velocity.addMul  (acceleration,    timeDilation)}; // apply acceleration (controller)
		    if (       friction.notNil) { velocity.mulPower(friction,        timeDilation)}; // apply friction
			if (        gravity.notNil) { velocity.addMul  (gravity,         timeDilation)}; // apply gravity
			if (    maxVelocity.notNil) { velocity.clipVelocity(maxVelocity)};               // clip velocity to maximum
			this.myTick;                                                                     // do the subclass tick
			origin.addMul(velocity, timeDilation);                                           // apply velocity
			this.worldEdge;                                                                  // apply world edge response
			parent.updateBoundingBox;                                                        // update boundingBox
		}{
			if (previouslyActive && inRangeOfPlayer) {
				inRangeOfPlayer = false;
				components[\fireTimer].stop; // this fixes a bug when offscreen + zoomed in and the timer isn't turned off
			};
		}
	}

	// AI for the NPC
	myTick{
		var playerOrigin     = players[0].origin;
		var distanceToPlayer = origin.distance(playerOrigin);
		var angleToPlayer    = playerOrigin.thetaFromPoint(origin);
		// add after rand time change direction
		if (players[0].isAlive) {
			// if the player is alive hover a distance away (between 200 - 550)
			if (distanceToPlayer<200) {
				// move away
				tempVector.fromPolar(((200-distanceToPlayer/200).clip(0,15)).neg, angleToPlayer);
				velocity.addMul(tempVector,timeDilation).clipVelocityValue(2);
			}{
				if ((distanceToPlayer>400)&&(distanceToPlayer<550)) {
					// move towards
					tempVector.fromPolar(((distanceToPlayer - 400 / 400).clip(0,10)), angleToPlayer);
					velocity.addMul(tempVector,timeDilation).clipVelocityValue(2);
				}
			};
			// fire a bullet, a timer is used for this
			if (distanceToPlayer<550) {
				if (inRangeOfPlayer.not) {
					inRangeOfPlayer = true;
					if (components[\fireTimer].notNil) { components[\fireTimer].start };
				};
			}{
				// stop firing
				if (inRangeOfPlayer) {
					inRangeOfPlayer = false;
					components[\fireTimer].stop;
				};
			};
		}{
			// if player dead
			if (distanceToPlayer<500) {
				tempVector.fromPolar(((500-distanceToPlayer).clip(0,5)*0.0045), angleToPlayer);
				velocity.addMul(tempVector,timeDilation).clipVelocityValue(3);
			};
			// stop firing
			if (inRangeOfPlayer) {
				inRangeOfPlayer = false;
				components[\fireTimer].stop;
			};
		};
		// lag angle of sprite to velocity
		angle = (velocity.x*0.2).clip(-1,1).mix(lastAngle,0.95**timeDilation); // point in direction of travel
		lastAngle = angle;
	}

}

// NPC Collider ///////////////////////////////////////////////////////////////////////////////////////

World_NPC_Collider : World_Circle_Collider_Base {

	var <>restitution;

	isNPC { ^true }

	*new{|parent,radius,restitution = 1| ^super.new.initComponent(parent).initUGP.initShape(radius).init(restitution) }

	init{|argRestitution|
		this.collisionResponder_(\tiles, true);

		this.collisionSource_(\npcs, true);
		this.collisionResponder_(\npcs, true);

		restitution = argRestitution;
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile ) {
			if (collider.isRect    ) { this.rectResponse_rigidBodyBounce(collider,restitution); ^this };
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider); ^this };
		};

		if (collider.isNPC ) {
			// kinda works but npcs can push other npcs through walls
			// prob need them to reverse direction
			if (collider.isCircle  ) { this.circleResponse_rigidBodyBounce(collider); ^this };
			// try go back in direction of velocity?
		}
	}

	takeDamage{|collider| components[\health].takeDamage(collider) }

}

// NPC Health Components ///////////////////////////////////////////////////////////////////////////////////////

World_NPC_Health : World_Health {

	takeDamage{|collider|
		var damage = 0;
		case {collider.isBullet} {
			damage = collider[\damage].standard  * components[\amour].standard;
			components[\hitTimer ].reset;
			components[\mechanics].velocity.scaleByValue(0.8); //  slow it down
			components[\death    ].takeDamage( Polar(4,collider.components[\mechanics].velocity.angle).asPoint );
		} {collider.isPlayer} {
			damage = collider[\damage].standard  * components[\amour].standard;
		};
		this.decHealth(damage);
	}

}

// NPC Death Components ///////////////////////////////////////////////////////////////////////////////////////

World_NPC_Death : World_Death {

	var <>fillColor, <>fillColor2, <>type, <>deathSound, <>damageSound;

	*new{|parent, fillColor, fillColor2, type=\square, deathSound, damageSound|
		^super.new.initComponent(parent).init(fillColor, fillColor2, type, deathSound, damageSound)
	}

	init{|argFillColor, argFillColor2, argType, argDeathSound, argDamageSound |
		fillColor   = argFillColor;
		fillColor2  = argFillColor2;
		type        = argType;
		deathSound  = argDeathSound;
		damageSound = argDamageSound;
	}

	doDeath{
		World_World.makeExplosion(parent.origin, 1, noSmall:40, noLarge:20, smallColor:fillColor ,largeColor:fillColor2,
			type:type, size:1);
		World_Camera.shake(10);
		lightMap.addDynamicLightSource(parent.origin.x, parent.origin.y, 5, 12);
		World_Audio.play(deathSound);
	}

	takeDamage{|velocityBias|
		World_World.makeExplosion(parent.origin, 0.25,
			velocityBias:velocityBias.scaleByValue(0.25),noSmall: 6, noLarge: 0, lifeTime: 0.66, smallColor: fillColor);
		World_Audio.play(damageSound);
	}

}

// NPC Draw Function ///////////////////////////////////////////////////////////////////////////////////////

World_NPC_DrawFunc : World_Sprite_DrawFunc {

	var <>hitTimerOn = false, <>hitImage;

	drawFunc{
		if (lightMap.isOn and: {
			var cell = components[\collider].cell;
			((lightMap.staticLightMap[cell] + lightMap.dynamicLightMap[cell])==0) }) { ^this }; // don't draw if in darkness
 		Pen.use{
			if (hitTimerOn) {
				var x = (worldTime*60).heatMap*8-4;
				var y = (worldTime*60+1000).heatMap*8-4;
				Pen.rotate( components[\mechanics].angle, origin.x, origin.y).prDrawImage(
					tempPoint.leftTop( boundingBox ).addXY(x,y), images[hitImage], nil, 0, opacity
				);
			}{
				Pen.rotate( components[\mechanics].angle, origin.x, origin.y).prDrawImage(
					tempPoint.leftTop( boundingBox ), images[image], nil, 0, opacity
				);
			};
 		};
 	}

}

World_NPC_Blink_DrawFunc : World_Sprite_DrawFunc {

	var <>hitTimerOn = false, <>hitImage;
	var <>blinkImages,       <>blinkIndex=0,      <>blinkTime=0;
	var <>openMinTime = 0.5,  <>openMaxTime = 3.0, <>closeMinTime = 0.1, <>closeMaxTime = 0.25;

	drawFunc{
 		Pen.use{
			if (hitTimerOn) {
				var x = (worldTime*60).heatMap*8-4;
				var y = (worldTime*60+1000).heatMap*8-4;
				Pen.rotate( components[\mechanics].angle, origin.x, origin.y).prDrawImage(
					tempPoint.leftTop( boundingBox ).addXY(x,y), images[hitImage], nil, 0, opacity
				);
			}{
				blinkTime = blinkTime - frameLength;
				if (blinkTime<0) {
					if (blinkIndex.isNil) {
						blinkIndex = 65536.rand.wrap(0,blinkImages.size - 1);
						blinkTime = closeMinTime.rrand(closeMaxTime);
					}{
						blinkIndex = nil;
						blinkTime = openMinTime.rrand(openMaxTime);
					};
				};
				if (blinkIndex.isNil) {
					Pen.rotate( components[\mechanics].angle, origin.x, origin.y).prDrawImage(
						tempPoint.leftTop( boundingBox ), images[image], nil, 0, opacity
					);
				}{
					Pen.rotate( components[\mechanics].angle, origin.x, origin.y).prDrawImage(
						tempPoint.leftTop( boundingBox ), images[blinkImages[blinkIndex]], nil, 0, opacity
					);
				};
			};
 		};
 	}
}

World_NPC_Shadow_DrawFunc : World_Sprite_DrawFunc {

	var <>dx = 25, <>dy = 25;

	drawFunc{
		Pen.use{
			Pen.rotate( components[\mechanics].angle, origin.x + dx, origin.y + dy)
			    .prDrawImage( tempPoint.leftTop(boundingBox).addXY(dx,dy) , images[image], nil, 0, opacity);
		};
	}

}

// fire bullets timer ///////////////////////////////////////////////////////////////////////////////////////

// i'm using a random timer here to fire a bullet towards the player rather than using a weapon

World_Fire_Timer : World_Random_Timer {

	var <>accuracy = 0; // a radom angle in radians, zero means all shots are dead on & pi is a completly rand direction
	var <>bulletSound;

	endAction {
		var origin        = parent.origin;
		var playerOrigin  = players[0].origin;
		var angleToPlayer = playerOrigin.thetaFromPoint(origin);
		World_Ranged_NPC_Bullet(origin.copy, 11, 10, Point.fromPolar(4,angleToPlayer+(accuracy.rand2)), 4);
		if (bulletSound.notNil) { World_Audio.play( bulletSound, rate: 0.8.rrand(1.2) ) };
	}

}

World_Fire_Type2_Timer : World_Random_Timer {

	var <>accuracy = 0; // a radom angle in radians, zero means all shots are dead on & pi is a completly rand direction
	var <>bulletSound;

	endAction {
		var origin        = parent.origin;
		var playerOrigin  = players[0].origin;
		var angleToPlayer = playerOrigin.thetaFromPoint(origin);
		{
			(1.rrand(3)).do{
				World_Ranged_NPC_Type2_Bullet(origin.copy, 11, 12, Point.fromPolar(5,angleToPlayer+(accuracy.rand2)), 4);
				if (bulletSound.notNil) { World_Audio.play( bulletSound, rate: 0.8.rrand(1.2) ) };
				0.2.wait;
			};
		}.forkInScene;
	}

}

// NPC Bullet Entity //////////////////////////////////////////////////////////////////////////////

World_Ranged_NPC_Bullet : World_Entity {

	*new{|origin, layer, radius, velocity, lifeSpan=2|
		^super.new.initEntityWithRadius(origin,radius).init(layer,radius,velocity,lifeSpan)
	}

	init{|layer,radius,velocity,lifeSpan|
		components[\mechanics ] = World_Bullet_Mechanics      (this, velocity);
		components[\collider  ] = World_NPC_Bullet_Collider   (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer        (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile        (this, 5);
		components[\timer     ] = World_Fill_Color_Alpha_Clock(this, 0.2, true, true);
		components[\drawFunc  ] = World_Circle_DrawFunc       (this, layer, Color.red, Color.black);
	}

}

World_Ranged_NPC_Type2_Bullet : World_Entity {

	*new{|origin, layer, radius, velocity, lifeSpan=2|
		^super.new.initEntityWithRadius(origin,radius).init(layer,radius,velocity,lifeSpan)
	}

	init{|layer, radius, velocity, lifeSpan|
		components[\mechanics ] = World_Bullet_Mechanics      (this, velocity);
		components[\collider  ] = World_NPC_Bullet_Collider   (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer        (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile        (this, 5);
		components[\drawFunc  ] = World_Wobbly_Circle_DrawFunc(this, layer, Color.red, Color.white)
		.startTime_(worldTime);
	}

}

World_Fill_Color_Alpha_Clock : World_Clock {

	tickAction { components[\drawFunc].fillColor.alpha_(this.frac) }

}

World_NPC_Bullet_Collider :  World_Circle_Collider_Base {

	isBullet { ^true }

	*new{|parent,radius| ^super.new.initComponent(parent).initUGP.initShape(radius).init }

	init{
		this.collisionResponder_(\tiles, true);
		this.collisionSource_(\npcBullets, true);
	}

	onCollision{|collider|
		if (collider.isTile ) { parent.kill; ^this }; // World Tile response
	}

}
