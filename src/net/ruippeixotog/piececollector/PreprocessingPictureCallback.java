package net.ruippeixotog.piececollector;

import net.ruippeixotog.piececollector.BoardGridView.GridDimensions;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

public class PreprocessingPictureCallback implements PictureCallback {
	private Context mContext;
	private GridDimensions mGrid;
	private int mCellSize;
	private boolean mGrayscale;
	
	public PreprocessingPictureCallback(Context context, GridDimensions grid, int cellSize, boolean grayscale) {
		mContext = context;
		mGrid = grid;
		mCellSize = cellSize;
		mGrayscale = grayscale;
	}
	
	@Override
	public final void onPictureTaken(byte[] data, Camera camera) {
		new PreprocessingTask(mContext, mGrid, mCellSize, mGrayscale) {
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				afterPreprocess(cellBitmaps, currFileName);
			}
		}.execute(data);
	}
	
	public void afterPreprocess(Bitmap[][] cellBitmaps, String fileName) {
	}
}
