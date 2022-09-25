package com.example.examplemod.Renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import com.example.examplemod.MManager.MManager;
import com.example.examplemod.OreFinder.OreFinder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Renderer {
	
	private MManager manager;	
	private boolean isActive;
	private List<List<RenderObject>> renderObjectList;
	private Map<Color, List<Vec3>> vertexToRender;
	private Map<Color, List<LivingEntity>> livingEntitiesToRender;
	private static final Color DEFAULT_COLOR = Color.GREEN;
	private List<RenderEffect> effectsToRenderList;
	public static final Color BROWN = new Color(51, 25, 0);	
	
//	private int MIN_COLOR_VEC_SIZE = 500;
//	private Color[] COLORS = {
//			Color.BLACK,
//			Color.GREEN,
//			Color.BLUE,
//			Color.CYAN,
//			Color.DARK_GRAY,
//			Color.GRAY,
//			Color.LIGHT_GRAY,
//			Color.MAGENTA,
//			Color.ORANGE,
//			Color.PINK,
//			Color.RED,
//			Color.WHITE,
//			Color.YELLOW,
//			BROWN,
//	};
	
	public Renderer(MManager manager) {
		this.manager = manager;
		isActive = false;
		vertexToRender = new HashMap<>();
		livingEntitiesToRender = new HashMap<>();
		effectsToRenderList = new LinkedList<>();
		effectsToRenderList.add(this.manager.mobTracker);
		effectsToRenderList.add(this.manager.oreFinder);	
		renderObjectList = new ArrayList<List<RenderObject>>();
	}
	
	public void addRenderEffectClass(RenderEffect rEffect) {
		effectsToRenderList.add(rEffect);
	}
	
	public void addRenderObjectList(List<RenderObject> rObjectList) {
		renderObjectList.add(rObjectList);
	}
	
	public void doRenderObject(BufferBuilder buffer, Matrix4f matrix, RenderEffect rEffect, Mode mode) {
		rEffect.getRenderEffect();
		int rLimit = rEffect.renderObjectsInUse();
		List<RenderObject> renderList = rEffect.getRenderObjectList();
		for (int i = 0; i < rLimit; ++i) {
			RenderObject rObject = renderList.get(i);
			if (!buffer.building())
				buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);
			int alpha;
			Color c;
			if (VertexFormat.Mode.QUADS.equals(mode)) {
				alpha = 50;
				c = rObject.getFillColor();
			}
			else {
				alpha = 0;
				c = rObject.getOutlineColor();
			}
			
			for (VPoint p: rObject.getVertexForMode(mode)) {
				buffer.vertex(matrix, p.getX(), p.getY(), p.getZ()).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() + alpha).endVertex();
			}
		}
	}
	
	
	
	@SubscribeEvent
	public void render(RenderLevelStageEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (!isActive || mc.level == null || !event.getStage().equals(Stage.AFTER_PARTICLES))
			return;
		

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tess = RenderSystem.renderThreadTesselator();
		BufferBuilder buffer = tess.getBuilder();
		PoseStack pStack = event.getPoseStack();
		Entity view = mc.getCameraEntity();
		
				
		pStack.pushPose();
		// Do interpolation translation for smoothness
		double x = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
		double y = (view.yOld + ((view.getY() - view.yOld) * event.getPartialTick()));
		double z = view.zOld + (view.getZ() - view.zOld) * event.getPartialTick();
		
		pStack.translate(-x, -y- view.getEyeHeight(), -z);
		
		for (RenderEffect rEffect: effectsToRenderList) {
			if (rEffect != null && rEffect.isRenderActive()) {
				setPolygonFill();
				doRenderObject(buffer, pStack.last().pose(), rEffect, VertexFormat.Mode.QUADS);
				if (buffer.building())
					tess.end();
				disablePolygonFill();		
				setPolygonLine();
				doRenderObject(buffer, pStack.last().pose(), rEffect, VertexFormat.Mode.DEBUG_LINES);
				if (buffer.building())
					tess.end();
				disablePolygonLine();
			}
		}
		pStack.popPose();
		resetVertexMap();		
	}

	
	public synchronized void addRenderList(Color color, List<Vec3> vList) {
		if (vertexToRender.containsKey(color)) {
			vertexToRender.get(color).addAll(vList);
		} else {		
			vertexToRender.put(color, vList);
		}
	}
	
	public synchronized void addRenderList(Color color, Iterator<Vec3> vList) {
		if (vertexToRender.containsKey(color)) {
			vList.forEachRemaining(vertexToRender.get(color)::add);			
		} else {
			LinkedList<Vec3> nList = new LinkedList<>();
			vList.forEachRemaining(nList::add);			
		}
	}
	
//	public synchronized void addRenderBoxList(Color color, List<AABB> boxList) {		
//		for (AABB box: boxList) {
//			addRenderList(color, getVertexListFromAABB(box));
//		}
//		
//	}
	
	public synchronized void addRenderListEntity(Color color, List<LivingEntity> entityList) {
		if (livingEntitiesToRender.containsKey(color)) {
			livingEntitiesToRender.get(color).addAll(entityList);
		} else {		
			livingEntitiesToRender.put(color, entityList);
		}
	}
	
	
	public void addRenderList(List<Vec3> vList) {
		addRenderList(Renderer.DEFAULT_COLOR, vList);
	}

	private void doVertexRender(BufferBuilder buffer, Matrix4f matrix, VertexFormat.Mode mode) {
		
		if (!buffer.building())
			buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);
		int alpha;
		if (VertexFormat.Mode.QUADS.equals(mode))
			alpha = 50;
		else
			alpha = 0;
		for (Entry<Color, List<Vec3>> entry: vertexToRender.entrySet()) {
			Color c = entry.getKey();
			List<Vec3> pointList;
			if (VertexFormat.Mode.DEBUG_LINES.equals(mode)) {
				LineFace lFace = new LineFace(entry.getValue());
				pointList = lFace.getQuadPointsAsLines();
			}
			else 
				pointList = entry.getValue();
			for (Vec3 v: pointList) {
				buffer.vertex(matrix, (float) v.x, (float) v.y, (float) v.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() + alpha).endVertex();
			}			
		}		
	}
	

	private void resetVertexMap() {
		vertexToRender.clear();
		livingEntitiesToRender.clear();
	}
	
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
	
//	public static List<Vec3> getVertexListLineFromAABB(AABB box) {
//		LinkedList<Vec3> vList = new LinkedList<>();
//		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
//		double minX = box.minX, minY = box.minY, minZ = box.minZ;		
//		
//		vList.add(new Vec3(minX, minY, minZ));						
//		vList.add(new Vec3(minX, maxY, minZ));
//		
//		vList.add(new Vec3(maxX, maxY, minZ));
//		vList.add(new Vec3(maxX, minY, minZ));		
//		
//		vList.add(new Vec3(minX, minY, maxZ));						
//		vList.add(new Vec3(minX, maxY, maxZ));
//		
//		vList.add(new Vec3(maxX, maxY, maxZ));
//		vList.add(new Vec3(maxX, minY, maxZ));		
//		
//		vList.add(new Vec3(minX, maxY, minZ));
//		vList.add(new Vec3(minX, maxY, maxZ));
//		vList.add(new Vec3(minX, maxY, maxZ));
//		vList.add(new Vec3(maxX, maxY, maxZ));
//		vList.add(new Vec3(maxX, maxY, maxZ));
//		vList.add(new Vec3(maxX, maxY, minZ));
//		vList.add(new Vec3(maxX, maxY, minZ));
//		vList.add(new Vec3(minX, maxY, minZ));
//		
//		vList.add(new Vec3(minX, minY, minZ));
//		vList.add(new Vec3(minX, minY, maxZ));
//		vList.add(new Vec3(minX, minY, maxZ));
//		vList.add(new Vec3(maxX, minY, maxZ));
//		vList.add(new Vec3(maxX, minY, maxZ));
//		vList.add(new Vec3(maxX, minY, minZ));
//		vList.add(new Vec3(maxX, minY, minZ));
//		vList.add(new Vec3(minX, minY, minZ));
//		return vList;
//	}
//	
//	public static List<Vec3> getVertexQuadListFromAABB(AABB box) {
//		return Renderer.getVertexListFromAABB(box);
//	}
//	
//	public static List<Vec3> getVertexListFromAABB(AABB box) {
//		LinkedList<Vec3> vList = new LinkedList<>();
//		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
//		double minX = box.minX, minY = box.minY, minZ = box.minZ;		
//		
//			vList.add(new Vec3(minX, minY, minZ));						
//			vList.add(new Vec3(minX, maxY, minZ));
//			vList.add(new Vec3(maxX, maxY, minZ));
//			vList.add(new Vec3(maxX, minY, minZ));
//			
//			vList.add(new Vec3(maxX, minY, minZ));
//			vList.add(new Vec3(maxX, maxY, minZ));
//			vList.add(new Vec3(maxX, maxY, maxZ));
//			vList.add(new Vec3(maxX, minY, maxZ));
//			
//			vList.add(new Vec3(minX, minY, maxZ));						
//			vList.add(new Vec3(minX, maxY, maxZ));
//			vList.add(new Vec3(maxX, maxY, maxZ));
//			vList.add(new Vec3(maxX, minY, maxZ));
//
//			vList.add(new Vec3(minX, minY, minZ));
//			vList.add(new Vec3(minX, maxY, minZ));
//			vList.add(new Vec3(minX, maxY, maxZ));
//			vList.add(new Vec3(minX, minY, maxZ));
//			
//			vList.add(new Vec3(minX, maxY, minZ));
//			vList.add(new Vec3(minX, maxY, maxZ));
//			vList.add(new Vec3(maxX, maxY, maxZ));
//			vList.add(new Vec3(maxX, maxY, minZ));
//			
//			vList.add(new Vec3(minX, minY, minZ));
//			vList.add(new Vec3(minX, minY, maxZ));
//			vList.add(new Vec3(maxX, minY, maxZ));
//			vList.add(new Vec3(maxX, minY, minZ));			
//		return vList;
//	}
	
	private void setPolygonFill() {
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disablePolygonFill() {
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
	
	private void disablePolygonLine() {
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
//		RenderSystem.disableBlend();	
		RenderSystem.disablePolygonOffset();
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
	
	private void setPolygonLine() {
//		RenderSystem.enableBlend();
//		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);		
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1f, -1f);
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	}
	
	
	private void announceActivationState() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null) {
			if (isActive) { 
				mc.gui.getChat().addMessage(Component.literal("Renderer Activated.").withStyle(ChatFormatting.GREEN));								
			}
			else {
				mc.gui.getChat().addMessage(Component.literal("Renderer Deactivated.").withStyle(ChatFormatting.RED));
				
			}				
		}
	}

	
//	private void prepareEntitisRender(float currentTick) {
//	for (List<LivingEntity> entityList: livingEntitiesToRender.values()) {
//		for (LivingEntity lEntity: entityList) {
////			double x = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
//			AABB bBox = lEntity.getBoundingBox();
//			double x = lEntity.xOld + (lEntity.getX() - lEntity.xOld) * currentTick;
//			double y = lEntity.yOld + (lEntity.getY() - lEntity.yOld) * currentTick;
//			double z = lEntity.zOld + (lEntity.getZ() - lEntity.zOld) * currentTick;
////			lEntity.
////			bBox = new AABB();
//		}
//	}
//}
//	public class RenderObject {
//
//		private static final Color[] DEFAULT_COLORS = new Color[] {Color.YELLOW, Color.RED};	
//		private Logger LOGGER = LogUtils.getLogger();
//		private final int OBJECT_VERTEX_AMOUNT = 24;
//		private Color outlineColor;
//		private Color fillColor;
//		private boolean render;
//		private VPoint[] quadRenderPoints;
//		private VPoint[] lineRenderPoints;
//		private long renderLastUpdate;
//		
//		
//		
//		
//		public RenderObject() {
//			quadRenderPoints = new VPoint[OBJECT_VERTEX_AMOUNT];
//			lineRenderPoints = new VPoint[OBJECT_VERTEX_AMOUNT];
//			for (int i = 0; i < OBJECT_VERTEX_AMOUNT; ++i) {
//				quadRenderPoints[i] = new VPoint();
//				lineRenderPoints[i] = new VPoint();
//			}
//			render = false;
//			renderLastUpdate = System.currentTimeMillis();
//			resetColor();
//		}
//		
//		public void resetColor() {
//			outlineColor = DEFAULT_COLORS[1];
//			fillColor = DEFAULT_COLORS[0];
//		}
//		
//		public boolean render() {
//			return render;
//		}
//		
//		public void disableRender() {
//			resetColor();
//			render = false;
//		}
//		
//		public void enableRender() {
//			render = true;
//		}
//		
//		public void setRender(boolean render) {
//			this.render = render;
//		}	
//		
//		public Color getOutlineColor() {
//			return outlineColor;
//		}
//
//		public void setOutlineColor(Color outlineColor) {
//			this.outlineColor = outlineColor;
//		}
//
//		public Color getFillColor() {
//			return fillColor;
//		}
//
//		public void setFillColor(Color fillColor) {
//			this.fillColor = fillColor;
//		}
//		
//		public void setColor(Color c) {
//			fillColor = c;
//			outlineColor = c;
//		}
//
//		public VPoint[] getQuadRenderPoints() {
//			return quadRenderPoints;
//		}
//
//		public VPoint[] getLineRenderPoints() {
//			return lineRenderPoints;
//		}
//		
//		public void updateData(BlockPos pos) {			
//			double minX = Math.min(pos.getX(), pos.getX() + 1);
//			double minY = Math.min(pos.getY(), pos.getY() + 1);
//			double minZ = Math.min(pos.getZ(), pos.getZ() + 1);
//			double maxX = Math.max(pos.getX(), pos.getX() + 1);
//			double maxY = Math.max(pos.getY(), pos.getY() + 1);
//			double maxZ = Math.max(pos.getZ(), pos.getZ() + 1);
//			RenderObject.setVertexLines(maxX, maxY, maxZ, minX, minY, minZ, lineRenderPoints);
//			RenderObject.setVertexQuads(maxX, maxY, maxZ, minX, minY, minZ, quadRenderPoints);
//		}
//		
//		public void updateData(AABB box) {			
//			double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
//			double minX = box.minX, minY = box.minY, minZ = box.minZ;		
//			RenderObject.setVertexLines(maxX, maxY, maxZ, minX, minY, minZ, lineRenderPoints);
//			RenderObject.setVertexQuads(maxX, maxY, maxZ, minX, minY, minZ, quadRenderPoints);		
//		}
//		
//		public VPoint[] getVertexForMode(Mode mode) {
//			if (mode.equals(Mode.QUADS)) {
//				return getQuadRenderPoints();
//			}
//			else if (mode.equals(Mode.DEBUG_LINES)) {
//				return getLineRenderPoints();
//			}
//			else {
//				LOGGER.info("Mode provided for vertex format not found.");
//				return new VPoint[0];
//			}
//		}
//		
//		public long getRenderLastUpdate() {
//			return renderLastUpdate;
//		}
//		
//		public void setRenderLastUpdate(long updateTime) {
//			renderLastUpdate = updateTime;
//		}
//		
//		public static void setVertexLines(double maxX, double maxY, double maxZ,
//										double minX, double minY, double minZ,
//										VPoint[] dataArray) {
//			
//			dataArray[0].update(minX, minY, minZ);
//			dataArray[1].update(minX, maxY, minZ);		
//			dataArray[2].update(maxX, maxY, minZ);
//			dataArray[3].update(maxX, minY, minZ);
//			dataArray[4].update(minX, minY, maxZ);						
//			dataArray[5].update(minX, maxY, maxZ);
//			dataArray[6].update(maxX, maxY, maxZ);
//			dataArray[7].update(maxX, minY, maxZ);
//			dataArray[8].update(minX, maxY, minZ);
//			dataArray[9].update(minX, maxY, maxZ);
//			dataArray[10].update(minX, maxY, maxZ);
//			dataArray[11].update(maxX, maxY, maxZ);
//			dataArray[12].update(maxX, maxY, maxZ);
//			dataArray[13].update(maxX, maxY, minZ);
//			dataArray[14].update(maxX, maxY, minZ);
//			dataArray[15].update(minX, maxY, minZ);
//			dataArray[16].update(minX, minY, minZ);
//			dataArray[17].update(minX, minY, maxZ);
//			dataArray[18].update(minX, minY, maxZ);
//			dataArray[19].update(maxX, minY, maxZ);
//			dataArray[20].update(maxX, minY, maxZ);
//			dataArray[21].update(maxX, minY, minZ);
//			dataArray[22].update(maxX, minY, minZ);
//			dataArray[23].update(minX, minY, minZ);
//
//		}
//		
//		public static void setVertexQuads(double maxX, double maxY, double maxZ,
//										double minX, double minY, double minZ,
//										VPoint[] dataArray) {
//			
//			dataArray[0].update(minX, minY, minZ);						
//			dataArray[1].update(minX, maxY, minZ);
//			dataArray[2].update(maxX, maxY, minZ);
//			dataArray[3].update(maxX, minY, minZ);		
//			dataArray[4].update(maxX, minY, minZ);
//			dataArray[5].update(maxX, maxY, minZ);
//			dataArray[6].update(maxX, maxY, maxZ);
//			dataArray[7].update(maxX, minY, maxZ);		
//			dataArray[8].update(minX, minY, maxZ);						
//			dataArray[9].update(minX, maxY, maxZ);
//			dataArray[10].update(maxX, maxY, maxZ);
//			dataArray[11].update(maxX, minY, maxZ);        
//			dataArray[12].update(minX, minY, minZ);
//			dataArray[13].update(minX, maxY, minZ);
//			dataArray[14].update(minX, maxY, maxZ);
//			dataArray[15].update(minX, minY, maxZ);		
//			dataArray[16].update(minX, maxY, minZ);
//			dataArray[17].update(minX, maxY, maxZ);
//			dataArray[18].update(maxX, maxY, maxZ);
//			dataArray[19].update(maxX, maxY, minZ);		
//			dataArray[20].update(minX, minY, minZ);
//			dataArray[21].update(minX, minY, maxZ);
//			dataArray[22].update(maxX, minY, maxZ);
//			dataArray[23].update(maxX, minY, minZ);		
//		}
//
//		
//	}
//

}
