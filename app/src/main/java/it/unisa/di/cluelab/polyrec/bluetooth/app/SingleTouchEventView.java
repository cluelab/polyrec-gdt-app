package it.unisa.di.cluelab.polyrec.bluetooth.app;

import java.io.IOException;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import it.unisa.di.cluelab.polyrec.TPoint;

public class SingleTouchEventView extends View {
	private Paint paint = new Paint();
	private Path path = new Path();

	private ClientSocketActivity bluetooth;
	private ObjectOutputStream oos;

	private float prevTouchX;
	private float prevTouchY;
	
	private int maxPointers = 0;

	public SingleTouchEventView(Context context, ClientSocketActivity bluetooth, AttributeSet attrs) {
		super(context, attrs);
		this.bluetooth = bluetooth;
		setBackgroundColor(Color.LTGRAY);
		
	

		paint.setAntiAlias(true);
		paint.setStrokeWidth(10);
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		
		
	}

	void clearCanvas() {
		path = new Path();
		invalidate();

	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawPath(path, paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float eventX = event.getX();
		float eventY = event.getY();
		long eventTime = event.getEventTime();
		TPoint tpoint = new TPoint(eventX, eventY, eventTime);

		// Log.d("EVENT",""+event.getAction());

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clearCanvas();

			path.moveTo(eventX, eventY);

			try {
				oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
				oos.writeObject(tpoint);
				oos.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			break;
		case MotionEvent.ACTION_MOVE:

			if (eventX != prevTouchX || eventY != prevTouchY) {
				maxPointers = Math.max(maxPointers, event.getPointerCount());
				int pointerid = event.getPointerId(0);
			
				path.lineTo(eventX, eventY);
				try {
					oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
					oos.writeObject(tpoint);
					oos.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			try {
				
				//Toast.makeText(getContext(), Integer.toString(maxPointers), Toast.LENGTH_SHORT).show();
				maxPointers =0;
				
				oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
				oos.writeObject(new TPoint(-1, -1, -1));
				oos.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			break;
		default:
			return false;
		}
		prevTouchX = eventX;
		prevTouchY = eventY;
		// Schedules a repaint.
		invalidate();
		return true;
	}
	
	

	
}