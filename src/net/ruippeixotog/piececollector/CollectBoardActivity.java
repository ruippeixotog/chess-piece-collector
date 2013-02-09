package net.ruippeixotog.piececollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
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

	public void takeBoardPicture(View v) {
		// get an image from the camera
		mCamera.takePicture(null, null, mPicture);
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			ImagePreprocessor preprocessor = new ImagePreprocessor();
			mCellBitmaps = preprocessor.preprocess(data, mGrid.getDimensions())
					.save().parseCells();
			mCurrFileName = preprocessor.getFileName();

			identifyCell(0);
		}
	};

	private void identifyCell(int cellIndex) {
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

		String pieceId = data.getStringExtra("pieceId");
		String cellFileName = mCurrFileName.replace(".jpg", "_" + requestCode
				+ ".jpg");

		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/PieceCollector/data");
		dir.mkdirs();

		// save bitmap file
		try {
			FileOutputStream out = new FileOutputStream(new File(dir,
					cellFileName));
			mCellBitmaps[requestCode / 8][requestCode % 8].compress(
					Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

		if (requestCode < 64) {
			identifyCell(requestCode + 1);
		} else {
			mCellBitmaps = null;
		}
	}

	@Override
	protected void onDestroy() {
		if (mCamera != null) {
			mCamera.release();
		}
		super.onDestroy();
	}
}
