////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Messages & Subtitles                                            //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// use the shortcuts at the bottom of this document
/*
"Hello world!".message;
["Still here", "Bye bye\nworld!"].message([1,3],[0.05,0.1]);
"Get to the Choppa".message(3,0.1,nil,true,"Dutch : ");
*/

World_Message : World_World {

	classvar <>dx = 0,          <>dy = -150,      <>dxSub = 0,         <>dySub = -45;
	classvar <>backgroundColor, <>textColor,      <>font,              <isOn = false;
	classvar <messages,         <durations,       <typingSounds,       <subtitles;
	classvar <currentMessage,   <currentDuration, <currentTypingSound, <prefixes;
	classvar <currentSubtitle,  <currentBounds,   <currentPrefix,      <timer;
	classvar <>spaceDoublesDuration = true;

	*init{
		if (verbose) { "World_Message:init".postln };
		backgroundColor = Color(0,0,0,0.66);
		textColor       = Color.white;
		font            = Font("Geomancy", 30 * globalZoom, true);
		messages        = [];
		durations       = [];
		typingSounds    = [];
		subtitles       = [];
		prefixes        = [];
	}

	*reset{
		backgroundColor = Color(0,0,0,0.66);
		textColor       = Color.white;
		font            = Font("Geomancy", 30 * globalZoom, true);
	}

	// add a message to the queue
	*message{|text, duration = 3, typingSound, subtitle = false, prefix|
		messages     = messages.add(text.asString);
		durations    = durations.add(duration);
		typingSounds = typingSounds.add(typingSound);
		subtitles    = subtitles.add(subtitle);
		prefixes     = prefixes.add(prefix);
		this.startTimer;
	}

	// the timer used to display the messages & subtitles
	*startTimer{
		if (isOn.not) {
			isOn = true;
			timer = {
				while ( {messages.size>0 }, {
					currentMessage     = messages[0];
					messages           = messages.drop(1);
					currentPrefix      = prefixes[0];
					prefixes           = prefixes.drop(1);
					if (currentPrefix.notNil) { currentMessage = currentPrefix ++ currentMessage };
					currentDuration    = durations[0];
					durations          = durations.drop(1);
					currentTypingSound = typingSounds[0];
					typingSounds       = typingSounds.drop(1);
					currentSubtitle    = subtitles[0];
					subtitles          = subtitles.drop(1);
					currentBounds      = currentMessage.realBounds(font);
					if (currentSubtitle) {
						currentBounds = Rect.aboutPoint(
							(halfScreenWidth @ (screenHeight - currentBounds.height)) + ((dxSub@dySub) * globalZoom),
							currentBounds.width/2+30, currentBounds.height/2+30);
					}{
						currentBounds = Rect.aboutPoint( screenCentre + ((dx@dy) * globalZoom),
							currentBounds.width/2+30, currentBounds.height/2+30);
					};
					if (currentTypingSound.notNil) {
						if (currentMessage.last!=$ ) {World_Audio.play(currentTypingSound, 1, channel: \dialog) };
					};
					if ((currentMessage.last==$ ) && spaceDoublesDuration)
					    { (2*currentDuration).wait } { currentDuration.wait };
				});
				isOn               = false;
				currentMessage     = nil;
				currentDuration    = nil;
				currentTypingSound = nil;
				subtitles          = nil;
				prefixes           = nil;
			}.forkInWorld(false);
		}
	}

	// stop all messages & subtitless
	*clearAll{
		isOn            = false;
		currentMessage  = nil;
		currentDuration = nil;
		messages        = [];
		durations       = [];
		typingSounds    = [];
		subtitles       = [];
		prefixes        = [];
		if (timer.notNil) { timer.stop };
	}

	*draw{
		if (isOn) {
			Pen.fillColor_(backgroundColor).roundedRect(currentBounds,15,15).draw(0);
			Pen.stringCenteredIn(currentMessage, currentBounds, font, textColor);
		};
	}

}

// shortcuts (use these)

// you can add mutliple messages & subtitles to the queue with Collection:message

+ Collection {

	subtitle{|duration = 3, typingSpeed = 0.1, typingSound, prefix|
		this.message(duration, typingSpeed, typingSound, true, prefix)
	}

	message {|duration = 3, typingSpeed = 0.1, typingSound, subtitle = false, prefix|
		// you can supply a list of values
		duration    = [] ++ duration;
		typingSpeed = [] ++ typingSpeed;
		typingSound = [] ++ typingSound;
		subtitle    = [] ++ subtitle;
		prefix      = [] ++ prefix;
		this.do{|item,i| item.message(duration.wrapAt(i), typingSpeed.wrapAt(i), typingSound.wrapAt(i), subtitle.wrapAt(i),
			prefix.wrapAt(i))
		};
	}

}

// or just add 1 message with String:message

+ String {

	subtitle{|duration = 3, typingSpeed = 0.1, typingSound, prefix|
		this.message(duration, typingSpeed, typingSound, true, prefix)
	}

	message {|duration = 3, typingSpeed = 0.1, typingSound, subtitle = false, prefix|
		if (typingSpeed<=0) {
			World_Message.message (this, duration, typingSound, subtitle, prefix)
		}{
			this.size.do{|i|
				World_Message.message (this[..i], ((i+1)==this.size).if( duration,typingSpeed), typingSound, subtitle, prefix)
			};
		};
	}

}

