package net.ruippeixotog.piececollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.ruippeixotog.piececollector.BoardGridView.GridDimensions;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

public class ImagePreprocessor {
	public static final String TAG = "ImagePreprocessor";
	
	private int mCellSize;
	private boolean mGrayscale;

	private Bitmap origBitmap;
	private Bitmap processedBitmap;
	private String mFileName;

	public ImagePreprocessor(int cellSize, boolean grayscale) {
		mCellSize = cellSize;
		mGrayscale = grayscale;
	}

	public ImagePreprocessor preprocess(byte[] data, GridDimensions grid) {
		origBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

		int croppedWidth = (int) (origBitmap.getWidth() * grid
				.getRelativeHeight());
		int croppedHeight = (int) (origBitmap.getHeight() * grid
				.getRelativeWidth());

		processedBitmap = Bitmap.createBitmap(origBitmap,
				(int) (origBitmap.getWidth() * grid.getRelativeStartY()),
				(int) (origBitmap.getHeight() * grid.getRelativeStartX()),
				croppedWidth, croppedHeight,
				scaleRotateMatrix(croppedWidth, croppedHeight, grid), true);
		if(mGrayscale) {
			processedBitmap = toGrayscale(processedBitmap);
		}
		origBitmap.recycle();

		return this;
	}

	private Matrix scaleRotateMatrix(int width, int height, GridDimensions grid) {
		Matrix matrix = new Matrix();
		float scaleWidth = mCellSize * 8 / (float) width;
		float scaleHeight = mCellSize * 8 / (float) height;
		matrix.postScale(scaleWidth, scaleHeight);
		matrix.postRotate(90);
		return matrix;
	}
	
	private Bitmap toGrayscale(Bitmap bitmap) {        
	    int width, height;
	    height = bitmap.getHeight();
	    width = bitmap.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bitmap, 0, 0, paint);
	    
	    bitmap.recycle(); // this destroys the original!
	    return bmpGrayscale;
	}

	public ImagePreprocessor save() {
		File rawPictureDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/PieceCollector/raw");
		rawPictureDir.mkdirs();

		// File origFile = new File(rawPictureDir, System.currentTimeMillis()
		// + "_raw.jpg");
		// try {
		// FileOutputStream out = new FileOutputStream(origFile);
		// out.write(origData);
		// out.close();
		// } catch (FileNotFoundException e) {
		// Log.d(TAG, "File not found: " + e.getMessage());
		// } catch (IOException e) {
		// Log.d(TAG, "Error accessing file: " + e.getMessage());
		// }

		mFileName = System.currentTimeMillis() + ".jpg";
		File croppedFile = new File(rawPictureDir, mFileName);

		try {
			FileOutputStream out = new FileOutputStream(croppedFile);
			processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}

		return this;
	}
	
	public void recycle() {
		
	}

	public Bitmap[][] parseCells() {
		Bitmap[][] board = new Bitmap[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board[i][j] = Bitmap.createBitmap(processedBitmap, j
						* mCellSize, i * mCellSize, mCellSize, mCellSize);
			}
		}
		return board;
	}

	public String getFileName() {
		return mFileName;
	}
}
