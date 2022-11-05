////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Tails & Tethers                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Tether_Entity : World_Entity {

	*new{|object1, object2, layer|
		^super.new.initEntityWithRect( Rect.fromPoints(object1.origin,object2.origin)  ).init(object1, object2, layer)
	}

	init{|object1, object2, layer|
		components[\drawFunc  ] = World_Line_DrawFunc (this, layer, object1.origin, object2.origin, Color.white);
	}

}

World_Tether_Mechanics : World_Tether_Mechanics_Base {

	*new{|parent|
		^super.new.initComponent(parent).init()
	}

}


World_Tether_Mechanics_Base : World_Component {

	var <dynamic          = true; // can it move? used to switch movement on/off

	var <>style = 3,    <>tensionAB = 0.04,  <>tensionBA = 0.01,  <>minDistance = 100,  <>ratio = 0.25;
	var <objectA,       <objectB,            <power = 0,          <distance;
	var <velocityA,     <velocityB,          <>frictionA,         <>frictionB;
	var <accelerationA, <accelerationB;

	init{|argVar|
		argVar   = argVar;
		dynamics = dynamics.add(this);
	}

	free{ dynamics.remove(this) }

	// does the object move?
	dynamic_{|bool|
		dynamic = bool;
		if (dynamic) { dynamics = dynamics.add(this) } { dynamics.remove(this) };
	}

	tick{

	}
}

///////////////////////////////////

World_Tail_Entity : World_Entity {

	*new{|origin, layer, radius|
		^super.new.initEntityWithRadius(origin, radius).init(layer, radius)
	}

	init{|layer, radius|
		components[\collider  ] = World_Tail_Collider (this, radius);
		components[\drawFunc  ] = World_Circle_DrawFunc (this, layer, Color.red, Color(1,0.5,0.5)).stroke_(true);
		components[\mechanics ] = World_Classical_Mechanics (this, 0@0,10@10, 0@0.95,0@0.1);
	}

}

World_Tail_Collider : World_Circle_Collider_Base {

	*new{|parent, radius| ^super.new.initComponent(parent).initUGP.initShape(radius).init() }

	init{ this.collisionResponder_(\tiles, true) }

	onCollision{|collider|
		// World Tile response
		if (collider.isTile) {
			// rect response
			if (collider.isRect    ) { this.rectResponse_rigidBody(collider,1); ^this };
			// triangle response
			if (collider.isTriangle) { this.triangleResponse_rigidBodyBounce(collider); ^this };
			// line response
			// if (collider.isLine    ) { this.lineResponse_SolidWallPlatformer(collider,0); ^this; };
		}
	}

}

/*
	// the tail
	sceneState[\tail] = { World_Tail(spawn,7.5,6) } ! 20;
	sceneState[\tether] = sceneState[\tail].collect{|item,x|
	World_Tether((x==0).if(players[0],sceneState[\tail][x-1]),item).minDistance_((x==0).if(23,15))
	.tensionAB_(1).tensionBA_(0).style_(0).visible_(false)
	};

// update tail colors
		sceneState[\tail].reverseDo{|object,k|
			var r,b,w;
			if (k==(sceneState[\tail].size-1)) {
				if (players[0].state[\wallGrab].isTrue) { r = (lnxState[2]*2).clip(0,1) }{ r = (lnxState[0]*1.5).clip(0,1) };
				b = lnxState[2];
				w = (lnxState[1]+lnxState[3]*18)+5;
			}{
				var nextColor = sceneState[\tail][sceneState[\tail].size-k-2].fillColor;
				r = nextColor.red;
				b = nextColor.blue;
				w = sceneState[\tail][sceneState[\tail].size-k-2].radius;
			};
			object.fillColor.replace(r,0,b);
			object.strokeColor.replaceClip(r*1.5,0,b*1.5);
			object.radius_(w);
		};

*/

/*

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                         LINE (a stright line between 2 point with a width)                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// the base line shape

// TO REMOVE, replace with ECS

World_Line : World_Object {

	var <>p1, <>p2;

	isLine{^true}

	*superNew{^super.new} // to enable subclassing

	// new line
	*new{|p1, p2, layer|
		^super.new.initInstance(layer).initShape(p1,p2).initObject.initPost.initBBoxRect.initCollisions
	}

	initShape{|argP1, argP2|
		p1             = argP1.copy.asPoint; // we need a copy if we are doing inplace operations
		p2             = argP2.copy.asPoint;
		origin         = Point().average(p1,p2); // mid point of line
		size           = Point().absdif(p1,p2);
		radius         = size.rho;
		velocity       = Point(0,0);      // point is a vector where Point(0,0) is at rest
	}

	// the bounding box is a rectangle that encompasses the shape
	initBBoxRect{
		boundingBox = Rect.fromPoints(p1, p2);
		cell = origin.x.div(ugp_width) + (origin.y.div(ugp_height) * ugp_x);
		previousCell = cell;
	}

	// World_Line
	updateBBoxRect{
		boundingBox.fromPoints(p1, p2);
		this.updateUGPCell;
	}

}

// a drawn line

World_Line_PenDraw : World_Line {

	var <>penWidth          = 1;            // pen width
	var <>stroke            = true;         // stroke
	var <>strokeColor;                      // color
	var <>dash;                             // dash list

	// new line
	*new{|p1, p2, layer, strokeColor|
		^super.superNew
		.initInstance(layer).initShape(p1,p2,strokeColor).initObject.initPost.initBBoxRect.initCollisions
	}

	initShape{|argP1, argP2, argStrokeColor|
		p1             = argP1.copy.asPoint; // we need a copy if we are doing inplace operations
		p2             = argP2.copy.asPoint;
		origin         = Point().average(p1,p2); // mid point of line
		size           = Point().absdif(p1,p2);
		radius         = size.rho;
		strokeColor    = argStrokeColor ? white.copy;
		velocity       = Point(0,0);      // point is a vector where Point(0,0) is at rest
	}

	// draw method
	draw{
		if (stroke) {
			Pen.strokeColor = strokeColor;
			Pen.width = penWidth;
			if (dash.notNil) {
				Pen.use({
					Pen.lineDash_(dash).line(p1,p2).stroke;
				});
			}{
				Pen.line(p1,p2).stroke;
			};
		};
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                               TETHER                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// chain 2 entities together

// TO REMOVE, replace with ECS

World_Tether : World_Line_PenDraw {

	var <>style = 3,    <>tensionAB = 0.04,  <>tensionBA = 0.01,  <>minDistance = 100,  <>ratio = 0.25;
	var <objectA,       <objectB,            <power = 0,          <distance;
	var <velocityA,     <velocityB,          <>frictionA,         <>frictionB;
	var <accelerationA, <accelerationB;

	*new{|objectA,objectB,layer,strokeColor|
		^super.superNew.initInstance(layer)
		    .initShape(objectA.origin,objectB.origin,strokeColor)
	  	    .initObject(objectA,objectB)
		    .initPost.initBBoxRect
	}

	// for subclassing
	initObject{|argObjectA,argObjectB|
		dash    = FloatArray[3,3];
		objectA = argObjectA;
		objectB = argObjectB;
		velocityA = Point(0,0);
		velocityB = Point(0,0);
		frictionA = Point(0.5,0.5);
		frictionB = Point(0.5,0.5);
		accelerationA = Point(0,0);
		accelerationB = Point(0,0);
		distance = p1.distance(p2);
		penWidth = 3;
	}

	// replace standard movement for tethers
	// lots of in place operations so that no new objects get created
	coreTick{
		p1.replace(objectA.origin);  // update p1
		p2.replace(objectB.origin);  // update p2
		origin.average(p1,p2);       // origin is the mid-point
		size.absdif(p1,p2);          // size is size of rect
		distance = p1.distance(p2);  // the length of the line
		this.updateBBoxRect;         // update tether pos
		if (distance>minDistance) {
			power = distance - minDistance;
			switch (style,
				// directly apply tension to position
				0, {
					velocityA.fromPolar(power * tensionBA, p2.thetaFromPoint(p1));
					velocityB.fromPolar(power * tensionAB, p1.thetaFromPoint(p2));
					objectA.origin.addMul(velocityA, timeDilation);
					objectB.origin.addMul(velocityB, timeDilation)
			    // directly apply tension to velocity
				}, 1, {
					velocityA.fromPolar(power * tensionBA, p2.thetaFromPoint(p1));
					velocityB.fromPolar(power * tensionAB, p1.thetaFromPoint(p2));
					objectA.velocity.addMul(velocityA, timeDilation);
					objectB.velocity.addMul(velocityB, timeDilation);
				// indirectly apply tension to velocity (avoids rotation of the player but has a judgery stop)
				}, 2, {
					accelerationA.fromPolar(power * tensionBA, p2.thetaFromPoint(p1));
					accelerationB.fromPolar(power * tensionAB, p1.thetaFromPoint(p2));
					(velocityA.addMul(accelerationA, timeDilation)).mulPower(frictionA, timeDilation);
					(velocityB.addMul(accelerationB, timeDilation)).mulPower(frictionB, timeDilation);
					objectA.origin.addMul(velocityA, timeDilation);
					objectB.origin.addMul(velocityB, timeDilation);
				// a ratio of style 1 & style 2 (works best)
				}, 3, {
					accelerationA.fromPolar(power * tensionBA, p2.thetaFromPoint(p1));
					accelerationB.fromPolar(power * tensionAB, p1.thetaFromPoint(p2));
					objectA.velocity.addMul(accelerationA, timeDilation * ratio);
					objectB.velocity.addMul(accelerationB, timeDilation * ratio);
					(velocityA.addMul(accelerationA, timeDilation)).mulPower(frictionA, timeDilation);
					(velocityB.addMul(accelerationB, timeDilation)).mulPower(frictionB, timeDilation);
					objectA.origin.addMul(velocityA, timeDilation * (1 -ratio));
					objectB.origin.addMul(velocityB, timeDilation * (1 -ratio));
				}
			);
		}{ power = 0 };
		strokeColor.alpha_(power.map(0,200,0.3,1).clip(0,1));
		if (dash.notNil) { dash[0] = power.map(0.0,200,1,4).asFloat; dash[1] = power.map(0.0,200,1,4).asFloat};
	}

}

World_Tail : World_Circle_PenDraw {

	initObject{
		this.collisionResponder_(\tiles, true);
		maxVelocity = Point(10,10);
		gravity     = Point(0,0.1);
		friction    = Point(0,0.95);
	}

	onCollision{|object|
		if (object.isTile) {
			// rect response
			if (object.isRect    ) { this.rectResponse_SolidWallVelocityCoefficient(object,1); ^this };
			// triangle response
			if (object.isTriangle) { this.triangleResponse_SolidWallPlatformer(object); ^this };
			// line response
			if (object.isLine    ) { this.lineResponse_SolidWallPlatformer(object,0); ^this; };
		}
	}

}

*/
