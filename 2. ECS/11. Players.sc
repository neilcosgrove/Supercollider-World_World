////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                              Players                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Player_Base : World_Entity {

	var <id;

	*isPlayer { ^true }

	initPlayer{|argID| id = argID; players[id] = this }

	isAlive { if (components.notNil) { ^components[\health].isAlive } { ^false } }

	invulnerable{ ^components[\health].invulnerable }

	invulnerable_{|bool, time|
		if (bool) {
			if (components[\invulTimer].notNil) {
				components[\invulTimer].reset;
				if (time.notNil) { components[\invulTimer].remaining_(time).total_(time) };
			};
			components[\health].invulnerable_(true); // also because tick of invulTimer starts 1 frame later
		}{
			if (components[\invulTimer].notNil) { components[\invulTimer].stop.endAction };
		};
	}

	doRetrigger{ if (components[\controller].notNil) { components[\controller].doRetrigger } }

}

World_Invulnerability_Timer : World_Clock {

	resetAction {
		components[\health  ].invulnerable_(true);
		components[\drawFunc].hitTimerOn_(true);
	}

	tickAction  {
		components[\health  ].invulnerable_(true);
		components[\drawFunc].invulAlpha_( (remaining*2).asInteger.odd.if(0.25,0.9) )
	}

	endAction   {
		components[\health  ].invulnerable_(false);
		components[\drawFunc].invulAlpha_(1)
	}

}
