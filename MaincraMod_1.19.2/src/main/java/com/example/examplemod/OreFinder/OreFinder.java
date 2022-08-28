package com.example.examplemod.OreFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;



public class OreFinder {

	private final Block[] TRACKORE = {
		Blocks.IRON_ORE,
		Blocks.DIAMOND_ORE,
		Blocks.REDSTONE_ORE,
		Blocks.LAPIS_BLOCK,
		Blocks.EMERALD_ORE,
		Blocks.NETHER_QUARTZ_ORE,		
	};
	
	private final ArrayList<Block> BLOCKLISTHUNT = new ArrayList<>(Arrays.asList(TRACKORE));
	private Map<Block, ArrayList<BlockPos>> oreMap;
	
	private static final Logger LOG = LogManager.getLogger();
	
	private final int ORERANGE = 100;
	private boolean isActive;
	
	public OreFinder() {
		isActive = false;		
		oreMap = new HashMap<>();
	}
	
	private Minecraft getGameInstance() {
		return Minecraft.getInstance();				
	}
	
	public void findOre() {
		Minecraft maincra = getGameInstance();
		oreMap = new HashMap<>();
		Vec3 playerPos = maincra.player.position();
		int halfRange = ORERANGE / 2;
		
		BlockPos playerBlockPos = new BlockPos(playerPos);		
		BlockPos startBlock = playerBlockPos.north(halfRange).east(halfRange).above(halfRange);
		BlockPos endBlock = playerBlockPos.south(halfRange).west(halfRange).below(halfRange);		
		
		for (BlockPos bPos: BlockPos.betweenClosed(startBlock, endBlock)) {
			bPos = bPos.immutable();
			Block currentBlock = maincra.level.getBlockState(bPos).getBlock();
			
			if (BLOCKLISTHUNT.contains(currentBlock)) {
				if (oreMap.containsKey(currentBlock))
					oreMap.get(currentBlock).add(bPos);
				else {
					oreMap.put(currentBlock, new ArrayList<>());
					oreMap.get(currentBlock).add(bPos);
				}
			}
		}		
	}
	
	@SubscribeEvent
	public void drawOres(RenderLevelStageEvent event) {
		Minecraft mc = getGameInstance();
		if (mc.level == null)
			return;
		
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
//		PoseStack matrixStack = event.getPoseStack();
//		matrixStack.pushPose();
		
		RenderSystem.disableDepthTest();
//		RenderSystem.lineWidth(10f);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();		
		BufferBuilder buffer = tesselator.getBuilder();
		Vec3 pPos = mc.player.position();
//		matrixStack.translate(-pPos.x, -pPos.y - mc.player.getEyeHeight(), -pPos.z);
//		matrixStack.translate(-view.x, -view.y, -view.z);
//		matrixStack.translate(-pPos.x, -pPos.y - mc.player.getEyeHeight(), -pPos.z);
		PoseStack matrixStack = event.getPoseStack();
		matrixStack.pushPose();		
		matrixStack.translate(-pPos.x, -pPos.y - mc.player.getEyeHeight(), -pPos.z);
		buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);	
		
		for (Entry<Block, ArrayList<BlockPos>> entry: oreMap.entrySet()) {
//			LOG.info("{} blocks found", entry.getValue().size());
			for (BlockPos bPos: entry.getValue()) {
				var box = new AABB(bPos);	
				
				Matrix4f matrix = event.getProjectionMatrix();
				
				buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				
				buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				
				buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				
				buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
				buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();			
				
				
			}
		}
//		buffer.vertex(0, -20, 0).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
//		buffer.vertex(10, 20, 10).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
		tesselator.end();
		matrixStack.popPose();
		
				
		RenderSystem.enableDepthTest();
		
	}
	
}
