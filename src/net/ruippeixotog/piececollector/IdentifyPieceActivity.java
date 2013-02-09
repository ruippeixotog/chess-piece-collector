package net.ruippeixotog.piececollector;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;

public class IdentifyPieceActivity extends Activity {

	private List<RadioButton> mPieceTypesRadios = new ArrayList<RadioButton>();
	private View mTypeSelected = null;

	private static final SparseArray<String> mPieceTypes = new SparseArray<String>();
	{
		mPieceTypes.put(R.id.none, "-");
		mPieceTypes.put(R.id.king, "K");
		mPieceTypes.put(R.id.queen, "Q");
		mPieceTypes.put(R.id.rook, "R");
		mPieceTypes.put(R.id.bishop, "B");
		mPieceTypes.put(R.id.knight, "N");
		mPieceTypes.put(R.id.pawn, "P");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_identify_piece);

		Bitmap b = getIntent().getExtras().getParcelable("bitmap");
		((ImageView) findViewById(R.id.piece_img)).setImageBitmap(b);

		// set "manual" radio group for piece types
		for (int i = 0; i < mPieceTypes.size(); i++) {
			RadioButton radio = (RadioButton) findViewById(mPieceTypes.keyAt(i));
			mPieceTypesRadios.add(radio);
			radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						mTypeSelected = buttonView;
						for (RadioButton button : mPieceTypesRadios) {
							if (button != buttonView)
								button.setChecked(false);
						}
					}
				}
			});
		}
		mTypeSelected = findViewById(mPieceTypes.keyAt(0));
	}

	public void confirm(View v) {
		boolean isBlack = ((RadioButton) findViewById(R.id.black)).isChecked();
		// RadioGroup pieceTypeRadioGroup = (RadioGroup)
		// findViewById(R.id.piece_type);
		// System.out.println(pieceTypeRadioGroup.getCheckedRadioButtonId());
		String pieceType = mPieceTypes.get(mTypeSelected.getId());

		String pieceId = pieceType;
		if (pieceType != "-") {
			pieceId = (isBlack ? "1" : "0") + pieceId;
		}
		Intent intent = new Intent();
		intent.putExtra("pieceId", pieceId);
		setResult(RESULT_OK, intent);
		finish();
	}
}
