package com.example.examplemod.Renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.example.examplemod.MManager.MManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
	private Map<Color, List<Vec3>> vertexToRender;
	private Map<Color, List<LivingEntity>> livingEntitiesToRender;
	private static final Color DEFAULT_COLOR = Color.GREEN;
	private List<RenderEffect> effectsToRenderList;
	
	public Renderer(MManager manager) {
		this.manager = manager;
		isActive = false;
		vertexToRender = new HashMap<>();
		livingEntitiesToRender = new HashMap<>();
		effectsToRenderList = new ArrayList<>();
	}
	
	public void addRenderEffectClass(RenderEffect rEffect) {
		effectsToRenderList.add(rEffect);
	}
	
	
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
		RenderSystem.disableBlend();	
		RenderSystem.disablePolygonOffset();
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
	
	private void setPolygonLine() {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);		
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1f, -1f);
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
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
		float oldLineWidth = RenderSystem.getShaderLineWidth();
				
		pStack.pushPose();
		// Do interpolation translation for smoothness
		double x = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
		double y = (view.yOld + ((view.getY() - view.yOld) * event.getPartialTick()));
		double z = view.zOld + (view.getZ() - view.zOld) * event.getPartialTick();
		
		pStack.translate(-x, -y- view.getEyeHeight(), -z);
		setPolygonFill();
		manager.mobTracker.trackMobs();
		manager.oreFinder.findOre();
		for (RenderEffect rEffect: effectsToRenderList) {
			rEffect.getRenderEffect();
		}
		doVertexRender(buffer, pStack.last().pose(), VertexFormat.Mode.QUADS);
		tess.end();
		disablePolygonFill();
		
		setPolygonLine();
		doVertexRender(buffer, pStack.last().pose(), VertexFormat.Mode.DEBUG_LINES);	
		tess.end();
		disablePolygonLine();		
		pStack.popPose();
		resetVertexMap();
		RenderSystem.lineWidth(oldLineWidth);
	}

	
	public synchronized void addRenderList(Color color, List<Vec3> vList) {
		if (vertexToRender.containsKey(color)) {
			vertexToRender.get(color).addAll(vList);
		} else {		
			vertexToRender.put(color, vList);
		}
	}
	
	public synchronized void addRenderListEntity(Color color, List<LivingEntity> entityList) {
		if (livingEntitiesToRender.containsKey(color)) {
			livingEntitiesToRender.get(color).addAll(entityList);
		} else {		
			livingEntitiesToRender.put(color, entityList);
		}
	}
	
//	private void prepareEntitisRender(float currentTick) {
//		for (List<LivingEntity> entityList: livingEntitiesToRender.values()) {
//			for (LivingEntity lEntity: entityList) {
////				double x = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
//				AABB bBox = lEntity.getBoundingBox();
//				double x = lEntity.xOld + (lEntity.getX() - lEntity.xOld) * currentTick;
//				double y = lEntity.yOld + (lEntity.getY() - lEntity.yOld) * currentTick;
//				double z = lEntity.zOld + (lEntity.getZ() - lEntity.zOld) * currentTick;
//				lEntity.
////				bBox = new AABB();
//			}
//		}
//	}
	
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
	}	
	
	public static List<Vec3> getVertexListLineFromAABB(AABB box) {
		ArrayList<Vec3> vList = new ArrayList<>(24);
		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
		double minX = box.minX, minY = box.minY, minZ = box.minZ;		
		
		vList.add(new Vec3(minX, minY, minZ));						
		vList.add(new Vec3(minX, maxY, minZ));
		
		vList.add(new Vec3(maxX, maxY, minZ));
		vList.add(new Vec3(maxX, minY, minZ));		
		
		vList.add(new Vec3(minX, minY, maxZ));						
		vList.add(new Vec3(minX, maxY, maxZ));
		
		vList.add(new Vec3(maxX, maxY, maxZ));
		vList.add(new Vec3(maxX, minY, maxZ));		
		
		vList.add(new Vec3(minX, maxY, minZ));
		vList.add(new Vec3(minX, maxY, maxZ));
		vList.add(new Vec3(minX, maxY, maxZ));
		vList.add(new Vec3(maxX, maxY, maxZ));
		vList.add(new Vec3(maxX, maxY, maxZ));
		vList.add(new Vec3(maxX, maxY, minZ));
		vList.add(new Vec3(maxX, maxY, minZ));
		vList.add(new Vec3(minX, maxY, minZ));
		
		vList.add(new Vec3(minX, minY, minZ));
		vList.add(new Vec3(minX, minY, maxZ));
		vList.add(new Vec3(minX, minY, maxZ));
		vList.add(new Vec3(maxX, minY, maxZ));
		vList.add(new Vec3(maxX, minY, maxZ));
		vList.add(new Vec3(maxX, minY, minZ));
		vList.add(new Vec3(maxX, minY, minZ));
		vList.add(new Vec3(minX, minY, minZ));
		return vList;
	}
	
	public static List<Vec3> getVertexQuadListFromAABB(AABB box) {
		return Renderer.getVertexListFromAABB(box);
	}
	
	public static List<Vec3> getVertexListFromAABB(AABB box) {
		ArrayList<Vec3> vList = new ArrayList<>(24);
		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
		double minX = box.minX, minY = box.minY, minZ = box.minZ;		
		
			vList.add(new Vec3(minX, minY, minZ));						
			vList.add(new Vec3(minX, maxY, minZ));
			vList.add(new Vec3(maxX, maxY, minZ));
			vList.add(new Vec3(maxX, minY, minZ));
			
			vList.add(new Vec3(maxX, minY, minZ));
			vList.add(new Vec3(maxX, maxY, minZ));
			vList.add(new Vec3(maxX, maxY, maxZ));
			vList.add(new Vec3(maxX, minY, maxZ));
			
			vList.add(new Vec3(minX, minY, maxZ));						
			vList.add(new Vec3(minX, maxY, maxZ));
			vList.add(new Vec3(maxX, maxY, maxZ));
			vList.add(new Vec3(maxX, minY, maxZ));

			vList.add(new Vec3(minX, minY, minZ));
			vList.add(new Vec3(minX, maxY, minZ));
			vList.add(new Vec3(minX, maxY, maxZ));
			vList.add(new Vec3(minX, minY, maxZ));
			
			vList.add(new Vec3(minX, maxY, minZ));
			vList.add(new Vec3(minX, maxY, maxZ));
			vList.add(new Vec3(maxX, maxY, maxZ));
			vList.add(new Vec3(maxX, maxY, minZ));
			
			vList.add(new Vec3(minX, minY, minZ));
			vList.add(new Vec3(minX, minY, maxZ));
			vList.add(new Vec3(maxX, minY, maxZ));
			vList.add(new Vec3(maxX, minY, minZ));			
		return vList;
	}	
}
