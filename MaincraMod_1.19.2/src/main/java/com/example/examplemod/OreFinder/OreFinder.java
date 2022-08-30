package com.example.examplemod.OreFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.Renderer.Renderer;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;



public class OreFinder {

	private final Block[] TRACKORE = {
		Blocks.IRON_ORE,
		Blocks.DIAMOND_ORE,
		Blocks.REDSTONE_ORE,
//		Blocks.LAPIS_BLOCK,
		Blocks.LAPIS_ORE,
		Blocks.EMERALD_ORE,
//		Blocks.EMERALD_BLOCK,
		Blocks.NETHER_QUARTZ_ORE,
		Blocks.GOLD_ORE,		
		Blocks.NETHERITE_BLOCK,
		Blocks.NETHERRACK,
	};
	
	private final HashMap<Block, Color> COLOR_ORE_MAP = new HashMap<>() {{
		put(TRACKORE[0], Color.GRAY);
		put(TRACKORE[1], Color.CYAN);
		put(TRACKORE[2], Color.RED);		
		put(TRACKORE[3], Color.BLUE);
		put(TRACKORE[4], Color.GREEN);
		put(TRACKORE[5], Color.GRAY);
		put(TRACKORE[6], Color.YELLOW);
		put(TRACKORE[7], Color.PINK);
		put(TRACKORE[8], Color.WHITE);
	}};
	
	private final ArrayList<Block> BLOCKLISTHUNT = new ArrayList<>(Arrays.asList(TRACKORE));
	private Map<Block, ArrayList<BlockPos>> oreMap;
	
	private static final Logger LOG = LogManager.getLogger();
	
	private final int ORERANGE = 100;
	private boolean isActive;
	private final long DEFAULT_SEARCH_DELAY = 1000;
	private long lastDelay;
	private Renderer renderer;
	
	public OreFinder() {
		isActive = false;		
		oreMap = new HashMap<>();
		lastDelay = 0;
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
	}
	
	private Minecraft getGameInstance() {
		return Minecraft.getInstance();				
	}
		
	public void findOre() {
		if (!isActive)
			return;
		if (lastDelay == 0 || lastDelay + DEFAULT_SEARCH_DELAY < System.currentTimeMillis()) 
			lastDelay = System.currentTimeMillis();			
		else {
			addOreIntoRenderer();
			return;			
		}
		
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
		addOreIntoRenderer();
	}
	
	private void addOreIntoRenderer() {
		if (!(renderer == null)) {
			for (Entry<Block, ArrayList<BlockPos>> entry: oreMap.entrySet()) {				
				Color c = COLOR_ORE_MAP.get(entry.getKey());				
				for (BlockPos bPos: entry.getValue()) {
					renderer.addRenderList(c, Renderer.getVertexListFromAABB(new AABB(bPos)));
				}
			}
		}
	}
	
	public void activate() {
		isActive = true;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public void toggle() {
		isActive = !isActive;
	}
	
//	@SubscribeEvent
	public void drawOres(RenderLevelStageEvent event) {
		Minecraft mc = getGameInstance();
		if (mc.level == null)
			return;
		
		if (event.getStage().equals(Stage.AFTER_PARTICLES)) {
			RenderSystem.disableDepthTest();			
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			Tesselator tesselator = RenderSystem.renderThreadTesselator();
//			Tesselator tesselator = Tesselator.getInstance();		
			BufferBuilder buffer = tesselator.getBuilder();
			
			PoseStack matrixStack = event.getPoseStack();
			matrixStack.pushPose();
			Vec3 pPos = mc.player.position();
			
			Entity view = mc.getCameraEntity();
			// Iterpolation
			double d0 = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
			double d1 = (view.yOld + (view.getY() - view.yOld) * event.getPartialTick()) - view.getEyeHeight();
			double d2 = view.zOld + (view.getZ() - view.zOld) * event.getPartialTick();
			
//			matrixStack.translate(-pPos.x, -pPos.y - mc.player.getEyeHeight(), -pPos.z);
			matrixStack.translate(-d0, -d1 , -d2);
			
			Matrix4f matrix = matrixStack.last().pose();
			buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
			
			for (Entry<Block, ArrayList<BlockPos>> entry: oreMap.entrySet()) {

				for (BlockPos bPos: entry.getValue()) {
					var box = new AABB(bPos);					
					
//					LevelRenderer.renderLineBox(buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
					
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
//			buffer.vertex(matrix, 0, -20, 0).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
//			buffer.vertex(matrix, 10, 20, 10).color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), Color.GREEN.getAlpha()).endVertex();
			matrixStack.translate(0,  0,  0);
			tesselator.end();
			matrixStack.popPose();	
					
			RenderSystem.enableDepthTest();
			
		}
	}
	
}
