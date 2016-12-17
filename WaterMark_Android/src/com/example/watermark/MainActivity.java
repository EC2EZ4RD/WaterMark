package com.example.watermark;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ImageView ivHost;
	private ImageView ivWM;
	private ImageView ivMarked;
	private ImageView ivExt;
	private Button btnSelHost;
	private Button btnSelWM;
	private Button btnEmbedWM;
	private Button btnExt;
	private Bitmap rawBmHost;
	private Bitmap rawBmWM;
	private Bitmap markedBmImg;
	private Bitmap extBmWM;
	private Bitmap showBmHost;
	private Bitmap showBmWM;
	private Bitmap showMarkedBmImg;
	WaterMarkProc wmp = new WaterMarkProc();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SetControl();
		btnSelHost.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intent.setType("image/*");
				startActivityForResult(intent, 0x1);
				Toast.makeText(getApplicationContext(), "load host", Toast.LENGTH_SHORT).show();
			}
		});

		btnSelWM.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intent.setType("image/*");
				startActivityForResult(intent, 0x2);
			}
		});

		btnEmbedWM.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (rawBmHost != null && rawBmWM != null) {
					try {
						Log.d("qqq", "fail embed watermark");
						double[][][] doubleHost1 = (double[][][]) wmp.Bm2DoubleYIQ(rawBmHost);
						Log.d("qqqq", "fail embed watermark");
						double[][][] doubleWM = (double[][][]) wmp.Bm2DoubleYIQ(rawBmWM);
						double[][] doubleHostY = doubleHost1[0];
						Log.d("qqqqq", "fail embed watermark");
						doubleHostY = wmp.SVDembed(doubleWM[0], doubleHostY);
						Log.d("qqqqqq", "fail embed watermark");
						showMarkedBmImg = (Bitmap) wmp.DoubleYIQ2Bm(doubleHostY, doubleHost1[1], doubleHost1[2],
								doubleHost1[3]);
						ivMarked.setImageBitmap(showMarkedBmImg);
					} catch (Exception e) {
						Log.d("watermark", "fail embed watermark");
					}
				}
			}
		});

		btnExt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					double[][][] doubleMarked = (double[][][]) wmp.Bm2DoubleYIQ(showMarkedBmImg);
					Log.d("tttt", "fail embed watermark");
					double[][] doubleHostY = (double[][]) doubleMarked[0];
					Log.d("ttttt", "fail embed watermark");
					double[][] doubleWM = wmp.SVDext(doubleHostY);
					Log.d("ttttttt", "fail embed watermark");
					extBmWM = (Bitmap) wmp.DoubleYIQ2Bm(doubleWM, doubleWM, doubleWM, doubleWM);
					ivExt.setImageBitmap(showBmWM);
				} catch (Exception e) {
					Log.d("tttwatermark", "fail extract watermark");
				}

			}
		});

	}

	private void SetControl() {
		ivHost = (ImageView) findViewById(R.id.show_host);
		ivWM = (ImageView) findViewById(R.id.show_watermark);
		ivMarked = (ImageView) findViewById(R.id.show_marked);
		ivExt = (ImageView) findViewById(R.id.show_ext);
		btnSelHost = (Button) findViewById(R.id.load_host);
		btnSelWM = (Button) findViewById(R.id.load_watermark);
		btnEmbedWM = (Button) findViewById(R.id.embed);
		btnExt = (Button) findViewById(R.id.extract);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// Toast.makeText(getApplicationContext(), "1",
		// Toast.LENGTH_SHORT).show();

		if (requestCode == 0x1 && resultCode == RESULT_OK && null != data) {
			// Toast.makeText(getApplicationContext(), "11",
			// Toast.LENGTH_SHORT).show();
			try {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				rawBmHost = BitmapFactory.decodeFile(picturePath);
				showBmHost = wmp.BmDownSample(picturePath);
				ivHost.setImageBitmap(rawBmHost);
			} catch (Exception e) {
				Log.d("dddwatermark", "fail load host");
			}
		} else if (requestCode == 0x2 && resultCode == RESULT_OK && null != data) {
			try {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				rawBmWM = BitmapFactory.decodeFile(picturePath);
				showBmWM = wmp.BmDownSample(picturePath);
				ivWM.setImageBitmap(showBmWM);
			} catch (Exception e) {
				Log.d("dddwatermark", "fail load watermark");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
