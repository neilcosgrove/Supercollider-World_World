////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Entities                                                      //
////////////////////////////////////////////////////////////////////////////////////////////////////////

// Basic shapes //////////////////////////////////////////////////////////////////////////////

World_Line : World_Entity {

	*new{|p1, p2, layer, color|
		^super.new.initEntityWithRect( Rect.fromPoints(p1,p2) ).init(p1, p2, layer, color)
	}

	init{|p1, p2, layer, color|
		components[\collider] = World_Line_Collider(this, p1, p2);
		components[\drawFunc] = World_Line_DrawFunc(this, layer, p1, p2, color);
	}

}

World_Circle : World_Entity {

	// how am i gonna make things immovable & solid ? £
	*new{|origin, layer, radius, fillColor, strokeColor, solid = true, dynamic = false|
		^super.new.initEntityWithRadius(origin, radius).init(layer, radius, fillColor, strokeColor, solid, dynamic)
	}

	init{|layer, radius, fillColor, strokeColor, solid, dynamic|
		if (dynamic) { components[\mechanics] = World_Classical_Mechanics(this, 0@0, nil, nil, nil, 0@0); };
		components[\collider] = World_Circle_Collider(this, radius, 1, solid, dynamic);
		components[\drawFunc] = World_Circle_DrawFunc(this, layer, fillColor, strokeColor);
	}

}

World_Rect : World_Entity {

	*new{|origin, layer, radius, fillColor, strokeColor, solid = true|
		^super.new.initEntityWithRadius(origin, radius).init(layer, fillColor, strokeColor, solid)
	}

	*newLTWH{|left, top, width, height, layer, fillColor, strokeColor, solid = true|
		^super.new.initEntityWithLTWH(left, top, width, height).init(layer, fillColor, strokeColor, solid)
	}

	*newRect{|rect, layer, fillColor, strokeColor, solid = true|
		^super.new.initEntityWithRect(rect).init(layer, fillColor, strokeColor, solid)
	}

	init{|layer, fillColor, strokeColor, solid|
		components[\collider] = World_Rect_Collider(this, 1, solid);
		components[\drawFunc] = World_Rect_DrawFunc(this, layer, fillColor, strokeColor);
	}

	// temp for title screen
	alpha_{|alpha|
		components[\drawFunc].fillColor.alpha_(alpha*0.3);
		components[\drawFunc].strokeColor.alpha_(alpha);
	}

}

World_Rounded_Rect : World_Rect {

	init{|layer, fillColor, strokeColor|
		components[\collider] = World_Rect_Collider        (this);
		components[\drawFunc] = World_Rounded_Rect_DrawFunc(this, layer, fillColor, strokeColor);
	}

}

World_Triangle : World_Entity {

	*new{|origin, layer, radius, corner, fillColor, strokeColor|
		^super.new.initEntityWithRadius(origin, radius).init(layer, corner, fillColor, strokeColor)
	}

	*newLTWH{|left, top, width, height, layer, corner, fillColor, strokeColor|
		^super.new.initEntityWithLTWH(left, top, width, height).init(layer, corner, fillColor, strokeColor)
	}

	init{|layer, corner, fillColor, strokeColor|
		components[\collider] = World_Triangle_Collider(this, corner);
		components[\drawFunc] = World_Triangle_DrawFunc(this, layer, corner, fillColor, strokeColor);
	}

	updateBoundingBox{
		boundingBox.left_(origin.x - (boundingBox.width*0.5)).top_(origin.y - (boundingBox.height*0.5));
		components[\collider].updateUGPCell.updatePoints;
		components[\drawFunc].updatePoints;
	}

}


// Tiles Base ///////////////////////////////////////////////////////////////////////////////////////

World_Tile_Base : World_Entity {

	var <>dx, <>dy;

	*isTile { ^true }

}

// Triangle Tiles ///////////////////////////////////////////////////////////////////////////////////////

World_Triangle_Tile : World_Tile_Base {

	*new{|left, top, width, height, dx, dy, layer, solid, image, corner|
		^super.new.initEntityWithLTWH(left, top, width*dx, height*dy).init(layer, solid, image, dx, dy, corner)
	}

	init{|layer, solid, image, argDX, argDY, corner|
		dx = argDX;
		dy = argDY;
		if (solid) { components[\collider] = World_TriangleTile_Collider(this, corner) };
		components[\drawFunc] = World_Static_SingleTile_DrawFunc(this, layer, image);
	}

}

// Rectangle Tiles ///////////////////////////////////////////////////////////////////////////////////////

World_Rect_Tile : World_Tile_Base {

	*new{|left, top, width, height, dx, dy, layer, solid, image, opacity=1|
		^super.new.initEntityWithLTWH(left, top, width*dx, height*dy).init(layer, solid, image, dx, dy, opacity)
	}

	init{|layer, solid, image, argDX, argDY, opacity|
		dx = argDX;
		dy = argDY;
		if (solid) { components[\collider] = World_RectTile_Collider(this) };
		components[\drawFunc] = World_Static_Tile_DrawFunc(this, layer, image, 1, 1).opacity_(opacity);
	}

}

World_Rect_Tile2 : World_Tile_Base {

	*new{|left, top, width, height, dx, dy, layer, solid, image, opacity=1|
		^super.new.initEntityWithLTWH(left, top, width*dx, height*dy).init(layer, solid, image, dx, dy, opacity)
	}

	init{|layer, solid, image, argDX, argDY, opacity|
		dx = argDX;
		dy = argDY;
		if (solid) { components[\collider] = World_RectTile_Collider(this) };
		components[\drawFunc] = World_Static_Tile_DrawFunc(this, layer, image, 0, 0).opacity_(opacity);
	}

}


World_Color_Rect_Tile : World_Tile_Base {

	*new{|left, top, width, height, dx, dy, layer, solid, fillColor, strokeColor, opacity=1|
		^super.new.initEntityWithLTWH(left, top, width*dx, height*dy).init(layer, solid, fillColor, strokeColor, dx, dy, opacity)
	}

	init{|layer, solid, fillColor, strokeColor, argDX, argDY, opacity|
		dx = argDX;
		dy = argDY;
		if (solid) { components[\collider] = World_RectTile_Collider(this) };
		components[\drawFunc] = World_Rect_DrawFunc(this, layer, fillColor, strokeColor);
	}

}

// Decor Tiles ///////////////////////////////////////////////////////////////////////////////////////

World_Decor_Tile : World_Tile_Base {

	// same interface as World_Rect_Tile but adjusts image to floor
	*new{|left, top, width, height, dx, dy, layer, solid, image|
		width  = imageBounds[image].width;
		height = imageBounds[image].height;
		top = top + (tileMap.tileHeight) - height;       // align bottom of tile with floor
		^super.new.initEntityWithLTWH(left, top, width, height).init(layer, solid, image, dx, dy)
	}

	init{|layer, solid, image, argDX, argDY|
		dx = argDX;
		dy = argDY;
		if (solid) { components[\collider] = World_RectTile_Collider(this) };
		components[\drawFunc] = World_Static_DecorTile_DrawFunc(this, layer, image);
	}

}

/////////////////////////////////////////////////////////////////////////////////////////////////////

World_Vector_Top_Down_Player : World_Entity {

	var <id;

	*new{|id,origin,layer,radius,enemies| ^super.new.initEntityWithRadius(origin,radius) .init(id,layer,radius,enemies) }

	init{|argID,layer,radius,enemies|
		id          = argID;
		players[id] = this;
		components[\mechanics  ] = World_TDP_Mechanics     (this, 0@0, 20@20, 0.8@0.8, 0@(0.25*0), 0@0).angle_(pi*1.5);
		components[\controller ] = World_TDP_Controller    (this,id).acceleration_(2);
		components[\collider   ] = World_TDP_Collider      (this, radius, enemies);
		components[\health     ] = World_TDP_Health        (this, 1, 1);
		components[\death      ] = World_TDP_Death         (this, Color(0.737, 0.71, 0.937, 0.7));
		components[\weapon     ] = World_Platformer_Weapon (this, 5,5,0.1,2);
		components[\damage     ] = World_Damage_Profile    (this, 100);
		components[\amour      ] = World_Armour_Profile    (this, 1, 1, 1, 1);
		components[\hitTimer   ] = World_Player_Hit_Timer  (this, 0.15, false);
		components[\drawFunc   ] = World_TDPVector_DrawFunc(this, layer, Color.red, Color.white);
	}

}

// Top Down Player Draw Func //

World_TDPVector_DrawFunc : World_Circle_DrawFunc {

	drawFunc{
		var angle       = components[\mechanics].angle;
		var w           = boundingBox.width;
		var w2          = w * 0.5;

		if (fill) {
			Pen.fillColor_(Color.red).addOval(boundingBox).draw(0);
			Pen.use{
				var x = origin.x, y = origin.y, w3 = w/3;
				Pen.rotate(halfpi + angle, x, y);
				Pen.moveTo(Point( x    , y-w2 ));
				Pen.lineTo(Point( x-w3 , y+w3 ));
				Pen.lineTo(Point( x+w3 , y+w3 ));
				Pen.fillColor_(black.copy);
				Pen.fill;
			};
		};
		if (stroke) { Pen.width_(penWidth).strokeColor_(strokeColor).addOval(boundingBox).draw(2) }
	}

}

// a background rotating sprite

World_Background_Mechanics : World_Basic_Mechanics {
	worldEdge{ if (origin.y > (worldBottom + 60)) { parent.delete } }
}


World_Background_Sprite : World_Entity {

	*new{|origin, layer, image, velocity|
		var radius = imageBounds[image].averageCord * 0.5;
		^super.new.initEntityWithRadius(origin, radius).init(layer, image, radius, velocity)
	}

	init{|layer, image, radius, velocity|
		components[\mechanics ] = World_Background_Mechanics     (this, velocity);
		components[\drawFunc  ] = World_Rotating_Sprite_DrawFunc (this, layer, image);
		components[\drawFunc  ].freq_( components[\drawFunc  ].freq * 0.01 );
	}

}
