"
Supercollider World_World Glossary
----------------------------------
"
// Glossary of some relevant words. All objects have the prefix World_.

Assets:
a class that manages the loading and access to a collection of Images and Buffers. Each scene has its own set of assets defined using a World_Assets method name with the postfix of _ASSET_DEF. Currently all assets are loaded when the engine starts.

Audio:
an audio player that manages the playback of buffers found in a Scene's assets. Includes the ability to make playlists.

boundingBox:
the smallest Rect that contains all the components of an entity. This axis-aligned minimum bounding box is also know as an AABB. These rectangles are used as an approximate location of an object and help speed up many calculations. They are also used to draw entities to the screen. The bounding box of an entity is used like a proxies so never replace them with new objects. Only ever do in-place operations on them. If you make a new Rect for boundingBox you will break your code.

Camera:
a class used to control what part of the world a player can see. It includes methods for zooming, panning, rotating, shaking and transitions such as fade to black.

Collider: // (Collision detection & response)
a component that handles the collision between 2 Entities. Typically split into two stages - detection and response. For example, we might detect a collision between two circles and as a response they bounce off each other in a perfectly elastic collision. If you are feeling adventurous or just curious, World_Super_Collider and it subclasses are where you'll find all the maths.
Colliders can be set up as either Sources, Responders or both. Sources are typically fixed (i.e. tiles) and Responders are the things that move (i.e. a player). This way the player entity only has to check which Sources its close to and do collision detection on them. Some things might be both a Source & a Responder, like NPCs, which can bounce off each other.
Sources & Responders are further divided into types. Currently collisionTypes = #[\tiles, \solids, \npcs, \items, \npcBullets]
The UGP, Sources, Responders and Collision Types all help to reduce the number of collision detections needed every frame. Collision detections can become very cpu expensive if not handled properly. Currently the engine supports collisions between Circles, Rectangles, Right Angled Triangles & Lines. I've not finished coding every interaction yet but Circle vs. any other shape is complete.

Component:
an entities behaviour is broken into separate Components, such as Mechanics, Colliders, Controllers, Clocks, Timers, Health, Death, Item Properties, Draw Functions and many more. They contain both the data of a system and methods that operate on them. Since you can't do memory mapping in Supercollider I combined both the Component & System part of an ECS into 1 thing.

Controllers:
a component that maps input from a HID device such as a P.S.5 controller or a keyboard onto other components.

controllerState:
a MultiLevelIdentityDictionary that stores the last input from a controller so that it can be looked up at a later stage. Sometimes you need to ask if a button is currently being held down and this will tell you.

Clocks & Timers:
are ways of setting tasks to happen in the future. Clocks repeat and Timers are one-off events. NEVER defer, fork, schedule, Routine or Task anything. There is no guarantee the scene or world will be the same or even running when your function yields. Use {}.deferInScene {}.deferInWorld {}.schedInScene {}.schedInWorld {}.forkInScene {}.forkInWorld, they are designed with scripts in mind. Or you could use one of the Timer components instead.

Debug Scene:
the scene that automatically opens when the debugMode flag is set to true. Use this scene to test, try out and debug your code.

DrawFunc:
a component that tells the graphics card how to draw the entity onto the screen. They get called as needed by the renderLoop. Here the function is actually a method called drawFunc inside of which you can use Pen to do all the drawing.

Dropped Frames:
Both the gameLoop & renderLoop are called once every frame but if the scene is particularly demanding and can't be drawn in time then you may get frames that are dropped. This is why sometimes frame isn't the same as guiFrame

Entity:
an object in the world, such as an item, a character or vehicle. They all have 3 things, an origin (Point), a boundingBox (Rect) and a collection of components which define the object's behaviour.

frame:
the current frame of the game loop. This can be different to guiFrame because frames can be dropped.

frameLength:
the time between frames in seconds. 60 fps gives you a frame length of 1/60 seconds. frameLength is effected by timeDilation. If fps = 60 & timeDilation = 0.5 then frameLength = 1/120 seconds. Use staticFrameLength if you want to exclude the effect of timeDilation in your calculations.

frameRate: // (fps)
the number of frames per second. 60 fps is typically used.

gameLoop:
a symbol with the method name of the game loop to be called every frame. The game loop runs on the SystemClock and ticks at frameRate. The systemClockLoop can be found in World_World:startSystemClockLoop

Heads Up Display: // (HUD)
the HUD is the method by which information is visually relayed to the player as part of a games interface. The HUD is frequently used to display several pieces of information including the main character's health, items, and an indication of game progression (such as score or level). Each scene can have its own HUD, you do this by setting it in the scene state.
For example sceneState[\hud] = \alienMaze_HUD; // will call World_HUD:alienMaze_HUD every frame.

Heat Maps:
are ways of generating repeatable random numbers in either 1 or 2 dimensions. Values can be interpolated. Here are some examples.
(1,1.1..10).collect(_.heatMap).plot;
(1,1.1..10).collect(_.heatMapSine).plot;
// Here we are using a 2D heat map to generate random terrain in an Image
i = Image.color(1200, 600, Color.black);
{|x,y,val| i.setColor( Color.grey(val), x, y) }.heatMapSine2D(i.width, i.height, 0.03, 0.045, 0.6, 1);
i.plot;

layer:
the order in which entities are drawn to the screen is managed in layers. layers[0] is drawn 1st then layers[1] etc. You can set how many layers you have with World_World:numLayers.

LightMap:
simulates lighting in your scene. The light map is split up into cells the same size as the tile map & UGP. There are 2 types of light source, static & dynamic. A static light source only needs to be added once and remains on, useful to illuminate specific areas of the scene. Dynamic light sources need to be added every frame and are useful for moving things such as adding a light source to the player.

Mechanics:
a component that makes things move. The main one is World_Classical_Mechanics which lets you control velocity, maxVelocity, acceleration, friction, gravity & angularVelocity. There are simpler ones if you don't need all this. This is also the component that handles the NPCs A.I. In the future I may move this into it own component.

Messages & Subtitles:
are used to print messages on the screen. In it's simplest form you can do things like this..
"Hello world!".message;

Non-Player-Character: (NPC)
a character not controlled by a player but the computer. It has a predetermined set of behaviours that potentially will impact gameplay.

origin:
a point which is in the middle of an entity and the centre of its bounding box. Both the origin and boundingBox are used as proxies so never replace them with new objects. Only ever do in-place operations on them. This is a design decision to make calculations and access to this data as fast as possible. They are the single and only reference to an entity's origin. If you make a new Point for origin you will break your code.
// Here is an example of an in-place operation that works:
origin += velocity; // the += method replaces the x & y cords in origin and doesn't make a new object.
// Here is a bad example that will break your code:
origin = origin + velocity; // Do not do this !!!

Particles:
are fast ways of drawing simple shapes. Used in explosions or simulating snow, rain or dust.

releaseMode:
when your project is ready or if you just want to see how it looks when finished use this mode. It will force the following start-up flags to these values...
verbose = false; startEngine = true; fullScreen = true; titleScreen = true; debugMode = false; skipIntro = false;
The user will no longer be able to turn on the debugger, tile editor or view dev info.

renderLoop:
is the method used to draw the world and everything in it. The background, the layers of entities, the light map, particles, HUD, messages, subtitles and Camera overlays. This is deferred every frame from the System Clock and you can swap it out at runtime with your own renderLoops. Each scene can have its own loop, by default sceneState[\renderLoop] = \renderLoop is used when a scene 1st opens.

Scenes:
are where we build a game, its entities & define their assets, tile sets, scripts, gameLoops, HUD etc. What to do when the game starts, what happens when you start a new level, revisit an old scene, or what to do when it's game over. A Scene doesn't need to have Assets, Tile Sets, Light Maps, Scripts or a HUD but typically does.

sceneScripts:
an IdentityDictionary that contains all the scripts (functions) a scene has. They include..
[\startGame    ] = {}; // Only called once, when a game starts. Uses include, an intro, setting up the camera, camera fades, starting a music playlist, initialise world state like score, high score & lives.
[\startScene   ] = {}; // Called only when a scene is 1st visited. Here you could include waves of enemies, snap the camera to a player, start sound fx, initialise sceneState like number of collectables.
[\leaveScene   ] = {}; // called every time the player leaves a scene. You can do tidying up stuff here, like stopping music or deleting temporary entities & particles.
[\revisitScene ] = {}; // called when the player revisits a scene for a 2nd, 3rd or further occasions. For example you could move the player to correct position or start playing music again.
[\playerDeath  ] = {}; // gets called every time the player dies. Whatever you want to happen put it here. It could be another life or gameOver.
[\gameOver     ] = {}; // gets called when gave over is triggered. You can do all the "You died", fade to black type stuff here. The scene won't end though until you call World_World:quitGame in this function.
[\gameWin      ] = {}; // congratulate the player here. Again the scene won't end until you call World_World:quitGame
[\worldEdge    ] = {}; // triggered when the player hits an edge of the world. Useful for moving between scenes.
[\tick         ] = {}; // anything that needs to happen every frame goes here. Maybe camera zooms or adjusting sound fx volumes.
[\controllerIn ] = {}; // any controller input you want to use to affect the scene.
[\keyDown      ] = {}; // any keyboard input you want to use to affect the scene.
[\keyUp        ] = {}; // any keyboard input you want to use to affect the scene.
[\mouseDown    ] = {}; // any mouse input you want to use to affect the scene. 2 points are passed in screenPoint & worldPoint
[\mouseUp      ] = {}; // any mouse input you want to use to affect the scene. + mod, button & clickCount
[\mouseMove    ] = {}; // any mouse input you want to use to affect the scene.

sceneState:
an IdentityDictionary that holds data relating to a scene. You can store anything you like in here, the following are predifined.
[\background    ] = \alienPlanet;   // a symbol to a background image defined in the scen.es assets, or a Color
[\backgroundMode] = \followCamera;  // how the background image is displayed
                                    // options are : \fixedNoZoom, \fixed, \strechToFit, \parallax, \followCamera, \scroll
[\gameLoop      ] = \gameLoop;      // the default game loop used when the scene opens.
[\renderLoop    ] = \renderLoop;    // the default render loop used when the scene opens.
[\gameOverLoop  ] = \gameOverLoop;  // the default gameOver loop used when its game over.
[\hud           ] = \alienMaze_HUD; // the method name of the scene's HUD.
[\spawn         ] = Point(100,100)  // default spawn point for player.
[\playerVisits  ] // For info only, number of times the player has visited a scene.
[\mapArea       ] // For info only, set by the World_Scene:setupScene method. Area   of Tile Map (in tiles).
[\mapHeight     ] // For info only, set by the World_Scene:setupScene method. Height of Tile Map (in tiles).
[\mapWidth      ] // For info only, set by the World_Scene:setupScene method. Width  of Tile Map (in tiles).
[\tileHeight    ] // For info only, set by the World_Scene:setupScene method. Height of 1 tile  (in pixels).
[\tileWidth     ] // For info only, set by the World_Scene:setupScene method. Width  of 1 tile  (in pixels).

tick:
a name for a method that calculates 1 frame's worth of work. Many things tick in World_World, examples include the camera, a mechanics component, a timer, a clock and the sceneScripts[\tick] function.

Tile:
an entity that is created by the tile map. It is typically in a fixed position and represents the terrain of the world.

TileMap:
a set of tools used to render a world made out of tiles. You can add mazes, shapes, borders, tunnels and use heat maps to generate random terrain. Tile maps can be saved as text files, either for storage or for editing in a text editor. There is a folder in assets called levels for keeping the maps you've designed.

Tile Set:
a set of predefined entities that are available to the tile map to make the world with. Each scene has its own set and are defined inside a method of World_Tile_Map. It must have the postFix _TILE_SET_DEF. For example World_Tile_Map:alienMaze_TILE_SET_DEF

timeDilation:
is used to speed up or slow down time. It's the speed at which time passes. If..
World_World.timeDilation = 1;   // 1 second passes every second (normal speed)
World_World.timeDilation = 0.5; // half a second passes every second (slower)
World_World.timeDilation = 2;   // 2 seconds pass every second (faster)
If you are designing mechanics and movement you'll need this variable for your calculations. For example if you are moving an x cord by the amount dx you might do something like this..
x = x + (dx * timeDilation)
or scaling the x cord by dx is..
x = x * (dx ** timeDilation)

Title Screen:
is the scene that opens at launch. There is nothing special about the title screen, it's just like any other scene. The only difference is that the scene specified in World_World:titleScreenBuildMethod will open if World_World:titleScreen = true or World_World:releaseMode = true

Uniform Grib Partition: (UGP)
is one of the ways World_World optimises collisions. The world is split into cells of equal size and only entities that are close to each other do collision detection.
Sources need to update their position with the World_UGP. This happens automatically when you use the World_Entity: origin_, moveBy, boundingBox_ & updateBoundingBox methods.
World_UGP:maxResponderSize is set when using World_Scene:setupScene. This is the largest size a responder can be (in num cells). Sources can be any size.

verbose & Dev Info:
if verbose == true then details of what the engine is doing is printed to the post window. This can also be turned on/off if you press the ? key. You will also get various useful info posted to the main game window, such as fps, dropped frames and more.

worldState:
an IdentityDictionary that holds data relating to the world and remains between scenes but is lost when you return to the title screen. You can store anything you like in here, typically things you need across scenes such as number of lives, score, progression, etc.

worldTime:
is the amount of time that has passed since the engine or a game started in seconds. This is effected by timeDilation and works as a good way to keep things synchronised.

zoom, globalZoom & scaleMode:
global zoom is set by the scaleMode. It attempts to scale what you see on the default screen size and zoom it in or out so it fits on the current screen. Options for scaleMode are...
\none           // don't scale
\diagonal       // scale to fit the diagonal
\width          // scale to fit the width
\height         // scale to fit the height
\nearestPixels  // scale to fit the sides which are nearest in pixels to each other.
\furthestPixels // scale to fit the sides which are furthest in pixels from each other.
\smallestRatio  // scale to fit the sides who's ratios are closest to each other.
\largestRatio   // scale to fit the sides who's ratios are furthest from each other.
If you want to zoom the camera in & out as part of the game use World_Camera:zoom_ or logZoom_
Programming tip: Qt works fastest when zoom is set to 1. Aim to have this as your most common zoom and you'll get the most bang for your buck.
