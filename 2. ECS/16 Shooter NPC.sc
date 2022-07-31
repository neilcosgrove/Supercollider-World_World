////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                            Shooter NPC                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Shooter2_NPC : World_Shooter_NPC {

	init{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, bulletSound, deathColor,bulletFreq|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin,radius);
		components[\mechanics ] = World_Shooter2_NPC_Mechanics(this, 0@0);
		components[\collider  ] = World_NPC_Collider        (this, radius);
		components[\health    ] = World_Shotter_NPC_Health  (this, 45, 45);
		components[\hitTimer  ] = World_Hit_Timer           (this, 0.15, false);
		components[\fireTimer ] = World_Fire_Type4_Timer    (this, bulletFreq, bulletFreq, true, true).bulletSound_(bulletSound);
		components[\death     ] = World_NPC2_Death          (this, Color(1,1,1), deathColor, \rect, deathSound, damageSound);
		components[\damage    ] = World_Damage_Profile      (this, 10);
		components[\amour     ] = World_Armour_Profile      (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_Blink_DrawFunc  (this,layer,image).hitImage_(imageHit).blinkImages_(blinkImages);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}
}

World_Fire_Type4_Timer : World_Random_Timer {

	var <>bulletSound;

	endAction {
		var origin = parent.origin;
		var angle  = worldTime;
		World_Ranged_NPC_Type3_Bullet(origin.copy, 11, 12, Point.fromPolar(5,angle), 4);
		World_Ranged_NPC_Type3_Bullet(origin.copy, 11, 12, Point.fromPolar(5,angle+pi), 4);
		World_Audio.play( bulletSound, rate: 0.8.rrand(1.2) );
	}

}

World_Shooter_NPC : World_Entity {

	*new{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, bulletSound, deathColor, bulletFreq = 1|
		^super.new.init(origin, layer, image, imageHit, deathSound, damageSound, blinkImages,bulletSound,deathColor, bulletFreq)
	}

	init{|origin, layer, image, imageHit, deathSound, damageSound, blinkImages, bulletSound, deathColor,bulletFreq|
		var radius = imageBounds[image].averageCord * 0.5; // we get the radius first
		this.initEntityWithRadius(origin,radius);
		components[\mechanics ] = World_Shooter_NPC_Mechanics(this, (1.0.rand)@0, 4@20, acceleration: 0@0);
		components[\collider  ] = World_NPC_Collider        (this, radius);
		components[\health    ] = World_Shotter_NPC_Health  (this, 6, 6);
		components[\hitTimer  ] = World_Hit_Timer           (this, 0.15, false);
		components[\fireTimer ] = World_Fire_Type3_Timer    (this, bulletFreq, bulletFreq * 2, false, true)
		                                                        .accuracy_(0.3).bulletSound_(bulletSound);
		components[\death     ] = World_NPC2_Death          (this, Color(1,1,1), deathColor, \rect, deathSound, damageSound);
		components[\damage    ] = World_Damage_Profile      (this, 10);
		components[\amour     ] = World_Armour_Profile      (this, 1, 1, 1, 1);
		components[\drawFunc  ] = World_NPC_Blink_DrawFunc  (this,layer,image).hitImage_(imageHit).blinkImages_(blinkImages);
		components[\mechanics ].activeDistance_(offScreenDistance + radius); // npcs become inactive off screen
	}

}

World_NPC2_Death : World_NPC_Death {

	doDeath{
		World_World.makeExplosion(parent.origin, 1, noSmall:25, noLarge:12, smallColor:fillColor ,largeColor:fillColor2,
			type:type, size:0.5);
		World_Camera.shake(10);
		World_Audio.play(deathSound);
		if (worldState[\score].notNil) { worldState[\score] = worldState[\score] + ((worldState[\wave] ? 0) * 10) + 10 };
	}

}

World_Shotter_NPC_Health : World_Health {

	takeDamage{|collider|
		var damage = 0;
		case {collider.isBullet} {
			damage = collider[\damage].standard * components[\amour].standard;
			components[\hitTimer ].reset;
			// components[\mechanics].velocity.scaleByValue(0.8); //  slow it down
			components[\death    ].takeDamage( Polar(4,collider.components[\mechanics].velocity.angle).asPoint );
		} {collider.isPlayer} {
			components[\hitTimer ].reset;
			damage = collider[\damage].standard * components[\amour].standard * 0.01;
		};
		this.decHealth(damage);
	}

}

World_Fire_Type3_Timer : World_Random_Timer {

	var <>accuracy = 0; // a radom angle in radians, zero means all shots are dead on & pi is a completly rand direction
	var <>bulletSound;

	endAction {
		var origin        = parent.origin;
		var playerOrigin  = players[0].origin;
		var angleToPlayer = playerOrigin.thetaFromPoint(origin);
		World_Ranged_NPC_Type3_Bullet(origin.copy, 11, 12, Point.fromPolar(5,angleToPlayer+(accuracy.rand2)), 4);
		if (bulletSound.notNil) { World_Audio.play( bulletSound, rate: 0.8.rrand(1.2) ) };
	}

}

World_Ranged_NPC_Type3_Bullet : World_Entity {

	*new{|origin, layer, radius, velocity, lifeSpan=2|
		^super.new.initEntityWithRadius(origin,radius).init(layer,radius,velocity,lifeSpan)
	}

	init{|layer, radius, velocity, lifeSpan|
		components[\mechanics ] = World_Shooter_Bullet_Mechanics  (this, velocity);
		components[\collider  ] = World_NPC_Bullet_Collider   (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer        (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile        (this, 5);
		components[\drawFunc  ] = World_Wobbly_Circle_DrawFunc(this, layer, Color(1,0.5,0.25), Color.red).penWidth_(5)
		.freq1_(0).freq2_(0).freq3_(16);

	}

}

World_Shooter2_NPC_Mechanics : World_Shooter_NPC_Mechanics {

	myTick{}

}

World_Shooter_NPC_Mechanics : World_Ranged_NPC_Mechanics {

	myTick{
		var playerOrigin     = players[0].origin;
		var distanceToPlayer = origin.distance(playerOrigin);
		var angleToPlayer    = playerOrigin.thetaFromPoint(origin);
		// add after rand time change direction
		if (players[0].isAlive) {
			// if the player is alive hover a distance away (between 200 - 550)

			if (distanceToPlayer>200) {
				// move towards
				tempVector.fromPolar(0.25, angleToPlayer).y_(0);

				velocity.addMul(tempVector,timeDilation);
			};

			// fire a bullet, a timer is used for this
			if (distanceToPlayer<1000) {
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

	// what happens when the entity hits the edge of the world? (for subclassing)
	worldEdge{
		if (origin.x < 0          ) { origin.x = origin.x.fold(0,worldRight ); velocity.x = velocity.x.neg };
		if (origin.x > worldRight ) { origin.x = origin.x.fold(0,worldRight ); velocity.x = velocity.x.neg };
//		if (origin.y < 0          ) { origin.y = origin.y.fold(0,worldBottom); velocity.y = velocity.y.neg };
		if (origin.y > (worldBottom + 60)) { parent.delete };
	}

}

