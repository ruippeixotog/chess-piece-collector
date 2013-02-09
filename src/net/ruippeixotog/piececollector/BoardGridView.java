package net.ruippeixotog.piececollector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BoardGridView extends View {
	public static final int MARGIN = 10;

	public static class GridDimensions {
		private int viewWidth, viewHeight;
		private int startX, startY, size;

		private GridDimensions(BoardGridView view) {
			viewWidth = view.getWidth();
			viewHeight = view.getHeight();
			size = Math
					.min((viewWidth - MARGIN) / 8, (viewHeight - MARGIN) / 8) * 8;
			startX = MARGIN / 2 + ((viewWidth - MARGIN) - size) / 2;
			startY = MARGIN / 2 + ((viewHeight - MARGIN) - size) / 2;
		}

		public int getViewWidth() {
			return viewWidth;
		}

		public int getViewHeight() {
			return viewHeight;
		}

		public int getStartX() {
			return startX;
		}
		
		public double getRelativeStartX() {
			return startX / (double) viewWidth;
		}

		public int getStartY() {
			return startY;
		}
		
		public double getRelativeStartY() {
			return startY / (double) viewHeight;
		}

		public int getSize() {
			return size;
		}
		
		public double getRelativeWidth() {
			return size / (double) viewWidth;
		}
		
		public double getRelativeHeight() {
			return size / (double) viewHeight;
		}

		@Override
		public String toString() {
			return "GridDimensions [viewWidth=" + viewWidth + ", viewHeight="
					+ viewHeight + ", startX=" + startX + ", startY=" + startY
					+ ", size=" + size + "]";
		}
	}

	private Paint p = new Paint();

	public BoardGridView(Context context) {
		super(context);
		init();
	}

	public BoardGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BoardGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GridDimensions getDimensions() {
		return new GridDimensions(this);
	}
	
	public void init() {
		p.setColor(Color.RED);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int step = Math.min((getWidth() - MARGIN) / 8,
				(getHeight() - MARGIN) / 8);
		int gridSize = step * 8;
		int xStart = MARGIN / 2 + ((getWidth() - MARGIN) - gridSize) / 2;
		int yStart = MARGIN / 2 + ((getHeight() - MARGIN) - gridSize) / 2;
		int xEnd = xStart + gridSize;
		int yEnd = yStart + gridSize;

		for (int i = 0; i < 9; i++) {
			canvas.drawLine(xStart, yStart + i * step, xEnd, yStart + i * step,
					p);
			canvas.drawLine(xStart + i * step, yStart, xStart + i * step, yEnd,
					p);
		}
	}
}
