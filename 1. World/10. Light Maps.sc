////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                             Lighting                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Light_Map : World_World {

	var <>isOn = false;
	var <lightMapWidth,   <lightMapHeight,    <cellWidth,       <cellHeight,   <>lightMapColor;
	var <staticLightMap,  <dynamicLightMap,   <lightMapRects;

	*new{|width, height, cellWidth, cellHeight, color| ^super.new.init(width, height, cellWidth, cellHeight, color) }

	init{|width, height, cWidth, cHeight, color|
		if (verbose) { "World_Light_Map:init".postln };
		lightMapWidth   = width;
		lightMapHeight  = height;
		cellWidth       = cWidth;
		cellHeight      = cHeight;
		lightMapColor   = color ? black.copy;
		staticLightMap  = FloatArray.newClear(lightMapWidth * lightMapHeight);
		dynamicLightMap = FloatArray.newClear(lightMapWidth * lightMapHeight);
		lightMapRects   = Array.new(lightMapWidth * lightMapHeight);
		lightMapHeight.do{|y|
			lightMapWidth.do{|x|
				lightMapRects = lightMapRects.add(Rect(x*cellWidth, y*cellHeight, cellWidth, cellHeight ));
			}
		};
		this.clearStaticLightMap;
		this.clearDynamicLightMap;
	}

	//////////////////////////
	// static light sources //
	//////////////////////////

	// clear
	clearStaticLightMap{ staticLightMap.fill(0) }

	// put
	addInStaticCell{|x,y,value|
		var cell = y * lightMapWidth + x;
		staticLightMap[cell] = (staticLightMap[cell] + value).clip(0,1);
	}

	// inverse square law
	addStaticLightSourceISL{|x, y, luminosity=3, diameter=12|
		var x0 = x / cellWidth;
		var y0 = y / cellHeight;
		var x1 = x0.asInteger;
		var y1 = y0.asInteger;
		for     ((x1 - diameter).clip(0,lightMapWidth -1), (x1 + diameter).clip(0,lightMapWidth -1), {|x2|
			for ((y1 - diameter).clip(0,lightMapHeight-1), (y1 + diameter).clip(0,lightMapHeight-1), {|y2|
				this.addInStaticCell(x2, y2, (1/(x0-(x2+0.5)).hypot(y0-(y2+0.5)) * luminosity) ** 2 );
			})
		});
	}

	// a user defined light func
	staticLightFunc{|func|
		for     (0, lightMapWidth  - 1, {|x|
			for (0, lightMapHeight - 1, {|y|
				var cell = y * lightMapWidth + x;
				var value = staticLightMap[cell];
				staticLightMap[cell] = func.value(value, x/(lightMapWidth  - 1), y/( lightMapHeight - 1) ).clip(0,1);
			})
		});
	}

	// ball shaped
	addStaticLightSource{|x, y, luminosity=3, diameter=12, exp=0.5|
		var x0 = x / cellWidth;
		var y0 = y / cellHeight;
		var x1 = x0.asInteger;
		var y1 = y0.asInteger;
		for     ((x1 - diameter).clip(0,lightMapWidth -1), (x1 + diameter).clip(0,lightMapWidth -1), {|x2|
			for ((y1 - diameter).clip(0,lightMapHeight-1), (y1 + diameter).clip(0,lightMapHeight-1), {|y2|
				this.addInStaticCell(x2, y2,
					((((x0-(x2+0.5)).hypot(y0-(y2+0.5))**exp)/(diameter**exp)).neg+1).clipNeg * luminosity
				);
			})
		});
	}

	// box shaped
	addStaticLightRect{|l, t, r, b, tlVal=1, trVal=1, blVal=1, brVal=1|
		l = l.div(cellWidth).clip(0,lightMapWidth-1);
		r = r.div(cellWidth).clip(0,lightMapWidth-1);
		t = (t/cellHeight).ceil.asInteger.clip(0,lightMapHeight-1);
		b = (b/cellHeight).ceil.asInteger.clip(0,lightMapHeight-1);
		for (l, r, {|x|
			var ratioX = (x -l) / (r - l);
			for (t, b, {|y|
				var ratioY = (y -t) / (b - t);
				this.addInStaticCell(x, y,
					tlVal.mix( trVal,ratioX ).mix( blVal.mix( brVal,ratioX ), ratioY)
				);
			})
		});
	}

	///////////////////////////
	// dynamic light sources //
	///////////////////////////

	// dynamic light sources
	clearDynamicLightMap{ dynamicLightMap.fill(0) }

	addInDynamicCell{|x,y,value|
		var cell = y * lightMapWidth + x;
		dynamicLightMap[cell] = dynamicLightMap[cell] + value;
	}

	// ball shaped
	addDynamicLightSource{|x, y, luminosity=2, diameter=12, exp=0.5|
		var x0 = x / cellWidth;
		var y0 = y / cellHeight;
		var x1 = x0.asInteger;
		var y1 = y0.asInteger;
		for     ((x1 - diameter).clip(0,lightMapWidth -1), (x1 + diameter).clip(0,lightMapWidth -1), {|x2|
			for ((y1 - diameter).clip(0,lightMapHeight-1), (y1 + diameter).clip(0,lightMapHeight-1), {|y2|
				this.addInDynamicCell(x2, y2,
					((((x0-(x2+0.5)).hypot(y0-(y2+0.5))**exp)/(diameter**exp)).neg+1).clipNeg * luminosity
				)
			})
		});
	}

	// contrast the light map so light is ligher and dark is darker
	contrastStaticLightMap{|scale=1.5,min=0.1,max=0.9|
		staticLightMap.do{|value,i|
			staticLightMap[i] = (staticLightMap[i]*2-1*scale).tanh+1/2;
			if (value<min) { staticLightMap[i]=0.0 } { if (value>max) { staticLightMap[i]=1.0 } };
		};
	}


	scaleAndClipStaticLightMap{|scale=1.5,min=0,max=1|
		staticLightMap.do{|value,i| staticLightMap[i] = (staticLightMap[i]*scale).clip(min,max) }
	}

	////////////////////////
	// draw light sources //
	////////////////////////

	// draw light map (this is slightly faster)
	draw{
		var x1 = actualLeftEdge.div(cellWidth).clip(0, lightMapWidth - 1);
		var x2 = (actualRightEdge.div(cellWidth) + 1).clip(0, lightMapWidth - 1) - 1;
		var xEnd = lightMapWidth-2;
		var y1 = actualTopEdge.div(cellHeight).clip(0, lightMapHeight - 1);
		var y2 = actualBottomEdge.div(cellHeight).clip(0, lightMapHeight - 1);
		for (y1, y2, {|y|
			var c = 1;
			var cell = y * lightMapWidth + x1;
			var light = (staticLightMap[cell] + dynamicLightMap[cell]).clip(0,1);
			for (x1, x2, {|x|
				var cell2 = y * lightMapWidth + x + 1;
				var light2 = (staticLightMap[cell2] + dynamicLightMap[cell2]).clip(0,1);
				if ((light == light2) and: {x < x2 }) {
					c=c+1;
				}{
					if (c==1) {
						if (light<1) {
							//Pen.strokeColor_(Color.red);
							Pen.fillColor_(lightMapColor.alpha_(1 - light)).addRect(lightMapRects[cell]).draw(0); // 3
						};
					}{
						if (light<1) {
							tempRect.replace(lightMapRects[cell - c + 1]);
							tempRect.width_(c*cellWidth);
							//Pen.strokeColor_(Color.red);
							Pen.fillColor_(lightMapColor.alpha_(1 - light)).addRect(tempRect).draw(0); // 3
						};
						c=1;
					};
				};
				cell = cell2;
				light = light2;
			});
			if (x2==xEnd) {
				//Pen.strokeColor_(Color.red);
				Pen.fillColor_(lightMapColor.alpha_(1 - light)).addRect(lightMapRects[cell]).draw(0); // 3
			};
		});
	}

	// TODO move this into its own object

	// draw overlay (colored radial gradient)
	drawOverlay{
		var point = sceneState[\overlayOrigin].value;
		Pen.addRect( Rect(actualLeftEdge, actualTopEdge,
			actualRightEdge - actualLeftEdge,actualBottomEdge - actualTopEdge)
		);
		Pen.fillRadialGradient(point, point,
			sceneState[\overlayStart], sceneState[\overlayStart] + sceneState[\overlayFade],
			sceneState[\overlayLight], sceneState[\overlayDark]
		);
	}

	/*
		sceneState[\overlay]        = false;
		sceneState[\overlayStart]   = 450/2;
		sceneState[\overlayFade]    = 650/2;
		sceneState[\overlayLight]   = Color(0,0,0,0);
		sceneState[\overlayDark]    = Color(0,0,0,1);
		sceneState[\overlayOrigin]  = {players[0].origin};
	*/

}
