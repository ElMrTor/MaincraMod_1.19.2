package com.example.examplemod.MobTracker;

import java.awt.Color;
import java.util.ArrayList;
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
	public static final Color DEFAULT_MOB_MONSTER_COLOR = Color.RED;
	public static final Color DEFAULT_MOB_CREATURE_COLOR = Color.PINK;
	public static final Color COLOR_PURPLE = new Color(139, 0, 139);
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
		LocalPlayer player = mc.player;		
		BlockPos startBPos = player.blockPosition().offset(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);
		BlockPos endBPos = player.blockPosition().offset(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE);
		AABB areaBox = new AABB(startBPos, endBPos);		
		
		List<LivingEntity> entityList = mc.level.getEntitiesOfClass(LivingEntity.class, areaBox);
//		renderObjectList.clear();
		currentUsedRenderObjects = 0;		
		for (Entry<EntityType<? extends Entity>, List<AABB>> entry: filterEntities(entityList).entrySet()) {
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
				if (entry.getKey().getCategory() == MobCategory.MISC) {
					if (entry.getKey().equals(EntityType.VILLAGER)) {
						obj.setFillColor(COLOR_PURPLE);
						obj.setOutlineColor(Color.YELLOW);
					}
					else if (entry.getKey().equals(EntityType.IRON_GOLEM)) {
						obj.setFillColor(Color.WHITE);
						obj.setOutlineColor(Color.GREEN);
					}
					else if (entry.getKey().equals(EntityType.PLAYER)) {
						obj.setFillColor(Color.RED);
						obj.setOutlineColor(Color.GREEN);
					}
				}
				currentUsedRenderObjects++;
//				renderer.addRenderList(entityColor, Renderer.getVertexListFromAABB(bBox));
			}
		}
	}

	public Map<EntityType<? extends Entity>, List<AABB>> filterEntities(List<LivingEntity> entityList) {
		Map<EntityType<? extends Entity>, List<AABB>> entityMap = new HashMap<>();
		for (LivingEntity lEntity: entityList) {
			if (lEntity.getType().getCategory() == MobCategory.MONSTER || lEntity.getType().getCategory() == MobCategory.CREATURE || lEntity.getType().getCategory() == MobCategory.MISC) {			
				if (!entityMap.containsKey(lEntity.getType()))
					entityMap.put(lEntity.getType(), new LinkedList<>());				
				
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
	

}
