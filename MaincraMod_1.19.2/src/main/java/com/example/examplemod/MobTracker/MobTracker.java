package com.example.examplemod.MobTracker;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.examplemod.Renderer.RenderEffect;
import com.example.examplemod.Renderer.RenderObject;
import com.example.examplemod.Renderer.Renderer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;

public class MobTracker implements RenderEffect {

	
	private static final int DEFAULT_DISTANCE = 50;
	private List<RenderObject> renderObjectList;
	private boolean isActive;	
	private Renderer renderer;
	public final Color DEFAULT_MOB_MONSTER_COLOR = Color.RED;
	public final Color DEFAULT_MOB_CREATURE_COLOR = Color.PINK;
	private int currentUsedRenderObjects;
	
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
		put(EntityType.PLAYER, Color.RED);
	}};
	
	
	public MobTracker() {
		isActive = false;	
		renderObjectList = new LinkedList<>();
		currentUsedRenderObjects = 0;
	}
	
	public boolean isRenderActive() {
		return isActive;
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		announceStatusToggle();
	}

	public void toggle() {
		isActive = !isActive;
		announceStatusToggle();
	}
	
	private void announceStatusToggle() {
		var mc = Minecraft.getInstance();
		var chat = mc.gui.getChat();
		if (!isActive)
			chat.addMessage(Component.literal("Mob Tracker Disabled.").withStyle(ChatFormatting.RED));
		else
			chat.addMessage(Component.literal("Mob Tracker Enabled.").withStyle(ChatFormatting.GREEN));
			
	}

	
	public void trackMobs() {
		Minecraft mc = Minecraft.getInstance();
		if (!isActive || mc.level == null || renderer == null)			
			return;
		doClearWhenNotInUse();
		LocalPlayer player = mc.player;
		Component pName = player.getName();
		BlockPos startBPos = player.blockPosition().offset(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);
		BlockPos endBPos = player.blockPosition().offset(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE);
		AABB areaBox = new AABB(startBPos, endBPos);		
		
		List<LivingEntity> entityList = mc.level.getEntitiesOfClass(LivingEntity.class, areaBox);
		currentUsedRenderObjects = 0;		
		
		EntityRenderDispatcher entRend = mc.getEntityRenderDispatcher();
//		for (LivingEntity entity: entityList) {
//			 EntityRenderer<?> renderer = entRend.getRenderer(entity);
//			 renderer.render(player, DEFAULT_DISTANCE, currentUsedRenderObjects, null, null, DEFAULT_DISTANCE);
//		}
		
		
		for (Entry<EntityType<? extends Entity>, List<AABB>> entry: filterEntities(entityList, pName).entrySet()) {
			Color entityColor = ENTITY_TYPE_COLOR_MAP.getOrDefault(entry.getKey(), DEFAULT_MOB_MONSTER_COLOR);
			if (entry.getKey().getCategory() == MobCategory.CREATURE)
				entityColor = DEFAULT_MOB_CREATURE_COLOR;
			
			RenderObject obj;
			for (AABB bBox: entry.getValue()) {
				if (currentUsedRenderObjects >= renderObjectList.size()) {
					obj = new RenderObject();
					renderObjectList.add(obj);
				}
				else
					obj = renderObjectList.get(currentUsedRenderObjects);
				obj.enableRender();
				obj.updateData(bBox);
				obj.setColor(entityColor);				
				currentUsedRenderObjects++;
			}
		}
	}

	public Map<EntityType<? extends Entity>, List<AABB>> filterEntities(List<LivingEntity> entityList, Component playerName) {
		Map<EntityType<? extends Entity>, List<AABB>> entityMap = new HashMap<>();
		for (LivingEntity lEntity: entityList) {
			if (lEntity.getType().getCategory() == MobCategory.MONSTER || lEntity.getType().getCategory() == MobCategory.CREATURE || EntityType.PLAYER.equals(lEntity.getType())) {			
				if (!entityMap.containsKey(lEntity.getType()))
					entityMap.put(lEntity.getType(), new LinkedList<>());				
				if (!lEntity.getName().equals(playerName))
					entityMap.get(lEntity.getType()).add(lEntity.getBoundingBox());
			}
		}
		return entityMap;
	}
	
	@Override
	public void getRenderEffect() {
		trackMobs();
//		doRenderUpdate();
	}

	@Override
	public int renderObjectsInUse() {
		// TODO Auto-generated method stub
		return currentUsedRenderObjects;
	}

	@Override
	public List<RenderObject> getRenderObjectList() {
		// TODO Auto-generated method stub
		return renderObjectList;
	}
	
	private void doClearWhenNotInUse() {
		if (!isActive && renderer != null && !renderer.isActive()) {
			renderObjectList.clear();
		}
	}

}
