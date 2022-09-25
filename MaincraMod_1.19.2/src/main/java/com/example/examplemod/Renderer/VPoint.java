package com.example.examplemod.Renderer;

import net.minecraft.world.phys.Vec3;

public class VPoint {

	private float x, y, z;
	private float data[];
	
	public VPoint() {
		this(0f, 0f, 0f);
	}
	
	public VPoint(double x, double y, double z) {
		this((float) x, (float) y, (float) z);
	}
	
	public VPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.data = new float[]{x, y, z};
	}
	
	public float[] getData() {		
		return data;
	}
	
	public void update(int x, int y, int z) {
		update((float) x, (float) y, (float) z);
	}
	
	public void update(double x, double y, double z) {
		update((float) x, (float) y, (float) z);
	}
	
	public void update(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		data[0] = x;
		data[1] = y;
		data[2] = z;
	}
	
	public void update(float[] vArr) {
		update(vArr[0], vArr[1], vArr[2]); 
	}
	
   public boolean equals(Object p_82552_) {
	      if (this == p_82552_) {
	         return true;
	      } else if (!(p_82552_ instanceof Vec3)) {
	         return false;
	      } else {
	         VPoint vec3 = (VPoint)p_82552_;
	         if (Float.compare(vec3.x, this.x) != 0) {
	            return false;
	         } else if (Float.compare(vec3.y, this.y) != 0) {
	            return false;
	         } else {
	            return Float.compare(vec3.z, this.z) == 0;
	         }
	      }
	   }

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

}
