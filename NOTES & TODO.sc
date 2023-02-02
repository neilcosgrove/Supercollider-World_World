////////////////////////////////////////////////////////////////////////////////////////////////////////
// [ NOTES + TODO ]          LNX_Games presents the World_World game engine          [ NOTES + TODO ] //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*                 made by Neil Cosgrove in 2021-2022 (GNU General Public License)

Vector ship designed and coded by mjsyts - https://github.com/mjsyts/spacegame/blob/main/spacegame
The Royalty-Free alien sprites come from https://www.pinterest.co.uk/pin/296533956721131545/

If you don't want the engine to start on launch goto 1.World/1.World.sc and change this line to...
classvar <startEngine = false;   // start the game engine on launch
You can then start the engine with
World_World.start;

ATM the Keys on a UK Mac are...
WASD = movement
space = jump
return = fire
; = wall grab
escape = pause

TODO List, post release :

1. Making games in the Interpreter?
2. Organise & Test the basic shapes. Platformer vs circle and fixed items
3. Tidy up shooter, reduce number of classes + make world edge response an entity
4. Make scenes a true instance?
5. Organise Colliders betters

Platformer - Use tails as collectables
World_Rect_Tile2 ? and tiles size 50x50 & 51x51
Negative restitution and gravity make things bouncy. make bouncy squares and lava
i need to think about font size in various places
Fix particle count
code review
test subtitles and backgrounds with different screen resolutions
falling balls as a sim, or add gravity to current sim, i could do a flocking algo easily
Standardise initial tile pos

Fix circle vs triangle hypot corners

NPC after rand time change direction
NPC add a hunter timer
NPC ray-marching so i can do line of site

make a fork timer component

add instance number or map number or something to scene state to identify and locate the scene
levels could be stored in a MultiLevelIdentityDictionary?

make string draw components
Map / ECS editor
save & load maps as text files

change volumes of channels \music, \fx, \dialog, \ambience
Play SynthDefs like buffers in Audio player
reverse on audio
what am i going to do about playlists and swapping scenes with different assets?

add more controllers : Xbox next

make camera an instance?
make ECS an instance?
Temporary Items ! ? £

expose Component interface for a gui editor, have methodName + spec [ \radius, Spec(1,2) ]

make a midi In & Out controller

Partition other container functions
Finish supercolliders

make alien minecraft, space invaders, asteroids, maze, gauntlet, pong, donkey kong (ladders), pacman, defender, r type
arkanoid, bolder dash

different layers, different user views, different fps and all that goes with it
Area of effect weapons, running - stamina - cooldown, check points, rain, floating dust
swinging balls, saw up/down  left/right, portals, moving platforms, flame thrower, cannon, spikes, proximity bombs
icy slippery floor, fans
random char text like matrix or 8bit guy
Shields

Engine Ideas, Game Ideas & things to do
=======================================

thoughts on organising physics engine...
	Friction as a figure (f) on Vector - Vector[x*f, y*f]
	Friction as a figure (f) on a Polar - Polar(rho*f,theta)
	Friction as a Point or Vector - f = Vector (fx, fy) -  Vector[x * fx, y * fy]; Here neg y gives bounce in platformer
	Properties : isSolid, isDynmic, friction, restitution, bounce, damage, kill, canGrab, canCollect, hardness, immovable, mass
	True refraction off surface & Using velocity to do proper reflections - i.e. ball simulation
	Mass & Conservation of momentum
	which surface/side was hit? Each side has a different property? Solid in 1 direction so can jump through the floor
	Does all this go into the collider?
	source vs. responder
	add/remove this collider as a collision Source of type ( use this when entity is bigger than 1 tile ! ) £
	collisionSourceBBox_{|type,bool,addAction = \addToTail|
	I should split them methods below into add & remove
	add/remove this collider as a collision Source of type
	collisionSource_{|type, bool = true, addAction = \addToTail|

animated tiles - extended tile blocks?
kill enemy plays amen crash - simple sample play that launches on beat
simple sample player
    sample list, loop/one shot, pitch, snap play to a beat, playSample via LNX_NoteOn?, volume, pan etc, ADSR, marker
weapon causes enemies to get smaller
make separate levels
a scan that is a circle that expands from the player and makes tiles into red outlines
map editor, shift arrow keys change size of world
animation of sprites and tiles
more work on backgrounds
tiles extend beyond boards for effects like grass or flowers
2 player game on 2 controllers, platform + players can tether, right joy 10% movement of other player
rain
thors hammer
fade to or in from black, disable controller, pause game
shutter style to black, circle zoom to black
ladders
teleporters
Doors
Inventory
Open Chest
Run
allow player to grab ceiling
auto scroll left by 8 tiles every bar
Shape template designer so I can do vector drawings of geometric patterns or shapes in graphic design
add big expanding circles to explosions
bombs that destroy map tiles
grappling hook
gravity gun
1 bullet into 2 into 4 into 8. and other fractal ideas
Advanced Gamer Acid mixed with Rhythm music levels + interacting with entities
tightly coupled to music, build as song builds
Different areas of the screen get a different background color and therefore different motion blur
focus on only1 as a mechanic. only 1 what though?
personise the controls as part of the game play - i.e user decides what buttons do what
fly to your rocket destination really fast, which bounces off walls
lower health stronger bullets
collect moving bullets + upgrades
kill enemy +1 score, enemy hits you -1 score & -1 health, zero health means doesn't die but score=0 and back to spawn
scores opens boss section
shield, weapons, character levelling, powers
down time, collect collectables
each enemy has its own weapons and when you kill it you are forced to take and use that weapon
spawns that activate when close to player
on kill 1) shake camera 2) slow timeDilation 3) flash white 4) knock back 5) sound
character part of the world. 1) dust particles on wall hit
enemies hold their ground a bit more
AI appears more intelligent if they have high health and powerful damage
AI states bullet hit vs distance from player
1st enemy bullet is not accurate
predictable AI is good
AI can use the world mechanics
unlock abilities: freeze enemies. max health is auto earned not a choice
choose ability for each level
pacman AI
multiplayer - 2 controllers
satisfying feedback
randomness - extra bonuses. bullets extra strength, each level random colors or what ever
progress
placing entities on the floor
the more yellow enemies are on screen, the closer they get

i like the idea of chain like here.. "https://www.youtube.com/watch?v=pUYw9D0aq6g"

STATE
=====
	// not used yet, is there anything below i can move to state?
	var <>mass           = 1;               // in kg
	var <>attractor      = false;           // is a source of gravity
	var <>attractorDist  = 10;              // distance gravity kicks in
	var <>feelsAttractor = false;           // feels gravity
	var <>direction      = \right;          // direction sprite is facing
	var <>hasShadow      = false;           // create a shadow layer


LNX Interface
=============
World_World.progToLNX(2);
World_World.controlToLNX(index,value);
World_World.lnxState[\absTime]
World_World.masterFadeOut(32);
World_World.masterFadeIn(32);
World_World.fadeOut(2,32);
World_World.fadeIn(2,32);

for shits & giggles
====================

layers.do{|entities| entities.do{|object|
object.draw;
Pen.rotate(pi/2, cameraPos.x, cameraPos.y);
object.draw;
Pen.rotate(pi/2, cameraPos.x, cameraPos.y);
object.draw;
Pen.rotate(pi/2, cameraPos.x, cameraPos.y);
object.draw;
Pen.rotate(pi/2, cameraPos.x, cameraPos.y);
}};  // draw all the entities

layers.do{|entities| entities.do{|object|
object.draw;
Pen.rotate(2pi/3, cameraPos.x, cameraPos.y);
object.draw;
Pen.rotate(2pi/3, cameraPos.x, cameraPos.y);
object.draw;
Pen.rotate(2pi/3, cameraPos.x, cameraPos.y);
}};  // draw all the entities

///////////////////////////////
	// for collision detection
	//     [Rect   : Rect  ] >> Rect(1,1,1,1).intersects(Rect(1,1,1,1));
	//     [Circle : Circle] >> Point(10,10).dist(Point(20,20)) - radius1 - radius2 <0; // do top bottom left right test 1st
	//     [Circle : Rect  ]

	//     [Line   : Line  ]
	//     [Line   : Rect  ]
	//     [Line   : Circle]
	//     quadrilateral ??
	//     triangle      ??

*/

