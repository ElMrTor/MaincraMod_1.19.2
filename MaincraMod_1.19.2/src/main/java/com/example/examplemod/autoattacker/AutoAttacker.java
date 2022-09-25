package com.example.examplemod.autoattacker;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoAttacker {

	private Logger log;
	private final int RANGE_OF_ATTACK = 4;
	private final int RANGE_OF_SCAN = 10;
	private final float SCAN_TIMER = 300;
	private long lastScanTime;
	private boolean isActive;
	
	public static final ArrayList<Item> PlantableBlocks = new ArrayList<>() {
		{
			this.add(Items.WHEAT_SEEDS);
			this.add(Items.BEETROOT_SEEDS);
			this.add(Items.CARROT);
			this.add(Items.POTATO);
		}
	};
	
	public AutoAttacker() {
		lastScanTime = 0;
		isActive = false;
		log = LogUtils.getLogger();
	}
	
	
	
	public void attackAllNearbyMonsters() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		Player player = mc.player;
		BlockPos pBox = player.blockPosition();
		List<LivingEntity> entityList = mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(pBox.offset(RANGE_OF_SCAN, RANGE_OF_SCAN, RANGE_OF_SCAN), pBox.offset(-RANGE_OF_SCAN, -RANGE_OF_SCAN, -RANGE_OF_SCAN)));
		for (LivingEntity lEntity: entityList) {
			if (lEntity.getType().getCategory().equals(MobCategory.MONSTER) && player.canAttack(lEntity)) {
				mc.gameMode.attack(player, lEntity);
				player.attack(lEntity);
			}
		}
	}
	
	public boolean isPlantable(BlockState bState, Player player) {
		return PlantableBlocks.contains(player.getMainHandItem().getItem()) && bState.getBlock().equals(Blocks.FARMLAND);			
	}
	
	public void clearDumbGrass() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		Vec3 playerPos = mc.player.position();		
		int halfRange = 10 / 2;		
		Level level = mc.level;
		Player player = mc.player;
		
		BlockPos playerBlockPos = new BlockPos(playerPos);		
		BlockPos startBlock = playerBlockPos.north(halfRange).east(halfRange).above(halfRange);
		BlockPos endBlock = playerBlockPos.south(halfRange).west(halfRange).below(halfRange);		
		
		for (BlockPos bPos: BlockPos.betweenClosed(startBlock, endBlock)) {
			bPos = bPos.immutable();
			BlockState bState = level.getBlockState(bPos);
			if ((bState.is(BlockTags.REPLACEABLE_PLANTS) || bState.is(BlockTags.SMALL_FLOWERS))) {
				if (mc.player.canInteractWith(bPos, 0d))
					mc.gameMode.startDestroyBlock(bPos, Direction.DOWN);				
			} else if (bState.getBlock().equals(Blocks.SUGAR_CANE)) {
				Block b1 = level.getBlockState(bPos.below()).getBlock();
				Block b2 = level.getBlockState(bPos.below().below()).getBlock();
				if (b1.equals(Blocks.SUGAR_CANE) && b2.equals(Blocks.SUGAR_CANE)) {
					if (mc.player.canInteractWith(bPos.below(), 0d))
						mc.gameMode.startDestroyBlock(bPos.below(), Direction.DOWN);
				}
			}
			else if (isPlantable(bState, player)) {
				if (player.canInteractWith(bPos, 0d)) {
					if (player instanceof LocalPlayer) {						
						mc.gameMode.useItemOn((LocalPlayer) player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(bPos.getX(), bPos.getY(), bPos.getZ()), Direction.UP, bPos, false));
//						mc.gameMode.useItem(player, player.getUsedItemHand());
					}
				}
			}
		}
	}	
	
	@SubscribeEvent
	public void checkAndAttackProjectile(ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();		
		if (!isActive || mc.level == null || mc.player == null || mc.level.dimension().equals(Level.OVERWORLD))
			return;
		if (checkTimer()) {			
			BlockPos pBox = mc.player.blockPosition();		
			List<Entity> entityList = mc.level.getEntitiesOfClass(Entity.class, new AABB(pBox.offset(RANGE_OF_ATTACK, RANGE_OF_ATTACK, RANGE_OF_ATTACK), pBox.offset(-RANGE_OF_ATTACK, -RANGE_OF_ATTACK, -RANGE_OF_ATTACK)));
			for (Entity entity: entityList) {
				if (entity.getType().equals(EntityType.SHULKER_BULLET) || entity.getType().equals(EntityType.FIREBALL)) {					
					mc.player.attack(entity);
					mc.gameMode.attack(mc.player, entity);
				}
			}
		}
	}
	
	public void toggle() {
		isActive = !isActive;
	}
	
	public void activate() {
		isActive = true;
	}
	
	public void disable() {
		isActive = false;
	}
	
	private boolean checkTimer() {
		if (lastScanTime + SCAN_TIMER > System.currentTimeMillis()) {
			return false;
		} else {
			lastScanTime = System.currentTimeMillis();			
			return true;
		}
	}
	
}
