package net.ruippeixotog.piececollector;

import java.io.IOException;

import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageRecognizer {
	private BasicNetwork mNetwork;

	public ImageRecognizer(Context context) throws IOException {
		mNetwork = (BasicNetwork) EncogDirectoryPersistence.loadObject(context
				.getAssets().open("neural-trained.eg"));
	}

	public String identifyBitmap(Bitmap cell) {
		return "1K"; // TODO
	}
}
