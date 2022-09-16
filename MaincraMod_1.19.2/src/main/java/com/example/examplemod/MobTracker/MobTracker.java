package com.example.examplemod.MobTracker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.examplemod.Renderer.RenderEffect;
import com.example.examplemod.Renderer.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;

public class MobTracker implements RenderEffect {

	
	private static final int DEFAULT_DISTANCE = 50;
	private boolean isActive;	
	private Renderer renderer;
	public final Color DEFAULT_MOB_MONSTER_COLOR = Color.RED;
	public final Color DEFAULT_MOB_CREATURE_COLOR = Color.PINK;
	
	private final Map<EntityType<? extends Entity>, Color> ENTITY_TYPE_COLOR_MAP = new HashMap<>() {{
		put(EntityType.CREEPER, Color.GREEN);
		put(EntityType.ZOMBIE, Color.BLUE);
		put(EntityType.SKELETON, Color.WHITE);
		put(EntityType.SLIME, Color.ORANGE);
		put(EntityType.BLAZE, Color.YELLOW);
		put(EntityType.DROWNED, Color.CYAN);
		put(EntityType.ENDERMAN, Color.MAGENTA);
		put(EntityType.GHAST, Color.WHITE);
		put(EntityType.MAGMA_CUBE, Color.ORANGE);
		put(EntityType.PHANTOM, Color.CYAN);
		put(EntityType.WITHER_SKELETON, Color.CYAN);
		put(EntityType.SPIDER, Color.BLACK);
		put(EntityType.SHULKER, Color.PINK);
		put(EntityType.PIGLIN, Color.GREEN);
		put(EntityType.PIGLIN_BRUTE, Color.PINK);
		put(EntityType.ZOMBIFIED_PIGLIN, Color.BLUE);
	}};
	
	
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
		for (Entry<EntityType<? extends Entity>, List<AABB>> entry: filterEntities(entityList).entrySet()) {
			Color entityColor = ENTITY_TYPE_COLOR_MAP.getOrDefault(entry.getKey(), DEFAULT_MOB_MONSTER_COLOR);
			if (entry.getKey().getCategory() == MobCategory.CREATURE)
				entityColor = DEFAULT_MOB_CREATURE_COLOR;
			for (AABB bBox: entry.getValue()) {				
				renderer.addRenderList(entityColor, Renderer.getVertexListFromAABB(bBox));
			}
		}
	}

	public Map<EntityType<? extends Entity>, List<AABB>> filterEntities(List<LivingEntity> entityList) {
		Map<EntityType<? extends Entity>, List<AABB>> entityMap = new HashMap<>();
		for (LivingEntity lEntity: entityList) {
			if (lEntity.getType().getCategory() == MobCategory.MONSTER || lEntity.getType().getCategory() == MobCategory.CREATURE) {			
				if (!entityMap.containsKey(lEntity.getType()))
					entityMap.put(lEntity.getType(), new ArrayList<>());				
				
				entityMap.get(lEntity.getType()).add(lEntity.getBoundingBox());
			}
		}
		return entityMap;
	}
	
	@Override
	public void getRenderEffect() {
		
	}
	

}
