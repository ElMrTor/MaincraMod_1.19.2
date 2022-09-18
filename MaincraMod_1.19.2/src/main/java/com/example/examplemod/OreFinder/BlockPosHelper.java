package com.example.examplemod.OreFinder;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class BlockPosHelper {

	int x, y, z;
	
	private int baseNumber;
	private int totalBlocks;
	private int halfNumber;
	private int targetTicksForRender;
	private int blocksPerSlice;
	private int sliceNeeded;	
	private int currentChunk;
	private int[] oresLoadedByChunk;
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public BlockPosHelper(int baseNumber, int targetTicksForRender) {
		this.baseNumber = baseNumber;
		this.halfNumber = (int) (baseNumber / 2);
		this.totalBlocks = baseNumber * baseNumber * baseNumber;
		this.targetTicksForRender = targetTicksForRender;
		this.blocksPerSlice = baseNumber * baseNumber;
		this.sliceNeeded = (int) ((totalBlocks / targetTicksForRender) / blocksPerSlice);
		this.currentChunk = 0;
		this.oresLoadedByChunk = new int[targetTicksForRender];
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public BlockPos[] getNextPos(BlockPos playerBPos) {		
		BlockPos startPos = playerBPos.offset(-halfNumber, -halfNumber + (currentChunk * sliceNeeded), -halfNumber);
		BlockPos endPos = playerBPos.offset(halfNumber, -halfNumber + (currentChunk * sliceNeeded) +sliceNeeded, halfNumber);
//		LOGGER.info("Currentchunk: {} from: -> {} {} {} -> {} {} {}", currentChunk, startPos.getX(), startPos.getY(), startPos.getZ(), endPos.getX(), endPos.getY(), endPos.getZ());
		return new BlockPos[] {startPos, endPos};
	}
	
	public void setChunkInformation(int oresFound) { // Badly implemented
		oresLoadedByChunk[currentChunk] = oresFound;
		currentChunk++;
		if (currentChunk >= targetTicksForRender) {
			currentChunk = 0;
		}
	}
	
	public int getCurrentChuk() {
		return currentChunk;
	}
	
	
}
