package co.sidhant.arsenalwallpaper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.sidhant.arsenalwallpaper.R;


import rajawali.BaseObject3D;
import rajawali.SerializedObject3D;
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
import android.content.res.Resources.NotFoundException;
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

		ObjectInputStream ois;
		SerializedObject3D afcSer = null;
		try {
			ois = new ObjectInputStream(mContext.getResources().openRawResource(R.raw.afc_obj));
			afcSer = (SerializedObject3D)ois.readObject();
		} catch (StreamCorruptedException e1) {
			e1.printStackTrace();
		} catch (NotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		afc = new BaseObject3D(afcSer);
		GouraudMaterial afcMat = new GouraudMaterial();
		afcMat.setSpecularIntensity(0.4f, 0.4f, 0.4f, 0.4f);
		afc.setMaterial(afcMat);
		Bitmap afcTex = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.afc_texture);
		afc.addTexture(mTextureManager.addTexture(afcTex));
		afc.addLight(light);
		addChild(afc);

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

		// If it's not currently rotating, but it isn't facing forward, reset it
		if(afc.getRotY() != 0 && speed == 0)
		{
			if(afc.getRotY() > 180)
			{
				speed = 0.5f;
			}
			else
				speed = -0.5f;
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
			bounceCount = 0;
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
			//After 2 seconds of spinning, bounce a few times then reset to face forward
			float firstBounce;
			float secondBounce;
			float interval = 2 * Math.abs(speed);
			boolean bounce1 = false;
			
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

			//If it's spinning too fast, don't bounce.
			if(speed > 3.6f)
			{
				speed -= 0.5f;
			}
			else if(speed < -3.6f)
			{
				speed += 0.5f;
			}
			else
				bounce1 = result > firstBounce && result < (firstBounce + interval);
			
			
			boolean bounce2 = result > secondBounce && result < (secondBounce + interval);

			if(bounceCount == 0 && bounce1)
			{
				bounce = true; 
				speed = -speed;
			}

			if(bounceCount == 1 && bounce1)
			{
				bounce = true;
//				Log.v("bounce", "1");
				speed = -0.5f * speed;
			}

			if(bounceCount == 2 && bounce2)
			{
				bounce = true;
//				Log.v("bounce", "2");
				speed = -speed;
			}

			if(bounceCount == 3 && bounce2)
			{
				bounce = true;
//				Log.v("bounce", "3");
				speed = -speed;
			}

			if(bounceCount == 4 && result < interval)
			{
				bounce = true;
//				Log.v("bounce", "4");
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

		if(speed == 0)
		{
			bounceCount = 0;
		}		
	}
}
