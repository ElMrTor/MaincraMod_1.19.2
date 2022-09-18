package com.example.examplemod.OreFinder;

import java.awt.Color;

import net.minecraft.core.BlockPos;

public class ChunkBlockPos extends BlockPos{

	private static final Color DEFAULT_COLOR = Color.WHITE;
	private int chunkAssigned;
	private Color currentColor;
	
	public ChunkBlockPos() {
		this(null);
	}
	
	public ChunkBlockPos(BlockPos bPos) {
		this(bPos, 0);
	}
	
	public ChunkBlockPos(BlockPos bPos, int chunkAssigned) {
		this(bPos, chunkAssigned, DEFAULT_COLOR);
	}
	
	public ChunkBlockPos(BlockPos bPos, int chunkAssigned, Color color) {
		super(bPos);
		this.chunkAssigned = chunkAssigned;
		this.currentColor = color;
	}	
	
	public int getChunkAssigned() {
		return chunkAssigned;
	}
	
	public Color getColor() {
		return currentColor;
	}
	
}
