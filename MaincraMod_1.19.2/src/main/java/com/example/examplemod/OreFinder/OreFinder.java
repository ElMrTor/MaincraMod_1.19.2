package com.example.examplemod.OreFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.example.examplemod.Renderer.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;



public class OreFinder {
	
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
	}};
	
	private final ArrayList<Block> BLOCKLISTHUNT = new ArrayList<>(Arrays.asList(COLOR_ORE_MAP.keySet().toArray(new Block[0])));
	private Map<Block, ArrayList<BlockPos>> oreMap;
	
//	private static final Logger LOG = LogManager.getLogger();
	
	private final int ORE_LIMIT_FOR_RENDER = 5;
	private final int ORERANGE = 50;
	private boolean isActive;
	private final long DEFAULT_SEARCH_DELAY = 3000;
	private long lastDelay;
	private Renderer renderer;
	private OreDistanceComparator oreComp;
	
	public OreFinder() {
		isActive = false;		
		oreMap = new HashMap<>();
		for (Block block: COLOR_ORE_MAP.keySet()) {
			oreMap.put(block, new ArrayList<>());
		}
		lastDelay = 0;
		oreComp = new OreDistanceComparator();
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
	}
	
	private Minecraft getGameInstance() {
		return Minecraft.getInstance();				
	}
	
	private void clearOreMap() {
		for (ArrayList<BlockPos> list: oreMap.values()) {
			list.clear();
		}
	}	
	
	public void findOre() {
		Minecraft maincra = getGameInstance();
		if (!isActive || maincra.level == null || maincra.player == null)
			return;
		if (lastDelay == 0 || lastDelay + DEFAULT_SEARCH_DELAY < System.currentTimeMillis()) 
			lastDelay = System.currentTimeMillis();			
		else {
			addOreIntoRenderer();
			return;			
		}
		

		clearOreMap();
		Vec3 playerPos = maincra.player.position();		
		int halfRange = ORERANGE / 2;
		
		BlockPos playerBlockPos = new BlockPos(playerPos);		
		BlockPos startBlock = playerBlockPos.north(halfRange).east(halfRange).above(halfRange);
		BlockPos endBlock = playerBlockPos.south(halfRange).west(halfRange).below(halfRange);		
		
		for (BlockPos bPos: BlockPos.betweenClosed(startBlock, endBlock)) {
			bPos = bPos.immutable();
			Block currentBlock = maincra.level.getBlockState(bPos).getBlock();
			
			if (BLOCKLISTHUNT.contains(currentBlock)) {				
					oreMap.get(currentBlock).add(bPos);				
			}
		}
		for (List<BlockPos> bPosList: oreMap.values()) {
			bPosList.sort(oreComp);
		}
		addOreIntoRenderer();
	}
	
	private void addOreIntoRenderer() {
		if (!(renderer == null)) {
			for (Entry<Block, ArrayList<BlockPos>> entry: oreMap.entrySet()) {
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
	
	private class OreDistanceComparator implements Comparator<BlockPos> {
		
		@Override
		public int compare(BlockPos bPos0, BlockPos bPos1) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			return Double.compare(player.distanceToSqr((double) bPos0.getX(), (double) bPos0.getY(), (double) bPos0.getZ()), player.distanceToSqr((double) bPos1.getX(), (double) bPos1.getY(), (double) bPos1.getZ()));
		}
		
	}
}
