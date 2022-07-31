////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                  Weapon Components                                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////

// idea: a little bullet travels, then explodes into an expanding circle

World_TDP_Weapon : World_Weapon_Base {

	var <>image;

	*new{|parent, clipCount=20, clipSize=20, fireRate=0.2, damage = 2, fireSound, image|
		^super.new.initComponent(parent).init(clipCount, clipSize, fireRate, damage, fireSound, image)
	}

	init{|argClipCount, argClipSize, argFireRate, argDamage, argFireSound, argImage|
		clipCount = argClipCount;
		clipSize  = argClipSize;
		fireRate  = argFireRate;
		damage    = argDamage;
		fireSound = argFireSound;
		image     = argImage;
	}

	start{
		fireTask.stop;
		isFiring = true;
		fireTask = {|me| inf.do{|i|
			if (parent.isNil) { nil.wait };
			if (isPlaying) {
				var origin   = parent.origin;
				var angle    = components[\mechanics].angle;
				var velocity = components[\mechanics].velocity;
				World_Sprite_Bullet(origin.copy, 6, image, Polar(15,angle).asPoint, 2, damage,
					Color.magenta, Color(1,0.7,1), 3
				);
				World_Audio.play(\tom,(this.frac+0.5*0.66**2),0.02);
				clipCount = clipCount - 1;
				if (clipCount <= 0) {
					isFiring = false;
					nil.wait
				};
			};
			fireRate.wait;
		} }.forkInScene;
	}

}

World_Platformer_Weapon : World_Weapon_Base {

	*new{|parent, clipCount=20, clipSize=20, fireRate=0.2, damage = 2, fireSound|
		^super.new.initComponent(parent).init(clipCount, clipSize, fireRate, damage, fireSound)
	}

}

World_Weapon_Base : World_Component {

	var <>clipCount, <>clipSize, <>fireRate, fireTask, <>damage, <>fireSound, <>fillColor, <>strokeColor;
	var <isFiring = false;

	init{|argClipCount, argClipSize, argFireRate, argDamage, argFireSound|
		clipCount = argClipCount;
		clipSize  = argClipSize;
		fireRate  = argFireRate;
		damage    = argDamage;
		fireSound = argFireSound;
		fillColor = Color.magenta;
		strokeColor = Color.black;
	}

	free{ fireTask.stop; fireTask = nil }

	start{
		fireTask.stop;
		isFiring = true;
		fireTask = {|me| inf.do{|i|
			if (parent.isNil) { nil.wait }; // temp bug fix: intermittent bug
			if (isPlaying) {
				var origin   = parent.origin;
				var angle    = components[\mechanics].angle;
				var velocity = components[\mechanics].velocity;
				// var adj      = i.fold(0,8) / 16 * pi;
				// if (angle==0) { adj = adj.neg };
				World_Vector_Bullet(origin.copy, 6, 10, Polar(9,angle).asPoint, 2, damage, fillColor, strokeColor);
				//  Polar(7.5,angle + adj).asPoint.addMul(velocity, 0.75)
				World_Audio.play(fireSound,(this.frac+0.5*0.66**2),0.02);
				clipCount = clipCount - 1;
				if (clipCount <= 0) {
					isFiring = false;
					nil.wait;
				}; // temp bug fix: me.stop catches a rare bug
			};
			fireRate.wait;
		} }.forkInScene;
	}

	stop{
		fireTask.stop;
		isFiring = false;
		this.reload;
	}

	frac{ ^clipCount / clipSize }

	noEmpty{ ^ clipSize - clipCount }

	reload{ clipCount = clipSize }

	incRateOfFire{ fireRate = fireRate * 0.866 }

	incClipSize{ clipSize = clipSize + 2; this.reload }

	mulClipSize{ clipSize = (clipSize * 1.1).asInteger }

}

// My Bullet Entity //////////////////////////////////////////////////////////////////////////////

World_Vector_Bullet : World_Entity {

	*new{|origin, layer, radius, velocity, lifeSpan=2, damage=2, fillColor, strokeColor, penWidth=2|
		^super.new.initEntityWithRadius(origin,radius).init(layer,radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth)
	}

	init{|layer,radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth|
		components[\mechanics ] = World_Bullet_Mechanics (this, velocity);
		components[\collider  ] = World_Bullet_Collider  (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer   (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile   (this, damage);
		components[\drawFunc  ] = World_Circle_DrawFunc  (this, layer, fillColor, strokeColor).penWidth_(penWidth);
	}

}

World_Sprite_Bullet : World_Entity {

	*new{|origin, layer, image, velocity, lifeSpan=2, damage=2, fillColor, strokeColor, penWidth=2|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(origin,radius)
		.init(layer, image, radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth)
	}

	init{|layer, image, radius,velocity,lifeSpan,damage,fillColor,strokeColor,penWidth|
		components[\mechanics ] = World_Bullet_Mechanics         (this, velocity);
		components[\collider  ] = World_Bullet_Collider          (this, radius);
		components[\lifeSpan  ] = World_LifeSpan_Timer           (this, lifeSpan);
		components[\damage    ] = World_Damage_Profile           (this, damage);
		components[\drawFunc  ] = World_Rotating_Sprite_DrawFunc (this, layer, image);
	}

}
