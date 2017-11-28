package com.yarin.android.Examples_08_09;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import it.unisa.di.weblab.polyrec.TPoint;

public class MultiTouchPaintView extends View {
	public static final int MAX_FINGERS = 5;
	private Path[] mFingerPaths = new Path[MAX_FINGERS];

	private ArrayList<TPoint>[] points = new ArrayList[MAX_FINGERS];
	private Paint mFingerPaint;

	private ArrayList<Path> mCompletedPaths;
	private RectF mPathBounds = new RectF();
	private Paint textPaint = new Paint();

	private static boolean first = false;

	private ClientSocketActivity bluetooth;
	private ObjectOutputStream oos;

	public MultiTouchPaintView(Context context) {
		super(context);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		textPaint.setTextSize(20);
	}

	public MultiTouchPaintView(Context context, ClientSocketActivity bluetooth, AttributeSet attrs) {
		super(context, attrs);
		this.bluetooth = bluetooth;
		
	}

	public MultiTouchPaintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setBackgroundColor(Color.LTGRAY);
		mCompletedPaths = new ArrayList<Path>();
		
		mFingerPaint = new Paint();
		mFingerPaint.setAntiAlias(true);
		mFingerPaint.setColor(Color.RED);
		mFingerPaint.setStyle(Paint.Style.STROKE);
		mFingerPaint.setStrokeWidth(10);
		// mFingerPaint.setStrokeCap(Paint.Cap.BUTT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (Path completedPath : mCompletedPaths) {
			canvas.drawPath(completedPath, mFingerPaint);
		}

		for (Path fingerPath : mFingerPaths) {
			if (fingerPath != null) {
				canvas.drawPath(fingerPath, mFingerPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int pointerCount = event.getPointerCount();

		int cappedPointerCount = pointerCount > MAX_FINGERS ? MAX_FINGERS : pointerCount;
		// pointer index
		int actionIndex = event.getActionIndex();
		int action = event.getActionMasked();
		int id = event.getPointerId(actionIndex);
		// if (id==1)
		


		if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && id < MAX_FINGERS) {
			// Toast.makeText(getContext(),"First point of "+ id,
			// Toast.LENGTH_SHORT).show();
			if (first)
				clearCanvas();
			mFingerPaths[id] = new Path();
			points[id] = new ArrayList<TPoint>();
			mFingerPaths[id].moveTo(event.getX(actionIndex), event.getY(actionIndex));
			points[id].add(new TPoint(event.getX(actionIndex), event.getY(actionIndex), event.getEventTime()));
		} else if ((action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP) && id < MAX_FINGERS) {

			mFingerPaths[id].setLastPoint(event.getX(actionIndex), event.getY(actionIndex));
			points[id].add(new TPoint(event.getX(actionIndex), event.getY(actionIndex), event.getEventTime()));
			mCompletedPaths.add(mFingerPaths[id]);
			mFingerPaths[id].computeBounds(mPathBounds, true);

			invalidate((int) mPathBounds.left, (int) mPathBounds.top, (int) mPathBounds.right,
					(int) mPathBounds.bottom);
			mFingerPaths[id] = null;
			
				
			if (pointerCount == 1) {
				first = true;
				longestPath();
			}
		}

		for (int i = 0; i < cappedPointerCount + mCompletedPaths.size(); i++) {
			int index = event.findPointerIndex(i);

			if (mFingerPaths[i] != null) {
				
				mFingerPaths[i].lineTo(event.getX(index), event.getY(index));
				points[i].add(new TPoint(event.getX(index), event.getY(index), event.getEventTime()));
				mFingerPaths[i].computeBounds(mPathBounds, true);

				invalidate((int) mPathBounds.left, (int) mPathBounds.top, (int) mPathBounds.right,
						(int) mPathBounds.bottom);

			}
		}

		return true;
	}

	void clearCanvas() {
		mFingerPaths = new Path[MAX_FINGERS];
		mCompletedPaths.clear();
		first = false;
		invalidate();

	}

	void longestPath() {
		first = true;
		//float maxLength = 0;
		double maxLength = 0;
		int longestPath = -1;
		//PathMeasure measure = new PathMeasure();
		String result = "";
		for (int i = 0; i < mCompletedPaths.size(); i++) {
			Double lunghezza = calculateLengths(points[i]);
			if (lunghezza>maxLength){
				maxLength = lunghezza;
			longestPath = i;
			}
			/*measure.setPath(mCompletedPaths.get(i), false);
			if (measure.getLength() > maxLength) {
				maxLength = measure.getLength();
				longestPath = i;
			}*/
			//result += "Path:" + i + " length:" + measure.getLength() + " - points " + points[i].size() + "\n";
			result += "Path:" + i + " length:" + lunghezza + " - points " + points[i].size() + "\n";

		}

		/*Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
		Toast.makeText(getContext(), "Longest path:" + longestPath, Toast.LENGTH_SHORT).show();
		Toast.makeText(getContext(), "Punti:" + points[longestPath].size(), Toast.LENGTH_SHORT).show();
		Toast.makeText(getContext(), "Numero di dita:" + mCompletedPaths.size(), Toast.LENGTH_SHORT).show();
*/
		try {

			// Toast.makeText(getContext(), Integer.toString(maxPointers),
			// Toast.LENGTH_SHORT).show();
		
			for (TPoint point : points[longestPath]) {
				oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
				
				oos.writeObject(point);
				oos.flush();
			}
			
			oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
			oos.writeObject(new TPoint(-3, mCompletedPaths.size(), -1));
			oos.flush();
			
			oos = new ObjectOutputStream(bluetooth.getSocket().getOutputStream());
			oos.writeObject(new TPoint(-1, -1, -1));
			oos.flush();
			
			

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	 private Double calculateLengths(ArrayList points) {
			Double length = 0.0d;
			ArrayList<Double> lengths = new ArrayList<Double>();
			lengths.add(length);
			TPoint temTPoint = null;
			ListIterator<TPoint> iterator = points.listIterator();

			while (iterator.hasNext()) {
				TPoint point = iterator.next();
				if (temTPoint != null) {
					length += temTPoint.distance(point);// TPoint.dist( temTPoint,
														// point );
					lengths.add(length);
				}
				temTPoint = point;
			}
			return lengths.get(points.size() - 1);
		}
}