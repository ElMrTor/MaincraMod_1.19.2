package com.example.examplemod.OreFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;

import com.example.examplemod.Renderer.RenderEffect;
import com.example.examplemod.Renderer.RenderObject;
import com.example.examplemod.Renderer.Renderer;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;



public class OreFinder implements RenderEffect{
	
	public static final Color BROWN = new Color(51, 25, 0);
	
	private final static HashMap<Block, Color> COLOR_ORE_MAP = new HashMap<>() {{
		put(Blocks.IRON_ORE, Color.GRAY);
		put(Blocks.DIAMOND_ORE, Color.CYAN);
		put(Blocks.REDSTONE_ORE, Color.RED);		
		put(Blocks.LAPIS_ORE, Color.BLUE);
		put(Blocks.EMERALD_ORE, Color.GREEN);
		put(Blocks.NETHER_QUARTZ_ORE, Color.GRAY);
		put(Blocks.GOLD_ORE, Color.YELLOW);
		put(Blocks.NETHERITE_BLOCK, Color.PINK);
		put(Blocks.COPPER_ORE, BROWN);
		put(Blocks.DEEPSLATE_IRON_ORE, Color.GRAY);
		put(Blocks.DEEPSLATE_COPPER_ORE, BROWN);
		put(Blocks.DEEPSLATE_DIAMOND_ORE, Color.CYAN);
		put(Blocks.DEEPSLATE_EMERALD_ORE, Color.GREEN);
		put(Blocks.DEEPSLATE_GOLD_ORE, Color.YELLOW);
		put(Blocks.DEEPSLATE_LAPIS_ORE, Color.BLUE);
		put(Blocks.DEEPSLATE_REDSTONE_ORE, Color.RED);
		put(Blocks.COAL_ORE, Color.BLACK);
		put(Blocks.DEEPSLATE_COAL_ORE, Color.BLACK);
		put(Blocks.ANCIENT_DEBRIS, Color.CYAN);
		put(Blocks.CHEST, BROWN);
		put(Blocks.TRAPPED_CHEST, BROWN);
		put(Blocks.NETHER_GOLD_ORE, Color.YELLOW);
		put(Blocks.SPAWNER, Color.WHITE);
	}};
	
	private final ArrayList<Block> BLOCKLISTHUNT = new ArrayList<>(Arrays.asList(COLOR_ORE_MAP.keySet().toArray(new Block[0])));
	private Map<Block, SortedSet<ChunkBlockPos>> oreMap;
	private static ArrayList<RenderObject> renderObjectList;
	private static final long RENDER_UPDATED_TIMER = 300;
	private static final Logger LOG = LogUtils.getLogger();
	private static final int TICKS_TO_FULL_SCAN = 20;
	private static final int ORE_LIMIT_FOR_RENDER = 7;
	private final int ORERANGE = 60; // Assume Ore range will be divisible by 10
	private boolean isActive;
	private final long DEFAULT_SEARCH_DELAY = 3000;
	private long lastDelay;
	private Renderer renderer;
	private BlockPosHelper bHelper;
	private int usedRenderObjects;
	
	
	public OreFinder() {
		isActive = false;		
		oreMap = new HashMap<>();
		for (Block block: COLOR_ORE_MAP.keySet()) {
			oreMap.put(block, new TreeSet<>(new OreDistanceComparator()));
		}
		lastDelay = 0;
		bHelper = new BlockPosHelper(ORERANGE, TICKS_TO_FULL_SCAN);
		int renderListSize = oreMap.keySet().size() * ORE_LIMIT_FOR_RENDER;
		renderObjectList = new ArrayList<>(renderListSize);
		for (int i = 0; i < renderListSize; ++i) {
			renderObjectList.add(new RenderObject());
		}		
	}	
	
	public int renderObjectsInUse() {
		return usedRenderObjects;
	}
	
	public boolean isRenderActive() {
		return isActive;
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
		renderer.addRenderObjectList(renderObjectList);
	}
	
	private Minecraft getGameInstance() {
		return Minecraft.getInstance();				
	}
	
	private void clearOreMap() {
		for (SortedSet<ChunkBlockPos> list: oreMap.values()) {
			list.clear();
		}
	}	
	
	@SubscribeEvent
	public void findOre(ClientTickEvent event) {
		Minecraft maincra = getGameInstance();
		if (!isActive && maincra.level != null && maincra.player != null && !oreMap.isEmpty())
			clearOreMap();
		if (!isActive || maincra.level == null || maincra.player == null)
			return;				
		
		Player player = maincra.player;
		BlockPos playerBPos = player.blockPosition();
		BlockPos[] range = bHelper.getNextPos(playerBPos);
		Level level = maincra.level;
		
		for (Entry<Block, SortedSet<ChunkBlockPos>> entry: oreMap.entrySet()) {
			Iterator<ChunkBlockPos> iter = entry.getValue().iterator();
			while (iter.hasNext()) {
				ChunkBlockPos chPos = iter.next();
				if (chPos.getChunkAssigned() == bHelper.getCurrentChuk())
					iter.remove();
			}
		}
		
		for (BlockPos bPos: BlockPos.betweenClosed(range[0], range[1])) {
			Block currentBlock = level.getBlockState(bPos).getBlock();
			if (BLOCKLISTHUNT.contains(currentBlock)) {
				bPos = bPos.immutable();
				oreMap.get(currentBlock).add(new ChunkBlockPos(bPos, bHelper.getCurrentChuk()));				
			}
		}
		bHelper.setChunkInformation(0);
	}

	public List<RenderObject> getRenderObjectList() {
		return renderObjectList;
	}
	
	public void findOre() {
		// Not being used atm.
		Minecraft maincra = getGameInstance();
		if (!isActive || maincra.level == null || maincra.player == null)
			return;
		if (lastDelay == 0 || lastDelay + DEFAULT_SEARCH_DELAY < System.currentTimeMillis()) 
			lastDelay = System.currentTimeMillis();			
		else {
//			addOreIntoRenderer();
			return;			
		}		
//		clearOreMap(); // Memory leak, list isnt cleaned.
		Vec3 playerPos = maincra.player.position();		
		int halfRange = ORERANGE / 2;
		
		BlockPos playerBlockPos = new BlockPos(playerPos);		
		BlockPos startBlock = playerBlockPos.north(halfRange).east(halfRange).above(halfRange);
		BlockPos endBlock = playerBlockPos.south(halfRange).west(halfRange).below(halfRange);
		
		
		for (BlockPos bPos: BlockPos.betweenClosed(startBlock, endBlock)) {			
			Block currentBlock = maincra.level.getBlockState(bPos).getBlock();			
			if (BLOCKLISTHUNT.contains(currentBlock)) {
				bPos = bPos.immutable();
				oreMap.get(currentBlock).add(new ChunkBlockPos(bPos, 0));				
			}
		}
//		addOreIntoRenderer();
	}
	
	private void doRenderUpdate() {
		if (renderer != null) {
			usedRenderObjects = 0;
			for (Entry<Block, SortedSet<ChunkBlockPos>> entry: oreMap.entrySet()) {
				Color c = COLOR_ORE_MAP.get(entry.getKey());
				int currentOreLimit = 0;
					for (BlockPos bPos: entry.getValue()) {
						if (currentOreLimit >= ORE_LIMIT_FOR_RENDER)
							break;
						RenderObject obj = renderObjectList.get(usedRenderObjects);						
						obj.enableRender();							
						obj.updateData(bPos);
						obj.setColor(c);										
						usedRenderObjects++;
						currentOreLimit++;
				}				
			}			
		}
	}
	
//	private void addOreIntoRenderer() {
//		if (!(renderer == null)) {
//			for (Entry<Block, SortedSet<ChunkBlockPos>> entry: oreMap.entrySet()) {
//				Color c = COLOR_ORE_MAP.get(entry.getKey());
//				int currentOreLimit = 0;
//					for (BlockPos bPos: entry.getValue()) {
//						if (currentOreLimit >= ORE_LIMIT_FOR_RENDER)
//							break;
//						renderer.addRenderList(c, Renderer.getVertexListFromAABB(new AABB(bPos)));
//						currentOreLimit++;
//				}				
//			}
//		}			
//	}
//	
	public void activate() {
		isActive = true;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public void toggle() {
		isActive = !isActive;
		announceActivationState();
	}
	
	private void announceActivationState() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null) {
			if (isActive) { 
				mc.gui.getChat().addMessage(Component.literal("Ore Finder Activated.").withStyle(ChatFormatting.GREEN));								
			}
			else {
				mc.gui.getChat().addMessage(Component.literal("Ore Finder Deactivated.").withStyle(ChatFormatting.RED));
				
			}				
		}
	}
	
	@Override
	public void getRenderEffect() {
//		 findOre();
//		addOreIntoRenderer();
		doRenderUpdate();
		
	}
	
	private class OreDistanceComparator implements Comparator<ChunkBlockPos> {		
		
		@Override
		public int compare(ChunkBlockPos bPos0, ChunkBlockPos bPos1) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			return Double.compare(player.distanceToSqr((double) bPos0.getX(), (double) bPos0.getY(), (double) bPos0.getZ()), player.distanceToSqr((double) bPos1.getX(), (double) bPos1.getY(), (double) bPos1.getZ()));
		}
		
	}
}
