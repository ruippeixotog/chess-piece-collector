package net.ruippeixotog.piececollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class CollectBoardActivity extends Activity {
	public static final String TAG = "CollectBoardActivity";

	private Camera mCamera;
	private CameraPreview mPreview;
	private BoardGridView mGrid;

	private Bitmap[][] mCellBitmaps;
	private String mCurrFileName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect_board);

		try {
			mCamera = Camera.open(); // attempt to get a Camera instance
			mCamera.setDisplayOrientation(90);
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			throw new RuntimeException(
					"Camera is not available (in use or does not exist)", e);
		}

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		mGrid = new BoardGridView(this);
		preview.addView(mGrid);
	}

	public void takeWhiteStartingPositionPicture(View v) {
		mCamera.takePicture(null, null, new StartingPicture(false));
	}

	public void takeBlackStartingPositionPicture(View v) {
		mCamera.takePicture(null, null, new StartingPicture(true));
	}

	public void takeBoardPicture(View v) {
		// get an image from the camera
		mCamera.takePicture(null, null, mPicture);
	}

	private class StartingPicture implements PictureCallback {
		private String mySide = "0", otherSide = "1";
		private String[] firstRow = { "R", "N", "B", "Q", "K", "B", "N", "R" };

		public StartingPicture(boolean blackSide) {
			if (blackSide) {
				mySide = "1";
				otherSide = "0";
				firstRow[3] = "K";
				firstRow[4] = "Q";
			}
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			new PreprocessingTask() {
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);

					new AsyncTask<Void, Void, Void>() {

						ProgressDialog mDialog = new ProgressDialog(
								CollectBoardActivity.this);

						@Override
						protected void onPreExecute() {
							mDialog.setMessage("Saving board data...");
							mDialog.show();
						}

						@Override
						protected Void doInBackground(Void... params) {
							for (int j = 0; j < 8; j++) {
								saveCell(0, j, otherSide + firstRow[j]);
								saveCell(1, j, otherSide + "P");
								saveCell(6, j, mySide + "P");
								saveCell(7, j, mySide + firstRow[j]);
							}
							for (int i = 2; i <= 5; i++) {
								for (int j = 0; j < 8; j++) {
									saveCell(i, j, "-");
								}
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							mDialog.dismiss();
						}

					}.execute((Void[]) null);
				}
			}.execute(data);
		}
	};

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			new PreprocessingTask() {
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					startIdentifyCellActivity(0);
				}
			}.execute(data);
		}
	};

	private void startIdentifyCellActivity(int cellIndex) {
		Intent intent = new Intent(CollectBoardActivity.this,
				IdentifyPieceActivity.class);
		intent.putExtra("bitmap", mCellBitmaps[cellIndex / 8][cellIndex % 8]);
		startActivityForResult(intent, cellIndex);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			mCellBitmaps = null;
			return;
		}
		saveCell(requestCode / 8, requestCode % 8,
				data.getStringExtra("pieceId"));

		if (requestCode < 64) {
			startIdentifyCellActivity(requestCode + 1);
		} else {
			mCellBitmaps = null;
		}
	}

	private void saveCell(int row, int col, String pieceId) {
		String cellFileName = mCurrFileName.replace(".jpg", "_"
				+ (row * 8 + col) + ".jpg");

		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/PieceCollector/data");
		dir.mkdirs();

		// save bitmap file
		try {
			FileOutputStream out = new FileOutputStream(new File(dir,
					cellFileName));
			mCellBitmaps[row][col]
					.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			mCellBitmaps[row][col].recycle();
			mCellBitmaps[row][col] = null;
		}

		// save identification data
		try {
			PrintStream out = new PrintStream(new FileOutputStream(new File(
					dir, "pieces.txt"), true));
			out.println(cellFileName + " " + pieceId);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "Saved piece at " + ((char) ('a' + col)) + (8 - row)
				+ " as: " + pieceId);
	}

	@Override
	protected void onDestroy() {
		if (mCamera != null) {
			mCamera.release();
		}
		super.onDestroy();
	}

	class PreprocessingTask extends AsyncTask<byte[], Void, Void> {
		ProgressDialog mDialog = new ProgressDialog(CollectBoardActivity.this);

		@Override
		protected void onPreExecute() {
			mDialog.setMessage("Preprocessing image...");
			mDialog.show();
		}

		@Override
		protected Void doInBackground(byte[]... data) {
			ImagePreprocessor preprocessor = new ImagePreprocessor();
			mCellBitmaps = preprocessor
					.preprocess(data[0], mGrid.getDimensions()).save()
					.parseCells();
			mCurrFileName = preprocessor.getFileName();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mDialog.dismiss();
		}
	}
}
