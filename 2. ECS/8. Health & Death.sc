////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                Health & Death Components                                           //
////////////////////////////////////////////////////////////////////////////////////////////////////////

// Health Components ////////////////////////////////////////////////////////////////////

World_Health : World_Component {

	var <>health, <>maxHealth, <>isAlive = true, <>invulnerable = false, <>damageSound; // might add hit timers here

	*new{|parent,health=10,maxHealth=10,damageSound| ^super.new.initComponent(parent).init(health,maxHealth,damageSound) }

	init{|argHealth,argMaxHealth,argDamageSound|
		health      = argHealth;
		maxHealth   = argMaxHealth;
		damageSound = argDamageSound;
	}

	free{}

	takeDamage{|collider|
		var damage = 0;
		this.decHealth(damage);
	}

	decHealth{|damage|
		health = (health - damage).clip(0,inf);
		if (health == 0) {
			if (isAlive) {
				isAlive = false; // stops multiple deaths in 1 frame
				parent.kill;
			};
		};
	}

	incHealth{|amount|
		health = (health + amount).clip(0,maxHealth);
	}

	restore{ health = maxHealth }

	frac{ ^health / maxHealth }

	isDead{ ^isAlive.not }

	atMaxHealth{ ^health >= maxHealth }

}

// Damage Profile Components ////////////////////////////////////////////////////////////////////

// all players, bullets and NPCs have a damage profile

World_Damage_Profile : World_Component {

	var <>standard, <>fire, <>ice, <>stun; // these are numerical values like 1,10 or 25

	*new{|parent, standard = 1, fire = 0, ice = 0, stun = 0|
		^super.new.initComponent(parent).init(standard, fire, ice, stun)
	}

	init{|argStandard, argFire, argIce, argStun|
		standard = argStandard;
		fire     = argFire;
		ice      = argIce;
		stun     = argStun;
	}

}

// all players and NPCs have an amour profile

World_Armour_Profile : World_Component {

	var <>standard, <>fire, <>ice, <>stun; // these are multiplier values like 1, 0.5 or 0.25

	*new{|parent, standard = 1, fire = 1, ice = 1, stun = 1|
		^super.new.initComponent(parent).init(standard, fire, ice, stun)
	}

	init{|argStandard, argFire, argIce, argStun|
		standard = argStandard;
		fire     = argFire;
		ice      = argIce;
		stun     = argStun;
	}

}

// Death Components ////////////////////////////////////////////////////////////////////

World_Death : World_Component {

	do{ this.doDeath }

	doDeath{} // to subclass

	takeDamage {} // to subclass
}
