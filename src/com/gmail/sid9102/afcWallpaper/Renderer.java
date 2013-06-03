package com.gmail.sid9102.afcWallpaper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.gmail.sid9102.afcWallpaper.R;


import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.RotateAnimation3D;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.GouraudMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager.TextureType;
import rajawali.math.Number3D;
import rajawali.parser.ObjParser;
import rajawali.parser.AParser.ParsingException;
import rajawali.primitives.Cube;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.AccelerateDecelerateInterpolator;

public class Renderer extends RajawaliRenderer {
	private BaseObject3D afc;
	private Plane bg;
	private long timeDown;
	private VelocityTracker velocity;
	private float speed;
	private boolean bounce;
	private int bounceCount;
	private float result;
	private long hoverTime;

	private float hoverDirection;

	public Renderer(Context context) {
		super(context);
	}

	public void initScene() {
		ALight light = new DirectionalLight();
		light.setPower(0.6f);
		light.setPosition(0, 4, -2);
		light.setLookAt(0, -0.7f, 4);
		mCamera.setPosition(0, 0, -7);
		mCamera.setLookAt(0, 0, 0);

		bg = new Plane(10, 20, 1, 1);
		bg.setPosition(0, 0, 15);
		bg.setRotY(bg.getRotY() + 180);
		SimpleMaterial bgMat = new SimpleMaterial();
		Bitmap bgTex = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.afc_bg);
		bg.setMaterial(bgMat);
		bg.addTexture(mTextureManager.addTexture(bgTex));
		bg.addLight(light);
		addChild(bg);

		ObjParser afcParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.afc_obj);
		try {
			afcParser.parse();
			afc = afcParser.getParsedObject();
			GouraudMaterial afcMat = new GouraudMaterial();
			afcMat.setSpecularIntensity(0.2f, 0.2f, 0.2f, 0.2f);
			afc.setMaterial(afcMat);
			Bitmap afcTex = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.afc_texture);
			afc.addTexture(mTextureManager.addTexture(afcTex));
			afc.addLight(light);
			addChild(afc);
		} catch (ParsingException e) {
			e.printStackTrace();
		}

		afc.setPosition(0, -0.7f, 4);

	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
		bounce = false;
		bounceCount = 0;
		timeDown = System.currentTimeMillis();
		hoverTime = System.currentTimeMillis();
		hoverDirection = 0.002f;
	}

	@Override
	public void onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		if(velocity == null)
		{
			velocity = VelocityTracker.obtain();
		}

		float xVelocity;
		if(action == MotionEvent.ACTION_MOVE)
		{
			timeDown = System.currentTimeMillis();
			velocity.addMovement(event);
			velocity.computeCurrentVelocity(2);
			xVelocity = velocity.getXVelocity();
		}
		else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP)
		{
			velocity.computeCurrentVelocity(2);
			xVelocity = velocity.getXVelocity();
			velocity.recycle();
			velocity = null;
		}
		else
		{
			velocity.computeCurrentVelocity(2);
			xVelocity = velocity.getXVelocity();
		}

		xVelocity /= 1.2f;
		speed = -xVelocity;
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);

		if(afc.getRotY() != 0 && speed == 0)
		{
			speed = 0.02f;
		}
		
		long hoverDiff = System.currentTimeMillis() - hoverTime;

		if(afc.getY() > -0.5f || afc.getY() < -0.7f)
		{
			hoverDirection = 0;
		}

		if(hoverDiff > 400)
		{
			if(afc.getY() <= -0.7f)
			{
				if(hoverDirection == 0)
				{
					hoverDirection = 0.002f;
				}
				else
					hoverDirection = 0;
			}
			else if(afc.getY() >= -0.5f)
			{
				if(hoverDirection == 0)
				{
					hoverDirection = -0.002f;
				}
				else
					hoverDirection = 0;
			}
			else if(hoverDirection == 0)
			{
				hoverDirection = 0.002f;
			}
			hoverTime = System.currentTimeMillis();
		}

		if(speed != 0)
		{
			hoverDirection = 0;
		}

		afc.setY(afc.getY() + hoverDirection);

		long diff = System.currentTimeMillis() - timeDown;
		if( diff > 500 && diff < 1000)
		{
			if(speed > 0 && !(speed <= 4))
			{
				speed -= 0.1f;
			}
			else if(speed < 0 && !(speed >= -4))
			{
				speed += 0.1f;
			}
		}
		else if(diff > 1000 && diff < 2000)
		{
			if(speed > 0 && !(speed <= 2.5f))
			{
				speed -= 0.13f;
			}
			else if(speed < 0 && !(speed >= -2.5f))
			{
				speed += 0.13f;
			}
		}
		else if(diff > 2000 && speed != 0)
		{			
			float firstBounce;
			float secondBounce;
			float interval = 2 * Math.abs(speed);

			if(speed < 0)
			{
				firstBounce = 360 + 25 * speed;
				secondBounce = 360 + 15 * speed;
			}
			else
			{
				firstBounce = 25 * speed;
				secondBounce = 15 * speed;
			}

			boolean bounce1 = result > firstBounce && result < (firstBounce + interval);
			boolean bounce2 = result > secondBounce && result < (secondBounce + interval);

			if(bounceCount == 0 && bounce1)
			{
				bounce = true; 
				speed = -speed;
			}

			if(bounceCount == 1 && bounce1)
			{
				bounce = true;
				Log.v("bounce", "1");
				speed = -0.5f * speed;
			}

			if(bounceCount == 2 && bounce2)
			{
				bounce = true;
				Log.v("bounce", "2");
				speed = -speed;
			}

			if(bounceCount == 3 && bounce2)
			{
				bounce = true;
				Log.v("bounce", "3");
				speed = -speed;
			}

			if(bounceCount == 4 && result < interval)
			{
				bounce = true;
				Log.v("bounce", "4");
				speed = -result;
			}
		}

		result = afc.getRotY() + speed;

		if(result == 0)
		{
			speed = 0;
		}

		//		if(speed != 0)
		//		{
		//			Log.v("rot", Float.toString(afc.getRotY()));
		//		}

		if(result < 0)
		{
			result += 360;
		}

		if(360 < result)
		{
			afc.setRotY(0);
		}
		else
			afc.setRotY(result);

		if(bounce)
		{
			bounce = false;
			bounceCount++;
		}

		if(bounceCount == 5)
		{
			bounceCount = 0;
		}		
	}
}
