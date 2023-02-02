////////////////////////////////////////////////////////////////////////////////////////////////////////
//                            Clocks & Timers for use in Engine                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// NEVER defer, fork, sched, Routine or Task anything.
// There is no guarantee the scene or world will be the same or even running when your function yields.
// use {}.deferInScene {}.deferInWorld {}.schedInScene {}.schedInWorld {}.forkInScene {}.forkInWorld instead !!!
// These Clocks & Timers are designed for use in scripts.
// They are bound to the scene or the world and will only tick if the scene or the world ticks.

World_Defer_Timer : World_World {

	var <>total     = inf;    // total duration of timer in seconds
	var <>remaining = inf;    // seconds remaining before timer does action
	var <isRunning  = true;   // is the timer running
	var <>loop      = false;  // loop lets the timer repeat
	var <global     = false;  // world or scene clock?
	var <dilation   = true;   // is timer effected by time dilation?
	var <>func;               // the function that gets called

	*new{|func, remaining, global = false, dilation = true| ^super.new.initOn(func, remaining, global, dilation) }

	initOn{|argFunc, argRemaining, argGlobal, argDilation|
		func        = argFunc;
		isRunning   = true;
		remaining   = (argRemaining ? 0).clip(staticFrameLength, inf);
		total       = remaining;
		global      = argGlobal;
		dilation    = argDilation;
		if (global) { worldTimers = worldTimers.add(this) }{ sceneTimers = sceneTimers.add(this) };
	}

	tick{
		if (dilation) { remaining = remaining - frameLength } { remaining = remaining - staticFrameLength };
		if (remaining<=0) {
			isRunning = false;
			remaining = 0;
			garbage   = garbage.add(this);
			this.endAction;
		};
	}

	start{
		if (isRunning.not) {
			isRunning = true;
			if (global) { worldTimers = worldTimers.add(this) }{ sceneTimers = sceneTimers.add(this) };
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
			isRunning  = true;
			if (global) { worldTimers = worldTimers.add(this) }{ sceneTimers = sceneTimers.add(this) };
			garbage    = garbage.remove(this);
			func.reset;
		};
		remaining = total;
	}

	pause{ this.stop }

	frac{ ^remaining / total }

	fracRemaining{ ^ 1 - (remaining / total) }

	endAction{ ^func.value(this) }

	free{ if (global) { worldTimers.remove(this) }{ sceneTimers.remove(this) } }

}

// schedule or fork a func & loop.
// The float you return specifies the delta to resched the function for.
// Returning nil will stop the task from being rescheduled.

World_Schedule_Timer : World_Defer_Timer {

	initOn{|argFunc, argRemaining, argGlobal, argDilation|
		func        = argFunc;
		isRunning   = true;
		remaining   = (argRemaining ? 0).clip(0,inf);
		total       = remaining;
		global      = argGlobal;
		dilation    = argDilation;
		if (global) { worldTimers = worldTimers.add(this) }{ sceneTimers = sceneTimers.add(this) };
		if (remaining==0) { this.eval };
	}

	tick{
		if (dilation) { remaining = remaining - frameLength } { remaining = remaining - staticFrameLength };
		if (remaining<=0) { this.eval };
	}

	eval{
		var returnValue = this.endAction;
		case
		// if func returns a number use this as next scheduled time in loop
		{returnValue.isNumber} {
			remaining = returnValue.clip(0,inf);
			total     = remaining;
		}
		// if func returns nil then stop
		{returnValue.isNil} {
			isRunning = false;
			remaining = 0;
			garbage   = garbage.add(this);
		}
		// else loop using previous time
		{
			remaining = remaining.wrap(0,total);
		};
	}

}

+ Nil { wait{ ^nil.yield } }  // so nil.wait can stop clocks

// Short cuts for timers above, works in similar ways to defer, sched & fork
// I recommend using these shortcuts in your scripts

+ Function {
	deferInScene{|delta=0, dilation = true| ^World_Defer_Timer   (this, delta, false, dilation ) } // once 1 in the Scene
	deferInWorld{|delta=0, dilation = true| ^World_Defer_Timer   (this, delta, true , dilation ) } // once 1 in the World
	schedInScene{|delta=0, dilation = true| ^World_Schedule_Timer(this, delta, false, dilation ) } // loops in the Scene
	schedInWorld{|delta=0, dilation = true| ^World_Schedule_Timer(this, delta, true , dilation ) } // loops in the World
	forkInScene {|dilation = true| ^World_Schedule_Timer(this.asRoutine, 0, false, dilation ) } // loops in Scene, can use .wait
	forkInWorld {|dilation = true| ^World_Schedule_Timer(this.asRoutine, 0, true , dilation ) } // loops in World, can use .wait
}

// if dilation = true then the timer is affected by timeDilation "game time", if false then works in "real time"
// deferInScene & schedInScene will add to the current Scene in runtime
// defer will always wait 1 frame, even if delta=0
// sched will start straight away if delta = 0
// fork will allow you to have 1.wait in the function
// TODO : temporary or permanent

/* examples...

a = {"a".post}.schedInScene(1);
b = {"b".post}.schedInScene(1); // try doing this in a different scene
c = {"c".post}.schedInWorld(1);
a.free; b.free; c.free;
a = {"*".post; 0.1.rrand(2)}.schedInScene(1);
a.stop;
a = {"*".post; [0.1.rrand(2),0.5,0.2,nil].choose}.schedInScene(1);
a.free;
a = {World_World.worldFrame.postln; 0}.schedInScene; // will do every frame
a.free;
{"defer".postln}.deferInScene(1);
a = {".".post}.schedInScene;
a.stop;
a = { 100.do{|i| i.postln; 0.1.rrand(0.5).wait;}  }.forkInScene;
a.stop;
a.reset; // if scene has changed this will reschudle in the current runtime scene
b = { 100.do{|i| i.postln; 0.1.rrand(0.5).wait;}  }.forkInWorld;
b.stop;

*/
