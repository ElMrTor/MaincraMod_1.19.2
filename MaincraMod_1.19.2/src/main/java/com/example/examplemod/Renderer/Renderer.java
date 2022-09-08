package com.example.examplemod.Renderer;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Renderer {
	
	private MManager manager;	
	private boolean isActive;
	private Map<Color, List<Vec3>> vertexToRender;
	private static final Color DEFAULT_COLOR = Color.GREEN;
	private List<RenderEffect> effectsToRenderList;
	
	public Renderer(MManager manager) {
		this.manager = manager;
		isActive = false;
		vertexToRender = new HashMap<>();
		effectsToRenderList = new ArrayList<>();
	}
	//	public void addRenderFuntion(Function<Object, Object> o) {
//		functionList.add((Function<Object, Object>) o);
//	}
	
	public void addRenderEffectClass(RenderEffect rEffect) {
		effectsToRenderList.add(rEffect);
	}
	
	@SubscribeEvent
	public void render(RenderLevelStageEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (!isActive || mc.level == null || !event.getStage().equals(Stage.AFTER_PARTICLES))
			return;
		
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tess = RenderSystem.renderThreadTesselator();
//		Tesselator tess = Tesselator.getInstance();
		BufferBuilder buffer = tess.getBuilder();
		PoseStack pStack = event.getPoseStack();
		Entity view = mc.getCameraEntity();
		float oldLineWidth = RenderSystem.getShaderLineWidth();
		RenderSystem.lineWidth(oldLineWidth * 50);
				
		pStack.pushPose();
		// Do interpolation translation for smoothness
		double x = view.xOld + (view.getX() - view.xOld) * event.getPartialTick();
		double y = (view.yOld + ((view.getY() - view.yOld) * event.getPartialTick()));
		double z = view.zOld + (view.getZ() - view.zOld) * event.getPartialTick();
		
		pStack.translate(-x, -y- view.getEyeHeight(), -z);
		
		manager.mobTracker.trackMobs();
		manager.oreFinder.findOre();
		for (RenderEffect rEffect: effectsToRenderList) {
			rEffect.getRenderEffect();
		}
		
		doVertexRender(buffer, pStack.last().pose());
		tess.end();
		pStack.popPose();
		RenderSystem.enableDepthTest();
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
	
	public void addRenderList(List<Vec3> vList) {
		addRenderList(Renderer.DEFAULT_COLOR, vList);
	}
	
	private void doVertexRender(BufferBuilder buffer, Matrix4f matrix) {
		buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
		
		for (Entry<Color, List<Vec3>> entry: vertexToRender.entrySet()) {
			Color c = entry.getKey();
			for (Vec3 v: entry.getValue()) {
				buffer.vertex(matrix, (float) v.x, (float) v.y, (float) v.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
			}			
		}
	}	
	
	private void resetVertexMap() {
		vertexToRender = new HashMap<>();		
	}
	
	public void activate() {
		isActive = true;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public void toggleRenderer() {
		isActive = !isActive;
	}
	
	public static List<Vec3> getVertexListFromAABB(AABB box) {
		ArrayList<Vec3> vList = new ArrayList<>();
		double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
		double minX = box.minX, minY = box.minY, minZ = box.minZ;
//			TODO Some vertex are probably redundant pero puej... not today :)
//		 	Assemble front face
			vList.add(new Vec3(minX, minY, minZ));
			vList.add(new Vec3(minX, maxY, minZ));			
			vList.add(new Vec3(minX, maxY, minZ));
			vList.add(new Vec3(maxX, maxY, minZ));			
			vList.add(new Vec3(maxX, maxY, minZ));
			vList.add(new Vec3(maxX, minY, minZ));			
			vList.add(new Vec3(maxX, minY, minZ));
			vList.add(new Vec3(minX, minY, minZ));
			
//			Assemble right face
			vList.add(new Vec3(maxX, minY, minZ));
			vList.add(new Vec3(maxX, minY, maxZ));			
			vList.add(new Vec3(maxX, maxY, minZ));
			vList.add(new Vec3(maxX, maxY, maxZ));			
			vList.add(new Vec3(maxX, maxY, maxZ));
			vList.add(new Vec3(maxX, minY, maxZ));
			
//			Assemble left face
			vList.add(new Vec3(minX, minY, minZ));
			vList.add(new Vec3(minX, minY, maxZ));			
			vList.add(new Vec3(minX, minY, maxZ));
			vList.add(new Vec3(minX, maxY, maxZ));			
			vList.add(new Vec3(minX, maxY, maxZ));
			vList.add(new Vec3(minX, maxY, minZ));
			
//			Assemble back face, only two lines left
			vList.add(new Vec3(maxX, maxY, maxZ));
			vList.add(new Vec3(minX, maxY, maxZ));			
			vList.add(new Vec3(minX, minY, maxZ));
			vList.add(new Vec3(maxX, minY, maxZ));
		
		return vList;
	}	
}
