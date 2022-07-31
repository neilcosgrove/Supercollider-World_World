////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 Collider Bases                                                     //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// super class of all Colliders ( i couldn't resist the name )
// this is probably the most maths heavy part of the engine

World_Super_Collider : World_Component {

	var <collisionSource = false, <collisionResponder = false, <collisionSourceTypes, <collisionResponderTypes;
	var <origin, <boundingBox, <cell, <previousCell, <>isSolid = false, <previousBoundingBox;

	isCircle   { ^false }
	isRect     { ^false }
	isLine     { ^false }
	isTriangle { ^false }
	isPlayer   { ^false }
	isTile     { ^false }
	isNPC      { ^false }
	isItem     { ^false }
	isBullet   { ^false }

	active     { ^parent.active }

	// set up Uniform Grid Partition vars
	initUGP{
		collisionSourceTypes    = IdentitySet[];
		collisionResponderTypes = IdentitySet[];
		origin                  = parent.origin;
		boundingBox             = parent.boundingBox;
		previousBoundingBox     = boundingBox.copy;
		cell                    = ugp.getCell(origin);
		previousCell            = cell;
	}

	// update Uniform Grid Partition vars
	updateUGPCell{
		cell = ugp.getCell(origin);
		// update ugp
		if (collisionSource) {
			if (cell != previousCell) {
				ugp.moveCell(this, collisionSourceTypes, previousCell, cell, boundingBox, previousBoundingBox);
				previousCell = cell;
				previousBoundingBox.replace(boundingBox);
			};
		};
	}

	// add/remove this collider as a collision Source of type
	collisionSource_{|type, bool = true, addAction = \addToTail|
		if (bool) {
			collisionSourceTypes = collisionSourceTypes.add(type);
			collisionSource = true;
			ugp.addRectToCells(this, boundingBox, type, addAction);
		}{
			// remove
			collisionSourceTypes.remove(type);
			ugp.removeFromAllCells(this, type);
			if (collisionSourceTypes.isEmpty) { collisionSource = false};
		};
	}

	// add/remove this collider as a collision responder of type
	collisionResponder_{|type, bool = true|
		if (bool) {
			// add
			collisionResponderTypes = collisionResponderTypes.add(type);
			collisionResponder      = true;
			ugp.addResponder(this);
		}{
			// remove
			collisionResponderTypes.remove(type);
			if (collisionResponderTypes.isEmpty) {
				collisionResponder = false;
				ugp.removeResponder(this);
			};
		};
	}

	// free me
	free{
		if (collisionSource   ) { ugp.freeTypesFromCell(this, collisionSourceTypes, previousBoundingBox) };
		if (collisionResponder) { ugp.removeResponder(this) };
	}

	// take radius into account when testing for active distance
	//activeDistanceIncludingRadius_{|dist| activeDistance = dist + radius }

	initShape{}                          // for subclassing
	testForCollision{|collider| ^false } // for subclassing
	onCollision{|collider| }             // for subclassing
	takeDamage{|collider| }              // for subclassing

}

// Line Collider - superclass for all Line Colliders ///////////////////////////////////////////////////////////

World_Line_Collider_Base : World_Super_Collider {

	var <>p1, <>p2;

	isLine { ^true }

	initShape{|argP1, argP2|
		p1 = argP1.copy.asPoint; // we need a copy if we are doing inplace operations
		p2 = argP2.copy.asPoint;
	}

	// collision detection for Lines
	testForCollision{|collider|
		// simple bBox test, only used in UGP so far (temp for now)
		var bBox = collider.boundingBox;
		if ((boundingBox.bottom) <= (bBox.top   ) ) {^false};
		if ((boundingBox.right ) <= (bBox.left  ) ) {^false};
		if ((boundingBox.top   ) >= (bBox.bottom) ) {^false};
		if ((boundingBox.left  ) >= (bBox.right ) ) {^false};
		^true
	}

}

// Circle Collider - superclass for all Circle Colliders ///////////////////////////////////////////////////////////

World_Circle_Collider_Base : World_Super_Collider {

	var <>radius, <>mass = 1;

	isCircle {^true}

	initShape{|argRadius| radius = argRadius.abs }

	// collision detection for Circles
	testForCollision{|collider|
		// stop self detection
		if (collider===this) {^false}; // this only happens if a responder is also a source of the same type (i.e NPC vs NPC)
		// circle vs circle
		if (collider.isCircle) {
			^(hypot( (origin.x) - (collider.origin.x), (origin.y) - (collider.origin.y) ) - radius - (collider.radius) < 0)
		};
		// cirlce vs rectangle ( rect is boundary aligned )
		if (collider.isRect) {
			var cx = origin.x;
			var cy = origin.y;
			var rx = collider.boundingBox.left;
			var ry = collider.boundingBox.top;
			var rw = collider.boundingBox.width;
			var rh = collider.boundingBox.height;
			var testX = cx.clip(rx, rx+rw);
			var testY = cy.clip(ry, ry+rh);
			// distance from closest edges vs radius
			if ((cx-testX).hypot(cy-testY) < radius) { ^true } { ^false };
		};
		// cirlce vs triangle ( a boundary aligned right angled triangle )
		if (collider.isTriangle) {
			var x      = origin.x;
			var y      = origin.y;
			var p1     = collider.p1;     // p1 is always the right angled corner
			var p2     = collider.p2;     // p2 & p3 are always the hypotenuse, points go clockwise
			var p3     = collider.p3;
			var corner = collider.corner; // #[\bottomLeft, \bottomRight, \topRight, \topLeft ];
			var bBox   = collider.boundingBox;
			// 1. check bounding box vs origin including radius
			if (x + radius < bBox.left  ) {^false};
			if (x - radius > bBox.right ) {^false};
			if (y + radius < bBox.top   ) {^false};
			if (y - radius > bBox.bottom) {^false};
			// 2. check if lines intersects circle (circle_vs_line4 assumes bBox checking already done, as above)
			if (origin.circle_vs_line4(radius,p2,p3,tempPoint)) { ^true }; // vs hypot
			if (origin.circle_vs_line4(radius,p1,p2,tempPoint)) { ^true }; // can i do these faster, they are aligned?
			if (origin.circle_vs_line4(radius,p3,p1,tempPoint)) { ^true }; // can i do these faster, they are aligned?
			// 3. check bounding box vs just origin (just orgin inside triangle remains and this covers small areas outside)
			if (x < bBox.left  ) {^false};
			if (x > bBox.right ) {^false};
			if (y < bBox.top   ) {^false};
			if (y > bBox.bottom) {^false};
			// 4. test origin vs pos on hypot (most stuff is picked up by this point, below only happens if velocity is high)
			if (corner < 2) {
				if (y > x.yCordOnLine(p2,p3)) {^true}; // bottomLeft & bottomRight
			}{
				if (y < x.yCordOnLine(p2,p3)) {^true}; // topRight & topLeft
			};
			^false;
		};
		// cirlce vs line (width of line not taken into account)
		if (collider.isLine) {
			^origin.circle_vs_line3(radius,collider.p1,collider.p2,tempPoint);
		};
		^false; // add all other shapes before this
	}

	// circle vs cirlce response for a topdown player and a solid cirlce
	circleResponse_rigidBody{|collider, friction=1|
		var originC    = collider.origin;
		var radiusC    = collider.radius;
		var angle      = origin.thetaToPoint(originC);
		var distance   = origin.distance(originC) - radius - radiusC;
		origin += tempPoint.fromPolar(distance, angle); // only this moves not collider
		if (friction!=1) {
			var velocity = components[\mechanics].velocity;
			velocity.scaleByValue(friction ** timeDilation);
			// reduce velocity, use timeDilation because in contact with surface
		};
		parent.updateBoundingBox;
	}

	// elastic collision betwen 2 cirlces, 1 is dynamic the other is not (i.e. fixed in position)
	circleResponse_fixedBodyBounce{|collider, restitution=1|
		var originC  = collider.origin;
		var radiusC  = collider.radius;
		var angle    = origin.thetaToPoint(originC);
		var distance = origin.distance(originC) - radius - radiusC;
		var velocity = components[\mechanics].velocity;
		var velocity2= 0@0;
		this.doElasticCollisionFixedCirlce(origin, originC, velocity, velocity2, 0, mass, restitution); // £
		origin  += tempPoint.fromPolar(distance, angle);
		parent.updateBoundingBox;
	}

	// elastic collision betwen 2 cirlces, 1 is dynamic the other is fixed
	// all the energy is kept in object1, object2 doesn't move
	doElasticCollisionFixedCirlce{|origin1, origin2, velocity1, velocity2, mass1=0, mass2=1, restitution=1|
		// get the normal & the tangent
		var normal  = origin1.normal(origin2);
		var tangent = Vector(normal.y.neg, normal.x);
		// get the scalar projections onto the normal and tangent
		var scalar1Normal  = normal .dot(velocity1);
		var scalar1Tangent = tangent.dot(velocity1);
		var scalar2Normal  = normal .dot(velocity2);
		// get the new scalars
		var newScalar1Normal = 2 * scalar2Normal - scalar1Normal;
		// get the new normal and tangential vectors
		var newVelocity1Normal  = normal .scaleAsPolar(newScalar1Normal * restitution);
		var newVelocity1Tangent = tangent.scaleAsPolar(scalar1Tangent   * restitution);
		// get the new velocity
		var newVelocity = Vector(newVelocity1Normal.x + newVelocity1Tangent.x, newVelocity1Normal.y + newVelocity1Tangent.y);
		// and make its rho the same as it was before the collision
		velocity1.fromPolar(velocity1.rho, newVelocity.theta);
	}

	// circle vs cirlce elastic response. (only if both cirlces are dynmanic and have mass)
	circleResponse_rigidBodyBounce{|collider, restitution=1|
		var originC  = collider.origin;
		var radiusC  = collider.radius;
		var angle    = origin.thetaToPoint(originC);
		var distance = origin.distance(originC) - radius - radiusC;
		var velocity = components[\mechanics].velocity;
		var velocity2= collider[\mechanics].velocity;
		this.doElasticCollisionTwoCirlces(origin, originC, velocity, velocity2, mass, collider.mass, restitution); // £
		origin  += tempPoint.fromPolar(distance/2, angle);
		originC -= tempPoint.fromPolar(distance/2, angle);
		parent.updateBoundingBox;
		collider.parent.updateBoundingBox;
	}

	// elastic collision betwen 2 cirlces
	// using conservation of momentum and conservation of kinetic energy
	doElasticCollisionTwoCirlces{|origin1, origin2, velocity1, velocity2, mass1=1, mass2=1, restitution=1|
		// get the normal & the tangent
		var normal  = origin1.normal(origin2);
		var tangent = Vector(normal.y.neg, normal.x);
		// get the scalar projections onto the normal and tangent
		var scalar1Normal  = normal .dot(velocity1);
		var scalar1Tangent = tangent.dot(velocity1);
		var scalar2Normal  = normal .dot(velocity2);
		var scalar2Tangent = tangent.dot(velocity2);
		// get the new scalars
		var newScalar1Normal = (scalar1Normal * (mass1 - mass2) + (2 * mass2 * scalar2Normal)) / (mass1 + mass2);
		var newScalar2Normal = (scalar2Normal * (mass2 - mass1) + (2 * mass1 * scalar1Normal)) / (mass1 + mass2);
		// get the new normal and tangential vectors
		var newVelocity1Normal  = normal .scaleAsPolar(newScalar1Normal * restitution);
		var newVelocity1Tangent = tangent.scaleAsPolar(scalar1Tangent   * restitution);
		var newVelocity2Normal  = normal .scaleAsPolar(newScalar2Normal * restitution);
		var newVelocity2Tangent = tangent.scaleAsPolar(scalar2Tangent   * restitution);
		// set the new velocities x and y
		velocity1.replaceXY(newVelocity1Normal.x + newVelocity1Tangent.x, newVelocity1Normal.y + newVelocity1Tangent.y);
		velocity2.replaceXY(newVelocity2Normal.x + newVelocity2Tangent.x, newVelocity2Normal.y + newVelocity2Tangent.y);
	}

	// same as doElasticCollisionTwoCirlces but slighty more effiecient if both masses are the same and no restitution
	doElasticCollisionTwoCirlcesEqualMass{|origin1, origin2, velocity1, velocity2|
		// get the normal & the tangent
		var normal  = origin1.normal(origin2);
		var tangent = Vector(normal.y.neg, normal.x);
		// get the scalar projections onto the normal and tangent
		var scalar1Normal  = normal .dot(velocity1);
		var scalar1Tangent = tangent.dot(velocity1);
		var scalar2Normal  = normal .dot(velocity2);
		var scalar2Tangent = tangent.dot(velocity2);
		// get the new normal and tangential vectors
		var newVelocity1Normal  = normal .scaleAsPolar(scalar2Normal );
		var newVelocity1Tangent = tangent.scaleAsPolar(scalar1Tangent);
		var newVelocity2Normal  = normal .scaleAsPolar(scalar1Normal );
		var newVelocity2Tangent = tangent.scaleAsPolar(scalar2Tangent);
		// set the new velocities x and y
		velocity1.replaceXY(newVelocity1Normal.x + newVelocity1Tangent.x, newVelocity1Normal.y + newVelocity1Tangent.y);
		velocity2.replaceXY(newVelocity2Normal.x + newVelocity2Tangent.x, newVelocity2Normal.y + newVelocity2Tangent.y);
	}

	// circle vs cirlce response for a plaform player and a solid cirlce
	circleResponse_SolidWallPlatformer{|collider, friction|
		var originC    = collider.origin;
		var radiusC    = collider.radius;
		var angle      = origin.thetaToPoint(originC);
		var distance   = origin.distance(originC) - radius - radiusC;

		origin += tempPoint.fromPolar(distance/2, angle);
		originC -= tempPoint.fromPolar(distance/2, angle); // i should check if the circle is fixed £

		if (friction.notNil) {
			var velocity = components[\mechanics].velocity;
			if (friction.isNumber) {
				velocity.scaleAsPolarReplace(friction ** timeDilation);
			}{
				// experiment using points to sim bouncy friction with neg value
				var sign = friction.copy.sign;
				velocity.scaleReplace( friction.pow(friction.x, friction.y ).scaleReplace(sign) )
			};
		};
		parent.updateBoundingBox;
		collider.parent.updateBoundingBox;
		// which of my edges are touching the collider?
		if ((origin.y)<(originC.y)) {
			if ((((origin.y)-(originC.y)).abs) > (((origin.x)-(originC.x)).abs)) { ^\bottom }{
				if ((origin.x)>(originC.x)) { ^\left } { ^\right } };
		}{
			if ((((origin.y)-(originC.y)).abs) > (((origin.x)-(originC.x)).abs)) { ^\top }{
				if ((origin.x)>(originC.x)) { ^\left } { ^\right } };
		};
	}

	// collision response - bounce off a fixed rect with restitution
	rectResponse_rigidBodyBounce{|collider,restitution=1|
		var x        = origin.x;
		var y        = origin.y;
		var bBox     = collider.boundingBox;
		var velocity = components[\mechanics].velocity;

		if (bBox.containsPoint(	origin )) {
			// if the origin of the circle is inside the gg
			var edge   = \left;
			var value  = x - bBox.left;
			var right  = bBox.right - x;
			var top    = y - bBox.top;
			var bottom = bBox.bottom - y;
			if (right  < value) { edge = \right; value = right};
			if (top    < value) { edge = \top;   value = top  };
			if (bottom < value) { edge = \bottom };
			switch (edge,
				\left,   { origin.x = bBox.left   - radius },
				\right,  { origin.x = bBox.right  + radius },
				\top,    { origin.y = bBox.top    - radius },
				\bottom, { origin.y = bBox.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect (this can be optimized)
			// dV is distance vector from nearest point on Rect to origin of the Cirle
			var dV      = origin.copy.clipInPlace(bBox.left, bBox.top, bBox.right, bBox.bottom) -= origin;
			var offset  = Polar(dV.rho.abs - radius, dV.theta).asPoint; // offset needed to move the cirlce out of the rect
			origin += offset;
		};
		// rebound velocity
		if ( ((origin.x - x).abs) > ((origin.y - y).abs) ) {
			if (origin.x > x) { velocity.x = velocity.x.abs } {
				if (origin.x < x) { velocity.x = velocity.x.abs.neg }
			};
		}{
			if (origin.y > y) { velocity.y = velocity.y.abs }{
				if (origin.y < y) { velocity.y = velocity.y.abs.neg }
			};
		};
		if (restitution!=1) { velocity.scaleByValue(restitution) }; // reduce velocity
		parent.updateBoundingBox; // update position
	}

	// collision response - solid rect with velocity reduction
	rectResponse_rigidBody{|collider, friction=1|
		var x        = origin.x;
		var y        = origin.y;
		var bBox     = collider.boundingBox;
		var velocity = components[\mechanics].velocity;
		if (bBox.containsPoint(	origin )) {
			// if the origin of the circle is inside the rect
			var edge   = \left;
			var value  = x - bBox.left;
			var right  = bBox.right - x;
			var top    = y - bBox.top;
			var bottom = bBox.bottom - y;
			if (right  < value) { edge = \right; value = right};
			if (top    < value) { edge = \top;   value = top  };
			if (bottom < value) { edge = \bottom };
			switch (edge,
				\left,   { origin.x = bBox.left   - radius },
				\right,  { origin.x = bBox.right  + radius },
				\top,    { origin.y = bBox.top    - radius },
				\bottom, { origin.y = bBox.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect (this can be optimized)
			var dV     = origin.copy.clipInPlace(bBox.left, bBox.top, bBox.right, bBox.bottom) -= origin;
			var offset = Polar(dV.rho.abs - radius, dV.theta).asPoint; // offset needed to move the cirlce out of the rect
			origin += offset;
		};
		if (friction!=1) { velocity.scaleByValue(friction ** timeDilation) }; // walls have friction
		parent.updateBoundingBox; // update position
	}

	// collision response - solid rect with velocity reduction
	// WARNING: edge names have been flipped for platformer clarity
	rectResponse_SolidWallPlatformer{|collider, onSlope=false, friction=1|
		var velocity = components[\mechanics].velocity;
		var bBox = collider.boundingBox;
		var x    = origin.x;        // need to test against @ end of method
		var y    = origin.y;        // need to test against @ end of method
		var edge;
		if(onSlope) {
			// check for topleft & topright points in cirlce so we can disable the code below when at top of a slope
			tempPoint.replaceXY(bBox.left,bBox.top);
			tempPoint2.replaceXY(bBox.right,bBox.top);
			if ((origin.hypot(tempPoint)>radius) && (origin.hypot(tempPoint2)>radius)) { onSlope = false };
		};
		if (bBox.containsPoint(	origin )) {
			// if the origin of the circle is inside the rect
			var value  = x - bBox.left;
			var right  = bBox.right - x;
			var top    = y - bBox.top;
			var bottom = bBox.bottom - y;
			edge = \right;
			if (right <value) { edge = \left;   value = right};
			if (top   <value) { edge = \bottom; value = top  };
			if (bottom<value) { edge = \top };
			switch (edge,
				\right,  { origin.x = bBox.left   - radius },
				\left,   { origin.x = bBox.right  + radius },
				\bottom, { origin.y = bBox.top    - radius },
				\top,    { origin.y = bBox.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect
			tempPoint2.replace(origin).clipInPlace(bBox.left, bBox.top, bBox.right, bBox.bottom) -= origin; // dv
			offset.fromPolar(tempPoint2.rho.abs - radius, tempPoint2.theta); // offset to move the cirlce out of the rect
			if ((offset.x.abs)>=(offset.y.abs)) {
				if (offset.x.isNegative) { edge = \right } { edge = \left }
			}{
				if (offset.y.isNegative) { edge = \bottom } { edge = \top }
			};
			if (onSlope) { offset.x = 0 }; // stop x if on slope, this clashes with my problem below
			origin += offset;
		};
		// need to look at this in more detail ?
		if (origin.x == x) { velocity.y = 0 };
		if (onSlope.not) { if (origin.y == y) { velocity.x = 0 } };
		// if (friction!=1) { velocity.scaleByValue(friction ** timeDilation) }; // walls have friction
		// update position
		parent.updateBoundingBox; // update position
		^edge;
	}

	// bounce a cirlce off a fixed triangle
	triangleResponse_rigidBodyBounce{|collider, restitution = 1|
		var velocity = parent.velocity;
		var x        = origin.x;
		var y        = origin.y;
		var p1       = collider.p1;     // p1 is always the right angled corner
		var p2       = collider.p2;     // p2 > p3 is always the hypotenuse, points go clockwise
		var p3       = collider.p3;
		// are we hitting a corner?
		if  ((origin.distance(p1) < radius) or: {origin.distance(p2) < radius}  or: {origin.distance(p3) < radius}) {
			// stealing the response to a rect here, not accurate are acute angles but it will do for now
			// a lot easier than my attempt in the comments at the bottom of this file
			// this still needs a proper fix
			var bBox     = collider.boundingBox;
			if (bBox.containsPoint(	origin )) {
				// if the origin of the circle is inside the gg
				var edge   = \left;
				var value  = x - bBox.left;
				var right  = bBox.right - x;
				var top    = y - bBox.top;
				var bottom = bBox.bottom - y;
				if (right  < value) { edge = \right; value = right};
				if (top    < value) { edge = \top;   value = top  };
				if (bottom < value) { edge = \bottom };
				switch (edge,
					\left,   { origin.x = bBox.left   - radius },
					\right,  { origin.x = bBox.right  + radius },
					\top,    { origin.y = bBox.top    - radius },
					\bottom, { origin.y = bBox.bottom + radius }
				);
			}{
				// if the origin of the circle is outside the rect (this can be optimized)
				// dV is distance vector from nearest point on Rect to origin of the Cirle
				var dV      = origin.copy.clipInPlace(bBox.left, bBox.top, bBox.right, bBox.bottom) -= origin;
				var offset  = Polar(dV.rho.abs - radius, dV.theta).asPoint; // offset needed to move the cirlce out of the rect
				origin += offset;
			};
			// rebound velocity
			if ( ((origin.x - x).abs) > ((origin.y - y).abs) ) {
				if (origin.x > x) { velocity.x = velocity.x.abs } {
					if (origin.x < x) { velocity.x = velocity.x.abs.neg }
				};
			}{
				if (origin.y > y) { velocity.y = velocity.y.abs }{
					if (origin.y < y) { velocity.y = velocity.y.abs.neg }
				};
			};
			if (restitution!=1) { velocity.scaleByValue(restitution) }; // reduce velocity
			parent.updateBoundingBox; // update position
		}{
			var dist1_2, dist2_3, dist1_3, thetaToOffset;
			var closestPoint, distance, point1, point2, surface;
			// swap if needed so slope remains consistent
			if ( p3.x < p2.x) { var temp = p2; p2=p3; p3=temp };
			// which side of the triangle is closest to the circle? 1st find the nearest point on each line segment
			p1_2.replaceClosestPointOnLineSeg(origin,p1,p2);
			p2_3.replaceClosestPointOnLineSeg(origin,p2,p3);
			p1_3.replaceClosestPointOnLineSeg(origin,p1,p3);
			// and calc their distance from the circle's origin
			dist1_2 = origin.distance(p1_2);
			dist2_3 = origin.distance(p2_3);
			dist1_3 = origin.distance(p1_3);
			// bounce of the slopes
			// we compair these distances with each other to decide which side is closest
			case {(dist2_3 < dist1_2) && (dist2_3 < dist1_3)}
		  	                     { closestPoint = p2_3; distance = dist2_3; point1 = p2; point2 = p3; }
			{ dist1_2 < dist1_3} { closestPoint = p1_2; distance = dist1_2; point1 = p1; point2 = p2; }
			                     { closestPoint = p1_3; distance = dist1_3; point1 = p1; point2 = p3; };
			// get the offset needed to move the circle outside of the triangle
			thetaToOffset = origin.thetaToPoint(closestPoint);
			offset.fromPolar(distance + (radius * origin.originInTriangle(collider).if(1,-1)), thetaToOffset);
			origin += offset;
			parent.updateBoundingBox;
			// and get the angle of travel & then reflect it off the angle of the slope
			velocity.fromPolar(velocity.rho * restitution, point1.thetaToPoint(point2) * 2 - velocity.angle );
		};
	}

	// collision response - fixed triangle with velocity reduction
	// we already know the cirlce is colliding with the triangle
	triangleResponse_SolidWallPlatformer{|collider|
		var x      = origin.x;
		var y      = origin.y;
		var p1     = collider.p1;     // p1 is always the right angled corner
		var p2     = collider.p2;     // p2 > p3 is always the hypotenuse, points go clockwise
		var p3     = collider.p3;
		var corner = collider.corner; // #[\bottomLeft, \bottomRight, \topRight, \topLeft ];
		var dist1_2, dist2_3, dist1_3, thetaToOffset;
		if ( p3.x < p2.x) { var temp = p2; p2=p3; p3=temp }; // swap if needed so slope remains same
		// which surface is closer? find the nearest point om each line and calc distance from there
		p1_2.replaceClosestPointOnLine(origin,p1,p2).clipToLineSeg(p1,p2); // magenta
		p2_3.replaceClosestPointOnLine(origin,p2,p3).clipToLineSeg(p2,p3); // cyan, hypot
		p1_3.replaceClosestPointOnLine(origin,p1,p3).clipToLineSeg(p1,p3); // yellow
		dist1_2 = origin.hypot(p1_2); //
		dist2_3 = origin.hypot(p2_3); // hypots
		dist1_3 = origin.hypot(p1_3); //
		// is the origin of the cirlce inside the triangle?
		if (origin.originInTriangle(collider).not) {
			// outside
			if ((dist2_3 <= dist1_2) && (dist2_3 <= dist1_3)) {
				thetaToOffset = origin.thetaToPoint(p2_3);
				offset.fromPolar(dist2_3 - radius, thetaToOffset); // hypot
			}{
				if (dist1_2<dist1_3) {
					thetaToOffset = origin.thetaToPoint(p1_2);
					offset.fromPolar(dist1_2 - radius, thetaToOffset);
				}{
					thetaToOffset = origin.thetaToPoint(p1_3);
					offset.fromPolar(dist1_3 - radius, thetaToOffset);
				}
			};
		}{
			// inside
			if ((dist2_3 <= dist1_2) && (dist2_3 <= dist1_3)) {
				thetaToOffset = origin.thetaToPoint(p2_3);
				offset.fromPolar(dist2_3 + radius, thetaToOffset);  // hypot
			}{
				if (dist1_2<dist1_3) {
					thetaToOffset = origin.thetaToPoint(p1_2);
					offset.fromPolar(dist1_2 + radius, thetaToOffset);
				}{
					thetaToOffset = origin.thetaToPoint(p1_3);
					offset.fromPolar(dist1_3 + radius, thetaToOffset);
				}
			};
		};
		// ~p1 = origin; ~p2 = p1_2; ~p3 = p2_3; ~p4 = p1_3; ~p7 = origin.copy += offset; ~r = radius; // debug draw
		origin += offset;
		parent.updateBoundingBox; // update position
		thetaToOffset = thetaToOffset / pi;
		// return [edge hit, onSlope, slopeAngle]
		if (thetaToOffset==0        ) { ^[ \right,  false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==1        ) { ^[ \left,   false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==0.5      ) { ^[ \bottom, false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==(-0.5)   ) { ^[ \top,    false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset.isPositive) { ^[ \bottom, true,  p2.thetaToPoint(p3) ] } { ^[ \top, true, p2.thetaToPoint(p3) ] };
	}

}

// Rect Collider - superclass for all Rectangle Colliders ///////////////////////////////////////////////////////////

World_Rect_Collider_Base : World_Super_Collider {

	isRect { ^true }

	// collision detection for Rectangles
	testForCollision{|collider|
		if (collider===this) {^false}; // stop self detection
		// rectangle vs circle
		if (collider.isCircle) {
			var cx = collider.origin.x;
			var cy = collider.origin.y;
			var rx = boundingBox.left;
			var ry = boundingBox.top;
			var rw = boundingBox.width;
			var rh = boundingBox.height;
			var testX = cx.clip(rx, rx+rw);
			var testY = cy.clip(ry, ry+rh);
			// distance from closest edges vs radius
			if ((cx-testX).hypot(cy-testY) < (collider.radius)) { ^true } { ^false };
		};
		// rectangle vs rectangle ( rect is not rotated )
		if (collider.isRect) {
			var bBox = collider.boundingBox;
			if ((boundingBox.bottom) <= (bBox.top   ) ) {^false};
			if ((boundingBox.right ) <= (bBox.left  ) ) {^false};
			if ((boundingBox.top   ) >= (bBox.bottom) ) {^false};
			if ((boundingBox.left  ) >= (bBox.right ) ) {^false};
			^true
		};
		^false; // add other shapes later
	}

}

// Triangle Collider - superclass for all Triangle Colliders ///////////////////////////////////////////////////////////

World_Triangle_Collider_Base : World_Super_Collider {

	classvar <corners = #[\bottomLeft, \bottomRight, \topRight, \topLeft ]; // which corner has the right-angle?
	var <p1, <p2, <p3, <corner = 0;

	isTriangle { ^true }

	initShape{|argCorner|
		corner = (argCorner.isSymbol.if { corners.indexOf(argCorner) } { argCorner ? corner });
		p1 = Point();
		p2 = Point();
		p3 = Point();
		this.updatePoints;
	}

	updatePoints{
		var l = boundingBox.left;
		var r = boundingBox.right;
		var t = boundingBox.top;
		var b = boundingBox.bottom;
		switch (corner,
			0 , { p1.replaceXY(l,b); p2.replaceXY(l,t); p3.replaceXY(r,b) },
			1 , { p1.replaceXY(r,b); p2.replaceXY(l,b); p3.replaceXY(r,t) },
			2 , { p1.replaceXY(r,t); p2.replaceXY(r,b); p3.replaceXY(l,t) },
			3 , { p1.replaceXY(l,t); p2.replaceXY(r,t); p3.replaceXY(l,b) }
		);
	}

	corner_{|argCorner|
		corner = (argCorner.isSymbol.if { corners.indexOf(argCorner) } { argCorner ? corner });
		this.updatePoints;
	}

	// collision detection for Triangles
	testForCollision{|collider|
		// simple bBox test, only used in UGP so far (temp for now)
		var bBox = collider.boundingBox;
		if ((boundingBox.bottom) <= (bBox.top   ) ) {^false};
		if ((boundingBox.right ) <= (bBox.left  ) ) {^false};
		if ((boundingBox.top   ) >= (bBox.bottom) ) {^false};
		if ((boundingBox.left  ) >= (bBox.right ) ) {^false};
		^true
	}

}


+ Rect {
	// so i can pass a Rect() into .testForCollision method
	isCircle   {^false }
	isRect     {^true  }
	isLine     {^false }
	boundingBox{^this  }
}

+ Point {

	clipToLineSeg{|p1,p2|
		var x1 = p1.x;
		var y1 = p1.y;
		var x2 = p2.x;
		var y2 = p2.y;
		x = x.clip(x1.min(x2), x1.max(x2));
		y = y.clip(y1.min(y2), y1.max(y2));
	}

	rotateReplace{|point,angle|
		var sinr, cosr;
		var px = point.x;
		var py = point.y;
		sinr = angle.sin;
		cosr = angle.cos;
		x = (px * cosr) - (py * sinr);
		y = (py * cosr) + (px * sinr);
	}

	// replace and func made for speed but equivalent of closestPointOnLine method below
	replaceClosestPointOnLine{|c1,p1,p2|
		var dot = (((c1.x - (p1.x)) * (p2.x - (p1.x))) + ((c1.y - (p1.y)) * (p2.y - (p1.y)))) / ( p1.sumsqr(p2) );
		x = p1.x + (dot * (p2.x - (p1.x)));
		y = p1.y + (dot * (p2.y - (p1.y)));
	}

	// same as replaceClosestPointOnLine but only for line segment
	replaceClosestPointOnLineSeg{|c1,p1,p2|
		var x1 = p1.x;
		var y1 = p1.y;
		var x2 = p2.x;
		var y2 = p2.y;
		var dot = (((c1.x - x1) * (x2 - x1)) + ((c1.y - y1) * (y2 - y1))) / sumsqr(x1 - x2, y1 - y2);
		x = (x1 + (dot * (x2 - x1))).clip(x1.min(x2), x1.max(x2));
		y = (y1 + (dot * (y2 - y1))).clip(y1.min(y2), y1.max(y2));
	}

	// this is the closet point on the entire line, not the closet point on the line seg
	// get length of the line, get dot product of the line and circle and find the closest point on the line
	closestPointOnLine{|p1,p2|
		var dot = this.dot2(p1,p2) / ( p1.sumsqr(p2) );
		^Point(p1.x + (dot * (p2.x - (p1.x))),  p1.y + (dot * (p2.y - (p1.y))));
	}

	// get the dot product of (this - p1) & (p2 - p1)
	dot2{|p1,p2| ^ ( ((x - (p1.x)) * (p2.x - (p1.x))) + ((y - (p1.y)) * (p2.y - (p1.y))) ) }

	// given the point already lies on the line, is it in the segment? this is not a test for: is this point on the line?
	pointAlreadyInLineSegment{|p1,p2|
		if (p1.x != p2.x) {
			if (x < (p1.x.min(p2.x)) ) {^false} { if (x > (p1.x.max(p2.x)) ) {^false} {^true} }
		};
		if (p1.y != p2.y) {
			if (y < (p1.y.min(p2.y)) ) {^false} { if (y > (p1.y.max(p2.y)) ) {^false} {^true} }
		};
		^false
	}

	// this = origin of circle for example (10@10).circle_vs_line(5,5@5,15@15, Point())
	circle_vs_line{|radius,p1,p2,tempPoint|
		if (this.hypot(p1) < radius) {^true};                    // is p1 of the line in the circle ?
		if (this.hypot(p2) < radius) {^true};                    // is p2 of the line in the circle ?
		tempPoint.replaceClosestPointOnLine(this,p1,p2);         // tempPoint = closest point on the line
		if (tempPoint.pointAlreadyInLineSegment(p1,p2)) {        // closest point needs to be within line seg
			if (this.hypot(tempPoint) < radius) {^true} {^false} // and is it inside the circle?
		}{
			^false
		}
	}

	// alternate version (edge collision is 20% faster, no collision 5% faster but if point collision then 400% slower)
	// i anticipate more test will be edge and miss
	// i can use this version in circle vs triangle.hypot and do a bounds check on everyhting 1st
	circle_vs_line2{|radius,p1,p2,tempPoint|
		var x1 = p1.x;
		var x2 = p2.x;
		var y1 = p1.y;
		var y2 = p2.y;
		tempPoint.replaceClosestPointOnLine(this,p1,p2).clipInPlace(x1.min(x2),y1.min(y2),x1.max(x2),y1.max(y2));
		if (this.hypot(tempPoint) < radius) {^true} {^false};
	}

	// this is 500% faster on no collision, 16% faster on edge collision, and 400% slower on point collision
	circle_vs_line3{|radius,p1,p2,tempPoint|
		var x1 = p1.x;
		var x2 = p2.x;
		var y1 = p1.y;
		var y2 = p2.y;
		var x3 = x1.min(x2);
		var x4 = x1.max(x2);
		var y3 = y1.min(y2);
		var y4 = y1.max(y2);
		if (((x + radius)<x3) or: {(x - radius)>x4} or: {(y + radius)<y3} or: {(y - radius)>y4}) {^false}; // bounds check
		tempPoint.replaceClosestPointOnLine(this,p1,p2).clipInPlace(x3,y3,x4,y4);
		if (this.hypot(tempPoint) < radius) {^true} {^false};
	}

	// if boundingBox checks already done, which happens in Circle vs Triangle
	circle_vs_line4{|radius,p1,p2,tempPoint|
		var x1 = p1.x;
		var x2 = p2.x;
		var y1 = p1.y;
		var y2 = p2.y;
		var x3 = x1.min(x2);
		var x4 = x1.max(x2);
		var y3 = y1.min(y2);
		var y4 = y1.max(y2);
		// if boundingBox checks already done
		tempPoint.replaceClosestPointOnLine(this,p1,p2).clipInPlace(x3,y3,x4,y4);
		if (this.hypot(tempPoint) < radius) {^true} {^false};
	}

	// just origin, does not include radius
	originInTriangle{|object|
		var boundingBox = object.boundingBox;
		if (x < boundingBox.left)   {^false};
		if (x > boundingBox.right)  {^false};
		if (y < boundingBox.top)    {^false};
		if (y > boundingBox.bottom) {^false};
		if (object.corner < 2) {
			if (y > x.yCordOnLine(object.p2,object.p3)) {^true}; // bottomLeft & bottomRight
		}{
			if (y < x.yCordOnLine(object.p2,object.p3)) {^true}; // topRight & topLeft
		};
		^false;
	}

}

/* old still to covert to ECS


////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                               Circle vs.                                           //
////////////////////////////////////////////////////////////////////////////////////////////////////////

+ World_Circle {

	// collision response - bounce off solid rect with a velocity reduction coefficient
	rectResponse_SolidWallBounceCoefficient{|object,coefficient=1|
		var rect = object.boundingBox;
		var x    = origin.x;
		var y    = origin.y;
		if (rect.containsPoint(	origin )) {
			// if the origin of the circle is inside the rect
			var edge   = \left;
			var value  = x - rect.left;
			var right  = rect.right - x;
			var top    = y - rect.top;
			var bottom = rect.bottom - y;
			if (right<value)  { edge = \right; value = right};
			if (top<value)    { edge = \top;   value = top  };
			if (bottom<value) { edge = \bottom };
			switch (edge,
				\left,   { origin.x =  rect.left   - radius },
				\right,  { origin.x =  rect.right  + radius },
				\top,    { origin.y =  rect.top    - radius },
				\bottom, { origin.y =  rect.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect (this can be optimized)
			// dV is distance vector from nearest point on Rect to origin of the Cirle
			var dV      = origin.copy.clipInPlace(rect.left, rect.top, rect.right, rect.bottom) -= origin;
			var offset  = Polar(dV.rho.abs - radius, dV.theta).asPoint; // offset needed to move the cirlce out of the rect
			origin += offset;
		};
		// rebound velocity
		if ( ((origin.x - x).abs) > ((origin.y - y).abs) ) {
			if (origin.x > x) { velocity.x = velocity.x.abs } {
				if (origin.x < x) { velocity.x = velocity.x.abs.neg }
			};
		}{
			if (origin.y > y) { velocity.y = velocity.y.abs     }{
				if (origin.y < y) { velocity.y = velocity.y.abs.neg }
			};
		};
		if (coefficient!=1) { velocity.scaleByValue(coefficient**timeDilation) }; // reduce velocity
		this.updateBBoxRect; // update position
	}

	// collision response - solid rect with velocity reduction
	rectResponse_SolidWallVelocityCoefficient{|object, coefficient=1|
		var rect = object.boundingBox;
		var x    = origin.x;
		var y    = origin.y;
		if (rect.containsPoint(	origin )) {
			// if the origin of the circle is inside the rect
			var edge   = \left;
			var value  = x - rect.left;
			var right  = rect.right - x;
			var top    = y - rect.top;
			var bottom = rect.bottom - y;
			if (right <value) { edge = \right; value = right};
			if (top   <value) { edge = \top;   value = top  };
			if (bottom<value) { edge = \bottom };
			switch (edge,
				\left,   { origin.x = rect.left   - radius },
				\right,  { origin.x = rect.right  + radius },
				\top,    { origin.y = rect.top    - radius },
				\bottom, { origin.y = rect.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect (this can be optimized)
			var dV     = origin.copy.clipInPlace(rect.left, rect.top, rect.right, rect.bottom) -= origin;
			var offset = Polar(dV.rho.abs - radius, dV.theta).asPoint; // offset needed to move the cirlce out of the rect
			origin += offset;
		};
		if (coefficient!=1) { velocity.scaleByValue(coefficient**timeDilation) }; // walls have friction
		this.updateBBoxRect; // update position
	}

	*debugCall{
		if (true) {^this};
		if (~p2.notNil) { Pen.strokeColor_(Color.magenta).line(~p1, ~p2).stroke };
		if (~p3.notNil) { Pen.strokeColor_(Color.cyan   ).line(~p1, ~p3).stroke };
		if (~p4.notNil) { Pen.strokeColor_(Color.yellow ).line(~p1, ~p4).stroke };
		if (~p5.notNil) { Pen.fillColor_(white.copy).fillOval( Rect.aboutPoint(~p5,4,4) ) };
		if (~p6.notNil) { Pen.fillColor_(Color.red  ).fillOval( Rect.aboutPoint(~p6,4,4) ) };
		if (~p7.notNil) { Pen.strokeColor_(Color.cyan).strokeOval( Rect.aboutPoint(~p7,~r,~r) ) };
		~p1 = ~p2 = ~p3 = ~p4 = ~p5 = ~p6 = ~p7 = nil;
	}

	// collision response - solid rect with velocity reduction
	// we already know the cirlce is colliding with the triangle
	triangleResponse_SolidWallPlatformer{|object|
		var x      = origin.x;
		var y      = origin.y;
		var p1     = object.p1;     // p1 is always the right angled corner
		var p2     = object.p2;     // p2 > p3 is always the hypotenuse, points go clockwise
		var p3     = object.p3;
		var corner = object.corner; // #[\bottomLeft, \bottomRight, \topRight, \topLeft ];
		var dist1_2, dist2_3, dist1_3, thetaToOffset;
		if ( p3.x < p2.x) { var temp = p2; p2=p3; p3=temp }; // swap if needed so slope remains same
		// which surface is closer? find the nearest point om each line and calc distance from there
		p1_2.replaceClosestPointOnLine(origin,p1,p2).clipToLineSeg(p1,p2); // magenta
		p2_3.replaceClosestPointOnLine(origin,p2,p3).clipToLineSeg(p2,p3); // cyan, hypot
		p1_3.replaceClosestPointOnLine(origin,p1,p3).clipToLineSeg(p1,p3); // yellow
		dist1_2 = origin.hypot(p1_2); //
		dist2_3 = origin.hypot(p2_3); // hypots
		dist1_3 = origin.hypot(p1_3); //
		// is the origin of the cirlce inside the triangle?
		if (origin.originInTriangle(object).not) {
			// outside
			if ((dist2_3 <= dist1_2) && (dist2_3 <= dist1_3)) {
				thetaToOffset = origin.thetaToPoint(p2_3);
				offset.fromPolar(dist2_3 - radius, thetaToOffset); // hypot
			}{
				if (dist1_2<dist1_3) {
					thetaToOffset = origin.thetaToPoint(p1_2);
					offset.fromPolar(dist1_2 - radius, thetaToOffset);
				}{
					thetaToOffset = origin.thetaToPoint(p1_3);
					offset.fromPolar(dist1_3 - radius, thetaToOffset);
				}
			};
		}{
			// inside
			if ((dist2_3 <= dist1_2) && (dist2_3 <= dist1_3)) {
				thetaToOffset = origin.thetaToPoint(p2_3);
				offset.fromPolar(dist2_3 + radius, thetaToOffset);  // hypot
			}{
				if (dist1_2<dist1_3) {
					thetaToOffset = origin.thetaToPoint(p1_2);
					offset.fromPolar(dist1_2 + radius, thetaToOffset);
				}{
					thetaToOffset = origin.thetaToPoint(p1_3);
					offset.fromPolar(dist1_3 + radius, thetaToOffset);
				}
			};
		};
		// ~p1 = origin; ~p2 = p1_2; ~p3 = p2_3; ~p4 = p1_3; ~p7 = origin.copy += offset; ~r = radius; // debug draw
		origin += offset;
		this.updateBBoxRect; // update position
		thetaToOffset = thetaToOffset / pi;
		// return [edge hit, onSlope, slopeAngle]
		if (thetaToOffset==0        ) { ^[ \right,  false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==1        ) { ^[ \left,   false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==0.5      ) { ^[ \bottom, false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset==(-0.5)   ) { ^[ \top,    false, p2.thetaToPoint(p3) ] };
		if (thetaToOffset.isPositive) { ^[ \bottom, true,  p2.thetaToPoint(p3) ] } { ^[ \top, true, p2.thetaToPoint(p3) ] };
	}


	// collision response - bounce off solid rect with a velocity reduction coefficient
	triangleResponse_SolidWallBounceCoefficient{|object,coefficient=1|

	}

	// collision response - solid rect with velocity reduction
	// we already know the cirlce is colliding with the line
	lineResponse_SolidWallPlatformer{|object, coefficient=1|
		var dist, thetaToOffset;
		var p1   = object.p1;
		var p2   = object.p2;
		if ( p2.x < p1.x) { var temp = p1; p1=p2; p2=temp }; // swap if needed so slope remains same
		tempPoint.replaceClosestPointOnLine(origin,p1,p2).clipToLineSeg(p1,p2); // this is now the closest point on the line
		dist = origin.hypot(tempPoint) - radius; // amount to move away from line
		thetaToOffset = origin.thetaToPoint(tempPoint);
		offset.fromPolar(dist, thetaToOffset ); // offset to move circle off line
		origin += offset;
		if (coefficient!=1) { velocity.scaleByValue(coefficient**timeDilation) }; // walls have friction, tidy this up
		this.updateBBoxRect; // update position
		if (thetaToOffset.isPositive) { ^[\bottom, p1.thetaToPoint(p2)] } { ^[\top, p1.thetaToPoint(p2)] };
	}

	// collision response - solid rect with velocity reduction
	// WARNING: edge names have been flipped for platformer clarity
	rectResponse_SolidWallPlatformer{|object, onSlope=false|
		var rect = object.boundingBox;
		var x    = origin.x;        // need to test against @ end of method
		var y    = origin.y;        // need to test against @ end of method
		var edge;
		if(onSlope) {
			// check for topleft & topright points in cirlce so we can disable the code below when at top of a slope
			tempPoint.replaceXY(rect.left,rect.top);
			tempPoint2.replaceXY(rect.right,rect.top);
			if ((origin.hypot(tempPoint)>radius) && (origin.hypot(tempPoint2)>radius)) { onSlope = false };
		};
		if (rect.containsPoint(	origin )) {
			// if the origin of the circle is inside the rect
			var value  = x - rect.left;
			var right  = rect.right - x;
			var top    = y - rect.top;
			var bottom = rect.bottom - y;
			edge = \right;
			if (right <value) { edge = \left;   value = right};
			if (top   <value) { edge = \bottom; value = top  };
			if (bottom<value) { edge = \top };
			switch (edge,
				\right,  { origin.x = rect.left   - radius },
				\left,   { origin.x = rect.right  + radius },
				\bottom, { origin.y = rect.top    - radius },
				\top,    { origin.y = rect.bottom + radius }
			);
		}{
			// if the origin of the circle is outside the rect
			tempPoint2.replace(origin).clipInPlace(rect.left, rect.top, rect.right, rect.bottom) -= origin; // dv
			offset.fromPolar(tempPoint2.rho.abs - radius, tempPoint2.theta); // offset to move the cirlce out of the rect
			if ((offset.x.abs)>=(offset.y.abs)) {
				if (offset.x.isNegative) { edge = \right } { edge = \left }
			}{
				if (offset.y.isNegative) { edge = \bottom } { edge = \top }
			};
			if (onSlope) { offset.x = 0 }; // stop x if on slope, this clashes with my problem below
			origin += offset;
		};
		if (origin.x == x) { velocity.y = 0 };
		if (onSlope.not) { if (origin.y == y) { velocity.x = 0 } }; //  // here is my problem
		// update position
		this.updateBBoxRect;
		^edge;
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                Rect vs.                                            //
////////////////////////////////////////////////////////////////////////////////////////////////////////

+ World_Rect {
	// collision detection for Rectangles
	testForCollision{|object|
		if (object===this) {^false}; // stop self detection
		// rectangle vs circle
		if (object.isCircle) {
			var cx = object.origin.x;
			var cy = object.origin.y;
			var rx = boundingBox.left;
			var ry = boundingBox.top;
			var rw = boundingBox.width;
			var rh = boundingBox.height;
			var testX = cx.clip(rx, rx+rw);
			var testY = cy.clip(ry, ry+rh);
			// distance from closest edges vs radius
			if ((cx-testX).hypot(cy-testY) < (object.radius)) { ^true } { ^false };
		};
		// rectangle vs rectangle ( rect is not rotated )
		if (object.isRect) {
			var rect2 = object.boundingBox;
			if ((boundingBox.bottom) <= (rect2.top   ) ) {^false};
			if ((boundingBox.right ) <= (rect2.left  ) ) {^false};
			if ((boundingBox.top   ) >= (rect2.bottom) ) {^false};
			if ((boundingBox.left  ) >= (rect2.right ) ) {^false};
			^true
		};
		^false; // add other shapes later
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                Line vs.                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////////

+ World_Line {
	testForCollision{|object|
		// simple bBox test, only used in UGP so far (temp for now)
		var rect2 = object.boundingBox;
		if ((boundingBox.bottom) <= (rect2.top   ) ) {^false};
		if ((boundingBox.right ) <= (rect2.left  ) ) {^false};
		if ((boundingBox.top   ) >= (rect2.bottom) ) {^false};
		if ((boundingBox.left  ) >= (rect2.right ) ) {^false};
		^true
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                              Triangle vs.                                          //
////////////////////////////////////////////////////////////////////////////////////////////////////////

+ World_Triangle {
	testForCollision{|object|
		// simple bBox test, only used in UGP so far (temp for now)
		var rect2 = object.boundingBox;
		if ((boundingBox.bottom) <= (rect2.top   ) ) {^false};
		if ((boundingBox.right ) <= (rect2.left  ) ) {^false};
		if ((boundingBox.top   ) >= (rect2.bottom) ) {^false};
		if ((boundingBox.left  ) >= (rect2.right ) ) {^false};
		^true
	}
}

*/

/*

1st attempt, almost but very complex

	// bounce a cirlce off a triangle
	triangleResponse_rigidBodyBounce{|collider, restitution = 1|
		var velocity = parent.velocity;
		var x        = origin.x;
		var y        = origin.y;
		var p1       = collider.p1;     // p1 is always the right angled corner
		var p2       = collider.p2;     // p2 > p3 is always the hypotenuse, points go clockwise
		var p3       = collider.p3;
		var corner   = collider.corner; // #[\bottomLeft, \bottomRight, \topRight, \topLeft ];
		var dist1_2, dist2_3, dist1_3, thetaToOffset;
		var closestPoint, distance, point1, point2, surface;
		// swap if needed so slope remains consistent
		if ( p3.x < p2.x) { var temp = p2; p2=p3; p3=temp };
		// which side of the triangle is closest to the circle? 1st find the nearest point on each line segment
		p1_2.replaceClosestPointOnLineSeg(origin,p1,p2);
		p2_3.replaceClosestPointOnLineSeg(origin,p2,p3);
		p1_3.replaceClosestPointOnLineSeg(origin,p1,p3);
		// and calc their distance from the circle's origin
		dist1_2 = origin.distance(p1_2);
		dist2_3 = origin.distance(p2_3);
		dist1_3 = origin.distance(p1_3);
		// we then compair these distances with each other to decide which side is closest
		// TODO: CORNERS ARE STILL A PROBLEM, use Velocity and Position to select the correct face to bounce off ££


		case {origin.distance(p1) < radius} {
			// right angled corner


			if ((origin.x - p2.x ).abs < (origin.y - p2.y ).abs) {
				surface = \b
			}{
				surface = \c
			};
			1.post;
		}
		{origin.distance(p2) < radius} {
			//	~p1 = origin.copy;
			//	~p2 = p2.copy;
			if (origin.aboveLine(p3,p2)) {
				if (origin.y <= p2.y ) { surface = \a } { surface = \d };

			} {
				if (origin.x >= p2.x ) { surface = \b } { surface = \d };
			};
			//	2.post;
		}
		{origin.distance(p3) < radius} {
			~p1 = origin.copy;
			~p2 = p3.copy;

			if (origin.leftLine(p3,p2)) {
				"left".post;
				if (origin.x <= p3.x ) { surface = \a } { surface = \d };

			}{
				"right".post;
				if (origin.y >= p3.y ) { surface = \c } { surface = \d };
			};


			3.post;
		};


		// best fix? move away from corner and bounce of slope at right angles to the corner!! I DONT THINK SO
		// JUST CHOOSE RIGHT SURFACE

		case {(dist2_3 < dist1_2) && (dist2_3 < dist1_3)} {
			closestPoint = p2_3; distance = dist2_3; point1 = p2; point2 = p3;
		//	"a".post;
		}
		{ dist1_2 < dist1_3} {
			closestPoint = p1_2; distance = dist1_2; point1 = p1; point2 = p2;
		//	"b".post;
		}
		{
			closestPoint = p1_3; distance = dist1_3; point1 = p1; point2 = p3;
		//	"c".post;
		};



		case {surface == \a} {
			closestPoint = p2_3; distance = dist2_3; point1 = p2; point2 = p3;
			"A".postln;
		}
		{ surface == \b} {
			closestPoint = p1_2; distance = dist1_2; point1 = p1; point2 = p2;
			"B".postln;
		}
		{ surface == \c} {
			closestPoint = p1_3; distance = dist1_3; point1 = p1; point2 = p3;
			"C".postln;
		}
		{ surface == \d} {
			// tangent
			closestPoint = p2_3; distance = dist2_3; point1 = (0@0); point2 = p2.tangent(p3);
			"D".postln;
		};



		// get the offset needed to move the circle outside of the triangle
		thetaToOffset = origin.thetaToPoint(closestPoint);
		offset.fromPolar(distance + (radius * origin.originInTriangle(collider).if(1,-1)), thetaToOffset);
		origin += offset;
		parent.updateBoundingBox;
		// and get the angle of travel & then reflect it off the angle of the slope
		velocity.fromPolar(velocity.rho * restitution, point1.thetaToPoint(point2) * 2 - velocity.angle );

	}



*/

