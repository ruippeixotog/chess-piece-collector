package net.ruippeixotog.piececollector;

import net.ruippeixotog.piececollector.BoardGridView.GridDimensions;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

class PreprocessingTask extends AsyncTask<byte[], Void, Void> {
	private ProgressDialog mDialog;
	private GridDimensions mGrid;
	private int mCellSize;
	private boolean mGrayscale;
	
	protected Bitmap[][] cellBitmaps;
	protected String currFileName;
	
	public PreprocessingTask(Context context, GridDimensions grid, int cellSize, boolean grayscale) {
		mDialog = new ProgressDialog(context);
		
		mGrid = grid;
		mCellSize = cellSize;
	}

	@Override
	protected void onPreExecute() {
		mDialog.setMessage("Preprocessing image...");
		mDialog.show();
	}

	@Override
	protected Void doInBackground(byte[]... data) {
		ImagePreprocessor preprocessor = new ImagePreprocessor(mCellSize, mGrayscale);
		cellBitmaps = preprocessor
				.preprocess(data[0], mGrid).save()
				.parseCells();
		currFileName = preprocessor.getFileName();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		mDialog.dismiss();
	}
}
