package com.example.examplemod.OreFinder;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;

public class OreFinderChunk {

	private int blocksLoaded;
	private List<BlockState> blockStateList;
	
	public OreFinderChunk() {
		blockStateList = new LinkedList<>();
		blocksLoaded = 0;
	}
	
	public List<BlockState> getBlockStateSet() {
		return blockStateList;
	}
	
	public void setBlockStateSet(LinkedList<BlockState> blockList) {
		blockStateList = blockList;
		blocksLoaded = blockStateList.size();
	}
	
	public void clear() {
		blockStateList.clear();
		blocksLoaded = 0;
	}
	
	public void addBlockState(BlockState block) {
		blockStateList.add(block);
	}
	
	public int getBlocksLoaded() {
		return blocksLoaded;
	}
	
}
