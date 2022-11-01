package com.example.examplemod.Renderer;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public class EntityHealthDisplay implements RenderEffect{

	private Renderer renderer;
	
	public EntityHealthDisplay() {
		
	}
	
	public void setRenderer(Renderer r) {
		renderer = r;
	}
	
	@Override
	public void getRenderEffect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRenderActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int renderObjectsInUse() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<RenderObject> getRenderObjectList() {
		// TODO Auto-generated method stub
		return null;
	}
	
//	protected void renderNameTag(T p_114498_, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_) {
//	      double d0 = this.entityRenderDispatcher.distanceToSqr(p_114498_);
//	      if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(p_114498_, d0)) {
//	         boolean flag = !p_114498_.isDiscrete();
//	         float f = p_114498_.getBbHeight() + 0.5F;
//	         int i = "deadmau5".equals(p_114499_.getString()) ? -10 : 0;
//	         p_114500_.pushPose();
//	         p_114500_.translate(0.0D, (double)f, 0.0D);
//	         p_114500_.mulPose(this.entityRenderDispatcher.cameraOrientation());
//	         p_114500_.scale(-0.025F, -0.025F, 0.025F);
//	         Matrix4f matrix4f = p_114500_.last().pose();
//	         float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
//	         int j = (int)(f1 * 255.0F) << 24;
//	         Font font = this.getFont();
//	         float f2 = (float)(-font.width(p_114499_) / 2);
//	         font.drawInBatch(p_114499_, f2, (float)i, 553648127, false, matrix4f, p_114501_, flag, j, p_114502_);
//	         if (flag) {
//	            font.drawInBatch(p_114499_, f2, (float)i, -1, false, matrix4f, p_114501_, false, 0, p_114502_);
//	         }
//
//	         p_114500_.popPose();
//	      }
//	   }
	

}
