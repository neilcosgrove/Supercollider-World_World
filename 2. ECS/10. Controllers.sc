////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                  Controller Components                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
PS4 controller index
[0]   square,   [1]   X         [2]   O     [3]   Triangle     [4]   L1 (on/off)     [5]   R1 (on/off)
[6]   L2 (pos)  [7]   R2 (pos)  [8]   Share [9]   Options      [10]  L3              [11]  R3
[12]  PS button [13]  Track PAD button      [14]  L Joy L-R    [15]  L Joy U-D       [16]  R Joy L-R
[17]  R Joy U-D [18]  DPad LEFT [19]  DPad RIGHT               [20]  DPad UP         [21]  DPad DOWN
*/

// you can set the deviceNo to nil and the component will respond to all controllers but be careful about controllerState after

World_Controller_Base : World_Component {

	var <active=true, <>deviceNo;

	*new{|parent, deviceNo=0| ^super.new.initComponent(parent).init(deviceNo) }

	init{|argDeviceNo|
		deviceNo = argDeviceNo;
		inputs=inputs.add(this)
	}

	free{ inputs.remove(this) }

	// inputs is an IdentitySet so nothing gets added twice
	active_{|bool|
		active = bool;
		if (active) {
			inputs=inputs.add(this);
			this.doRetrigger;
		} {
			inputs.remove(this)
		}
	}

	controllerInput{|device, index, value|
		if ((device==deviceNo) or: {deviceNo.isNil}) { this.controllerIn(device, index, value) }
	}

	*doRetrigger{ inputs.do(_.doRetrigger) }

	// called by open scene, create scene, death, unpause, active_(true).
	doRetrigger{
		if (active) {
			this.retrigger.do{|index|
				if (controllerState[deviceNo].notNil) {
					if (controllerState[deviceNo][index].notNil) {
						this.controllerIn(deviceNo, index, controllerState[deviceNo][index] )
					}
				}
			}
		}
	}

	// to subclass - if still pressed these controls retrigger on open scene, create scene, death, unpause, active_(true).
	retrigger{ ^[] }

	controllerIn{|device, index, value| } // to subclass

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                     Controller Patch                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/* access all PS4 and PS5 controllers and maps them to this indexing...

[0]   square,   [1]   X         [2]   O     [3]   Triangle     [4]   L1 (on/off)     [5]   R1 (on/off)
[6]   L2 (pos)  [7]   R2 (pos)  [8]   Share [9]   Options      [10]  L3              [11]  R3
[12]  PS button [13]  Track PAD button      [14]  L Joy L-R    [15]  L Joy U-D       [16]  R Joy L-R
[17]  R Joy U-D [18]  DPad LEFT [19]  DPad RIGHT               [20]  DPad UP         [21]  DPad DOWN

World_Controller_Patch.allHIDs
World_Controller_Patch.restartAll
*/

// Patch that allows access to ps4 & ps5 controllers

World_Controller_Patch : World_World {

	classvar <allHIDs, <paths, <pathAsStrings, <dependants;
	classvar <elementNames, <noElements;
	classvar <drift = 0.01; // minimum drift
	classvar <off   = 0.05;	// off threshold

	*init{
		if (verbose) { "World_Controller_Patch:init".postln };
		StartUp.add{ this.restartAll };
		elementNames = ["Square", "Cross","Circle", "Triangle", "L1", "R1", "L2", "R2", "Share", "Options", "L3", "R3",
			"PS Button", "TPad Button", "LJoy L&R", "LJoy U&D", "RJoy L&R", "RJoy U&D", "DPad Left", "DPad Right",
			"DPad Up", "DPad Down"];
		noElements=elementNames.size;
		//controllerState = IdentityDictionary[];
		controllerState = MultiLevelIdentityDictionary[];
	}

	*restartAll{
		if (verbose) { "World_Controller_Patch:restartAll".postln };
		allHIDs		= IdentityDictionary[]; // only & all ps4 controllers
		dependants	= dependants ? IdentityDictionary[]; // make a dict for dependants if it doesn't already exist
		{
			var deviceNo=0;
			if (HID.running) { HID.closeAll; 1.wait }; // this should stop 2 on 1 device when no. devces>1
			HID.findAvailable;                         // this will init HID
			HID.available.do{|info,i|
				// ps5 controller
				if (info.vendorName  =="Sony Interactive Entertainment" && (info.productID == 3302) ) {
					var path         = info.path.asSymbol;
					var hid          = HID.openPath(path: (path.asString) ); 	 // the device
					var	exclude      = #[6,7,22]; // inputs to exclude
					var reverse		 = #[17,19];	// reverse the joy's up & down
					var lastValue    = IdentityDictionary[];
					var lastDPad	 = 4;									 	 // last position the dPad was in
					if (hid.notNil) {
						var idName		   = deviceNo;		 // make a name
						deviceNo		   = deviceNo + 1;
						allHIDs[idName]    = hid;
						dependants[idName] = dependants[idName] ? IdentitySet[]; // dependants that get updates
						// for the 1st 24 elements
						24.do{|index|
							lastValue[index] = inf;
							if (exclude.includes(index).not) {
								// actions for each element...
								hid.elements[index].action = {|i,element|
									var value = element.value;
									var pass  = false;
									var newIndex;
									// 16,17,18,19 Joys
									if (((value-0.5).abs < off)and:{(index>=16)&&(index<=19)}) { // Joys are off
										value=0.5;
										if ((lastValue[index] - value).abs >= drift) { pass = true };
									}{
										if (value==0 ) { pass = true }; // override drift if value is 0 or 1
										if (value==1 ) { pass = true };
										if ((lastValue[index] - value).abs >= drift) { pass = true }; // drift
										// dpad stuff, important this comes after above line
										if (index==23) {
											value = (value*4).asInteger; // 0=up, 1=right, 2=down, 3=left, 4=centre
											// dpad out as up/down [18,19] left/right [20,21]
											// dpad out as up/down [20,21] left/right [18,19]
											switch (lastDPad)
												{0} { dependants[idName].do{|func| func.value(20, 0)}; } // up off
												{1} { dependants[idName].do{|func| func.value(19, 0)}; } // right off
												{2} { dependants[idName].do{|func| func.value(21, 0)}; } // down off
												{3} { dependants[idName].do{|func| func.value(18, 0)}; } // left off
												{4} { pass = false }; // centre
											switch (value)
												{0} { newIndex = 20; lastDPad = 0; value = 1; pass = true } // up on
												{1} { newIndex = 19; lastDPad = 1; value = 1; pass = true } // right on
												{2} { newIndex = 21; lastDPad = 2; value = 1; pass = true } // down on
												{3} { newIndex = 18; lastDPad = 3; value = 1; pass = true } // left on
												{4} { lastDPad =  4; pass = false }; // centre
										};
									};
									// output value if passes all tests above
									if (pass) {
										if (index==20) { newIndex = 6  }; // move L2 from 20 to 6
										if (index==21) { newIndex = 7  }; // move R2 from 21 to 7
										if (index==16) { newIndex = 14 }; // move LJoy L-R from 16 to 14
										if (index==17) { newIndex = 15 }; // move LJoy U-D from 17 to 15
										if (index==18) { newIndex = 16 }; // move LJoy L-R from 18 to 16
										if (index==19) { newIndex = 17 }; // move LJoy U-D from 19 to 17
										lastValue[index] = value;							// store last value
										if (reverse.includes(index)) { value = 1 - value }; // reverse jpad up/down
										dependants[idName].do{|func| func.value(newIndex ? index, value)}; // action
									};
								};
							};
						};
					};
				};
				// ps4 controller
				if (info.vendorName  =="Sony Interactive Entertainment" && (info.productID == 2508) ) {
					var path         = info.path.asSymbol;
					var hid          = HID.openPath(path: (path.asString) ); 	 // the device
					var	exclude      = #[6,7,19]; // inputs to exclude
					var reverse		 = #[15,17];	// reverse the joy's up & down
					var lastValue    = IdentityDictionary[];
					var lastDPad	 = 4;									 	 // last position the dPad was in
					if (hid.notNil) {
						var idName		   = deviceNo;		 // make a name
						deviceNo		   = deviceNo + 1;
						allHIDs[idName]    = hid;
						dependants[idName] = dependants[idName] ? IdentitySet[]; // dependants that get updates
						// for the 1st 22 elements
						22.do{|index|
							lastValue[index] = inf;
							if (exclude.includes(index).not) {
								// actions for each element...
								hid.elements[index].action = {|i,element|
									var value = element.value;
									var pass  = false;
									var newIndex;
									// 14,15,16,17 joys
									if (((value-0.5).abs < off)and:{(index>=14)&&(index<=17)}) { // Joys are off
										value=0.5;
										if ((lastValue[index] - value).abs >= drift) { pass = true };
									}{
										if (value==0 ) { pass = true }; // override drift if value is 0 or 1
										if (value==1 ) { pass = true };
										if ((lastValue[index] - value).abs >= drift) { pass = true }; // drift
										// dpad stuff, important this comes after above line
										if (index==18) {
											value = (value*4).asInteger; // 0=up, 1=right, 2=down, 3=left, 4=centre
											// dpad out as up/down [18,19] left/right [20,21]
											// dpad out as up/down [20,21] left/right [18,19]
											switch (lastDPad)
												{0} { dependants[idName].do{|func| func.value(20, 0)}; } // up off
												{1} { dependants[idName].do{|func| func.value(19, 0)}; } // right off
												{2} { dependants[idName].do{|func| func.value(21, 0)}; } // down off
												{3} { dependants[idName].do{|func| func.value(18, 0)}; } // left off
												{4} { pass = false }; // centre
											switch (value)
												{0} { newIndex = 20; lastDPad = 0; value = 1; pass = true } // up on
												{1} { newIndex = 19; lastDPad = 1; value = 1; pass = true } // right on
												{2} { newIndex = 21; lastDPad = 2; value = 1; pass = true } // down on
												{3} { newIndex = 18; lastDPad = 3; value = 1; pass = true } // left on
												{4} { lastDPad =  4; pass = false }; // centre
										};
									};
									// output value if passes all tests above
									if (pass) {
										if (index==20) { newIndex =6 }; // move L2 from 20 to 6
										if (index==21) { newIndex =7 }; // move R2 from 21 to 7
										lastValue[index] = value;							// store last value
										if (reverse.includes(index)) { value = 1 - value }; // reverse jpad up/down
										dependants[idName].do{|func| func.value(newIndex ? index, value)}; // action
									};
								};
							};
						};
					};
				};
			};
			paths = [\None] ++ (allHIDs.keys.asList.sort);  // all paths as symbols
			pathAsStrings = paths.collect(_.asString);		// all paths as strings
		}.fork(AppClock); // on AppClock fixes a bug
	}

	*addAction{|path,func|
		//if (path.isNumber) { path = paths[path] } {	path = path.asSymbol };
		dependants[path] = dependants[path].add(func)
	}

	*removeAction{|path,func| dependants[path.asSymbol].remove(func) }

	*externalIn{|device,index, value| dependants[device].do{|func| func.value(index, value) } } // for keyboard

}