package com.example.examplemod.MobTracker;

import java.awt.Color;
import java.util.List;

import com.example.examplemod.Renderer.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class MobTracker {

	
	private static final int DEFAULT_DISTANCE = 50;
	private boolean isActive;	
	private Renderer renderer;
	
	
	public MobTracker() {
		isActive = false;		
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void toggle() {
		isActive = !isActive; 
	}

	
	public void trackMobs() {
		Minecraft mc = Minecraft.getInstance();
		if (!isActive || mc.level == null || renderer == null)			
			return;
		LocalPlayer player = mc.player;		
		BlockPos startBPos = player.blockPosition().offset(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);
		BlockPos endBPos = player.blockPosition().offset(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE);
		AABB areaBox = new AABB(startBPos, endBPos);		
		
		List<LivingEntity> entityList = mc.level.getEntitiesOfClass(LivingEntity.class, areaBox);
//		List<List<Vec3>> lLVecs = new ArrayList<>();
		for (LivingEntity ent: entityList) {
//			lLVecs.add(Renderer.getVertexListFromAABB(ent.getBoundingBox()));
			renderer.addRenderList(Color.YELLOW, Renderer.getVertexListFromAABB(ent.getBoundingBox()));
		}
		return;
	}

	
	
//	private class EntityFilter<T> implements Predicate<T> {
//		
//		private final float DISTANCE_FILTER = 100;
//		@Override
//		public boolean apply(T input) {
//			if (!(input instanceof LivingEntity)) 
//				return false;
//			else {
//				Minecraft mc = Minecraft.getInstance();
//				LivingEntity ent = (LivingEntity) input;
//				return ent.distanceTo(mc.player) <= DISTANCE_FILTER;
//			}
//		}
//		
//	} 
	
}
