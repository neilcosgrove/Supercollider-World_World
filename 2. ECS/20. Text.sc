////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                       Text                                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////

World_Text : World_Entity {

	*new{|origin, layer, string, font, color, shadowColor|
		^super.new.initEntityAboutPoint(origin,100,25).init(layer,string,font,color,shadowColor)
	}

	init{|layer, string, font, color, shadowColor|
		components[\drawFunc] = World_Text_At_Origin_DrawFunc(this,layer,string,font,color,shadowColor)
	}

	alpha_ {|alpha|  components[\drawFunc].alpha_(alpha)   }
	string_{|string| components[\drawFunc].string_(string) }

}

// draw some text relative to parent.origin

World_Text_At_Origin_DrawFunc : World_DrawFunc {

	var <>dx=0, <>dy=0, <>width, <>height, <string, <>font, <>color, <>shadowColor, <>shadowDepth = 2;

 	*new{|parent, layer, string, font, color, shadowColor|
		^super.new.initComponent(parent).initDrawFunc(layer).init(string,font,color,shadowColor)
	}

 	init{|argString, argFont, argColor, argShadowColor|
		var bounds;
		string      = argString;
		font        = argFont  ?? {Font.default};
		bounds      = string.bounds(font);
		width       = bounds.width;
		height      = bounds.height;
		color       = argColor       ?? {Color.white};
		shadowColor = argShadowColor ?? {Color.black};
	}

	alpha_{|alpha|
		color.alpha_(alpha);
		if (shadowColor.notNil) { shadowColor.alpha_(alpha*0.66) };
	}

	string_{|argString|
		var bounds;
		string      = argString;
		bounds      = string.bounds(font);
		width       = bounds.width;
		height      = bounds.height;
	}

 	drawFunc2{
		Pen.stringCenteredIn(string, tempRect.aboutPointOffset(origin, dx - shadowDepth, dy - shadowDepth,
			width, height), font, shadowColor);
		Pen.stringCenteredIn(string, tempRect.replaceMoveBy(shadowDepth,shadowDepth), font, color);
	}

	drawFunc1{
		Pen.stringCenteredIn(string, tempRect.aboutPointOffset(origin, dx, dy, width, height), font, color);
	}

 	drawFunc{
		Pen.stringCenteredIn(string, tempRect.aboutPointOffset(origin, dx - shadowDepth, dy - shadowDepth,
			width, height), font, shadowColor);
		Pen.stringCenteredIn(string, tempRect.replaceMoveBy(shadowDepth*2,0), font, shadowColor);
		Pen.stringCenteredIn(string, tempRect.replaceMoveBy(0,shadowDepth*2), font, shadowColor);
		Pen.stringCenteredIn(string, tempRect.replaceMoveBy(shadowDepth*(-2),0), font, shadowColor);
		Pen.stringCenteredIn(string, tempRect.aboutPointOffset(origin, dx, dy, width, height), font, color);
	}

}
