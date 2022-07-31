////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                 Mechanical Components                                              //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// mechanics make things move

// Classical Mechanics Base //////////////////////////////////////////////////////////////////////////////

World_Classical_Mechanics_Base : World_Component {

	var <dynamic          = true; // can it move? used to switch movement on/off
	var <active           = true; // is object active? set by activeDistance to the cam position, off camera is inactive
	var <>activeDistance  = nil;  // distance from the camera that the object is active, nil is inf
	var <distanceToCamera = 9999; // some random distance to start, this value gets updated
	var <>angle           = 0;    // angle in radians
	var <>angularVelocity;        // angle turned every frame
	var <>velocity;               // point is a vector where Point(0,0) is at rest
	var <>acceleration;           // point is a vector where Point(0,0) is no acceleration
	var <>maxVelocity;            // clip velocity to maximum
	var <>friction;               // where Point(x,y) are the acceleration coefficients
	var <>gravity;                // acceleration due to gravity
	var <origin, <boundingBox;

	init{|argVelocity,argMaxVelocity,argFriction,argGravity,argAceleration|
		origin       = parent.origin;
		boundingBox  = parent.boundingBox;
		velocity     = argVelocity ?? {Point(0,0)}; // there must always be a velocity in Standard Mechanics
		maxVelocity  = argMaxVelocity;
		friction     = argFriction;
		gravity      = argGravity;
		acceleration = argAceleration;
		dynamics     = dynamics.add(this);
	}

	free{ dynamics.remove(this) }

	// does the object move?
	dynamic_{|bool|
		dynamic = bool;
		if (dynamic) { dynamics = dynamics.add(this) } { dynamics.remove(this) };
	}

	// Classical Mechanics, can be overridden if needed
	// using an "euler integration" type approach add accelerations to velocity and velocity to position every frame
	// all operations are in place to avoid any new objects, which increases performance
	// for example... += -= *= /= addMul mulPower wrapInPlace clipInPlace aboutPoint clipVelocity are all in place operations
	tick{
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
			this.myTick;                                                                     // do the subclas tick
			origin.addMul(velocity, timeDilation);                                           // apply velocity (last thing)
			this.worldEdge;                                                                  // apply world edge response
			parent.updateBoundingBox;                                                        // update boundingBox
		};
	}

	// what happens when the entity hits the edge of the world? (for subclassing)
	worldEdge{
		if (origin.x < 0          ) { origin.x = origin.x.fold(0,worldRight ); velocity.x = velocity.x.neg };
		if (origin.x > worldRight ) { origin.x = origin.x.fold(0,worldRight ); velocity.x = velocity.x.neg };
		if (origin.y < 0          ) { origin.y = origin.y.fold(0,worldBottom); velocity.y = velocity.y.neg };
		if (origin.y > worldBottom) { origin.y = origin.y.fold(0,worldBottom); velocity.y = velocity.y.neg };
	}

	myTick{} // for subclassing

}

// Classical Mechanics & nothing else ///////////////////////////////////////////////////////////////////

World_Classical_Mechanics : World_Classical_Mechanics_Base {

	var myHidden;

	*new{|parent,velocity,maxVelocity,friction,gravity,acceleration|
		^super.new.initComponent(parent).init(velocity,maxVelocity,friction,gravity,acceleration)
	}

}

// Bullet Mechanics ///////////////////////////////////////////////////////////////////////////////////////

World_Bullet_Mechanics : World_Classical_Mechanics {

	myTick{
		if (lightMap.isOn) { lightMap.addDynamicLightSource(origin.x, origin.y, 2, 3, 0.4) };
	}

}

// Basic Mechanics //////////////////////////////////////////////////////////////////////////////

// simple. just velocity, a worldEdge and myTick

World_Basic_Mechanics : World_Basic_Mechanics_Base {

	*new{|parent, velocity| ^super.new.initComponent(parent).init(velocity) }

}

World_Basic_Mechanics_Base : World_Component {

	var <>velocity, <origin, <boundingBox;

	init{|argVelocity|
		velocity    = argVelocity;
		dynamics    = dynamics.add(this);
		origin      = parent.origin;
		boundingBox = parent.boundingBox;
	}

	free{ dynamics.remove(this) }

	tick{
		this.myTick;
		origin.addMul(velocity, timeDilation);
		this.worldEdge;
		parent.updateBoundingBox;
	}

	active{ ^true } //  for subclassing

	worldEdge{}  // for subclassing

	myTick{} // for subclassing

}

// Basic Mechanics //////////////////////////////////////////////////////////////////////////////

World_Velocity_Friction_Mechanics : World_Velocity_Friction_Mechanics_Base {

	*new{|parent,velocity,friction| ^super.new.initComponent(parent).init(velocity,friction) }

}

World_Velocity_Friction_Mechanics_Base : World_Component {

	var <>velocity, <>friction, <origin, <boundingBox;

	init{|argVelocity,argFriction|
		velocity    = argVelocity;
		friction    = argFriction;
		dynamics    = dynamics.add(this);
		origin      = parent.origin;
		boundingBox = parent.boundingBox;
	}

	free{ dynamics.remove(this) }

	tick{
		velocity.mulPower(friction,timeDilation);
		this.myTick;
		origin.addMul(velocity, timeDilation);
		this.worldEdge;
		parent.updateBoundingBox;
	}

	active{ ^true } // for subclassing

	worldEdge{}  // for subclassing

	myTick{} // for subclassing

}

