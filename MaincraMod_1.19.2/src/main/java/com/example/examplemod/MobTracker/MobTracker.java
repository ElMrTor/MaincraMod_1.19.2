package com.example.examplemod.MobTracker;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class MobTracker {

	
	public MobTracker() {
		
		
	}
	
	public void trackMobs() {
		Minecraft mc = Minecraft.getInstance();
//		mc.level.getEntitiesOfClass(LivingEntity.class, new EntityFilter<LivingEntity>());
//		mc.level.get
		
		
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
