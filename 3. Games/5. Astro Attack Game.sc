////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                        Astro Attack scene                                          //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// A scene is the space, containers & scripts in which a level or game and anything in it can be built and run.
// This is where we build the scenes, its entities & define their assets, tile sets, scripts, gameLoops, HUD etc..
// What to do when the engine starts, what happens when you start a new game, a new level, or what to do when its game over
// A Scene doesn't need to have Assets, Tile Sets, Light Maps, Scripts or a HUD but typically does.
//
/////////////////////////
// Astro Attack Assets //
/////////////////////////

+ World_Assets {
	// All asset method names must end with "_ASSET_DEF". Any methods with this prefix get called during Asset class init
	// This is because the assets are needed before the scene is created and before the engine is allowed to start
	astroAttack_ASSET_DEF{
		imagePaths = IdentityDictionary[
			\space         -> "backgrounds/space.jpg",
			// sprites
			\redAlien      -> "sprites/red ship.png",
			\redAlienHit   -> "sprites/red ship hit.png",
			\redAlienB1    -> "sprites/red ship blink 1.png",
			\redAlienB2    -> "sprites/red ship blink 2.png",
			\redAlienB3    -> "sprites/red ship blink 3.png",
			\greenAlien    -> "sprites/green ship.png",
			\greenAlienHit -> "sprites/green ship hit.png",
			\greenAlienB1  -> "sprites/green ship blink 1.png",
			\missile       -> "sprites/missile.png",
			\bubble        -> "sprites/bubble.png",
			\oneUp         -> "sprites/1UP.png",
			\ship2         -> "sprites/ship 1 fire.png",
			\star          -> "sprites/Yellow sphere.png",
			\powerUp       -> "sprites/powerUp.png",
			\powerUp2      -> "sprites/powerUp2.png",
			\shieldUp      -> "sprites/Shield Up.png",
			// rocks
			\rock1         -> "sprites/rocks/1.png",
			\rock2         -> "sprites/rocks/2.png",
			\rock3         -> "sprites/rocks/3.png",
			\rock4         -> "sprites/rocks/4.png",
			\rock5         -> "sprites/rocks/5.png",
			\rock6         -> "sprites/rocks/6.png",
			\rock7         -> "sprites/rocks/7.png",
		];
		bufferPaths = IdentityDictionary[
			\thruster -> "gas loop.ogg",
			\pop      -> "expSpace.ogg",
			\pop3     -> "pop.ogg",
			\pop2     -> "pop short.ogg",
			\laser    -> "lazer.ogg",
			\laser2   -> "laser.ogg",
			\explode  -> "exp2.ogg",
			\zap      -> "zap1.ogg",
			\bubble   -> "nice pop.ogg",
			\pickup   -> "1up.ogg",
			\newWave  -> "new wave.ogg"
		];
	}
}

////////////////////////////////
// Astro Attack Scene Builder //
////////////////////////////////

+ World_Scene {

	*buildAstroAttackScene{
		var enemies = [World_Shooter_NPC, World_Shooter2_NPC, World_Ranged_NPC_Type3_Bullet];
		World_Scene.setupScenePX(1280, 800, 40, 40, 1, \astroAttack_ASSET_DEF).addStars(200,1);
		sceneState[\background    ] = \space;
		sceneState[\backgroundMode] = \scroll;
		sceneState[\backgroundDY  ] = 1.5;
		sceneState[\renderLoop    ] = \renderLoopShooter;
		sceneState[\gameOverLoop  ] = \gameOverLoopAltVersion;
		sceneState[\hud           ] = \astroAttack_HUD;

		World_Shooter_Player(0, centrePoint.copy + (0@720), 9, 40, enemies, \missile, \explode, nil, \zap, \bubble);

		// called when the game starts
		sceneScripts[\startGame ] = {
			var saveState = "AstroAttack".loadState ? (); // if you use an Event or IdentDic you can easy expand on this method
			worldState[\highScore ] = saveState[ \highScore ] ? 0; // here we are just getting a high score
			worldState[\lives     ] = 5;
			worldState[\score     ] = 0;
			worldState[\hasShield ] = false;
			worldState[\wave      ] = 0;
			World_Camera.reset;
			World_Audio.play(\thruster, 0.15, 4, 2, 1, 0, true, \fx);
			World_Message.backgroundColor_(Color(1,1,1,0.7)).textColor_(Color.black);
		};

		// called when the scene starts
		sceneScripts[\startScene ] = {
			{
				// intro
				if(skipIntro.not){
					players[0][\controller].active = false;
					["ASTRO ATTACK","Wave 1"].message([5,3.2],[0,0]);
					{World_Audio.play(\newWave)}.deferInScene(5);
					500.do{|i| players[0][\drawFunc].radius_( (i/499**2).map(0,1,3000,40)); (1/60).wait; };
				};
				players[0][\controller].active = true;
				// spawn NPCs
				inf.do{|wave|
					var time, numShips, numWave, speed, bulletFreq, boss;
					#time, numShips, numWave, speed, bulletFreq, boss = [
						// wave difficulty settings
						[2.5, wave+1,  4, 2, 1.5,  0],
						[2,   wave+1,  8, 3, 1.25, 1],
						[1.5, wave+1, 12, 4, 1,    2],
						[1,   wave+1, 16, 5, 0.9,  3],
						[0.5, wave+1, 24, 6, 0.8,  4],
					].clipAt(worldState[\wave]);
					numWave.do{|i|
						var randPoint = Point(worldWidth.rand, -40);
						numShips.do{
							World_Shooter_NPC( randPoint.copy, 11, \redAlien, \redAlienHit, \pop, \pop2,
								[\redAlienB1, \redAlienB2, \redAlienB3], \laser, Color(0.4,0.4,0.5), bulletFreq: bulletFreq )
							.velocity += (0@speed);
							(0.3 / (speed/5)).wait;
						};
						time.wait;
					};
					// boss fight
					boss.do{|i|
						World_Shooter2_NPC( Point((worldWidth-120)/(boss+1) * (i+1) + 60, -40), 11,
							\greenAlien, \greenAlienHit, \pop, \pop2,
							[\greenAlienB1], \laser2, Color(0.4,0.4,0.5), bulletFreq: boss.map(1,4,0.5,0.3) )
						.velocity += (0@ boss.map(1,4,4,2));
					};
					(18 / boss.map(1,4,4,2)).wait;
					worldState[\wave] = worldState[\wave] + 1;
					("Wave "++(worldState[\wave]+1)).message(3,0);
					World_Audio.play(\newWave);
					3.wait
				};
			}.forkInScene;
			// drop items
			{
				(20.0.rrand(40)).wait;
				inf.do{
					0.6.coin.if {
						0.5.coin.if {
							World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10,\bubble, \bubble,
								World_Shield_Property, \none);
						}{
							World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10,\shieldUp, \pickup,
								World_HasShield_Property, \none);
						}
					}{
						World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10, \oneUp, \pickup, World_1UP_Property);
					};
					(20.0.rrand(40)).wait;
				};
			}.forkInScene;
			{
				10.wait;
				inf.do{
					0.33.coin.if {
						World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10, \star, \pop3, World_1Point_Property);
					}{
						0.5.coin.if {
							World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10, \powerUp, \pop3,
								World_ClipSizeMul_Property);
						}{
							World_Shooter_Item.newXY((worldWidth-120).rand+60,-40,10, \powerUp2, \pop3,
								World_RateOfFire_Property);
						};
					};
					1.0.rrand(3.0).wait;
				};
			}.forkInScene;
			// rocks (decor)
			{
				inf.do{
					World_Background_Sprite( (worldWidth-120).rand+60 @ -40, 0,
						[\rock1,\rock2,\rock3,\rock4,\rock5,\rock6,\rock7].choose, Vector(0,2.rrand(3)));
					(worldState[\wave].map(0,6,4,0.6).clip(0.6,4) * 0.3.rrand(1.3)).wait;
				};
			}.forkInScene;
		};

		// volume of thruster
		sceneScripts[\tick] = {
			if ( players[0].notNil) {
				World_Audio.amp(\thruster, players[0].isAlive.if {players[0][\mechanics].velocity.rho * 0.01 + 0.1 } {0} );
			};
		};

		// key down
		sceneScripts[\keyDown] = {|key|
			if (key.isPause) { World_World.tooglePause }; // pause
			if ((key.isQuitGame) && (isPlaying.not)) { World_Audio.stopEverything; World_World.quitGame }; // quit
		};

		// plyer death
		sceneScripts[\playerDeath] = {|id|
			{
				worldState[\lives] = worldState[\lives] - 1;
				(2/60).wait;
				100.do{|i|
					timeDilation = timeDilation * 0.99; // slow down time
					0.025.wait;
				};
				if (worldState[\lives] > 0) {
					World_Shooter_Player(0, centrePoint.copy += (0@720), 9, 40, enemies,
						\missile, \explode, nil,  \zap, \bubble).invulnerable_(true).doRetrigger;
					timeDilation = 1;
				}{
					World_World.gameOver; // also triggers sceneScripts[\gameOver] below
				};
			}.forkInScene(false);
		};
		// game over
		sceneScripts[\gameOver] = {
			World_Audio.releaseEverything;
			if ( worldState[\score] > worldState[\highScore] ) {
				worldState[\highScore] = worldState[\score];
				(highScore:worldState[\highScore]).saveState("AstroAttack"); // this is all you need to do to save
			};
			{
				5.wait;
				World_Camera.fadeToBlack(1);
				1.wait;
				World_World.quitGame;
			}.forkInScene(false);
		};

	}
}


//////////////////////
// Astro Attack HUD //
//////////////////////

+ World_HUD {

	*astroAttack_HUD{
		Pen.use{
			var string = ("SCORE : "++worldState[\score]).asString;
			Pen.scale(globalZoom,globalZoom);
			Pen.stringCenteredIn(string,
				tempRect.replaceLTWB(0, 0 / globalZoom, screenWidth, 60 / globalZoom),
				hudFont, white
			);
			string = ("HIGH SCORE : "++worldState[\highScore]).asString;
			Pen.scale(globalZoom,globalZoom);
			Pen.stringRightJustIn(string,
				tempRect.replaceLTWB(0, 0 / globalZoom, screenWidth, 60 / globalZoom),
				hudFont, white
			);
			Pen.use{
				Pen.scale(0.45,0.45);
				worldState[\lives].do{|i|
					Pen.prDrawImage(Point( (42 + (100*i) ) / globalZoom,  (12 * 3) / globalZoom), images[\ship2], nil, 0, 1.0);
				};

				if (worldState[\hasShield]==true) {
					Pen.prDrawImage(Point( worldWidth * 0.5 / 0.45,  worldHeight - 32 / 0.45), images[\shieldUp], nil, 0, 1.0);
				};

			};
		};
	}

}
