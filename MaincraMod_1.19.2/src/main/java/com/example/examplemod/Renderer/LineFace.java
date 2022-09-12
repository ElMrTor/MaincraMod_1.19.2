package com.example.examplemod.Renderer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.phys.Vec3;

public class LineFace {

	private List<Vec3> pointLs;	
	
	public LineFace(List<Vec3> pList) {
		this.pointLs = pList;
	}
	
	public LineFace() {
		this(new ArrayList<>());
	}
	
	public List<Vec3> getQuadPointsAsLines() {
		List<Vec3> toRet = new ArrayList<>(24*2);
		for (int i = 0; i < pointLs.size(); ++i) {			
			Vec3 v1 = pointLs.get(i);
			Vec3 v2;
			if ((1+i) % 4 == 0)
				v2 = pointLs.get(i-3);
			else
				v2 = pointLs.get(i+1);
			toRet.add(v1);
			toRet.add(v2);
		}		
		return toRet;
	}
	
	public List<Vec3> getPointsAsQuads() {
		return pointLs;
	}
	
}
