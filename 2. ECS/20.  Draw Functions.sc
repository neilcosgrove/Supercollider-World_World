////////////////////////////////////////////////////////////////////////////////////////////////////////
//                              Draw Function Components                                              //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// super class of all draw functions

// do i change draw to drawFunc ands draw to draw

World_DrawFunc : World_Component {

	var <layer, <visible=true, <origin, <boundingBox;

	// off screen culling = test entity is on the screen before drawing it
	draw{
		if (boundingBox.left   > actualRightEdge  ) {^this};
		if (boundingBox.right  < actualLeftEdge   ) {^this};
		if (boundingBox.top    > actualBottomEdge ) {^this};
		if (boundingBox.bottom < actualTopEdge    ) {^this};
		this.drawFunc;
	}

	initDrawFunc{|argLayer|
		layer         = argLayer ? layer;
		layers[layer] = layers[layer].add(this);
		origin        = parent.origin;
		boundingBox   = parent.boundingBox;
	}

	// is object visible
	visible_{|bool|
		if (visible!=bool) {
			visible = bool;
			if (visible) {
				layers[layer] = layers[layer].add(this);
			}{
				layers[layer].remove(this);
			};
		};
	}

	// move to a different layer
	layer_{|value|
		layers[layer].remove(this);
		layer = value;
		layers[layer] = layers[layer].add(this);
	}

	// move this object to the top of the current layer
	moveToTop{
		layers[layer].remove(this);
		layers[layer] = layers[layer].add(this);
	}

	// move this object to the bottom of the current layer
	moveToBottom{
		layers[layer].remove(this);
		layers[layer].insert(0,this);
	}

	// and free this instance
	free{
		layers[layer].remove(this);
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Circles                                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Circle_DrawFunc : World_DrawFunc {

	var <>fillColor, <>strokeColor, <>penWidth = 2, <>fill = true, <>stroke = true;

	*new{|parent, layer, fillColor, strokeColor|
		^super.new.initComponent(parent).initDrawFunc(layer).init(fillColor, strokeColor)
	}

	init{|argFillColor, argStrokeColor|
		fillColor    = argFillColor   ?? {grey.copy};
		strokeColor  = argStrokeColor ?? {white.copy};
	}

	drawFunc{
		if (fill) {
			if (stroke) {
				Pen.width_(penWidth).strokeColor_(strokeColor).fillColor_(fillColor).addOval(boundingBox).draw(3)
			}{
				Pen.fillColor_(fillColor).addOval(boundingBox).draw(0);
			}
		}{
			if (stroke) { Pen.width_(penWidth).strokeColor_(strokeColor).addOval(boundingBox).draw(2);}
		};
	}

}

World_Wobbly_Circle_DrawFunc : World_Circle_DrawFunc {

	var <>startTime = 0, <>freq1 = 2, <>freq2 = 8, <>freq3 = 15;

	drawFunc{
		var time = worldTime - startTime;
		var colorValue  = 1 - (time * freq1).fold(0,1);
		var colorValue2 = 1 - (time * freq2).fold(0,1);
		tempRect.replace(boundingBox).insetByReplace( (time*freq3 * 10).fold(-5,5), (time*freq3 * 10 + 10).fold(-5,5) );
		Pen.width_(penWidth)
		.strokeColor_(
			tempColor.replace(strokeColor.red * colorValue, strokeColor.green * colorValue, strokeColor.blue * colorValue,1)
		)
		.fillColor_(
			tempColor.replace(fillColor.red * colorValue2, fillColor.green * colorValue2, fillColor.blue * colorValue2,1)
		)
		.addOval(tempRect).draw(3);
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Rectangles                                                      //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Rect_DrawFunc : World_DrawFunc {

	var <>fillColor, <>strokeColor, <>penWidth = 2, <>fill = true, <>stroke = true;

	*new{|parent, layer, fillColor, strokeColor|
		^super.new.initComponent(parent).initDrawFunc(layer).init(fillColor, strokeColor)
	}

	init{|argFillColor, argStrokeColor|
		fillColor    = argFillColor   ?? {grey.copy};
		strokeColor  = argStrokeColor ?? {white.copy};
	}

	drawFunc{
		if (fill) {
			if (stroke) {
				Pen.width_(penWidth).strokeColor_(strokeColor).fillColor_(fillColor).addRect(boundingBox,20,20).draw(3)
			}{
				Pen.fillColor_(fillColor).addRect(boundingBox,20,20).draw(0);
			}
		}{
			if (stroke) { Pen.width_(penWidth).strokeColor_(strokeColor).addRect(boundingBox,20,20).draw(2) }
		}
	}

}

World_Rounded_Rect_DrawFunc : World_Rect_DrawFunc {

	var <>dx = 40, <>dy = 40;

	drawFunc{
		if (fill) {
			if (stroke) {
				Pen.width_(penWidth).strokeColor_(strokeColor).fillColor_(fillColor)
				    .roundedRect(boundingBox,20,20).draw(3)
			}{
				Pen.fillColor_(fillColor).roundedRect(boundingBox,dx,dy).draw(0);
			}
		}{
			if (stroke) { Pen.width_(penWidth).strokeColor_(strokeColor).roundedRect(boundingBox,20,20).draw(2) }
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    Triangles                                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Triangle_DrawFunc : World_DrawFunc {

	classvar  <corners = #[\bottomLeft, \bottomRight, \topRight, \topLeft ]; // which corner has the right-angle?
	var <>fillColor, <>strokeColor, <>penWidth = 2, <>fill = true, <>stroke = true;
	var <p1, <p2, <p3, <corner = 0;

	*new{|parent, layer, corner, fillColor, strokeColor|
		^super.new.initComponent(parent).initDrawFunc(layer).init(corner, fillColor, strokeColor)
	}

	init{|argCorner, argFillColor, argStrokeColor|
		fillColor    = argFillColor   ?? {grey.copy};
		strokeColor  = argStrokeColor ?? {white.copy};
		corner       = (argCorner.isSymbol.if { corners.indexOf(argCorner) } { argCorner ? corner });
		p1           = Point();
		p2           = Point();
		p3           = Point();
		this.updatePoints;
	}

	updatePoints{
		var l = boundingBox.left;
		var r = boundingBox.right;
		var t = boundingBox.top;
		var b = boundingBox.bottom;
		switch (corner,
			0 , { p1.replaceXY(l,b); p2.replaceXY(l,t); p3.replaceXY(r,b) },
			1 , { p1.replaceXY(r,b); p2.replaceXY(l,b); p3.replaceXY(r,t) },
			2 , { p1.replaceXY(r,t); p2.replaceXY(r,b); p3.replaceXY(l,t) },
			3 , { p1.replaceXY(l,t); p2.replaceXY(r,t); p3.replaceXY(l,b) }
		);
	}

	corner_{|argCorner|
		corner = (argCorner.isSymbol.if { corners.indexOf(argCorner) } { argCorner ? corner });
		this.updatePoints;
	}

	drawFunc{
		Pen.width_(penWidth);
		Pen.moveTo(p1).lineTo(p2).lineTo(p3).lineTo(p1);
		if (fill) {
			if (stroke) {
				Pen.fillColor_(fillColor).strokeColor_(strokeColor).width_(penWidth).fillStroke;
			}{
				Pen.fillColor_(fillColor).fill;
			};
		} {
			if (stroke) { Pen.strokeColor_(strokeColor).width_(penWidth).stroke };
		};
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Lines                                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Line_DrawFunc : World_DrawFunc {

	var <>strokeColor, <>penWidth = 2;
	var <p1, <p2;

	*new{|parent, layer, p1, p2, strokeColor|
		^super.new.initComponent(parent).initDrawFunc(layer).init(p1, p2, strokeColor)
	}

	init{|argP1, argP2, argStrokeColor|
		strokeColor  = argStrokeColor ?? {white.copy};
		p1           = argP1;
		p2           = argP2;
	}

	drawFunc{
		Pen.moveTo(p1).lineTo(p2).strokeColor_(strokeColor).width_(penWidth).stroke;
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Sprites                                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Sprite_DrawFunc : World_DrawFunc {

	var <>image, <>opacity=1.0, <>angle = 0;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

 	init{|argImage| image = argImage }

 	drawFunc{
 		Pen.use{
 			Pen.rotate( angle, origin.x, origin.y).prDrawImage( tempPoint.leftTop( boundingBox ),
				images[image], nil, 0, opacity
			);
 		};
	}

}

// Draw A Rotating Sprite, rotates based on the worldTime (used by topdown player bullets)

World_Rotating_Sprite_DrawFunc : World_DrawFunc {

	var <>image, <>opacity=1.0, <>phase = 0, <>freq = 10;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

 	init{|argImage|
		image = argImage;
		phase = 2pi.rand;
		freq  = 5.rrand(25.0);
	}

	drawFunc{
 		Pen.use{
			Pen.rotate( (worldTime * freq + phase).wrap(0,2pi), origin.x, origin.y).prDrawImage(
				tempPoint.leftTop( boundingBox ), images[image], nil, 0, opacity
			);
 		};
 	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                       Tiles                                                        //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Static_Tile_DrawFunc : World_DrawFunc {

	var <image, <>opacity=1.0, <oneByOne, <cell, <left, <right, <top, <bottom, <asset;

 	*new{|parent,layer,image,dx=0,dy=0| ^super.new.initComponent(parent).initDrawFunc(layer).init(image, dx, dy) }

 	init{|argImage, dx, dy|
		oneByOne    = ((boundingBox.width > ugp.cellWidth) or: { boundingBox.height > ugp.cellHeight }).not;
		cell        = ugp.getCell(origin);
		image       = argImage;
		boundingBox.addWH(dx,dy); // all tiles are 1 pixel wider and taller so we dont see gaps whem we zoom in & out
		left        = boundingBox.left;
		right       = boundingBox.right;
		top         = boundingBox.top;
		bottom      = boundingBox.bottom;
		asset       = images[image];
	}

	image_{|argImage| image = argImage; asset = images[image] }

	// the most drawn entity so as fast as possible please
	draw{
		if (left   > actualRightEdge  ) {^this};
		if (right  < actualLeftEdge   ) {^this};
		if (top    > actualBottomEdge ) {^this};
		if (bottom < actualTopEdge    ) {^this};
		if (oneByOne and: { ((lightMap.staticLightMap[cell] + lightMap.dynamicLightMap[cell])==0) && lightMap.isOn }) { ^this };
		Pen.prTileImage(boundingBox, asset, nil, 0, opacity);
	}

}

World_Static_SingleTile_DrawFunc : World_DrawFunc {

	var <image, <>opacity=1.0, <oneByOne, <cell, <boundingBox, <topLeft, <left, <right, <top, <bottom, <asset;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

 	init{|argImage|
		topLeft     = Point().leftTop(boundingBox);
		oneByOne    = ((boundingBox.width > ugp.cellWidth) or: { boundingBox.height > ugp.cellHeight }).not;
		cell        = ugp.getCell(origin);
		image       = argImage;
		left        = boundingBox.left;
		right       = boundingBox.right;
		top         = boundingBox.top;
		bottom      = boundingBox.bottom;
		asset       = images[image];
	}

	image_{|argImage| image = argImage; asset = images[image] }

	// most drawn entity so as fast as possible please
	draw{
		if (left   > actualRightEdge  ) {^this};
		if (right  < actualLeftEdge   ) {^this};
		if (top    > actualBottomEdge ) {^this};
		if (bottom < actualTopEdge    ) {^this};
		if (oneByOne and: { ((lightMap.staticLightMap[cell]+lightMap.dynamicLightMap[cell])==0)&&lightMap.isOn}) { ^this };
		Pen.prDrawImage(topLeft, asset, nil, 0, opacity);
	}

}

World_Static_DecorTile_DrawFunc : World_DrawFunc {

	var <image, <>opacity=1.0, <oneByOne, <cell, <leftTop, <asset;

 	*new{|parent, layer, image| ^super.new.initComponent(parent).initDrawFunc(layer).init(image) }

 	init{|argImage|
		leftTop         = boundingBox.leftTop;
		oneByOne        = ((boundingBox.width > ugp.cellWidth) or: { boundingBox.height > ugp.cellHeight }).not;
		cell            = ugp.getCell(origin);
		image           = argImage;
		asset           = images[image];
	}

	image_{|argImage| image = argImage; asset = images[image] }

 	drawFunc{
		if (oneByOne and: { ((lightMap.staticLightMap[cell]+lightMap.dynamicLightMap[cell])==0)&&lightMap.isOn}) { ^this };
		Pen.prDrawImage(leftTop, asset, nil, 0, opacity);
	}

}
