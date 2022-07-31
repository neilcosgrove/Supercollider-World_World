////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          World_Audio                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// An audio buffer player - has the functionality to make it useful for a game engine
// every sound asset has a max polyphony of 1 voice.
// examples at the bottom of this file

World_Audio : World_World {

	classvar <channelNames = #[\music, \fx, \dialog, \ambience, \interface       ]; // add more if you like
	classvar <synthNames   = #[\bufMono, \bufMonoLoop, \bufStereo, \bufStereoLoop];
	classvar <isPlaying    = true, <server, <bufferSynths, <pauseChannel, <pauseSynth;
	classvar <channels, <loopArgs, <playlists, <playlistStates;

	// World_Audio init
	*init{
		if (verbose) { "World_Audio:init".postln };
		bufferSynths   = IdentityDictionary[];  // Synths that are playing the Buffers. There is only ever 1 Synth per Buffer
		channels       = IdentityDictionary[];  // the Channels
		loopArgs       = IdentityDictionary[];  // the args for the Synths that are currently looping
		playlists      = IdentityDictionary[];  // each channel has a playlist
		playlistStates = IdentityDictionary[];  // and each playlist has state
		channelNames.do{|name|
			playlists[name]                  = OrderedIdentitySet[]; // these are the individual play list
			playlistStates[name]             = IdentityDictionary[]; // these are the individual states
			playlistStates[name][\isPlaying] = false;                // is play list playing?
			this.setupPlaylist(name);                                // set up the states
		};
		CmdPeriod.add(World_Audio);
	}

	// called on cmd period
	*cmdPeriod{
		bufferSynths.clear;
		{ this.startChannelsAndSynths }.defer(0.1);
	}

	// boot the server
	*bootServer{|action|
		if (verbose) { "World_Audio:bootServer".postln };
		Server.killAll;
		server = Server.default;
		Platform.case(
			\osx, {
				server.options.inDevice  = nil;
				server.options.outDevice = nil;
				// these are temp short cuts for my Mac !!
				//server.options.inDevice  = "Built-in Microph";
				//server.options.outDevice = "Built-in Output";
				//server.options.inDevice  = "BlackHole 2ch";
				//server.options.outDevice = "BlackHole 2ch";
				//server.options.inDevice  = "Soundflower (2ch)";
				//server.options.outDevice = "Soundflower (2ch)";
			},
			\linux,		{ },
			\windows,	{ }
		);
		ServerBoot.add({ this.postServerBoot(action) }, server);
		server.boot;
	}

	// load Buffers and SynthDefs after server boot
	*postServerBoot{|action|
		if (verbose) { "World_Audio:postServerBoot".postln };
		server.sendMsg("error",0); // turn off error messaging.
		// make the synth defs (only supports mono or stereo samples)
		synthNames.do{|synthName,i|
			var numChannels = [1,1,2,2][i];
			var loop        = [0,1,0,1][i];
			SynthDef(synthName, {|bufnum, gate=1, amp=1, fadeIn=0, fadeOut=1, rate=1, pan=0|
				var player = PlayBuf.ar(numChannels, bufnum, BufRateScale.kr(bufnum) * rate, loop: loop);
				var out	   = player * EnvGen.ar(Env([0,1,0], [fadeIn, fadeOut], \lin, 1), gate, doneAction:2)*amp;
				if (numChannels==1) { out = [out,out] };
				Out.ar(0, Balance2.ar(out[0], out[1], pan) );
				if (loop==0) { FreeSelfWhenDone.kr(player) };
			}).send;
		};
		SynthDef(\pause, {|id=1000,gate=1| Pause.kr(gate, id); }).send;
		World_Assets.loadSoundAssets({
			this.startChannelsAndSynths;
			action.value;
		});
	}

	*startChannelsAndSynths{
		if (verbose) { "World_Audio:startChannelsAndSynths".postln };
		pauseChannel = Group(); // group used for Pause
		channelNames.do{|name| channels[name] = Group(pauseChannel) };
		pauseSynth   = Synth(\pause, [\id, pauseChannel.nodeID] );
		loopArgs.do{|args| this.performWithEnvir(\play,args) }; // start up looping sounds again
	}

	// play a buffer interface ////////////////////////////////////////////////////////////////////////////////////////////////

	// reverse ?

	// play a Buffer already loaded in World_Assets ( if looping store for later incase of cmd period )
	*play{|key, amp=1, fadeIn=0, fadeOut=1, rate=1, pan=0, loop=false, channel=\fx|
		if (key.isNil) {^this} {
			var synthArg, synthName, previousSynth;
			var buffer    = buffers[key];
			if (buffer.isNil) { if (verbose) {"Buffer % not found".format(key).warn}; ^this};
			previousSynth = bufferSynths[key];
			synthName     = synthNames[buffer.numChannels - 1 * 2 + loop.binaryValue];
			synthArg      = [\bufnum, buffer.bufnum, \amp, amp, \fadeIn, fadeIn, \fadeOut, fadeOut, \rate, rate, \pan, pan];
			bufferSynths[key] = Synth(synthName, synthArg, channels[channel]); // play synth
			{ 0.02.wait; previousSynth.release(0.05) }.fork;               // release previous synth
			if (loop.asBoolean) {
				loopArgs[key] = (synthArg ++ [\loop: loop, \key, key, \channel, channel]).asDict
			}{
				loopArgs[key] = nil
			};
			^bufferSynths[key];
		};
	}

	// support functions
	*releaseAll{ bufferSynths.do(_.release); loopArgs.clear; }
	*stopAll   { bufferSynths.do(_.free   ); loopArgs.clear; }

	*release{|key       | bufferSynths[key].release; loopArgs[key] = nil; }
	*stop   {|key       | bufferSynths[key].free;    loopArgs[key] = nil; }
	*amp    {|key, value| bufferSynths[key].set(\amp, value); }
	*rate   {|key, value| bufferSynths[key].set(\rate,value); }
	*pan    {|key, value| bufferSynths[key].set(\pan, value); }
	*set    {|key...args| bufferSynths[key].set(*args)        } // for future expansion
	*volume {|newVolume | server.volume_(newVolume)           }

	*mute   { server.mute   }
	*unmute { server.unmute }

	*pause  {
		if (isPlaying) {
			isPlaying = false;
			this.mute;
			{ pauseSynth.set(\gate,0) }.defer(0.1);
		};
	}
	*resume {
		if (isPlaying.not) {
			isPlaying = true;
			pauseSynth.set(\gate,1);
			{this.unmute}.defer(0.1);
		};
	}
	*unpause{ this.resume }

	*volumeChannel{|channel,volume| }
	*releaseChannel{}
	*stopChannel{}

	// play a playlist interface /////////////////////////////////////////////////////////////////////////////////////////////

	// set up the playlist
	*setupPlaylist{|channel=\music, startDelay=0, minGap=0, maxGap=0, mode=0, loop=true|
		if (verbose) { "World_Audio:setupPlaylist".postln };
		playlistStates[channel][\startDelay] = startDelay;      // gap before starting the play list
		playlistStates[channel][\minGap]     = minGap;          // random minimum gap between items on the playlist
		playlistStates[channel][\maxGap]     = maxGap;          // random maximum gap between items on the playlist
		playlistStates[channel][\mode]       = mode;            // order items on the play list are played
		playlistStates[channel][\loop]       = loop;            // play 1 item at a time or loop
		if (playlistStates[channel][\timer].notNil) { playlistStates[channel][\timer].min_(minGap).max_(maxGap) };
	}

	// as above but release and clear other playlists 1st
	*newPlaylist{|channel=\music, startDelay=0, minGap=0, maxGap=0, mode=0, loop=true|
		this.releasePlaylist(channel);
		this.clearPlaylist(channel);
		this.setupPlaylist(channel, startDelay, minGap, maxGap, mode, loop);
	}

	// add a song to the play list (loop is not used)
	*addToPlaylist{|channel=\music, key, amp=1, fadeIn=0, fadeOut=1, rate=1, pan=0, loop=false|
		var args, buffer = buffers[key];
		if (buffer.isNil) { "Buffer % not found".format(key).warn; ^this};
		args   = IdentityDictionary[
			\key     -> key,     \bufnum -> buffer.bufnum, \amp -> amp, \fadeIn -> fadeIn,
			\fadeOut -> fadeOut, \rate   -> rate,          \pan -> pan, \channel  -> channel
		];
		playlists[channel] = playlists[channel].add(args);
	}

	// interrupt the playlist with a song not on the playlist
	*interruptPlaylist{|channel=\music, key, amp=1, fadeIn=0, fadeOut=1, rate=1, pan=0, loop=false|
		// TODO
	}

	// start playing the playlist
	*startPlaylist{|channel=\music, startIndex|
		var playlist = playlists[channel];
		var states   = playlistStates[channel];
		if (verbose) { "World_Audio:startPlaylist".postln };
		// exceptions
		if (states[\isPlaying] == true) { ^this }; // stop if already running
		if (playlist.size == 0) {"Playlist % empty, timer stopped".format(channel).warn; ^this }; // stop if empty
		// start playlist
		states[\isPlaying] = true;
		if (startIndex.notNil) { states[\index] = startIndex };
		states[\index] = states[\index] ? 0;
		// playlist timer
		states[\timer] = World_Playlist_Timer(states[ \minGap ], states[ \maxGap ], true, true).func_{|me|
			if (states[\index] >= playlist.size) { states[\index] = 0 };  // incase out of range or playlist size has changed
			if (playlist.notEmpty) {
				//  buffers[        below will be a problem if sound is not carried over scenes
				var duration = buffers[ playlist[states[\index]][\key] ].duration / playlist[states[\index]][\rate];
				me.remaining = me.remaining + duration;                   // inc timer by buffer duration
				this.performWithEnvir(\play, playlist[ states[\index] ]); // play song
				states[\lastItemPlayed] = states[\index];                 // store this for use in mode below
				if (verbose) { "playing: %, %".format(channel, playlist[ states[\index] ][\key]).postln};
				switch ( states[\mode],
					// 0 = play in order
					0, { states[\index] = (states[\index] + 1).wrap(0,playlist.size - 1) },
					// 1 = random order (repeats allowed)
					1, { states[\index] = ((playlist.size - 1).rand).wrap(0,playlist.size - 1) },
					// 2 = random order (no repeats)
					2, { states[\index] = (0 .. playlist.size - 1).reject{|index| index == states[\lastItemPlayed] }.choose;
						if (states[\index] == nil) { states[\index] = 0 } },
					// 3 = random order (every sound once) TODO
					3, {},
				);
				if (states[\loop].not) { states[\isPlaying] = false; me.stop }; // stop if not looping
			}{
				"Playlist % empty, timer stopped".format(channel).warn;
				states[\isPlaying] = false;
				me.stop; // stop the timer
			};
		}.remaining_( states[ \startDelay ] ); // initial delay before 1st item plays
	}

	// support functions
	*stopPlaylist    {|channel=\music| playlistStates[channel][\timer].stop;  playlistStates[channel][\isPlaying] = false; }
	*continuePlaylist{|channel=\music| playlistStates[channel][\timer].start; playlistStates[channel][\isPlaying] = true; }
	*clearPlaylist   {|channel=\music| playlists[channel].clear; playlistStates[channel][\index] = 0; }
	*releasePlaylist {|channel=\music|
		var lastIndex = playlistStates[channel][\lastItemPlayed];
		if ((lastIndex.notNil) and: {playlists[channel][lastIndex].notNil}) {
			this.release( playlists[channel][lastIndex][\key] )
		};
		playlistStates[channel][\timer].stop;
		playlistStates[channel][\isPlaying] = false;
	}

	// skip to next item
	*skipPlaylist{|channel=\music|
		var lastIndex = playlistStates[channel][\lastItemPlayed];
		if (lastIndex.notNil) {
			this.release( playlists[channel][lastIndex][\key] );
			playlistStates[channel][\timer].remaining_(0);
			this.continuePlaylist(channel);
		};
	}

	// cue item (move index in playlist so next item played is index)
	*cuePlaylist{|channel=\music, index| playlistStates[channel][\index] = index }

	// stop last sound and start playing index
	*cueNowPlaylist{|channel=\music, index|
		var lastIndex = playlistStates[channel][\lastItemPlayed];
		if (lastIndex.notNil) {
			this.release( playlists[channel][lastIndex][\key] );
			playlistStates[channel][\index] = index;
			playlistStates[channel][\timer].remaining_(0);
			this.continuePlaylist(channel);
		};
	}

	// stop and clear everything
	*stopAllPlaylists   { channelNames.do{|name| this.stopPlaylist (name) } }
	*clearAllPlaylists  { channelNames.do{|name| this.clearPlaylist(name) } }
    *releaseAllPlaylists{ channelNames.do{|name| this.releasePlaylist(name) } }
	*releaseEverything  { this.releaseAllPlaylists; this.releaseAll }
	*stopEverything     { this.stopAll; this.releaseAllPlaylists; }

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                   Timer for use in Playlists                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Playlist_Timer : World_World {

	var <>total     = inf;    // total duration of timer in seconds
	var <>remaining = inf;    // seconds remaining before timer does action
	var <isRunning  = true;   // is the timer running
	var <>loop      = false;  // loop lets the timer repeat
	var <>min       = 1;      // min gap between songs
	var <>max       = 1;      // max gap between songs
	var <>func;               // func to call

	*new{|min,max,isRunning=true,loop=false|
		if (isRunning) { ^super.new.initOn(min,max,loop) }{ ^super.new.initOff(min,max,loop) };
	}

	initOn{|argMin,argMax,argLoop|
		min         = argMin;
		max         = argMax;
		isRunning   = true;
		remaining   = min.rrand(max);
 		total       = remaining;
		loop        = argLoop;
		worldTimers = worldTimers.add(this);
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
		remaining = remaining - staticFrameLength; // use real time instead
		if (remaining<=0) {
			if (loop) {
				remaining = min.rrand(max);
				total     = remaining;
			}{
				isRunning = false;
				remaining = 0;
				garbage   = garbage.add(this);
			};
			func.value(this);
		};
	}

	start{
		if (isRunning.not) {
			isRunning   = true;
			worldTimers = worldTimers.add(this); // add this to worldTimers
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
			worldTimers = worldTimers.add(this); // add this to worldTimers
			isRunning   = true;
			garbage     = garbage.remove(this);
		};
		remaining = total;
	}

	pause{ this.stop }

	frac{ ^remaining / total }

	fracRemaining{ ^ 1 - (remaining / total) }

	free{ worldTimers.remove(this) }

}

/*
World_Audio.play(\pop);
World_Audio.play(\pop, 0.5, 2, 2, 1, 0, true);
World_Audio.pause;
World_Audio.resume;
World_Audio.volume(-6);
World_Audio.volume(0);
World_Audio.rate(\pop, 8 / (2**(4.0.rand)));
World_Audio.pan(\pop, 1.0.rand2);
World_Audio.release(\pop);
World_Audio.releaseAll;
World_Audio.stopAll;
World_Audio.play(\song8, rate:-1, loop:true);
World_Audio.stop(\wind);
World_Audio.setupPlaylist(\fx,0,0,0,0,true); // 5 sec start, then a gap of 20-40 secs
World_Audio.addToPlaylist(\fx, \pop);
World_Audio.addToPlaylist(\fx, \pop , rate:1.75.rand);
World_Audio.addToPlaylist(\fx, \pickup);
World_Audio.addToPlaylist(\fx, \pickup, rate:0.75);
World_Audio.startPlaylist(\fx,0);
World_Audio.stopPlaylist(\fx);
World_Audio.continuePlaylist(\fx);
World_Audio.clearPlaylist(\fx);
World_Audio.skipPlaylist(\music);
World_Audio.cueNowPlaylist(\music,15.rand);
World_Audio.stopAllPlaylists;
World_Audio.releaseAllPlaylists;
World_Audio.releaseEverything;
World_Audio.stopEverything;
*/
