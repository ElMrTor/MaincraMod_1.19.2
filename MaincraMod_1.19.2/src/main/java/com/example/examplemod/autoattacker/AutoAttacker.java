package com.example.examplemod.autoattacker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
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
	private boolean isHostile;
	
	public static final ArrayList<Item> PlantableBlocks = new ArrayList<>() {
		{
			this.add(Items.WHEAT_SEEDS);
			this.add(Items.BEETROOT_SEEDS);
			this.add(Items.CARROT);
			this.add(Items.POTATO);
		}
	};
	
	public static final HashMap<Item, ArrayList<EntityType<? extends Entity>>> BREEDING_MAP = new HashMap<>() {
		{
			put(Items.WHEAT, new ArrayList<>() {{
				add(EntityType.COW);
				add(EntityType.SHEEP);
			}});
			put(Items.GOLDEN_APPLE, new ArrayList<>() {{
				add(EntityType.HORSE);
			}});
			
			put(Items.CARROT, new ArrayList<>() {{
				add(EntityType.HORSE);
			}});
		}
	};
	
	public AutoAttacker() {
		isHostile = false;
		lastScanTime = 0;
		isActive = false;
		log = LogUtils.getLogger();
	}
	
	
	public void toggleHostile() {
		var mc = Minecraft.getInstance();
		var chat = mc.gui.getChat();
		isHostile = !isHostile;
		if (isHostile) {
			chat.addMessage(Component.literal("Hostile").withStyle(ChatFormatting.GREEN));
		} else {
			chat.addMessage(Component.literal("Non Hostile").withStyle(ChatFormatting.RED));
		}
			
	}
	
	public void attackAllNearbyMonsters() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		Player player = mc.player;
		Component pName = mc.player.getName();
		BlockPos pBox = player.blockPosition();
		List<LivingEntity> entityList = mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(pBox.offset(RANGE_OF_SCAN, RANGE_OF_SCAN, RANGE_OF_SCAN), pBox.offset(-RANGE_OF_SCAN, -RANGE_OF_SCAN, -RANGE_OF_SCAN)));
		for (LivingEntity lEntity: entityList) {
			if (lEntity.getType().getCategory().equals(MobCategory.MONSTER) || (isHostile && lEntity.getType().equals(EntityType.PLAYER) && !lEntity.getName().equals(pName)) && player.canAttack(lEntity)) {
				mc.gameMode.attack(player, lEntity);
				player.attack(lEntity);
			} else if (player.getMainHandItem().getItem().equals(Items.SHEARS) && lEntity.getType().equals(EntityType.SHEEP)) {
				Sheep sheep = (Sheep) lEntity;
				if (!sheep.isSheared() && player.canInteractWith(sheep, 0D))
					mc.gameMode.interact(player, sheep, InteractionHand.MAIN_HAND);
			} else if (BREEDING_MAP.keySet().contains(player.getMainHandItem().getItem())) {
				Item item = player.getMainHandItem().getItem();
				var breeding_ls = BREEDING_MAP.get(item);
				if (breeding_ls.contains(lEntity.getType()) && player.canInteractWith(lEntity, 0D) && lEntity instanceof Animal && ((Animal) lEntity).canFallInLove() && !((Animal) lEntity).isBaby()) {				
					mc.gameMode.interact(player, lEntity, InteractionHand.MAIN_HAND);					
				}
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
			if ((bState.is(BlockTags.REPLACEABLE_PLANTS) || bState.is(BlockTags.SMALL_FLOWERS)) && !isHostile) {
				if (mc.player.canInteractWith(bPos, 0d))
					mc.gameMode.startDestroyBlock(bPos, Direction.DOWN);				
			} else if (bState.getBlock().equals(Blocks.SUGAR_CANE)) {
				Block b1 = level.getBlockState(bPos.below()).getBlock();
				Block b2 = level.getBlockState(bPos.below().below()).getBlock();
				if (b1.equals(Blocks.SUGAR_CANE) && b2.equals(Blocks.SUGAR_CANE)) {
					if (mc.player.canInteractWith(bPos.below(), 0D))
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
