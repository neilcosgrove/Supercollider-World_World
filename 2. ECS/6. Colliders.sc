////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 Collider Components                                                //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Circle_Collider : World_Circle_Collider_Base {

	var <>restitution;

	*new{|parent, radius, restitution = 1, solid = false, dynamic = false|
		^super.new.initComponent(parent).initUGP.initShape(radius).init(restitution, solid, dynamic)
	}

	init{|argRestitution, solid, dynamic|
		isSolid = solid;
		if (isSolid) {
			this.collisionSource_(\solids, true);
		};
		if (dynamic) {
			this.collisionResponder_(\tiles, true);
			this.collisionResponder_(\solids, true);
		};
		restitution = argRestitution;
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile ) {
			if (collider.isRect    ) { this.    rectResponse_rigidBodyBounce(collider,restitution); ^this };
			// its coming here at the moment !! £
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider,restitution); ^this }; // £
		};
		// World Tile or Solid response
		if (collider.isSolid) {
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider); ^this }; // this doesnt happen here yet
			if (collider.isRect    ) { this.rectResponse_rigidBodyBounce(collider); ^this };
			if (collider.isLine    ) { this.lineResponse_SolidWallPlatformer(collider,1);   ^this }; // to change
			if (collider.isCircle  ) {
				if (collider[\mechanics].notNil) {
					this.circleResponse_rigidBodyBounce(collider,1); ^this;
				}{
					this.circleResponse_fixedBodyBounce(collider,1); ^this;
				}; // TODO £
			};
		};
	}

}

World_Line_Collider : World_Line_Collider_Base {

	var <>restitution;

	*new{|parent,p1,p2,restitution = 1| ^super.new.initComponent(parent).initUGP.initShape(p1,p2).init(restitution) }

	init{|argRestitution|
		this.collisionSource_(\npcs, true);
		this.collisionResponder_(\tiles, true);
		restitution = argRestitution;
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile ) {
			//if (collider.isRect    ) { this.    rectResponse_rigidBodyBounce(collider,restitution); ^this };
			//if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider,restitution); ^this };
		};
	}

}

World_Rect_Collider : World_Rect_Collider_Base {

	var <>restitution;

	*new{|parent, restitution = 1, solid = false|
		^super.new.initComponent(parent).initUGP.init(restitution, solid) // initShape not needed here, shape is a rect
	}

	init{|argRestitution, solid|
		isSolid = solid;
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

World_Triangle_Collider : World_Triangle_Collider_Base {

	isTile { ^true }

	*new{|parent,corner| ^super.new.initComponent(parent).initUGP.initShape(corner).init }

	init{
		this.collisionSource_(\solids, true, #[\addToHead,\addToTail,\addToTail,\addToTail][corner] );
	}



} // TODO

// Bullet Collider /////////////////////////////////////////////////////////////////////////

World_Bullet_Collider :  World_Circle_Collider_Base {

	var <>restitution;

	isBullet { ^true }

	*new{|parent,radius, restitution=1| ^super.new.initComponent(parent).initUGP.initShape(radius).init(restitution) }

	init{|argRestitution|
		this.collisionResponder_(\tiles, true);
		this.collisionResponder_(\npcs, true);
		restitution = argRestitution;
	}

	onCollision{|collider|
		// World Tile response
		if (collider.isTile) {
			if (collider.isRect    ) { this.    rectResponse_rigidBodyBounce(collider,restitution); ^this };
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider,restitution); ^this };
		};
		// NPC
		if (collider.isNPC) {
			collider.takeDamage(this);
			this.parent.kill;
			^this;
		};
	}

}

// Tile Collider /////////////////////////////////////////////////////////////////////////

World_RectTile_Collider : World_Rect_Collider_Base {

	isTile { ^true }

	*new{|parent| ^super.new.initComponent(parent).initUGP.init }

	init{
		this.collisionSource_(\tiles, true);
	}

}

World_TriangleTile_Collider : World_Triangle_Collider_Base {

	isTile { ^true }

	*new{|parent,corner| ^super.new.initComponent(parent).initUGP.initShape(corner).init }

	init{
		this.collisionSource_(\tiles, true, #[\addToHead,\addToTail,\addToTail,\addToTail][corner] );
	}

}
