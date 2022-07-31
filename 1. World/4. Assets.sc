////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Assets (Sounds & Images)                                        //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
At the moment all assets are loaded at launch, it shouldn't be to difficult to change this in the future if needed
Each Scene has a World_Assets method which defines its assets. They get swapped out when changing scenes.
Everything is stored in World_Assets class def, and indexing is via a pathname.asString.hash
World_Assets.revealInFinder; // this is where all the assets are kept.
(World_Assets.assetDirectory+/+ "fonts").revealInFinder; // fonts are here
*/

World_Assets : World_World {

	classvar <all_ASSET_DEF_methods, <all_ASSET_DEF_instances;
	classvar <assetDirectory, <imageDirectory, <soundDirectory, <mapsDirectory;
	classvar <allImagePaths,  <allImages,      <allImageBounds, <allBufferPaths, <allBuffers;

	var <method, <imagePaths, <images, <imageBounds, <bufferPaths, <buffers;

	*init{
		if (verbose) { "World_Assets:init".postln };
		Platform.case(
			// put here where you want your asset folder to be located
			\osx,     { assetDirectory = World_World.filenameSymbol.asString.dropFolder(-2) +/+ "assets" },
			\linux,	  { assetDirectory = World_World.filenameSymbol.asString.dropFolder(-2) +/+ "assets" },
			\windows, { assetDirectory = World_World.filenameSymbol.asString.dropFolder(-2) +/+ "assets" }
		);
		soundDirectory = assetDirectory +/+ "sounds";
		imageDirectory = assetDirectory +/+ "images";
		mapsDirectory  = assetDirectory +/+ "maps";
		if (PathName(assetDirectory ).isFolder.not) { ("Asset directory not found at"+assetDirectory ).throw };
		if (PathName(soundDirectory ).isFolder.not) { ("Sound directory not found at"+soundDirectory ).throw };
		if (PathName(imageDirectory ).isFolder.not) { ("Image directory not found at"+imageDirectory ).throw };
		if (PathName(mapsDirectory  ).isFolder.not) { ("Map directory not found at"+mapsDirectory  ).throw };
		all_ASSET_DEF_methods   = this.methods.collect(_.name).select{|name| name.asString.keep(-9) == "ASSET_DEF"};
		all_ASSET_DEF_instances = IdentityDictionary[];
		all_ASSET_DEF_methods.do{|method| all_ASSET_DEF_instances[method] = World_Assets(method) }; // create the instances here
		this.loadImageAssets;
		all_ASSET_DEF_instances.keysValuesDo{|method,instance| instance.init(method)}; // then init instances after we load assets
		// advice to install Geomancy for looks
		if (Font.availableFonts.select{|s| s=="Geomancy"}.size==0) {
			("This demo was made with Geomancy as the intended font. You don't have to have this font installed for it to work but it does look better if you do. Geomancy can be found in the \"" ++ (World_Assets.assetDirectory+/+ "fonts") ++ "\" folder or here... https://www.dafont.com/geomancy.font. Please see its Readme.txt file for usage.").warn;
		};
	}

	*loadImageAssets{
		allImagePaths  = IdentityDictionary[];
		allBufferPaths = IdentityDictionary[];
		all_ASSET_DEF_methods.do{|method|
			all_ASSET_DEF_instances[method].imagePaths.keysValuesDo {|key,path| allImagePaths[path.hash] = path };
			all_ASSET_DEF_instances[method].bufferPaths.keysValuesDo{|key,path| allBufferPaths[path.hash] = path };
		};
		allImages      = allImagePaths.collect{|path | Image.open(imageDirectory +/+ path).interpolation_(interpolation) };
		allImageBounds = allImages.collect{|image| Rect(0, 0, image.width, image.height) };
		allBuffers     = IdentityDictionary[]; // loaded in World_Audio at the moment, move here?
	}

	*loadSoundAssets{|action|
		var remaining = allBufferPaths.size;
		allBufferPaths.pairsDo{|key,path|
			allBuffers[key] = Buffer.read(path: World_Assets.soundDirectory +/+ path, action:{
				remaining = remaining - 1;
				if (remaining == 0) { {
					this.initBuffers;
					action.value;
				}.defer(0.1) };
			})
		};
	}

	*revealInFinder{ assetDirectory.revealInFinder }

	*new{|method|
		if (verbose) { "World_Assets:%".format(method).postln };
		^super.new.perform(method);
	} // no init done here but we do call the _ASSET_DEF method to get asset paths

	init{|argMethod|
		method = argMethod;
		images      = IdentityDictionary[];
		imageBounds = IdentityDictionary[];
		imagePaths.keysValuesDo{|key,path|
			images[key]      = allImages[path.hash];
			imageBounds[key] = allImageBounds[path.hash];
		};
	}

	*initBuffers{
		if (verbose) { "World_Assets:initBuffers".postln };
		all_ASSET_DEF_instances.do(_.initBuffers);
	}

	initBuffers{
		buffers = IdentityDictionary[];
		bufferPaths.keysValuesDo{|key,path| buffers[key] = allBuffers[path.hash] };
	}

	// swapping out assets
	*open{|method|
		if (method.isNil) { method = \nil_ASSET_DEF };
		all_ASSET_DEF_instances[method].open;
	}

	// called when swapping scenes
	open{
		World_World.images      = images;
		World_World.imageBounds = imageBounds;
		World_World.buffers     = buffers;
	}

	*reloadAssets{} // TODO

	*freeAssets{} // TODO

	// default for no assets
	nil_ASSET_DEF{
		imagePaths  = IdentityDictionary[];
		bufferPaths = IdentityDictionary[];
	}

}

/*
This is how the containers above work...

IMAGES

World_Assets.all_ASSET_DEF_methods      List[ \debug_ASSET_DEF, \titleScreen_ASSET_DEF, \platformer_ASSET_DEF ]
World_Assets.all_ASSET_DEF_instances    IdentityDictionary[ method -> World_Assets() ]
World_Assets.allImagePaths              IdentityDictionary[ path.hash -> "path" ]
World_Assets.allImages                  IdentityDictionary[ path.hash -> Image() ]
World_Assets.allImageBounds             IdentityDictionary[ path.hash -> Rect() ]
World_World.images                      IdentityDictionary[ \key -> Image() ]         // << this is where the scene access the images
World_World.imageBounds                 IdentityDictionary[ \key -> Rect() ]

SOUNDS

World_Assets.allBufferPaths             IdentityDictionary[ path.hash -> "path" ]
World_Assets.allBuffers                 IdentityDictionary[ path.hash -> Buffer() ]
World_World.buffers                     IdentityDictionary[ \key -> Buffer() ]       // << this is where the scene access the Buffers

*/
