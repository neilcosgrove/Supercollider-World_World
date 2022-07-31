////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                     Keyboard Layout                                                //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// i'v not tried this on PC or Linux yet so these maybe mapped to something completely useless
// feel free to change these to what you want or expand the idea to other platforms or keyboards from different countries
// let me know if you do changes for Linux or PC and I'll pop it in my main branch
/*
ATM the Keys on a UK Mac are...
WASD = movement
space = jump
return = fire
; = wall grab
escape = pause
*/

World_Key {
	var <char, <modifiers, <unicode, <keycode, <key;
	*new{|char, modifiers, unicode, keycode, key|
		Platform.case(
			\osx,     { ^World_Mac_Key  (char, modifiers, unicode, keycode, key) },
			\linux,   { ^World_PC_Key   (char, modifiers, unicode, keycode, key) },
			\windows, { ^World_Linux_Key(char, modifiers, unicode, keycode, key) }
		);
	}
	printOn { arg stream;
		stream << this.class.name << "( " << char.asCompileString << ", " << modifiers << ", "
		       << unicode << ", " << keycode << ", " << key << " )";
	}
	storeArgs { ^[char, modifiers, unicode, keycode, key] }
}

// these are the keys i've set up for my UK Mac keyboard
World_Mac_Key : World_Key {
	*new{|char, modifiers, unicode, keycode, key| ^super.newCopyArgs(char, modifiers, unicode, keycode, key) }
	// user keys
	isFullScreen      { ^(key==70) && (modifiers==1048576) } // cmd + f
	isScreenShot_png  { ^(key==83) && (modifiers==1179648) } // cmd + shift + s
	isScreenShot_jpg  { ^(key==83) && (modifiers==1048576) } // cmd + s
	isSwitchPlayer    { ^keycode==57 } // caps lock = switch keyboard control between players[0] & players[1]
	isQuitSCLang      { ^(key==81) && (modifiers==1048576 ) } // cmd + q
	isQuitGame        { ^key == 81 } // q
	isPause           { ^key == 16777216 } // escape
	// scene specific keys - Space Jumper Game
	isSaveTileMap     { ^(key==83) && (modifiers==1048576) } // cmd + s
	isBuildNewScene   { ^key == 16777235 } // up arrow
	isIncScene        { ^key == 16777234 } // right arrow
	isDecScene	      { ^key == 16777236 } // left arrow
	// dev keys
	isVerbose         { ^char==$? } // ? = toggle verbose mode
	isAntiAliasing    { ^(key==65) && (modifiers==1048576) } // cmd + a
	isFPS             { ^key==96 } // ` = toggle fps from 60 to 120
	// debug keys
	isDebugMode       { ^(key==68) && (modifiers==1048576) } // cmd + d
	isIncTimeDilation { ^key == 16777235 } // up arrow
	isDecTimeDilation { ^key == 16777237 } // down arrow
	isIncMotionBlur   { ^key == 16777236 } // left arrow
	isDecMotionBlur   { ^key == 16777234 } // right arrow
	// tile editor keys
	isTileEditorOnOff { ^(key==84) && (modifiers==1048576) } // cmd + t
	isZoomIn          { ^key==61 } // = zoom in
	isZoomOut         { ^key==45 } // - zoom out
	isDecTileType     { ^key==49 } // 1 select previous tile type to draw
	isIncTileType     { ^key==50 } // 2 select next tile type to draw
	isDeleteTile      { ^key==16777219 } // backspace deletes selected tile
	isIncTileWidth    { ^key==16777236 } // right arrow
	isDecTileWidth    { ^key==16777234 } // left arrow
	isIncTileHeight   { ^key==16777237 } // down arrow
	isDecTileHeight   { ^key==16777235 } // up arrow
}

World_PC_Key : World_Mac_Key {
	*new{|char, modifiers, unicode, keycode, key| ^super.newCopyArgs(char, modifiers, unicode, keycode, key) }
	// override or add any changes here that are specific to a PC keyboard
}

World_Linux_Key : World_Mac_Key {
	*new{|char, modifiers, unicode, keycode, key| ^super.newCopyArgs(char, modifiers, unicode, keycode, key) }
	// override or add any changes here that are specific to a Linux keyboard
}

+ World_World {

	*addKeyboardControls{
		var platformMap, keysPressed = IdentitySet[];
		// change these to whats appropriate for you, platformMap is key from keyboard mapped to each PS controller element
		Platform.case(
			\osx, {
				platformMap = IdentityDictionary[
					\leftJoyLeft  -> 65, \leftJoyRight  -> 68, \leftJoyUp  -> 87, \leftJoyDown  -> 83,
					\rightJoyLeft -> 74, \rightJoyRight -> 76, \rightJoyUp -> 73, \rightJoyDown -> 75,
					\x            -> 32, \square        -> 44, \triangle   -> 46, \cirlce       -> 47,
					\l1           -> 59, \l2            -> 39, \r1         -> 92, \r2           -> 16777220,
					\share        -> 79, \trackPad      -> 80, \options    -> 91, \psButton     -> 93,
					\dPadUp -> 16777235, \dPadDown -> 16777237, \dPadLeft -> 16777234, \dPadRight -> 16777236
				];
				/* which on my mac uk keyboard is set up as... (typical WASD for movement, space is x and return is r2)
			        \leftJoyLeft  -> a,     \leftJoyRight  -> d,     \leftJoyUp    -> w,      \leftJoyDown  -> s,
					\rightJoyLeft -> j,     \rightJoyRight -> l,     \rightJoyUp   -> i,      \rightJoyDown -> k,
					\x            -> space, \square        -> comma, \triangle     -> period, \cirlce       -> /,
				    \l1           -> ;,     \l2            -> ',     \r1           -> \,      \r2           -> return,
				    \share        -> o,     \trackPad      -> p,     \options      -> [,      \psButton     -> ],
					\dPadUp -> up arrow, \dPadDown -> down arrow, \dPadLeft -> left arrow , \dPadRight -> right arrow
				*/
			},
			// these might be wrong, i've not tried this on linux
			\linux, {
				platformMap = IdentityDictionary[
					\leftJoyLeft  -> 65, \leftJoyRight  -> 68, \leftJoyUp  -> 87, \leftJoyDown  -> 83,
					\rightJoyLeft -> 74, \rightJoyRight -> 76, \rightJoyUp -> 73, \rightJoyDown -> 75,
					\x            -> 32, \square        -> 44, \triangle   -> 46, \cirlce       -> 47,
					\l1           -> 59, \l2            -> 39, \r1         -> 92, \r2           -> 16777220,
					\share        -> 79, \trackPad      -> 80, \options    -> 91, \psButton     -> 93,
					\dPadUp -> 16777235, \dPadDown -> 16777237, \dPadLeft -> 16777234, \dPadRight -> 16777236
				];
			},
			// these might be wrong, i've not tried this on PC
			\windows, {
				platformMap = IdentityDictionary[
					\leftJoyLeft  -> 65, \leftJoyRight  -> 68, \leftJoyUp  -> 87, \leftJoyDown  -> 83,
					\rightJoyLeft -> 74, \rightJoyRight -> 76, \rightJoyUp -> 73, \rightJoyDown -> 75,
					\x            -> 32, \square        -> 44, \triangle   -> 46, \cirlce       -> 47,
					\l1           -> 59, \l2            -> 39, \r1         -> 92, \r2           -> 16777220,
					\share        -> 79, \trackPad      -> 80, \options    -> 91, \psButton     -> 93,
					\dPadUp -> 16777235, \dPadDown -> 16777237, \dPadLeft -> 16777234, \dPadRight -> 16777236
				];
			}
		);

		// platformMap[element key] -> [deviceNum, controllerIndex, keyDownValue, keyUpValue, tieToKey]
		// tieToKey = if both dpad left + right or up & down are pressed & 1 is lifted then the other reactives
		keyboardMap = IdentityDictionary[
			platformMap[\leftJoyLeft  ] -> [0, 14, 0, 0.5, 68],  // a > [14] L Joy L-R left
			platformMap[\leftJoyRight ] -> [0, 14, 1, 0.5, 65],  // d > [14] L Joy L-R right
			platformMap[\leftJoyUp    ] -> [0, 15, 1, 0.5, 83],  // w > [15] L Joy U-D Up
			platformMap[\leftJoyDown  ] -> [0, 15, 0, 0.5, 87],  // s > [15] L Joy U-D  Down
			platformMap[\rightJoyLeft ] -> [0, 16, 0, 0.5, 76],  // j > [16] R Joy L-R left
			platformMap[\rightJoyRight] -> [0, 16, 1, 0.5, 74],  // l > [16] R Joy L-R right
			platformMap[\rightJoyUp   ] -> [0, 17, 1, 0.5, 75],  // i > [17] R Joy U-D Up
			platformMap[\rightJoyDown ] -> [0, 17, 0, 0.5, 73],  // k > [17] R Joy U-D  Down
			platformMap[\x            ] -> [0,  1, 1, 0      ],  // space > [1] X
			platformMap[\square       ] -> [0,  0, 1, 0      ],  // , > [0]   square
			platformMap[\triangle     ] -> [0,  3, 1, 0      ],  // . > [3]   Triangle
			platformMap[\cirlce       ] -> [0,  2, 1, 0      ],  // / > [2]   O
			platformMap[\l1           ] -> [0,  4, 1, 0      ],  // ; > [4]   L1 (on/off)
			platformMap[\l2           ] -> [0,  6, 1, 0      ],  // ' > [6]   L2 (pos)
			platformMap[\r1           ] -> [0,  5, 1, 0      ],  // \ > [5]   R1 (on/off)
			platformMap[\r2           ] -> [0,  7, 1, 0      ],  // return > [7]   R2 (pos)
			platformMap[\share        ] -> [0,  8, 1, 0      ],  // o > [8]   Share
			platformMap[\trackPad     ] -> [0, 13, 1, 0      ],  // p > [13]  Track PAD button
			platformMap[\options      ] -> [0,  9, 1, 0      ],  // [ > [9]   Options
			platformMap[\psButton     ] -> [0,  4, 1, 0      ],  // ] > [12]  PS button
			platformMap[\dPadUp       ] -> [0, 20, 1, 0      ],  // up > [20]  DPad UP
			platformMap[\dPadDown     ] -> [0, 21, 1, 0      ],  // down > [21]  DPad DOWN
			platformMap[\dPadLeft     ] -> [0, 18, 1, 0      ],  // left > [18]  DPad LEFT
			platformMap[\dPadRight    ] -> [0, 19, 1, 0      ],  // right > [19]  DPad RIGHT
		];
		// key down
		userView.keyDownAction_{|me, char, modifiers, unicode, keycode, key|
			var keyPress = World_Key(char, modifiers, unicode, keycode, key);
			World_World.keyDown(keyPress);
			World_Scene.sceneScripts[\keyDown].value(keyPress);
			if (releaseMode.not) {
				World_Tile_Map.keyDown(keyPress);
				World_Debug.keyDown(keyPress);
			};
			if (players.size<=1) { capsLock = 0 } { if (keyPress.isSwitchPlayer) { capsLock = 1 } }; // switch players
			if (keyboardMap.includesKey(key)) {
				var device = keyboardMap[key][0] + capsLock;
				var index  = keyboardMap[key][1];
				var value  = keyboardMap[key][2];
				if (controllerState[device, index] != value) {
					World_Controller_Patch.externalIn(device, index, value);
					keysPressed = keysPressed.add(key);
					keysPressed;
				};
			};
		};
		// key up
		userView.keyUpAction_{|me, char, modifiers, unicode, keycode, key|
			var keyPress = World_Key(char, modifiers, unicode, keycode, key);
			World_World.keyUp(keyPress);
			World_Scene.sceneScripts[\keyUp].value(keyPress);
			if (keyPress.isSwitchPlayer) { capsLock = 0 }; // switch players
			if (keyboardMap.includesKey(key)) {
				var device = keyboardMap[key][0] + capsLock;
				var index  = keyboardMap[key][1];
				var value  = keyboardMap[key][3];
				keysPressed.remove(key);
				World_Controller_Patch.externalIn(device, index, value);
				if (keyboardMap[key][4].notNil) {
					var newKey   = keyboardMap[key][4];
					var device   = keyboardMap[newKey][0] + capsLock;
					var newIndex = keyboardMap[newKey][1];
					var newValue = keyboardMap[newKey][2];
					if (keysPressed.includes(newKey)) { World_Controller_Patch.externalIn(device, newIndex, newValue) };
				};
			};
		};
	}
}
