////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                        Items                                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////

// this is what the items can do...
World_1Point_Property      : World_Item_Property_Base { onCollection{ worldState[\score] = (worldState[\score] ? 0) + 1 } }
World_1UP_Property         : World_Item_Property_Base { onCollection{ worldState[\lives] = (worldState[\lives] ? 0) + 1 } }
World_Coin_Property        : World_Item_Property_Base { onCollection{ sceneState[\coins] = (sceneState[\coins] ? 0) + 1 } }
World_Star_Property        : World_Item_Property_Base { onCollection{ sceneState[\stars] = (sceneState[\stars] ? 0) + 1 } }
World_RateOfFire_Property  : World_Item_Property_Base { onCollection{|player| player[\weapon].incRateOfFire } }
World_ClipSize_Property    : World_Item_Property_Base { onCollection{|player| player[\weapon].incClipSize } }
World_ClipSizeMul_Property : World_Item_Property_Base { onCollection{|player| player[\weapon].mulClipSize } }
World_Health_Property      : World_Item_Property_Base { onCollection{|player| player[\health].incHealth(20) }
	canCollect {|player| ^player[\health].atMaxHealth.not }
}
World_Shield_Property      : World_Item_Property_Base { onCollection{|collider| collider.parent.invulnerable_(true, 15) } }
World_HasShield_Property   : World_Item_Property_Base {
	onCollection{|collider| worldState[\hasShield] = true  }
	canCollect {|player| ^worldState[\hasShield ] == false }
}

// Item Entity & Base Class ////////////////////////////////////////////////////////////////////////////////////////

World_Item : World_Item_Base {

	*new{|x, y, layer, image, sound, property, mode = \zoomOut|
		var radius = imageBounds[image].averageCord * 0.5;
		x = x + 0.5 * (tileMap.tileWidth);
		y = y + 1 * (tileMap.tileHeight) - radius;
		^super.new.initEntityWithRadius(x@y,radius).init(radius, layer, image, sound, property, mode)
	}

	*newXY{|x, y, layer, image, sound, property, mode = \zoomOut|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(x@y,radius).init(radius, layer, image, sound, property, mode)
	}

}

// this test is done in Tile Map >> if (object.superclasses.includes(World_Item_Base)) {

World_Item_Base : World_Entity {

	*isItem{ ^true }

	init{|radius, layer, image, sound, property, mode|
		components[\collider] = World_Item_Collider     (this, radius);
		components[\lifeSpan] = World_Collection_Clock  (this, 0.4, false);
		components[\property] = property.new            (this, sound);
		components[\drawFunc] = World_Item_DrawFunc     (this, layer, image).radius_(radius).mode_(mode);
	}

}

// shooter items //

World_Shooter_Item : World_Item {
	init{|radius, layer, image, sound, property, mode|
		components[\mechanics] = World_Shooter_Item_Mechanics  (this, 0@(4.rrand(6)));
		components[\collider]  = World_Item_Collider     (this, radius);
		components[\lifeSpan]  = World_Collection_Clock  (this, 0.4, false);
		components[\property]  = property.new            (this, sound);
		components[\drawFunc]  = World_Item_DrawFunc     (this, layer, image).radius_(radius).mode_(mode);
	}
}

World_Shooter_Item_Mechanics : World_Basic_Mechanics {
	worldEdge{
		if (origin.y > (worldBottom + 60)) { parent.delete };
	}
}

// moving items //

World_Moving_Item : World_Item_Base {

	*new{|x, y, layer, image, sound, property, mode = \zoomOut, speed = 5|
		var radius = imageBounds[image].averageCord * 0.5;
		x = x + 0.5 * (tileMap.tileWidth);
		y = y + 1 * (tileMap.tileHeight) - radius;
		^super.new.initEntityWithRadius(x@y,radius).init(radius, layer, image, sound, property, mode, speed)
	}

	*newXY{|x, y, layer, image, sound, property, mode = \zoomOut, speed = 5|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(x@y,radius).init(radius, layer, image, sound, property, mode, speed)
	}

	init{|radius, layer, image, sound, property, mode, speed|
		components[\mechanics] = World_Item_Mechanics      (this, Point.randRangePolar(speed * 0.5, speed * 1.0), 0.988@0.988);
		components[\collider ] = World_Moving_Item_Collider(this, radius);
		components[\lifeSpan ] = World_Collection_Clock    (this, 0.4, false);
		components[\property ] = property.new              (this, sound);
		components[\drawFunc ] = World_Item_DrawFunc       (this, layer, image).radius_(radius).mode_(mode);
	}

}

World_Item_Mechanics : World_Velocity_Friction_Mechanics {

	myTick{
		 // stop & free once moving below threshold
		if (velocity.rho < 0.5) {
			garbage = garbage.add(this);
			garbage = garbage.add(components[\collider]);
			components[\collider] = World_Item_Collider(parent, components[\collider].radius);
		};
	}

}

World_Moving_Item_Collider : World_Circle_Collider_Base {

	isItem{ ^true }

	*new{|parent, radius| ^super.new.initComponent(parent).initUGP.initShape(radius).init }

	init{
		this.collisionResponder_(\tiles, true);
		this.collisionSource_(\items, true);
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile ) {
			if (collider.isRect    ) { this.rectResponse_rigidBodyBounce(collider); ^this };
			if (collider.isTriangle) { this.triangleResponse_SolidWallPlatformer(collider); ^this };
		};
	}

	collected{|player| components[\property].doCollection(player) }

}

// Item Properties Component Base Class ////////////////////////////////////////////////////////////////////////////

World_Item_Property_Base : World_Component {

	var <collected = false, <sound;

	*new{|parent, sound| ^super.new.initComponent(parent).init(sound) }

	init{|argSound| sound = argSound }

	doCollection{|player|
		if(collected) { ^this };
		if (this.canCollect(player)) {
			collected = true;
			components[\collider].collisionSource_(\items, false);
			components[\drawFunc].layer_(10);
			components[\lifeSpan].start;
			this.onCollection(player);
			World_Audio.play(sound);
		};
	}

	canCollect  {|player| ^true} // for subclassing
	onCollection{|player|      } // for subclassing

}

// Collection Clock (does animation) //

World_Collection_Clock : World_Clock {

	tickAction{ components[\drawFunc].animate_( this.frac ) }

	endAction{ parent.kill }

}

// Item collider //

World_Item_Collider : World_Circle_Collider_Base {

	isItem{ ^true }

	*new{|parent, radius| ^super.new.initComponent(parent).initUGP.initShape(radius).init }

	init{ this.collisionSource_(\items, true) }

	collected{|player| components[\property].doCollection(player) }

}

// Item Draw Function //

World_Item_DrawFunc : World_Sprite_DrawFunc {

	var <>animate = 0, <>radius=1, <>mode = \zoomOut;

	drawFunc{
		if (lightMap.isOn and: {
			var cell = components[\collider].cell;
			((lightMap.staticLightMap[cell] + lightMap.dynamicLightMap[cell])==0) }) { ^this }; // don't draw if in darkness
		if (animate==0) {
			Pen.prDrawImage(tempPoint.leftTop(boundingBox), images[image], nil, 0, opacity);
		}{
			switch ( mode, \zoomOut, {
				Pen.use{
					var zoom   = 2 ** animate.map(0,1,3,0);
					var offset = radius.neg * (zoom-1);
					Pen.scale(zoom, zoom).prDrawImage(
						tempPoint.leftTop(boundingBox).addXY(offset,offset).scaleByValue(1/zoom),
						images[image], nil, 0, opacity * (animate**0.75) );
				}
			},
			\moveUp, {
				var offset = (animate**2).map(0,1,-100,0);
				Pen.prDrawImage(tempPoint.leftTop(boundingBox).addXY(0,offset),
					images[image], nil, 0, opacity * (animate**0.25) );
			});
		};
	}

}
