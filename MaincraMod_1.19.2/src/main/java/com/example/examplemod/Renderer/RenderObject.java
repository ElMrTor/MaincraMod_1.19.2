package com.example.examplemod.Renderer;

import java.awt.Color;

import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class RenderObject {

	private static final Color[] DEFAULT_COLORS = new Color[] {Color.YELLOW, Color.RED};	
	private Logger LOGGER = LogUtils.getLogger();
	private final int OBJECT_VERTEX_AMOUNT = 24;
	private Color outlineColor;
	private Color fillColor;
	private boolean render;
	private VPoint[] quadRenderPoints;
	private VPoint[] lineRenderPoints;
	
	
	
	
	public RenderObject() {
		quadRenderPoints = new VPoint[OBJECT_VERTEX_AMOUNT];
		lineRenderPoints = new VPoint[OBJECT_VERTEX_AMOUNT];
		for (int i = 0; i < OBJECT_VERTEX_AMOUNT; ++i) {
			quadRenderPoints[i] = new VPoint();
			lineRenderPoints[i] = new VPoint();
		}
		render = false;
	
		resetColor();
	}
	
	public void resetColor() {
		outlineColor = DEFAULT_COLORS[1];
		fillColor = DEFAULT_COLORS[0];
	}
	
	public boolean render() {
		return render;
	}
	
	public void disableRender() {
		resetColor();
		render = false;
	}
	
	public void enableRender() {
		render = true;
	}
	
	public void setRender(boolean render) {
		this.render = render;
	}	
	
	public Color getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(Color outlineColor) {
		this.outlineColor = outlineColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	
	public void setColor(Color c) {
		fillColor = c;
		outlineColor = c;
	}

	public VPoint[] getQuadRenderPoints() {
		return quadRenderPoints;
	}

	public VPoint[] getLineRenderPoints() {
		return lineRenderPoints;
	}
	
	public void updateData(BlockPos pos) {
		double minX = Math.min(pos.getX(), pos.getX() + 1);
		double minY = Math.min(pos.getY(), pos.getY() + 1);
		double minZ = Math.min(pos.getZ(), pos.getZ() + 1);
		double maxX = Math.max(pos.getX(), pos.getX() + 1);
		double maxY = Math.max(pos.getY(), pos.getY() + 1);
		double maxZ = Math.max(pos.getZ(), pos.getZ() + 1);
		RenderObject.setVertexLines(maxX, maxY, maxZ, minX, minY, minZ, lineRenderPoints);
		RenderObject.setVertexQuads(maxX, maxY, maxZ, minX, minY, minZ, quadRenderPoints);
	}
	
	public void updateData(AABB box) {
		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
		double minX = box.minX, minY = box.minY, minZ = box.minZ;		
		RenderObject.setVertexLines(maxX, maxY, maxZ, minX, minY, minZ, lineRenderPoints);
		RenderObject.setVertexQuads(maxX, maxY, maxZ, minX, minY, minZ, quadRenderPoints);		
	}
	
	public VPoint[] getVertexForMode(Mode mode) {
		if (mode.equals(Mode.QUADS)) {
			return getQuadRenderPoints();
		}
		else if (mode.equals(Mode.DEBUG_LINES)) {
			return getLineRenderPoints();
		}
		else {
			LOGGER.info("Mode provided for vertex format not found.");
			return new VPoint[0];
		}
	}
	
	
	public static void setVertexLines(double maxX, double maxY, double maxZ,
									double minX, double minY, double minZ,
									VPoint[] dataArray) {
		
		dataArray[0].update(minX, minY, minZ);
		dataArray[1].update(minX, maxY, minZ);		
		dataArray[2].update(maxX, maxY, minZ);
		dataArray[3].update(maxX, minY, minZ);
		dataArray[4].update(minX, minY, maxZ);						
		dataArray[5].update(minX, maxY, maxZ);
		dataArray[6].update(maxX, maxY, maxZ);
		dataArray[7].update(maxX, minY, maxZ);
		dataArray[8].update(minX, maxY, minZ);
		dataArray[9].update(minX, maxY, maxZ);
		dataArray[10].update(minX, maxY, maxZ);
		dataArray[11].update(maxX, maxY, maxZ);
		dataArray[12].update(maxX, maxY, maxZ);
		dataArray[13].update(maxX, maxY, minZ);
		dataArray[14].update(maxX, maxY, minZ);
		dataArray[15].update(minX, maxY, minZ);
		dataArray[16].update(minX, minY, minZ);
		dataArray[17].update(minX, minY, maxZ);
		dataArray[18].update(minX, minY, maxZ);
		dataArray[19].update(maxX, minY, maxZ);
		dataArray[20].update(maxX, minY, maxZ);
		dataArray[21].update(maxX, minY, minZ);
		dataArray[22].update(maxX, minY, minZ);
		dataArray[23].update(minX, minY, minZ);

	}
	
	public static void setVertexQuads(double maxX, double maxY, double maxZ,
									double minX, double minY, double minZ,
									VPoint[] dataArray) {
		
		dataArray[0].update(minX, minY, minZ);						
		dataArray[1].update(minX, maxY, minZ);
		dataArray[2].update(maxX, maxY, minZ);
		dataArray[3].update(maxX, minY, minZ);		
		dataArray[4].update(maxX, minY, minZ);
		dataArray[5].update(maxX, maxY, minZ);
		dataArray[6].update(maxX, maxY, maxZ);
		dataArray[7].update(maxX, minY, maxZ);		
		dataArray[8].update(minX, minY, maxZ);						
		dataArray[9].update(minX, maxY, maxZ);
		dataArray[10].update(maxX, maxY, maxZ);
		dataArray[11].update(maxX, minY, maxZ);        
		dataArray[12].update(minX, minY, minZ);
		dataArray[13].update(minX, maxY, minZ);
		dataArray[14].update(minX, maxY, maxZ);
		dataArray[15].update(minX, minY, maxZ);		
		dataArray[16].update(minX, maxY, minZ);
		dataArray[17].update(minX, maxY, maxZ);
		dataArray[18].update(maxX, maxY, maxZ);
		dataArray[19].update(maxX, maxY, minZ);		
		dataArray[20].update(minX, minY, minZ);
		dataArray[21].update(minX, minY, maxZ);
		dataArray[22].update(maxX, minY, maxZ);
		dataArray[23].update(maxX, minY, minZ);		
	}

	
}
