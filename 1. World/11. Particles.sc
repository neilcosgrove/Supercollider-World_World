////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                              Particles                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////
// the most stripped down and fastest I can get, no ECS here ///////////////////////////////////////////

World_Particle_Base : World_World {

	var <boundingBox, <>dx, <>dy, <>lifeSpan, <layer, <>fillColor;

	*new{|origin,layer,radius,fillColor,dx,dy,lifeSpan| ^super.new.init(origin,layer,radius,fillColor,dx,dy,lifeSpan) }

	init{|argOrigin,argLayer,argRadius,argFillColor,argDX,argDY,argLifeSpan|
		boundingBox   = Rect.aboutPoint(argOrigin,argRadius,argRadius);
		layer         = argLayer;
		fillColor     = argFillColor;
		dx            = argDX;
		dy            = argDY;
		lifeSpan      = argLifeSpan;
		layers[layer] = layers[layer].add(this);
	}

	free{ layers[layer].remove(this) }

	//draw{}       // to subclass

}

World_Rect_Particle : World_Particle_Base {
	draw{
		// off screen culling kills entity
		if ( boundingBox.left   > actualRightEdge  ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.right  < actualLeftEdge   ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.top    > actualBottomEdge ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.bottom < actualTopEdge    ) { garbage = garbage.add(this); ^this };
		lifeSpan = lifeSpan - frameLength;
		if (lifeSpan <= 0) { garbage = garbage.add(this); ^this };
		Pen.fillColor_(fillColor).addRect(boundingBox).draw(0);
		boundingBox.replaceMoveBy(dx * timeDilation, dy * timeDilation);
	}
}

World_Sparkle_Particle : World_Particle_Base {
	draw{
		// off screen culling kills entity
		if ( boundingBox.left   > actualRightEdge  ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.right  < actualLeftEdge   ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.top    > actualBottomEdge ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.bottom < actualTopEdge    ) { garbage = garbage.add(this); ^this };
		lifeSpan = lifeSpan - frameLength;
		if (lifeSpan <= 0) { garbage = garbage.add(this); ^this };
		Pen.fillColor_( 0.05.coin.if(white,fillColor) ).addRect(boundingBox).draw(0);
		boundingBox.replaceMoveBy(dx * timeDilation, dy * timeDilation);
	}
}

World_Box_Particle : World_Particle_Base {
	draw{
		// off screen culling kills entity
		if ( boundingBox.left   > actualRightEdge  ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.right  < actualLeftEdge   ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.top    > actualBottomEdge ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.bottom < actualTopEdge    ) { garbage = garbage.add(this); ^this };
		lifeSpan = lifeSpan - frameLength;
		if (lifeSpan <= 0) { garbage = garbage.add(this); ^this };
		Pen.fillColor_(fillColor).strokeColor_(white).width_(1).addRect(boundingBox).draw(3);
		boundingBox.replaceMoveBy(dx * timeDilation, dy * timeDilation);
	}
}


World_Circle_Particle : World_Particle_Base {
	draw{
		// off screen culling kills entity
		if ( boundingBox.left   > actualRightEdge  ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.right  < actualLeftEdge   ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.top    > actualBottomEdge ) { garbage = garbage.add(this); ^this };
		if ( boundingBox.bottom < actualTopEdge    ) { garbage = garbage.add(this); ^this };
		lifeSpan = lifeSpan - frameLength;
		if (lifeSpan <= 0) { garbage = garbage.add(this); ^this };
		Pen.fillColor_(fillColor).addOval(boundingBox).draw(0);
		boundingBox.replaceMoveBy(dx * timeDilation, dy * timeDilation);
	}
}

// Foreground Particles ///////////////////////////////////////////////////////////////////////////////////

World_Foreground_Particle : World_World {

	classvar <gust, <>gustX = 0, gustY = 0;

	var <>x, <>y, <>z, <>dx, <>dy, <>fillColor, <boundingBox, <radius;

	*tick{
		gust   = (worldTime*0.25).heatMapSine;
		gustX  =       gust.map(0,1,-300,300);
		gustY  = (1 - gust).map(1,0,-400,0  );
	}

	*new{|x,y,z,radius,dx,dy,fillColor| ^super.new.init(x,y,z,radius,dx,dy,fillColor) }

	init{|argX,argY,argZ,argRadius,argDX,argDY,argFillColor|
		x               = argX;
		y               = argY;
		z               = argZ * 1.5;
		dx              = argDX;
		dy              = argDY;
		radius          = argRadius;
		boundingBox     = Rect(0,0,argRadius*2,argRadius*2);
		fillColor       = argFillColor;
		foregroundLayer = foregroundLayer.add(this);
	}

	free{ foregroundLayer.remove(this) } // i might remove this

	// tick{} // subclass these
	// draw{} // subclass these

}

World_Snow_Foreground_Particle : World_Foreground_Particle {

	draw{
		x = x + (dx * timeDilation);
		y = y + (dy * timeDilation);
		boundingBox.left = (x - (cameraPos.x + gustX * z)).mod(screenWidth  ) - radius;
		boundingBox.top  = (y - (cameraPos.y + gustY * z)).mod(screenHeight ) - radius;
		Pen.fillColor_(fillColor).addRect(boundingBox).draw(0);
	}

}

World_Sparkly_Dust_Foreground_Particle : World_Snow_Foreground_Particle {

	draw{
		x = x + (dx * timeDilation);
		y = y + (dy * timeDilation);
		boundingBox.left = (x - (cameraPos.x + (gustX * 0.15) * z)).mod(screenWidth  ) - radius;
		boundingBox.top  = (y - (cameraPos.y + (gustY * 0.15) * z)).mod(screenHeight ) - radius;
		// make these setting accessable £
		0.005.coin.if{
			Pen.fillColor_(white).addOval(boundingBox).draw(0);
		}{
			Pen.fillColor_(
				fillColor.copy.alpha_(fillColor.alpha*(0.8.rrand(1.3)))   // can be done better £
			).addOval(boundingBox).draw(0);
		};
	}

}

World_Dust_Foreground_Particle : World_Snow_Foreground_Particle {

	draw{
		x = x + (dx * timeDilation * zoom);
		y = y + (dy * timeDilation * zoom);
		boundingBox.left = (x - (cameraPos.x * (z + 0.75) * 0.55)).mod(screenWidth  ) - radius;
		boundingBox.top  = (y - (cameraPos.y * (z + 0.75) * 0.55)).mod(screenHeight ) - radius;
		Pen.fillColor_(fillColor.alpha_(zoom.clip(0.2,0.65)**2*0.7)).addRect(
			tempRect.replaceAndScale(boundingBox,zoom)).draw(0);
	}

}

World_Star_Background_Particle : World_Snow_Foreground_Particle {

	draw{
		x = x + (dx * timeDilation * zoom);
		y = y + (dy * timeDilation * zoom);
		boundingBox.left = (x - (cameraPos.x * (z + 0.75) * 0.55)).mod(screenWidth  ) - radius;
		boundingBox.top  = (y - (cameraPos.y * (z + 0.75) * 0.55)).mod(screenHeight ) - radius;

		0.01.coin.if {
			Pen.fillColor_([white, white, white, Color.red, Color.cyan, Color.blue].choose).addOval(
				tempRect.replaceAndScale(boundingBox,zoom)).draw(0);
		}{
			Pen.fillColor_(fillColor).addOval(
				tempRect.replaceAndScale(boundingBox,zoom)).draw(0);
		};
	}

}