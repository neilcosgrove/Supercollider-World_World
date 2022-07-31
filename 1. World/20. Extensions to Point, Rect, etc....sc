////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                         Extensions                                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// most of these operations are in place to improve cpu performance
// for example point.replaceXY(2,3) is twice as fast as Point(2,3)

Vector : Point {} // a vector is a point. Vector is used just for clarity. there is no difference between Vector & Point

+ Point {
	+= {|point| x = x + (point.x); y = y + (point.y) } // add in place
	-= {|point| x = x - (point.x); y = y - (point.y) } // minus in place
	*= {|point| x = x * (point.x); y = y * (point.y) } // multi in place
	/= {|point| x = x / (point.x); y = y / (point.y) } // div in place
	dotProduct {|point| ^(point.x * x) + (point.y * y) }
	dot{|point| ^(point.x * x) + (point.y * y) }
	crossProduct {|point| ^(point.x * x) - (point.y * y) }
	cross{|point| ^(point.x * x) - (point.y * y) }
	normal{|point2| var theta = atan2( point2.y - y,  point2.x - x); ^Vector(cos(theta), sin(theta) ) }
	tangent{|point2| var theta = atan2( point2.y - y,  point2.x - x); ^Vector(sin(theta).neg, cos(theta) ) }
	scaleAsPolar{|scale| var rho = hypot(x, y) * scale; var theta = atan2(y, x); ^Point( rho * cos(theta), rho * sin(theta) ) }
	scaleAsPolarReplace{|scale| var rho = hypot(x,y) * scale; var theta = atan2(y,x); x = rho * cos(theta); y = rho * sin(theta)}
	scaleReplace{|point| x = x * point.x; y = y * point.y }
	scaleByValue {|scale| x = x * scale; y = y * scale }
	addMul {|point,scale| x = x + (point.x * scale); y = y + (point.y * scale) } // add a scaled value in place
	mulPower {|point,scale| x = x * (point.x ** scale); y = y * (point.y ** scale) } // mul & power scaled value in place
	wrapInPlace {|l,t,r,b| x = x.wrap(l,r); y = y.wrap(t,b) }
    clipInPlace {|l,t,r,b| x = x.clip(l,r); y = y.clip(t,b) }
	clipVelocity {|point| x = x.clip(point.x.neg, point.x); y = y.clip(point.y.neg, point.y) }
	clipVelocityValue {|value| x = x.clip(value.neg, value); y = y.clip(value.neg, value) }
	clipToZero {|thresh = 0.1| if ((x.abs < thresh) and: {y.abs < thresh}) {x=0; y=0} }
	clipXYToZero {|thresh = 0.1| if (x.abs < thresh) {x=0}; if (y.abs < thresh) {y=0} }
	makeSquare { x=x.clip(y.abs.neg,y.abs); y=y.clip(x.abs.neg,x.abs) }
	average {|point1,point2| x = (point1.x + point2.x) * 0.5; y = (point1.y + point2.y) * 0.5 }
	*sum3rand {|x = 1.0, y = 1.0| ^this.new( x.sum3rand, y.sum3rand ); }
	replace {|point| x = point.x; y = point.y }
	replaceXY {|newX,newY| x = newX; y = newY }
	replaceX {|newX| x = newX }
	replaceY {|newY| y = newY }
	leftTop {|rect| x = rect.left;  y = rect.top }
	rightTop {|rect| x = rect.right; y = rect.top }
	leftBottom {|rect| x = rect.left;  y = rect.bottom }
	rightBottom {|rect| x = rect.right; y = rect.bottom }
	addXY {|x1,y1| x = x + x1; y = y + y1 }
	subXY {|x1,y1| x = x - x1; y = y - y1 }
	absdif {|p1,p2| x = p1.x.absdif(p2.x); y = p2.y.absdif(p2.y) }
	flipX { x = x.neg }
	flipY { y = y.neg }
	flipXY { x = x.neg; y = y.neg }
	thetaFromPoint {|point| ^atan2(y - point.y, x - point.x) }
	thetaToPoint {|point| ^atan2(point.y - y, point.x - x) }
	distance {|point| ^hypot(x - point.x, y - point.y) }
	hypot {|point| ^hypot(x - point.x, y - point.y) }
	sumsqr {|point| ^sumsqr(x - point.x, y - point.y) }
	isMoving { if (x!=0) {^true}; if (y!=0) {^true}; ^false }
	maxCord { ^x.max(y) }
	minCord { ^x.min(y) }
	averageCord { ^( (x + y) * 0.5) }
	angle { ^this.theta }
	fromPolar {|rho,theta| x = rho * cos(theta); y = rho * sin(theta) }
	*fromPolar {|rho,theta| ^this.new( rho * cos(theta), rho * sin(theta) ) }
	*randPolar {|max| var theta = 2pi.rand; max = max.rand;  ^this.new( max * cos(theta), max * sin(theta) ) }
	*randRangePolar {|min,max| var theta = 2pi.rand, rho = min.rrand(max); ^this.new( rho * cos(theta), rho * sin(theta) ) }
	*rand { | x = 1.0, y = 1.0 | ^this.new( x.rand,  y.rand ); }
	*rand2{ | x = 1.0, y = 1.0 | ^this.new( x.rand2, y.rand2 ); }
	aboveLine{|p1,p2| if (p1.x == p2.x) {^nil}; ^this.y < this.x.map(p1.x,p2.x,p1.y,p2.y) }
	leftLine {|p1,p2| if (p1.y == p2.y) {^nil}; ^this.x < this.y.map(p1.y,p2.y,p1.x,p2.x) }

	// wslib 2010
	// additional operators to make Point more flexible and powerful
	// this will let you do things like...Point(1.1,2.2).floor; Point(2,3).rand or Point(5,6).pow(3,2)
	// method below adapted for World_World to work in place (i.e. run a little faster = 2.8 x faster)

	performOnEach { arg selector ...args;
		var size = args.size;
		var argsList = Array.newClear(size);
		size.do{|i| argsList[i] = args[i].wrapAt(0) };
		x = x.performList( selector, argsList );
		size.do{|i| argsList[i] = args[i].wrapAt(1) };
		y = y.performList( selector, argsList);
	}
	// unary ops
	sign { ^this.performOnEach( thisMethod.name ) }
	neg { ^this.performOnEach( thisMethod.name ) }
	ceil { ^this.performOnEach( thisMethod.name ) }
	floor { ^this.performOnEach( thisMethod.name ) }
	frac { ^this.performOnEach( thisMethod.name ) }
	squared { ^this.performOnEach( thisMethod.name ) }
	cubed { ^this.performOnEach( thisMethod.name ) }
	sqrt { ^this.performOnEach( thisMethod.name ) }
	exp { ^this.performOnEach( thisMethod.name ) }
	reciprocal { ^this.performOnEach( thisMethod.name ) }

	log { ^this.performOnEach( thisMethod.name ) }
	log2 { ^this.performOnEach( thisMethod.name ) }
	log10 { ^this.performOnEach( thisMethod.name ) }

	sin { ^this.performOnEach( thisMethod.name ) }
	cos { ^this.performOnEach( thisMethod.name ) }
	tan { ^this.performOnEach( thisMethod.name ) }
	asin { ^this.performOnEach( thisMethod.name ) }
	acos { ^this.performOnEach( thisMethod.name ) }
	atan { ^this.performOnEach( thisMethod.name ) }
	sinh { ^this.performOnEach( thisMethod.name ) }
	cosh { ^this.performOnEach( thisMethod.name ) }
	tanh { ^this.performOnEach( thisMethod.name ) }

	rand { ^this.performOnEach( thisMethod.name ) }
	rand2 { ^this.performOnEach( thisMethod.name ) }
	linrand { ^this.performOnEach( thisMethod.name ) }
	bilinrand { ^this.performOnEach( thisMethod.name ) }
	sum3rand { ^this.performOnEach( thisMethod.name ) }

	distort { ^this.performOnEach( thisMethod.name ) }
	softclip { ^this.performOnEach( thisMethod.name ) }

	// binary ops
	pow { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	min { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	max { arg that=0, adverb; ^this.performOnEach( thisMethod.name, that, adverb )}
	roundUp { arg that=1.0, adverb; ^this.performOnEach( thisMethod.name, that, adverb )}

	clip2 { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	fold2 { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	wrap2 { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }

	excess { arg that, adverb;  ^this.performOnEach( thisMethod.name, that, adverb ) }
	firstArg { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	rrand { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }
	exprand { arg that, adverb; ^this.performOnEach( thisMethod.name, that, adverb ) }

	// other methods
	clip { arg lo, hi; ^this.performOnEach( \clip, lo, hi ) }
	wrap { arg lo, hi; ^this.performOnEach( \wrap, lo, hi ) }
	fold { arg lo, hi; ^this.performOnEach( \fold, lo, hi ) }

	linlin { arg inMin = 0, inMax = 1, outMin = 0, outMax = 1, clip=\minmax;
		^this.performOnEach( \linlin, inMin, inMax, outMin, outMax, clip) }

	linexp { arg inMin = 0, inMax = 1, outMin = 0.001, outMax = 1, clip=\minmax;
		^this.performOnEach( \linexp, inMin, inMax, outMin, outMax, clip) }

	explin { arg inMin = 0.001, inMax = 1, outMin = 0, outMax = 1, clip=\minmax;
		^this.performOnEach( \explin, inMin, inMax, outMin, outMax, clip) }

	expexp { arg inMin = 0.001, inMax = 1, outMin = 0.001, outMax = 1, clip=\minmax;
		^this.performOnEach( \expexp, inMin, inMax, outMin, outMax, clip) }

	lincurve { arg inMin = 0, inMax = 1, outMin = 0, outMax = 1, curve = -4, clip=\minmax;
		^this.performOnEach( \lincurve, inMin, inMax, outMin, outMax, clip) }

	curvelin { arg inMin = 0, inMax = 1, outMin = 0, outMax = 1, curve = -4, clip=\minmax;
		^this.performOnEach( \curvelin, inMin, inMax, outMin, outMax, clip) }

	bilin { arg inCenter, inMin, inMax, outCenter, outMin, outMax, clip=\minmax;
		^this.performOnEach( \bilin, inMin, inMax, outCenter, outMin, outMax, clip)
	}

	biexp { arg inCenter, inMin, inMax, outCenter, outMin, outMax, clip=\minmax;
		^this.performOnEach( \biexp, inMin, inMax, outCenter, outMin, outMax, clip)
	}

}

+ Number { wrapAt{^this} } // to optomize the Point:performOnEach method

+ Rect {

	// operations in place
	aboutPoint{|point, dx, dy|
		left = point.x - dx;
		top  = point.y - dy;
		width = 2*dx;
		height = 2*dy;
	}

	aboutPointPlus1{|point, dx, dy|
		left = point.x - dx;
		top  = point.y - dy;
		width = 2*dx + 1;
		height = 2*dy + 1;
	}

	*aboutPointPlus1{ arg point, dx, dy;
		^this.new(point.x-dx, point.y-dy, 2*dx + 1, 2*dy + 1)
	}

	aboutPointOffset{|point, x, y, dx, dy|
		left = point.x - dx + x;
		top  = point.y - dy + y;
		width = 2*dx;
		height = 2*dy;
	}

	replace{|rect|
		left   = rect.left;
		top    = rect.top;
		width  = rect.width;
		height = rect.height;
	}

	replaceAndScale{|rect,scale|
		left   = rect.left;
		top    = rect.top;
		width  = rect.width*scale;
		height = rect.height*scale;
	}

	fromPoints{|pt1, pt2|
		left = pt1.x min: pt2.x;
		top = pt1.y min: pt2.y;
		width = absdif(pt1.x, pt2.x);
		height = absdif(pt1.y, pt2.y);
	}

	replaceLTWB{|l,t,w,h|
		left = l;
		top = t;
		width = w;
		height = h;
	}

	addWH{|w,h|
		width  = width  + w;
		height = height + h;
	}

	replaceMoveBy{|dx,dy|
		left = left + dx;
		top  = top  + dy;
	}

	maxSide{ ^width.max(height) }
	minSide{ ^width.min(height) }

	averageCord { ^( (width + height) * 0.5) }

	insetByReplace{|h, v|
		if(v.isNil){ v = h };
		left   = left + h;
		top    = top + v;
		width  = width - h - h;
		height = height - v - v;
	}

}

+ Function {

	compare{|func,n=10000|
		var t0,       t1,       t2,       t3;
		var total1=0, total2=0, total3=0;
		var func1 = {1};
		var func2 = this;
		var func3 = func;
		n.do{|i|
			t0 = Main.elapsedTime;
			func1.value;
			t1 = Main.elapsedTime;
			func2.value;
			t2 = Main.elapsedTime;
			func3.value;
			t3 = Main.elapsedTime;
			total1 = total1 + t1 - t0 ;
			total2 = total2 + t2 - t1 ;
			total3 = total3 + t3 - t2 ;
		};
		total2 = total2;// - total1;
		total3 = total3;// - total1;
		func2.asCompileString.postln;
		total2.postln;
		func3.asCompileString.postln;
		total3.postln;
		^total2/total3;
	}

	benchMark{|n=10000,print=true|
		var dt,dt2;
		var t0;
		var total=0;
		var min=inf, max=0;
		var emptyFunc = {};
		n.do{|i|
			t0 = Main.elapsedTime;
			emptyFunc.value;
			dt2 = Main.elapsedTime - t0;
			t0 = Main.elapsedTime;
			this.value;
			dt = Main.elapsedTime - t0 - dt2;
			total = total + dt;
			if (dt<min) { min=dt};
			if (dt>max) { max=dt};
		};
		if (print) {
			"".postln;
			this.asCompileString.postln;
			"Total: ".post;
			total.post;
			" (x".post;
			n.post;
			")".postln;
			"Average: ".post;
			(total / n).postln;
			"Min: ".post;
			min.postln;
			"Max: ".post;
			max.postln;
		};
		^total
	}

}

+ Number {

	logN{|n| ^log2(this)/log2(n) } // Base n logarithm.

	mix{|value,index| ^this + (index * (value - this)) } // same maths as Signal:blend

	mixClip{|value,index| ^this + (index.clip(0,1) * (value - this)) } // same maths as Signal:blend

	clipNeg{ if (this<0) {^0}{^this} }
	clipPos{ if (this>0) {^0}{^this} }
	clipToZero{|thresh = 0.01| if (this.abs < thresh) {^0}{^this} }

	yCordOnLine{|p1,p2|
		var x1 = p1.x;
		var y1 = p1.y;
		var x2 = p2.x;
		var y2 = p2.y;
		^( (this-x1) * (y2-y1) / (x2-x1) ) + y1
	}

	xCordOnLine{|p1,p2|
		var x1 = p1.x;
		var y1 = p1.y;
		var x2 = p2.x;
		var y2 = p2.y;
		^( (this-y1) * (x2-x1) / (y2-y1) ) + x1
	}

	// i use this map function because its quicker than linlin & is what i learned in SC1
	map{|x1,x2,y1,y2| ^((this-x1/(x2-x1))*(y2-y1)+y1) }

	mapSine{|x1,x2,y1,y2| ^this.map(x1,x2,-pi*0.5,pi*0.5).sin.map(-1,1,y1,y2) }

	asInt{ ^this.asInteger }

	decimal{|dp| ^(this-(this.round)*(10**dp)).round/(10**dp)+(this.round)}

	asFormatedString{|wn=1,dp=1|
		var strList;

		if (this==inf) {^"inf"};
		if (this==(-inf)) {^"-inf"};

		strList=this.decimal(dp).asString.split($.);
		while ( { (strList@0).size < wn }, { strList=[" "++(strList@0),strList@1] });
		if (dp>0)	{
			if (strList.size<2) {
				strList=strList.add("0");
			};
			while ( { (strList@1).size < dp }, { strList=[strList@0 ,(strList@1)++"0"] });
			^(strList@0)++"."++(strList@1)
		}{
			^strList@0
		}
		^this
	}

}

+ Object { isSymbol{^false} }

+ Symbol { isSymbol{^true} }

+ SequenceableCollection {
	// select an element at random using an array of weights (total weights normalised to 1)
	wchooseNorm{ arg weights;
		var total = weights.sum;
		weights = weights / total;
		^this.at(weights.windex)
	}

}

+ OrderedIdentitySet {
	at{|index| if (items.notNil) { ^items[index] } { ^nil } } // i dont know why this isn't in the main supercolider library
	removeAt{|index| this.remove( items[index] )}
}

+ Color {
	scale{|r=1,g=1,b=1| red = red * r; green = green * g; blue = blue * b}
	replace{|r,g,b| red=r; green=g; blue=b}
	replaceRGBA{|r,g,b,a| red=r; green=g; blue=b; alpha=a}
	replaceClip{|r,g,b| red=r.clip(0,1); green=g.clip(0,1); blue=b.clip(0,1)}
	replaceBlend{|color1,color2,blend|
		var blend2 = 1 - blend;
		red    = (color1.red   * blend2) + (color2.red   * blend);
		green  = (color1.green * blend2) + (color2.green * blend);
		blue   = (color1.blue  * blend2) + (color2.blue  * blend);
		alpha  = (color1.alpha * blend2) + (color2.alpha * blend);
	}
	*orange { arg val = 1.0, alpha = 1.0; ^Color.new(min(1,val), min(1,val)*0.5, max(val-1,0), alpha) }
	*purple { arg val = 1.0, alpha = 1.0; ^Color.new(min(1,val), max(val-1,0), min(1,val), alpha) }

}

// some stuff from wslib

+ String {

	dropFolder{|d|
		var string="";
		if (d>0) {d=d+1};
		this.copy.split.drop(d).do{|s| string=string +/+ s };
		^string
	}

	revealInFinder {
		var path, string;
		var lines, cmd;
		path = this.standardizePath;
		if( path[0] != $/ ) { path = String.scDir +/+ path };
		string="tell application \"Finder\"
			activate
			reveal POSIX file %
		end tell".format( path.quote );
		lines = string.split( $\n );
		cmd = "osascript";
		lines.do({ |line|
			cmd = cmd + "-e" + line.asCompileString;
			});
		cmd.unixCmd;
	}

	isFile { ^PathName( this ).isFile; }

	isSoundFile { var sf;
		if( this.isFile )
			{ sf = SoundFile.new;
				if( sf.openRead( this.standardizePath ) )
					{ sf.close;
						^true }
					{ ^false };
			}
			{ ^false  }
		}

	isFolder { ^PathName( this ).isFolder; } // can also be bundle

	// "12\n3".realBounds
	realBounds{|font|
		var width = 0;
		var height = 0;
		var list = this.split($\n);
		list = list.collect(_.bounds(font));
		list.do{|rect| width = width.max(rect.width); height = rect.height + height; };
		^Rect(0,0,width,height)
	}

}

+ Collection {

	postList{
		var maxSize=0;
		"".postln;
		if (this.isSequenceableCollection) {
			maxSize=this.size.asString.size;
			this.do{|i,j|
				"[".post;
				j=j.asString;
				(maxSize-(j.size)).clip(0,maxSize).do{" ".post};

				j.post;
				"] ".post;
				i.postln;
			};

		}{
			this.pairsDo{|i| maxSize=maxSize.max(i.asString.size)};

			this.pairsDo{|i,j|
				i=i.asString;
				"[".post;
				i.post;
				(maxSize-(i.size)).clip(0,maxSize).do{" ".post};
				"] -> ".post;
				j.postln;
			};
		};
		^''
	}

}

+ Pen {
	*fastTileImage      {|target, image,         opacity = 1.0| this.prTileImage(target, image, nil,    0, opacity) }
	*fastDrawImage      {|target, image,         opacity = 1.0| this.prDrawImage(target, image, nil,    0, opacity) }
	*fastDrawImageSource{|target, image, source, opacity = 1.0| this.prDrawImage(target, image, source, 0, opacity) }
	*fastTileImageSource{|target, image, source, opacity = 1.0| this.prTileImage(target, image, source, 0, opacity) }
	*transPoint{|point| this.translate(point.x, point.y)}

	// from wslib
	*roundedRect{|rect, radius| // radius can be array for 4 corners
		var points, lastPoint;
		radius = radius ?? {  rect.width.min( rect.height ) / 2; };
		if( radius != 0 )
		{
			radius = radius.asCollection.collect({ |item| item ?? {  rect.width.min( rect.height ) / 2; }; });
			// auto scale radius if too large
			if ( radius.size == 1 )
			{ radius = min( radius, min( rect.width, rect.height ) / 2 ) }
			{ if( ((radius@@0) + (radius@@3)) > rect.height )
				{ radius = radius * ( rect.height / ((radius@@0) + (radius@@3))); };
				if( ((radius@@1) + (radius@@2)) > rect.height )
				{ radius = radius * ( rect.height / ((radius@@1) + (radius@@2))); };
				if( ((radius@@0) + (radius@@1)) > rect.width )
				{ radius = radius * ( rect.width / ((radius@@0) + (radius@@1))); };
				if( ((radius@@2) + (radius@@3)) > rect.width )
				{ radius = radius * ( rect.width / ((radius@@2) + (radius@@3))); };
			};
			points = [rect.rightTop, rect.rightBottom,rect.leftBottom, rect.leftTop];
			lastPoint = points.last;
			Pen.moveTo( points[2] - (0@radius.last) );
			points.do({ |point,i|
				Pen.arcTo( lastPoint, point, radius@@i );
				lastPoint = point;
			});
			^Pen; // allow follow-up methods
		}
		{	^Pen.addRect( rect ); }
	}

}
