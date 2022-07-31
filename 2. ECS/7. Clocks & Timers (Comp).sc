////////////////////////////////////////////////////////////////////////////////////////////////////////
//                         Clocks & Timer for use in Components                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// NEVER defer, fork, sched, Routine or Task anything.
// There is no guarantee the scene or world will be the same or even running when your function yields.
// You should use {}.deferInScene {}.deferInWorld {}.schedInScene {}.schedInWorld {}.forkInScene {}.forkInWorld
// Or use a Timer Component like the ones below instead !!!
// These Component are designed for use in Entities and as a result they are bound to the scene clock only
// and don't run on the world clock. They will only tick if the scene ticks. i.e. not during a pause

World_LifeSpan_Timer : World_Timer { endAction { parent.kill } }

World_Hit_Timer : World_Timer {
	resetAction { components[\drawFunc].hitTimerOn_(true ) }
	endAction   { components[\drawFunc].hitTimerOn_(false) }
}

World_Example_Clock : World_Clock {
	tickAction { this.frac.postln }
	endAction  { "endAction".postln }
}

World_Example_Random_Timer : World_Random_Timer {
	endAction  { "endAction".postln }
}

World_Func_Timer : World_Timer { var <>func; endAction{ func.value(this) } }

World_Func_Clock : World_Clock { var <>func; tickAction{ func.value(this) } }

World_Timer : World_Timer_Base {

	*new{|parent,remaining,isRunning=true,loop=false|
		if (isRunning) {
			^super.new.initComponent(parent).initOn(remaining,loop)
		}{
			^super.new.initComponent(parent).initOff(remaining,loop)
		};
	}

}

// timers only have an endAction and a reset action

World_Timer_Base : World_Component {

	var <>total     = inf;    // total duration of timer in seconds
	var <>remaining = inf;    // seconds remaining before timer does action
	var <isRunning  = true;   // is the timer running
	var <>loop      = false;  // loop lets the timer repeat

	initOn{|argRemaining,argLoop|
		isRunning   = true;
		remaining   = argRemaining;
		total       = argRemaining;
		loop        = argLoop;
		sceneTimers = sceneTimers.add(this);
	}

	initOff{|argRemaining,argLoop|
		isRunning = false;
		remaining = argRemaining;
		total     = argRemaining;
		loop      = argLoop;
	}

	tick{
		remaining = remaining - frameLength;
		if (remaining<=0) {
			if (loop) {
				remaining = remaining.wrap(0,total);
			}{
				isRunning = false;
				remaining = 0;
				garbage   = garbage.add(this);
			};
			this.endAction;
		};
	}

	start{
		if (isRunning.not) {
			isRunning   = true;
			sceneTimers = sceneTimers.add(this)
		};
	}

	stop{
		if (isRunning) {
			isRunning = false;
			garbage   = garbage.add(this);
		};
	}

	reset{
		if (isRunning.not) {
			sceneTimers = sceneTimers.add(this);
			isRunning   = true;
			garbage     = garbage.remove(this);
			this.resetAction;
		};
		remaining = total;
	}

	pause{ this.stop }

	free{ sceneTimers.remove(this) }

	frac{ ^remaining / total }

	fracRemaining{ ^ 1 - (remaining / total) }

	endAction { } // to subclass

	resetAction { } // to subclass

}

// a clock has an action every tick

World_Clock : World_Timer {

	tick{
		remaining = remaining - frameLength;
		if (remaining<=0) {
			if (loop) {
				remaining = remaining.wrap(0,total);
				this.endAction;
				this.tickAction;
			}{
				isRunning = false;
				remaining = 0;
				garbage = garbage.add(this);
				this.tickAction;
				this.endAction;
			};

		}{
			this.tickAction;
		}
	}

	tickAction { } // to subclass

}

// a random timer

World_Random_Timer : World_Random_Timer_Base {

	*new{|parent,min,max,isRunning=true,loop=false|
		if (isRunning) {
			^super.new.initComponent(parent).initOn(min,max,loop)
		}{
			^super.new.initComponent(parent).initOff(min,max,loop)
		};
	}

}

World_Random_Timer_Base : World_Timer_Base {

	var <>min, <>max; // range timer can have

	initOn{|argMin,argMax,argLoop|
		min       = argMin;
		max       = argMax;
		isRunning = true;
		remaining = min.rrand(max);
 		total     = remaining;
		loop      = argLoop;
		sceneTimers = sceneTimers.add(this);
	}

	initOff{|argMin,argMax,argLoop|
		min       = argMin;
		max       = argMax;
		isRunning = false;
		remaining = min.rrand(max);
		total     = remaining;
		loop      = argLoop;
	}

	tick{
		remaining = remaining - frameLength;
		if (remaining<=0) {
			if (loop) {
				remaining = min.rrand(max);
				total     = remaining;
			}{
				isRunning = false;
				remaining = 0;
				garbage = garbage.add(this);
			};
			this.endAction;
		};
	}

	reset{
		if (isRunning.not) {
			sceneTimers    = sceneTimers.add(this);
			isRunning = true;
			garbage   = garbage.remove(this);
			this.resetAction;
		};
		remaining = min.rrand(max);
		total     = remaining;
	}

}
