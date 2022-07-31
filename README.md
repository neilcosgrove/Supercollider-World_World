# Supercollider-World_World

World_World is a 2D game engine made in Supercollider. I've made it because I love messing around
with both audio and games. Most game engines have a basic audio capability but what if it
was the other way round? What if one of the most advanced audio languages had its own game engine
instead? What could be made then?

The engine is capable of making large worlds (scenes), containing many entities that can interact in
complex ways. World_World's coding style is focused on efficiency and getting as much out of Qt as possible.
It's many features include...

• Support for Scenes that are bigger than the screen, which can be worlds, games or levels

• An Entity Component type system that allows object behaviour to be split up into components

• Garbage Collection so that objects are only freed at the end of a frame

• Separate Game & Render loops that can be swapped out at runtime

• A camera that can zoom, pan, rotate, shake and do transitions such as fade to black

• Tools to help scale a default screen size to other resolutions & aspect ratios

• Asset management of image and buffer files

• An audio player that supports the playback of buffers such as sound, musical & environmental fx & playlists

• Tile Maps that can make scenes with a wide range of tools such as random maze & terrain generation

• A Tile Map editor so users can edit them in game. Maps can also be saved and loaded from disk.

• A Uniform Grid Partition to optimize collision detection

• Offscreen culling in both the render & game loops

• Static & dynamic light maps

• A particle system

• Time dilation so the speed at which time passes in a scene can be changed

• A Heads Up Display system

• A message & subtitle system

• Integrated support for Keyboard & Controllers. Currently PS4 & PS5 controllers are supported.

• Components such as Mechanics, Colliders, Timers, A.I, Controller Input, Draw Functions and many more

• Scripting and Timers that run on the World or a Scene clock

Drop the "Supercollider-World_World" folder and its entire contents into the "SCClassLibary" folder to get it working. If you don't want the engine to start on launch open file 1.World/1.World.sc and change this line to...
classvar <startEngine = false;
You can then start the engine with
World_World.start;

ATM the Keys on a UK Mac are...
WASD = movement
space = jump
return = fire
; = wall grab
escape = pause

This demo was made with Geomancy as the intended font. You don't have to have this font installed for it to work but it does look better if you do. Geomancy can be found in the assets/fonts folder or here... https://www.dafont.com/geomancy.font
Please see its Readme.txt file for usage.

I've not written any help files yet. The glossary or the Games folder are good places to start. Or you could use the debug mode to inspect Entities and see what Components they're made of + their internal state. Game loops give you a good idea of the order thing happen in.
