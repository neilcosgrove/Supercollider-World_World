////////////////////////////////////////////////////////////////////////////////////////////////////////
//                               Entity (ECS similar System)                                          //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// super class of all Entities
// Entities are all the objects in a Scene, inside they have Components that define the Entities behaviour
// Like an ECS but the Component + System are one.
// Supercollider doesn't have memory management so no point in making them separate

World_Entity : World_World {

	// all entities have 3 things: an origin, a boundingBox and a set of components

	var <origin, <boundingBox, <components;

	// CAUTION! origin & boundingBox are used as proxies. Always do in-place operations on them
	// never replace them with a different object

	at{|component| ^components[component] } // shortcut

	componentExists{|component| ^components[component].notNil }

	initEntity{|argOrigin|
		origin      = argOrigin;
		boundingBox = Rect.aboutPoint(origin, 1, 1);
		entities    = entities.add(this);
		components  = IdentityDictionary[];
	}

	initEntityAboutPoint{|argOrigin, width, height|
		origin      = argOrigin;
		boundingBox = Rect.aboutPoint(origin, width, height);
		entities    = entities.add(this);
		components  = IdentityDictionary[];
	}

	initEntityWithRadius{|argOrigin, radius|
		origin      = argOrigin;
		boundingBox = Rect.aboutPoint(origin, radius, radius);
		entities    = entities.add(this);
		components  = IdentityDictionary[];
	}

	initEntityWithRect{|rect|
		boundingBox = rect;
		origin      = rect.center;
		entities    = entities.add(this);
		components  = IdentityDictionary[];
	}

	initEntityWithLTWH{|left, top, width, height|
		boundingBox = Rect(left, top, width, height);
		origin      = boundingBox.center;
		entities    = entities.add(this);
		components  = IdentityDictionary[];
	}

	origin_{|point|
		origin.replace(point);
		this.updateBoundingBox;
	}

	moveBy{|x,y|
		origin.addXY(x,y);
		this.updateBoundingBox;
	}

	boundingBox_{|rect|
		boundingBox.replace(rect);
		origin.replaceXY(boundingBox.left + (boundingBox.width * 0.5), boundingBox.top + (boundingBox.height * 0.5));
		if (components[\collider].notNil) { components[\collider].updateUGPCell };
	}

	updateBoundingBox{
		boundingBox.left_(origin.x - (boundingBox.width*0.5)).top_(origin.y - (boundingBox.height*0.5));
		if (components[\collider].notNil) { components[\collider].updateUGPCell };
	}

	// you can't free an entity halfway through a frame because its still referenced by other entities, components & containers
	// free is called by the garbage collector. use kill to free your entity instead.
	free{
		components.do(_.freeComponent);
		components.clear;
		components = nil;
		entities.remove(this);
	}

	// kill will add an entity to the garbage. the garbage gets collected at the end of a frame.
	kill{
		garbage = garbage.add(this);
		components[\death].do;
	}

	delete{
		garbage = garbage.add(this);
	}

	saveState{^IdentityDictionary[\class-> this.class.asSymbol, \origin -> origin] }

	loadState{|dict| origin = dict[\origin]; this.updateBoundingBox }

	// short cuts
	active{ if (components[\mechanics].notNil) { ^components[\mechanics].active }{ ^true } }
	velocity{ if (components[\mechanics].notNil) { ^components[\mechanics].velocity } { ^nil } }
	velocity_{|point| if (components[\mechanics].notNil) { components[\mechanics].velocity.replace(point) } }
	mass{ if (components[\collider].notNil) { ^components[\collider].mass } { ^nil } }
	mass_{|number| if (components[\collider].notNil) { components[\collider].mass_(number) } }

	// for tile editor
	*isTile   { ^false }
	*isNPC    { ^false }
	*isItem   { ^false }
	*isPlayer { ^false }
	isTile  { ^this.class.isTile   }
	isNPC   { ^this.class.isNPC    }
	isItem  { ^this.class.isItem   }
	isPlayer{ ^this.class.isPlayer }
}

