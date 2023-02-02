////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                             Camera                                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
World_Camera.fadeToBlack;
World_Camera.fadeToClear;
World_Camera.setToColor(Color.rand.alpha_(0.75)); World_Camera.fadeToClear(2);
World_Camera.shake;
*/

World_Camera : World_World {

	classvar <cameraObject,  <cameraTime=1,   <cameraIndex=0,  <cameraPanCurve=6;
	classvar <lastCameraPos, <cameraMargin=0, <cameraShake=0,  <shakeCoefficient=0.95, <shakeFreq=60;
	classvar <cameraMaxLeft, <cameraMaxTop,   <cameraMaxRight, <cameraMaxBottom;
	classvar <previousColor, <currentColor,   <nextColor;
	classvar <fadeTime=1,    <fadeIndex=0,    <fadeCurve=0;

	// Camera controls ////////////////////////////////////////////////////////////////////////////////

	*reset{
		this.setToClear;
		this.zoom_(1);
		this.angle_(0);
		motionBlur = 0.8;
	}

	// move the camera instantly to a point or an object
	*snapTo{|object|
		cameraObject = object;
		cameraIndex  = 0;
	}

	// pan the camera to a point or an object
	*panTo{|object, time=1, curve=6|
		lastCameraPos  = cameraPos.copy;
		cameraObject   = object;
		cameraPanCurve = curve;
        cameraTime     = time;
		cameraIndex    = time;
	}

	// shake the camera
	*shake{|pixels = 30, coefficient = 0.95, freq=60|
		if (pixels>=cameraShake) {
			cameraShake      = pixels;
			shakeCoefficient = coefficient;
			shakeFreq        = freq;
		};
	}

	// zoom, value = 1 is normal, 2 is double size, etc. (centre of the screen remains fixed on cameraObject)
	*zoom_{|value|
		zoom = value * globalZoom;
		if (cameraAngle!=0){ this.updateDrawBounds };
	}

	// log zoom, 0 = is normal, 1 = everything is double the size, -1 = everything is half the size
	*logZoom_{|value|
		zoom = (2**value) * globalZoom;
		if (cameraAngle!=0){ this.updateDrawBounds };
	}

	// rotate the camera by the angle, 0 = normal, pi = upsidedown
	*angle_{|angle,margin = 0|
		cameraAngle  = angle;
		cameraMargin = margin; // set a margin so we dont see past the world edge
		this.updateDrawBounds;
	}

	// using background alpha as a crude motion blur
	*motionBlur_{|alpha| motionBlur = alpha.clip(0.0,1.0) }

	// set the screen to be a color
	*setToBlack{ this.setToColor(Color.black ) }
	*setToWhite{ this.setToColor(Color.white ) }
	*setToClear{ this.setToColor(Color.clear ) }
	*setToColor{|color|
		previousColor = color.copy;
		currentColor  = color.copy;
		nextColor     = color.copy;
		fadeIndex     = 0;
		fadeTime      = 1;
	}

	// fade to a color
	*fadeToBlack{|time=1, curve=1| this.fadeToColor(Color.black, time, curve) } // i need to be able to do this in either RT or WT
	*fadeToWhite{|time=1, curve=1| this.fadeToColor(Color.white, time, curve) }
	*fadeToClear{|time=1, curve=1| this.fadeToColor(Color.clear, time, curve) }
	*fadeToColor{|color, time=1, curve=1|
		fadeCurve     = curve;
		previousColor = currentColor.copy;
		nextColor     = color.copy;
		fadeIndex     = time;
		if (time == 0 ) { fadeTime = 1 } { fadeTime = time }; // to avoid div0
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// set up camera
	*init{
		if (verbose) { "World_Camera:init".postln };
		cameraPos     = Point();
		lastCameraPos = Point();
		cameraAngle   = 0;
		previousColor = Color.clear;
		currentColor  = Color.clear;
		nextColor     = Color.clear;
	}

	// get the camera zoom value, which is different to the actual zoom value because its adjusted by screen res in globalZoom
	*zoom   { ^ zoom / globalZoom }
	*logZoom{ ^(zoom / globalZoom).logN(2) }

	// extend the bounds of draw area to account for the rotation
	*updateDrawBounds{
		var x = rightEdge - leftEdge;
		var y = bottomEdge - topEdge;
		var a = cameraAngle.fold(0,pi/2);
		rotBoundX = (((x * cos(a)) - (y * sin(a))).abs - x)*0.5;
		rotBoundY = (((x * sin(a)) + (y * cos(a))).abs - y)*0.5;
	}

	// set the bounds in which the camera can move
	*setCameraMaximumBounds{
		cameraMaxLeft     = screenWidth  * 0.5;                 // left clip edge for camera
		cameraMaxTop      = screenHeight * 0.5;                 // top clip edge for camera
		cameraMaxRight    = worldRight  - (screenWidth  * 0.5); // right clip edge for camera
		cameraMaxBottom   = worldBottom - (screenHeight * 0.5); // bottom clip edge for camera
	}

	// update camera position
	*tick{
		if (cameraObject.notNil) {
			// update camera to the latest position of the object
			if (cameraObject.isKindOf(Point)){
				cameraPos.replace(cameraObject);
			}{
				if (cameraObject.isKindOf(Function)){
					cameraPos.replace(cameraObject.value);
				}{
					cameraPos.replace(cameraObject.origin);
				};
			};
			// clip camera view area to edges of world
			cameraMaxLeft   = halfScreenWidth  / zoom;
			cameraMaxTop    = halfScreenHeight / zoom;
			cameraMaxRight  = worldRight  - (halfScreenWidth / zoom);
			cameraMaxBottom = worldBottom - (halfScreenHeight / zoom);
			cameraPos.clipInPlace(cameraMaxLeft  + cameraMargin, cameraMaxTop    + cameraMargin,
				                  cameraMaxRight - cameraMargin, cameraMaxBottom - cameraMargin);
			// do panning
			if (cameraIndex>0) {
				var ratio;
				cameraIndex = (cameraIndex - frameLength).clip(0,inf);
				ratio = (cameraIndex/cameraTime).lincurve(curve:cameraPanCurve);
				cameraPos.scaleByValue(1 - ratio).addMul(lastCameraPos,ratio);
			};
			// add shake
			if (cameraShake>=0.1) {
				var angle = (worldTime * shakeFreq       ).heatMap * 2pi;
				var mag   = (worldTime * shakeFreq + 1000).heatMap * 2 - 1 * cameraShake;
				var x     = mag * cos(angle);
				var y     = mag * sin(angle);
				cameraPos.addXY(x,y);
				cameraShake = cameraShake * (shakeCoefficient ** timeDilation);
			};
			// transposition for Pen.translate so cameraPos is in the centre of the screen
			transX = halfScreenWidth  - (cameraPos.x * zoom);
			transY = halfScreenHeight - (cameraPos.y * zoom);
			// if zoomed out so far the world is smaller than the screen then move to centre
			if (centreOnScreen) {
				if (worldWidth  * zoom < screenWidth ) { transX = transX + ((screenWidth  - (worldWidth  * zoom)) * 0.5) };
				if (worldHeight * zoom < screenHeight) { transY = transY + ((screenHeight - (worldHeight * zoom)) * 0.5) };
			};
			// off screen culling edges, used by DrawFunc(s) to avoid drawing entities that aren't on the screen
			leftEdge   = transX.neg / zoom;
			topEdge    = transY.neg / zoom;
			rightEdge  = (screenWidth - transX ) /  zoom;
			bottomEdge = (screenHeight - transY ) / zoom;
			// actual offscreen edges after adjusting for rotation
			actualLeftEdge   = leftEdge    + rotBoundX;
			actualTopEdge    = topEdge     - rotBoundY;
			actualRightEdge  = rightEdge   - rotBoundX;
			actualBottomEdge = bottomEdge  + rotBoundY;
			// and parallax scrolling for the background
			if (sceneState[\background].isSymbol) {
				backX = cameraMaxLeft - cameraPos.x / (worldWidth - cameraMaxLeft - cameraMaxLeft)
				    * ( imageBounds[sceneState[\background]].width * globalZoom - screenWidth);
				backY = cameraMaxTop - cameraPos.y / (worldHeight - cameraMaxTop - cameraMaxTop)
				    * ( imageBounds[sceneState[\background]].height * globalZoom - screenHeight);
			};
		};
		// do fades
		if (fadeIndex>0) {
			// TODO if (dilation)
			if (false) { fadeIndex = (fadeIndex - frameLength).clip(0,inf) } {
				fadeIndex = (fadeIndex - staticFrameLength).clip(0,inf)
			};
			currentColor.replaceBlend(nextColor, previousColor, (fadeIndex/fadeTime).lincurve(curve:fadeCurve) );
		};
	}

	// draw the overlay
	*draw{
		if (currentColor.alpha<=0) {^this};
		Pen.fillColor_(currentColor).addRect(screenRect).draw(0);
	}

	*isOpaque     { ^currentColor.alpha >=1 }
	*isTransparent{ ^currentColor.alpha < 1 }
	*alpha        { ^currentColor.alpha     }

	// transform a point on the UserView to a point in the World
	*screenSpaceToWorldSpace{|point|
		var sinr = cameraAngle.neg.sin;
		var cosr = cameraAngle.neg.cos;
		var x = point.x - transX / zoom - cameraPos.x;
		var y = point.y - transY / zoom - cameraPos.y;
		^Point((x * cosr) - (y * sinr) + cameraPos.x, (y * cosr) + (x * sinr) + cameraPos.y);
	}

	// transform a point in the World to a point on the UserView
	*worldSpaceToScreenSpace{|point|
		var sinr = cameraAngle.sin;
		var cosr = cameraAngle.cos;
		var x = point.x - cameraPos.x;
		var y = point.y - cameraPos.y;
		^Point((x * cosr) - (y * sinr) + cameraPos.x * zoom + transX, (y * cosr) + (x * sinr) + cameraPos.y * zoom + transY);
	}

	// transform a point from the view to the world (with no rotation)
	*screenSpaceToWorldSpaceNR{|point| ^Point( (point.x - transX) / zoom, (point.y - transY) / zoom)  }

	// transform a point from the world to the view (with no rotation)
	*worldSpaceToScreenSpaceNR{|point| ^Point( point.x * zoom + transX,  point.y * zoom + transY  ) }

	// TODO
	*worldSpaceToMapSpace{}
	*screenSpaceToMapSpace{}
	*mapSpaceToWorldSpace{}
	*mapSpaceToScreenSpace{}

}
