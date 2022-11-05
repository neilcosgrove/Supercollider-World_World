////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          Archive                                                   //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
quick and easy ways of saving and loading data to disk. As long as they are simple objects with a valid asCompileString
then you should be able to save them.
[1,2,3,\a,[100,200], IdentityDictionary[1->2], "\n\nhello"].saveState("test");
a = "test".loadState;
a.collect(_.class);
a[5][1].class
(a: Color.black, b:Point(1,2), c:[1,2,3]).saveState("test"); // Events or IdentityDictionary are good ways to store stuff
"test".loadState[\b]
*/

World_Archive : World_World {

	classvar <saveStateDirectory;

	*init{ saveStateDirectory = World_Assets.assetDirectory.dropFolder(-1) +/+ "save state" }

	*save{|path,object| File(path,"w").write(object.asCompileString).close }

	*load{|path|
		var file, string, line;
		if (File.exists(path).not) {^nil};
		file   = File(path,"r");
		string = "";
		line   = file.getLine;
		while ( {line.notNil}, {
			string = string ++ line; // i think strings might loose any newline. TBC.
			line = file.getLine;
		});
		file.close;
		^string.interpret
	}

	*delete{|path| path.removeFile(silent:true) }

	// these save directly to saveStateDirectory
	*saveState{|path,list| this.save(saveStateDirectory+/+path,list)}
	*loadState{|path| ^this.load(saveStateDirectory +/+ path) }
	*deleteState{|path| this.delete(saveStateDirectory +/+ path) }

}

// convenience methods

+String{
	loadFile   { ^World_Archive.load(this)        }
	loadState  { ^World_Archive.loadState(this)   }
	deleteFile { ^World_Archive.delete(this)      }
	deleteState{ ^World_Archive.deleteState(this) }
}

+ Object {
	saveState{|path| World_Archive.saveState(path,this) }
	saveFile {|path| World_Archive.save(path,this)      }
}

// from wslib

+String {
	removeFile { |toTrash = true, ask = true, silent = false|
		// by default only moves to trash
		// watch out with this one..
		// also removes folders...
		// does not ask when moving to trash
		var result, exists, rmFunc;
		exists = this.pathExists;
		rmFunc = { result = ("rm -R" + this.standardizePath.quote).systemCmd;
			("String-removeFile: removed file" + this.basename.quote)
			.postlnIfTrue( (result == 0) && silent.not );
		};

		if(  exists != false )
		{ if( toTrash )
			{ ^this.moveRename( "~/.Trash", silent: silent );  }
			{ if( ask )
				{ SCAlert( "delete" + this.basename.quote ++ "?",
					[ "cancel", "ok" ], [nil, rmFunc]);
				} { rmFunc.value };
			};
		};
	}
}
