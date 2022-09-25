package com.example.examplemod.Renderer;

import java.util.List;



public interface RenderEffect {
	public void getRenderEffect();
	public boolean isRenderActive();
	public int renderObjectsInUse();
	public List<RenderObject> getRenderObjectList();	
}
