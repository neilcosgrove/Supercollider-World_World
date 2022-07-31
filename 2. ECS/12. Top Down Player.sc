////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                   Top Down Player (TDP)                                            //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Top_Down_Player : World_Player_Base {

	*new{|id, origin, layer, enemies, image, image2, imageHit, imageShadow, imageBullet, deathSound, damageSound, fireSound|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(origin, radius).initPlayer(id)
		.init(layer, radius, enemies, image, image2, imageHit, imageShadow, imageBullet, deathSound, damageSound, fireSound)
	}

	init{|layer, radius, enemies, image, image2, imageHit, imageShadow, imageBullet, deathSound, damageSound, fireSound|
		components[\mechanics  ] = World_TDP_Mechanics      (this, 0@0, 20@20, 0.8@0.8, 0@(0.25*0), 0@0).angle_(pi*1.5);
		components[\controller ] = World_TDP_Controller     (this).acceleration_(2);
		components[\collider   ] = World_TDP_Collider       (this, radius-5, enemies);
		components[\health     ] = World_TDP_Health         (this, 1, 1, damageSound);
		components[\death      ] = World_TDP_Death          (this, Color(0.737, 0.71, 0.937, 0.7), deathSound);
		components[\weapon     ] = World_TDP_Weapon         (this, 5, 5, 0.1, 2, fireSound, imageBullet);
		components[\damage     ] = World_Damage_Profile     (this, 100);
		components[\amour      ] = World_Armour_Profile     (this, 1, 1, 1, 1);
		components[\hitTimer   ] = World_Player_Hit_Timer   (this, 0.15, false);
		components[\drawFunc   ] = World_TDP_DrawFunc       (this, layer, image, image2, imageHit);
		components[\drawFunc2  ] = World_TDP_Shadow_DrawFunc(this, 0, imageShadow);
	}

}

// Top Down Player Controller //

World_TDP_Controller : World_Controller_Base {

	var <>acceleration=1;

	retrigger{ ^[14, 15, 7] } // L,R,U,D + fire get retriggered

	// ps4 controller in
	controllerIn{|device, index, value|
		// left joy L&R to acceleration X
		if (index== 14) { components[\mechanics].acceleration.x = value.map(0,1,-1.0,1.0) * acceleration};
		// left joy U&D to acceleration Y
		if (index== 15) { components[\mechanics].acceleration.y = value.map(0,1,1.0,-1.0) * acceleration};
		// R1 to fire
	    if (index==7) { if (value==1) { components[\weapon].start } { components[\weapon].stop } };
	}

}

// Top Down Player Mechanics //

World_TDP_Mechanics : World_Classical_Mechanics {

	var <>exhaustOffset = 0;

	myTick{
		if (velocity.isMoving)  { angle = velocity.theta };
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

// Top Down Player Collider //

World_TDP_Collider : World_Circle_Collider_Base {

	var <>enemies;

	isPlayer { ^true }

	*new{|parent, radius, enemies| ^super.new.initComponent(parent).initUGP.initShape(radius).init(enemies) }

	init{|argEnemies|
		enemies = argEnemies;
		this.collisionResponder_(\tiles,      true);
		this.collisionResponder_(\solids,     true);
		this.collisionResponder_(\npcs,       true);
		this.collisionResponder_(\items,      true);
		this.collisionResponder_(\npcBullets, true);
	}

	onCollision{|collider|
		// if player hits enemies then destroy enemies + to do: take damage
		if ( enemies.includes(collider.parent.class) ) {
			collider.takeDamage(this);
			this.takeDamage(collider);
			^this
		};
		// World Tile or Solid response
		if ((collider.isTile) or: {collider.isSolid}) {
			if (collider.isRect    ) { this.rectResponse_rigidBody(collider);               ^this };
			if (collider.isTriangle) { this.triangleResponse_SolidWallPlatformer(collider); ^this };
			if (collider.isLine    ) { this.lineResponse_SolidWallPlatformer(collider,1);   ^this };
			if (collider.isCircle  ) { this.circleResponse_SolidWallPlatformer(collider,1); ^this };
		};
		// Item response
		if (collider.isItem) { collider.collected(this); ^this; };
	}

	takeDamage{|collider| components[\health].takeDamage(collider) }

}

// Top Down Player Health //

World_TDP_Health : World_Health {

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
			};
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

// Top Down Player Death //

World_TDP_Death : World_Death {

	var <>fillColor,  <>deathSound;

	*new{|parent, fillColor, deathSound| ^super.new.initComponent(parent).init(fillColor, deathSound) }

	init{|argFillColor, argDeathSound| fillColor=argFillColor; deathSound = argDeathSound }

	doDeath{
		World_Camera.shake(150,0.92);
		World_Camera.setToWhite;
		World_Camera.fadeToClear(1,6);
		World_World.makeExplosionType2(parent.origin, 1, nil, 2.5, 30, 800, fillColor, 3);
		World_Audio.play(deathSound, rate: [4/3, 3/2, 5/3].choose );
		sceneScripts[\playerDeath].value(parent.id);
	}

	takeDamage{
		World_World.makeExplosion(parent.origin, noSmall: 0, noLarge: 6, lifeTime: 0.66, smallColor: fillColor);
	}

}

// Top Down Player Draw Functions //

World_TDP_DrawFunc : World_DrawFunc {

	var <>image, <>image2, <>imageHit, <>velocityToThrusterScale = 7.99, <>hitTimerOn = false;

 	*new{|parent, layer, image, image2, imageHit|
		^super.new.initComponent(parent).initDrawFunc(layer).init(image, image2, imageHit)
	}

	init{|argImage, argImage2, argImageHit|
		image    = images[argImage];
		image2   = images[argImage2];
		imageHit = images[argImageHit];
	}

	drawFunc{
		var angle = components[\mechanics].angle + halfpi;
		var alpha = (components[\mechanics].velocity.rho * velocityToThrusterScale).clip(0,1) * (1.0.rand**0.25);
		if (hitTimerOn) {
			Pen.use{
				Pen.rotate( angle, origin.x,  origin.y).prDrawImage(tempPoint.leftTop(boundingBox), imageHit, nil, 0, 1.0);
			};
		}{
			Pen.use{
				Pen.rotate( angle, origin.x,  origin.y).prDrawImage(tempPoint.leftTop(boundingBox), image, nil, 0, 1.0);
				if (alpha>0){ Pen.prDrawImage(tempPoint.leftTop(boundingBox), image2, nil, 0, alpha) };
			};
		};
	}

}

World_TDP_Shadow_DrawFunc : World_DrawFunc {

	var <>image, <>dx = 25, <>dy = 25, <>opacity = 1;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

	init{|argImage| image = images[argImage] }

	drawFunc{
		var angle = components[\mechanics].angle + halfpi;
		Pen.use{
			Pen.rotate(angle, origin.x + dx, origin.y + dy)
			    .prDrawImage(tempPoint.leftTop(boundingBox).addXY(dx,dy), image, nil, 0, opacity)
		};
	}

}
