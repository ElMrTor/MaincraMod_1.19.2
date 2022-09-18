package com.example.examplemod.OreFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.example.examplemod.Renderer.RenderEffect;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.example.examplemod.Renderer.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
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
	
	private final HashMap<Block, Color> COLOR_ORE_MAP = new HashMap<>() {{
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
	
//	private static final Logger LOG = LogManager.getLogger();
	private static final int TICKS_TO_FULL_SCAN = 20;
	private final int ORE_LIMIT_FOR_RENDER = 8;
	private final int ORERANGE = 60; // Assume Ore range will be divisible by 10
	private boolean isActive;
	private final long DEFAULT_SEARCH_DELAY = 3000;
	private long lastDelay;
	private Renderer renderer;
	private BlockPosHelper bHelper;
//	private OreDistanceComparator oreComp;
//	private int currentOreChunk;
//	private List<OreFinderChunk> oreChunkList;
	
	public OreFinder() {
		isActive = false;		
		oreMap = new HashMap<>();
		for (Block block: COLOR_ORE_MAP.keySet()) {
			oreMap.put(block, new TreeSet<>(new OreDistanceComparator()));
		}
		lastDelay = 0;
		bHelper = new BlockPosHelper(ORERANGE, TICKS_TO_FULL_SCAN);
//		oreComp = new OreDistanceComparator();
//		currentOreChunk = 0;
//		oreChunkList = new ArrayList<>(TICKS_TO_FULL_SCAN);
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
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
	public void findOre(ClientTickEvent event ) {
		Minecraft maincra = getGameInstance();
		if (!isActive && maincra.level != null && maincra.player != null)
			clearOreMap();
		if (!isActive || maincra.level == null || maincra.player == null)
			return;
				
//		if (lastDelay == 0 || lastDelay + DEFAULT_SEARCH_DELAY < System.currentTimeMillis()) 
//			lastDelay = System.currentTimeMillis();			
//		else {
//			addOreIntoRenderer();
//			return;			
//		}
		
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
//		addOreIntoRenderer();		
	}

	public void findOre() {
		// Not being used atm.
		Minecraft maincra = getGameInstance();
		if (!isActive || maincra.level == null || maincra.player == null)
			return;
		if (lastDelay == 0 || lastDelay + DEFAULT_SEARCH_DELAY < System.currentTimeMillis()) 
			lastDelay = System.currentTimeMillis();			
		else {
			addOreIntoRenderer();
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
		addOreIntoRenderer();
	}
	
	private void addOreIntoRenderer() {
		if (!(renderer == null)) {
			for (Entry<Block, SortedSet<ChunkBlockPos>> entry: oreMap.entrySet()) {
				Color c = COLOR_ORE_MAP.get(entry.getKey());
				int currentOreLimit = 0;
					for (BlockPos bPos: entry.getValue()) {
						if (currentOreLimit >= ORE_LIMIT_FOR_RENDER)
							break;
						renderer.addRenderList(c, Renderer.getVertexListFromAABB(new AABB(bPos)));
						currentOreLimit++;
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
	
	@Override
	public void getRenderEffect() {
//		 findOre();
		addOreIntoRenderer();
		
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
