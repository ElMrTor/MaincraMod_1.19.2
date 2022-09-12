package com.example.examplemod.Renderer;

import net.minecraft.world.phys.Vec3;

public class Line {

	public Vec3 startPoint;
	public Vec3 endPoint;
	
	public Line(Vec3 startPoint, Vec3 endPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}
	
	public Line() {
		this(new Vec3(0, 0, 0), new Vec3(0, 0, 0));
	}
	
}
