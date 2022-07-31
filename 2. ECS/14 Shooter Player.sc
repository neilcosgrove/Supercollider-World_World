////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                           Shooter Player                                           //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// Vector ship designed and coded by mjsyts
// https://github.com/mjsyts/spacegame/blob/main/spacegame

World_Shooter_Player : World_Player_Base {

	*new{|id, origin, layer, radius, enemies, bulletImage, deathSound, damageSound, fireSound, shieldImage|
		^super.new.initEntityWithRadius(origin, radius).initPlayer(id)
		.init(layer, radius, enemies, bulletImage, deathSound, damageSound, fireSound, shieldImage)
	}

	init{|layer, radius, enemies, bulletImage, deathSound, damageSound, fireSound, shieldImage|
		components[\mechanics  ] = World_Shooter_Mechanics    (this, 0@0, 20@20, 0.8@0.8, 0@0.25, 0@0)
		                                                           .angle_(pi*1.5).exhaustOffset_(-50).radius_(radius);
		components[\controller ] = World_Shooter_Controller   (this).acceleration_(3.5);
		components[\collider   ] = World_TDP_Collider         (this, radius-5, enemies);
		components[\health     ] = World_TDP_Health           (this, 1, 1, damageSound);
		components[\death      ] = World_Shooter_Death        (this, Color(0.737, 0.71, 0.937, 0.7), deathSound);
		components[\weapon     ] = World_Shooter_Weapon       (this, 30, 30, 0.1, 2, fireSound, bulletImage);
		components[\damage     ] = World_Damage_Profile       (this, 100);
		components[\amour      ] = World_Armour_Profile       (this, 1, 1, 1, 1);
		components[\hitTimer   ] = World_Player_Hit_Timer     (this, 0.15, false);
		components[\invulTimer ] = World_Invulnerability_Timer(this, 5, false);
		components[\drawFunc   ] = World_Vector_Ship_DrawFunc (this, layer, radius, shieldImage);
	}

}

World_Shooter_Controller : World_Controller_Base {

	var <>acceleration=1;

	retrigger{ ^[14, 15, 7, 1] } // L,R,U,D + fire get retriggered

	// ps4 controller in
	controllerIn{|device, index, value|
		// left joy L&R to acceleration X
		if (index== 14) { components[\mechanics].acceleration.x = value.map(0,1,-1.0,1.0) * acceleration};
		// left joy U&D to acceleration Y
		if (index== 15) {components[\mechanics].acceleration.y = value.map(0,1,1.0,-1.0) .clip(-inf,0.2)* acceleration};
		// R1 to fire
	    if (index==7) { if (value==1) { components[\weapon].start } { components[\weapon].stop } };
		// x to fire
		if (index==1) { if (value==1) { components[\weapon].start } { components[\weapon].stop } };
		// l1 shield
		if (index==2) {
			if ((worldState[\hasShield]==true) && (parent.invulnerable.not))  {
				parent.invulnerable_(true,15);
				worldState[\hasShield] = false;
				World_Audio.play(\bubble); // TODO pass in
			};
		};
	}

}

World_Shooter_Mechanics : World_Classical_Mechanics {

	var <>exhaustOffset = 0, <>radius = 40;

	myTick{
		angle = velocity.x.map(-40,40, pi, pi * 2).mix(1.5 * pi, 0.2 * timeDilation );
		velocity.clipToZero; // stop moving below a threshold
		gravity.y = (1 - (parent.origin.y / worldHeight)) * 5;
		if (lightMap.isOn) { lightMap.addDynamicLightSource(origin.x, origin.y, 2, 11, 0.7) };
		// smoke
		if ((0.3 * timeDilation * (velocity.rho + 4 / 8)).coin) {
				World_Circle_Particle(origin.copy += tempPoint.fromPolar(exhaustOffset, angle)
					,1,6.rrand(10),Color(1,1,1,0.25),1.0.rand2,1.0.rand2 + 5 ,0.25);
		};
	}

	// what happens when the entity hits the edge of the world?
	worldEdge{
		if (origin.x < radius               ) { origin.x = origin.x.clip(radius,  worldRight  - radius ) };
		if (origin.x > (worldRight - radius)) { origin.x = origin.x.clip(radius,  worldRight  - radius ) };
		if (origin.y < 0                    ) { origin.y = origin.y.clip(0,       worldBottom - radius ) };
		if (origin.y > (worldBottom - (radius * 2))) { origin.y = origin.y.clip(0,worldBottom - (radius * 2) ) };
	}

}

World_Shooter_Death : World_TDP_Death {

	doDeath{
		World_Exploding_Ship(parent.origin,10, components[\drawFunc   ].radius);
		World_Camera.shake(40,0.92);
		World_Camera.setToWhite;
		World_Camera.fadeToClear(0.66,6);
		World_World.makeExplosion(parent.origin, 1, nil, 2.5, 100, 0, fillColor, 3);
		World_World.makeExplosionType2(parent.origin, 1, nil, 2.5, 10, 0, Color.cyan(0.7), 3);
		World_Audio.play(deathSound, rate: [4/3, 3/2, 5/3].choose );
		sceneScripts[\playerDeath].value(parent.id);
	}

}

//////////////////////////////
// Shooter Weapon & Bullets //
//////////////////////////////

World_Shooter_Weapon : World_Weapon_Base {

	var <>image, <>maxRate;

	*new{|parent, clipCount=50, clipSize=50, fireRate=0.2, damage = 2, fireSound, image|
		^super.new.initComponent(parent).init(clipCount, clipSize, fireRate, damage, fireSound, image)
	}

	init{|argClipCount, argClipSize, argFireRate, argDamage, argFireSound, argImage|
		clipCount = argClipCount;
		clipSize  = argClipSize;
		fireRate  = argFireRate;
		maxRate   = fireRate;
		damage    = argDamage;
		fireSound = argFireSound;
		image     = argImage;
	}

	start{
		fireTask.stop;
		isFiring = true;
		fireRate = maxRate;
		fireTask = {|me|inf.do{|i|
			if (parent.isNil) { nil.wait };
			if (isPlaying) {
				var origin   = parent.origin;
				var angle    = components[\mechanics].angle;
				var velocity = components[\mechanics].velocity;
				i.asInteger.odd.if {
					World_Shooter_Bullet(origin.copy.addXY(-43,0), 10, image,
						Polar(12,angle).asPoint, 2, damage, fillColor, strokeColor);
				}{
					World_Shooter_Bullet(origin.copy.addXY( 30,0), 10, image,
						Polar(12,angle).asPoint, 2, damage, fillColor, strokeColor);
				};
				World_Audio.play(fireSound, 0.8, rate: (i/8).wrap(0.6,1.4));
				clipCount = clipCount - 1;
				fireRate  = (fireRate * 0.96).clip(2/60,inf);
				if (clipCount <= 0) {
					isFiring = false;
					nil.wait;
				};
			};
			fireRate.wait;
		} }.forkInScene;
	}

	incRateOfFire{ fireRate = fireRate * 0.95; maxRate = maxRate * 0.95 }

}

World_Shooter_Bullet : World_Entity {

	*new{|origin, layer, image, velocity, lifeSpan=2, damage=2, fillColor, strokeColor, penWidth=2|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(origin,radius)
		.init(layer, image, radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth)
	}

	init{|layer, image, radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth|
		components[\mechanics ] = World_Shooter_Bullet_Mechanics (this, velocity);
		components[\collider  ] = World_Bullet_Collider          (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer           (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile           (this, damage);
		components[\drawFunc  ] = World_Shooter_Bullet_DrawFunc  (this, layer, image);
	}

}

World_Shooter_Bullet_DrawFunc : World_DrawFunc {

	var <>image, <>opacity=1.0, <>phase = 0, <>freq = 10;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

 	init{|argImage|
		image = argImage;
	}

	drawFunc{
 		Pen.use{
			Pen.rotate( components[\mechanics ].velocity.angle - halfpi, origin.x, origin.y).prDrawImage(
				tempPoint.leftTop( boundingBox ), images[image], nil, 0, opacity
			);
 		};
 	}

}

World_Shooter_Bullet_Mechanics : World_Bullet_Mechanics {

	// what happens when the entity hits the edge of the world? (for subclassing)
	worldEdge{
		if (origin.x < 0          ) { parent.kill };
		if (origin.x > worldRight ) { parent.kill };
		if (origin.y < 0          ) { parent.kill };
		if (origin.y > worldBottom) { parent.kill };
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 Vector Space Ship                                                  //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// Vector ship designed and coded by mjsyts
// https://github.com/mjsyts/spacegame/blob/main/spacegame

World_Vector_Ship_DrawFunc : World_DrawFunc {

	var <>velocityToThrusterScale = 0.125, <>hitTimerOn = false, <>radius = 60, <>invulAlpha = 1;
	var <>shieldImage;

 	*new{|parent, layer, radius, shieldImage| ^super.new.initComponent(parent).initDrawFunc(layer).init(radius,shieldImage) }

	init{|argRadius, argShieldImage|
		radius      = argRadius;
		shieldImage = argShieldImage;
	}

	drawFunc {
		var playerX        = origin.x;
		var playerY        = origin.y;
		var spriteSize     = radius * 2;
		var playerRect     = Rect(playerX, playerY, spriteSize, spriteSize);
		var cannonWidth    = spriteSize/12;
		var cannonLength   = 0.6*spriteSize;
		var cockpitH       = spriteSize/3, cockpitW = spriteSize/7;
		var numLines       = 4;
		var propulsionSize = rrand(15.0,20.0);
		var delta          = rrand(0.1, 0.3)*spriteSize/6;
		var height         = exprand(0.4,(1/4))*spriteSize;
		var alpha          = (((controllerState[0] ? () ) [15] ? 0) * 10 + components[\mechanics].velocity.x.abs
			                 * velocityToThrusterScale).clip(0.5,1) * (1.0.rand**0.25);
		var tip            = radius+rand2(delta/2)@(spriteSize+(height * 2 * alpha));
		var angle          = components[\mechanics].angle + halfpi;
		var flashAlpha     = components[\weapon].isFiring.if(components[\weapon].frac * 0.75 + 0.25 ,0);
		// draw
		Pen.use {
			Pen.rotate( angle, playerX,  playerY);
			Pen.translate(playerX - radius, playerY - radius);
			Pen.use({
				//propulsion
				Pen.moveTo((radius)-(spriteSize/15) @ (spriteSize))
				.quadCurveTo((radius)+(spriteSize/15) @ (spriteSize), (tip.x) @ (tip.y*sqrtHalfPi))
				.lineTo((radius)-(spriteSize/15) @ (spriteSize))
				.fillAxialGradient((radius) @ (spriteSize), (spriteSize/2) @ (tip.y),
					Color.cyan(1,alpha * invulAlpha), Color.cyan(1.0,0));
				Pen.addOval(Rect.aboutPoint(radius @ (spriteSize), spriteSize/propulsionSize, spriteSize/propulsionSize))
				.color_(Color.cyan(1,alpha * invulAlpha)).fill;
			});
			2.do{ arg i;
				Pen.moveTo(radius + [delta.neg, delta].at(i) @ spriteSize)
				.quadCurveTo(tip, radius + [delta.neg, delta].at(i) @ (spriteSize+delta.rand))
				.lineTo(radius + [delta, delta.neg].at(i)@spriteSize)
				.fillAxialGradient(tip, radius @ (spriteSize-delta),
					Color.blue(1,alpha * invulAlpha), Color.cyan(1.5,alpha * invulAlpha))
			};
			Pen.addOval(Rect.aboutPoint(radius@spriteSize, delta, delta))
			.fillAxialGradient(tip, radius @ (spriteSize-delta), Color.cyan(1,alpha * invulAlpha),
				Color.grey(1,alpha * invulAlpha));
			Pen.use({
				//cannons
				2.do({ arg i;
					Pen.line(((i*spriteSize)+[(cannonWidth/2),(cannonWidth/2).neg].at(i))  @ spriteSize,
						((i*spriteSize)+[(cannonWidth/2),(cannonWidth/2).neg].at(i)) @ (cannonLength))
					.strokeColor_(Color.grey(0.5, * invulAlpha))
					.width_(cannonWidth)
					.stroke
				});
				//wings
				Pen.use({
					2.do({ arg i;
						var ratio = (5/8);
						Pen.moveTo(spriteSize/2 @ (0.875*spriteSize))
						.lineTo(i*spriteSize @ spriteSize)
						.lineTo(i*spriteSize @ (15*spriteSize/16))
						.quadCurveTo(spriteSize/2 @ (spriteSize/4.neg), spriteSize/2 @ (spriteSize*ratio))
						.fillColor_(Color.grey(0.3,invulAlpha))
						.fill
					});
					//decor
					numLines.do({ arg i;
						Pen.line(spriteSize/numLines*(i+0.5) @ spriteSize, spriteSize/numLines*(i+0.5) @ (7*spriteSize/10))
						.strokeColor_(Color.grey(0.5, invulAlpha))
						.width_(spriteSize/18)
						.stroke
					});
				});
				//cannon flash
				2.do({ arg i;
					var flashSize = exprand(spriteSize/12, spriteSize/24);
					Pen.addOval( Rect.aboutPoint(((i*spriteSize)+[(cannonWidth/2),
						(cannonWidth/2).neg].at(i))@((0.6*spriteSize)-(flashSize)), flashSize, flashSize*1.5) )
					.color_(Color.cyan(1.8, flashAlpha * invulAlpha))
					.fillAxialGradient(((i*spriteSize)+[(cannonWidth/2),(cannonWidth/2).neg].at(i))@(cannonLength),
						((i*spriteSize)+[(cannonWidth/2), (cannonWidth/2).neg].at(i))@(cannonLength-(flashSize*2)),
						Color.white.alpha_(flashAlpha * invulAlpha), Color.cyan(1,0.5*flashAlpha * invulAlpha));
				});
				Pen.use({
					var jetRect = Rect.aboutPoint(spriteSize/2 @ (0.9*spriteSize),spriteSize/15, spriteSize/8);
					//body
					2.do({ arg i;
						Pen.moveTo((spriteSize/2) @ 0)
						.quadCurveTo(spriteSize/2 @ (spriteSize), spriteSize/4+(i*spriteSize/2) @ (spriteSize/2))
						.width_(spriteSize/36)
						.fillAxialGradient(spriteSize/2 @ 0, spriteSize/2 @ spriteSize, Color.grey(0.6, invulAlpha),
							Color.grey(0.3,   invulAlpha))
					});
					//jet
					Pen.addRoundedRect(jetRect, jetRect.width/2, jetRect.height/4)
					.fillAxialGradient(jetRect.left@(jetRect.height/2), jetRect.right@(jetRect.height/2),
						Color.grey(0.7, invulAlpha), Color.grey(0.5, invulAlpha));
					//cockpit
					Pen.addOval(Rect(spriteSize-cockpitW/2, (0.4*spriteSize), cockpitW, cockpitH))
					.fillAxialGradient(spriteSize-cockpitW/2 @ (4*spriteSize/10),
						spriteSize-(cockpitW/2+cockpitW) @ ((4*spriteSize/10)+cockpitH), Color.grey(1, invulAlpha),
						Color.cyan(0.5,  invulAlpha))
					.strokeColor_(Color.grey(0.7, invulAlpha))
					.width_(2)
					.stroke;
				});
			});

		};
		// shield
		if (invulAlpha < 1) {
			var point = tempPoint.replace(origin).subXY(imageBounds[shieldImage].width/2,imageBounds[shieldImage].height/2);
			var frac = components[\invulTimer].frac;
			Pen.use{
				Pen.rotate(worldTime * 10, origin.x, origin.y);
				Pen.prDrawImage(point, images[shieldImage], nil, 0, (frac*4).clip(0,1));
			};
		};
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                            Exploding Vector Space Ship                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// Vector ship designed and coded by mjsyts
// https://github.com/mjsyts/spacegame/blob/main/spacegame

World_Exploding_Ship : World_Entity {

	*new{|origin, layer, radius|
		^super.new.initEntityWithRadius(origin, radius).init(layer, radius)
	}

	init{|layer, radius|
		components[\lifeSpan ] = World_LifeSpan_Timer(this, 2.5);
		components[\drawFunc ] = World_Vector_Ship_Exploding_DrawFunc(this, layer, radius);
	}

}

World_Vector_Ship_Exploding_DrawFunc : World_Vector_Ship_DrawFunc {

	var offSet, velocity, freq, time = 0;

	init{|argRadius|
		radius   = argRadius;
		offSet   = {Point.rand(0,0)}!11;
		velocity = {|i| Point(1.0.rrand(2.5)*(i.odd.if(-1,1)), 1.0.rrand(3)) }!11;
		freq     = {1.0.rand2}!11;
	}

	drawFunc {
		var playerX        = origin.x;
		var playerY        = origin.y;
		var spriteSize     = radius * 2;
		var playerRect     = Rect(playerX, playerY, spriteSize, spriteSize);
		var cannonWidth    = spriteSize/12;
		var cannonLength   = 0.6*spriteSize;
		var cockpitH       = spriteSize/3, cockpitW = spriteSize/7;
		var numLines       = 4;
		var delta          = rrand(0.1, 0.3)*spriteSize/6;
		var height         = exprand(0.4,(1/4))*spriteSize;
		var alpha          = 1;
		var tip            = radius+rand2(delta/2)@(spriteSize+(height * 2 * alpha));
		var angle          = 0;

		offSet.do{|point,i| point += velocity[i] };
		time = time + frameLength;

		// draw
		Pen.use {
			Pen.rotate( angle, playerX,  playerY);
			Pen.translate(playerX - radius, playerY - radius);
			Pen.use({
				//cannons
				2.do({ arg i;
					Pen.use {
						Pen.transPoint(offSet[i]).rotate(time * freq[i], 0, 0);
						Pen.line(((i*spriteSize)+[(cannonWidth/2),(cannonWidth/2).neg].at(i))  @ spriteSize,
							((i*spriteSize)+[(cannonWidth/2),(cannonWidth/2).neg].at(i)) @ (cannonLength))
						.strokeColor_(Color.grey)
						.width_(cannonWidth)
						.stroke
					}
				});
				//wings
				Pen.use({
					2.do({ arg i;
						var ratio = (5/8);
						Pen.use {
							Pen.transPoint(offSet[i+2]).rotate(time * freq[i+2], 0, 0);
							Pen.moveTo(spriteSize/2 @ (0.875*spriteSize))
							.lineTo(i*spriteSize @ spriteSize)
							.lineTo(i*spriteSize @ (15*spriteSize/16))
							.quadCurveTo(spriteSize/2 @ (spriteSize/4.neg), spriteSize/2 @ (spriteSize*ratio))
							.fillColor_(Color.grey(0.3))
							.fill
						}
					});
					//decor
					numLines.do({ arg i;
						Pen.use {
							Pen.transPoint(offSet[i+4]).rotate(time * freq[i+4], 0, 0);
							Pen.line(spriteSize/numLines*(i+0.5) @ spriteSize, spriteSize/numLines*(i+0.5) @ (7*spriteSize/10))
							.strokeColor_(Color.grey)
							.width_(spriteSize/18)
							.stroke
						}
					});
				});
				Pen.use({
					//body
					2.do({ arg i;
						Pen.use {
							Pen.transPoint(offSet[i+8]).rotate(time * freq[i+8], 0, 0);
							Pen.moveTo((spriteSize/2) @ 0)
							.quadCurveTo(spriteSize/2 @ (spriteSize), spriteSize/4+(i*spriteSize/2) @ (spriteSize/2))
							.width_(spriteSize/36)
							.fillAxialGradient(spriteSize/2 @ 0, spriteSize/2 @ spriteSize, Color.grey(0.6), Color.grey(0.3))
						}
					});
					//cockpit
					Pen.use {
						Pen.transPoint(offSet[10]).rotate(time * freq[10], 0, 0);
						Pen.addOval(Rect(spriteSize-cockpitW/2, (0.4*spriteSize), cockpitW, cockpitH))
						.fillAxialGradient(spriteSize-cockpitW/2 @ (4*spriteSize/10),
							spriteSize-(cockpitW/2+cockpitW) @ ((4*spriteSize/10)+cockpitH), Color.white, Color.cyan(0.5))
						.strokeColor_(Color.grey(0.7))
						.width_(2)
						.stroke;
					}
				});
			});

		};
	}

}
