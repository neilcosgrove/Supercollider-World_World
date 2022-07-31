////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                        Explosions                                                 //
////////////////////////////////////////////////////////////////////////////////////////////////////////

+ World_World {

	*makeExplosion{|origin, speed=1, velocityBias, lifeTime=1, noSmall=10, noLarge=10, smallColor, largeColor, type=\cirlce,
		size=1|
		var velocityBiasX, velocityBiasY;
		smallColor = smallColor ?? {white.copy};
		largeColor = largeColor ?? {white.copy};
		if (velocityBias.notNil) {
			velocityBiasX = velocityBias.x; velocityBiasY = velocityBias.y
		}{
			velocityBiasX=0; velocityBiasY=0
		};
		noSmall.do{
			var angle = 2pi.rand;
			var mag   = 22.0.rand+2;
			var xSpeed = mag * cos(angle);
			var ySpeed = mag * sin(angle);
			xSpeed = xSpeed * speed + velocityBiasX;
			ySpeed = ySpeed * speed + velocityBiasY;
			World_Rect_Particle(origin.copy,11,2.rrand(3),smallColor,xSpeed,ySpeed,lifeTime.asFloat.rand);
		};
		noLarge.do{
			var angle = 2pi.rand;
			var mag   = 10.0.rand+2;
			var xSpeed = mag * cos(angle);
			var ySpeed = mag * sin(angle);
			xSpeed = xSpeed * speed + velocityBiasX;
			ySpeed = ySpeed * speed + velocityBiasY;
			if (type==\circle) {
				World_Circle_Particle(origin.copy,11,5.rrand(15)*size, largeColor.copy.alpha_(0.2.rrand(0.8)),
					xSpeed,ySpeed, lifeTime.asFloat.rand);
			}{
				World_Rect_Particle(origin.copy,11,5.rrand(15)*size, largeColor.copy.alpha_(0.2.rrand(0.8)),
					xSpeed,ySpeed, lifeTime.asFloat.rand);
			};
		};
	}

	*makeExplosionType2{|origin, speed=1, velocityBias, lifeTime=1, noSmall = 10, noLarge=10, largeColor, size=1|
		var velocityBiasX, velocityBiasY;
		largeColor = largeColor ?? {white.copy};
		if (velocityBias.notNil) {
			velocityBiasX = velocityBias.x; velocityBiasY = velocityBias.y
		}{
			velocityBiasX=0; velocityBiasY=0
		};
		noSmall.do{
			var angle = 2pi.rand;
			var mag   = 5.0.rand;
			var xSpeed = mag * cos(angle);
			var ySpeed = mag * sin(angle);
			xSpeed = xSpeed * speed + velocityBiasX;
			ySpeed = ySpeed * speed + velocityBiasY;
			World_Box_Particle(origin.copy, 11, size * 2, largeColor, xSpeed, ySpeed, lifeTime.asFloat.rand);
		};
		noLarge.do{
			var angle = 2pi.rand;
			var mag   = 10.0.rand;
			var xSpeed = mag * cos(angle);
			var ySpeed = mag * sin(angle);
			xSpeed = xSpeed * speed + velocityBiasX;
			ySpeed = ySpeed * speed + velocityBiasY;
			World_Sparkle_Particle(origin.copy, 11, size, largeColor,xSpeed, ySpeed, lifeTime.asFloat.rand);
		};
	}

}
