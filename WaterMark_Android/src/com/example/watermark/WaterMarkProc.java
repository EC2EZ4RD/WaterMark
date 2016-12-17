package com.example.watermark;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

public class WaterMarkProc {
	private int Xmark = 0;
	private int delta = 105;
	public WaterMarkProc() {
		Xmark = 0;
		delta = 105;
	}
	
	public int getXmark(){
		return this.Xmark;
	}
	public void setdelta(int d){
		this.delta = d;
	}

	// LL为YIQ中Y分量的DWT的低频区域
	// 此函数对低频区域嵌入水印
	public double[][] SVDembed(double[][] WM, double[][] LL) {
		int xLL = LL.length;
		int yLL = LL[0].length;
		int xWM = WM.length;
		int yWM = WM.length;
		this.Xmark = xWM;
		int blocksize = (int) xLL / xWM;
		Matrix L = new Matrix(LL);
		for (int i = 0; i < xLL; i += blocksize) {
			Log.d("qqq123", "fail embed watermark");
			for (int j = 0; j < yLL; j += blocksize) {
				SingularValueDecomposition K = L.getMatrix(i, i + blocksize - 1, j, j + blocksize - 1).svd();
				Matrix U = K.getU();
				Matrix S = K.getS();
				Matrix V = K.getV();
				double max = S.get(0, 0);
				int maxx = 0;
				int maxy = 0;
				for (int ii = 0; ii < blocksize; ii++) {
					for (int jj = 0; jj < blocksize; jj++) {
						double tmp = S.get(ii, jj);
						if (max < tmp) {
							max = tmp;
							maxx = ii;
							maxy = jj;
						}
					}
				}

				double w = WM[(int) i / blocksize][(int) j / blocksize];
				int ga = (int) max / delta;
				double dd = (double) max / delta - ga;
				if ((ga + w) % 2 == 1 && dd < 0.5)
					S.set(maxx, maxy, (ga - 0.5) * delta);
				else if ((ga + w) % 2 == 1 && dd >= 0.5)
					S.set(maxx, maxy, (ga + 1.5) * delta);
				else
					S.set(maxx, maxy, (ga + 0.5) * delta);

				L.setMatrix(i, i + blocksize - 1, j, j + blocksize - 1, U.times(S).times(V.transpose()));
			}
		}
		LL = L.getArrayCopy();
		return LL;
	}
	
	public double[][] SVDext(double[][] Y){
		int len = Y.length;
		int blocksize = (int)len/this.Xmark;
		Matrix L = new Matrix(Y);
		double[][] WM = new double[this.Xmark][this.Xmark];
		for (int i = 0; i < len ; i += blocksize) {
			for (int j = 0; j < len; j += blocksize) {
				SingularValueDecomposition K = L.getMatrix(i, i + blocksize - 1, j, j + blocksize - 1).svd();
				Matrix S = K.getS();
				double max = S.get(0, 0);
				int maxx = 0;
				int maxy = 0;
				for (int ii = 0; ii < blocksize; ii++) {
					for (int jj = 0; jj < blocksize; jj++) {
						double tmp = S.get(ii, jj);
						if (max < tmp) {
							max = tmp;
							maxx = ii;
							maxy = jj;
						}
					}
				}
				int ga = (int) max / delta;
				if (ga%2 == 0)
					WM[i/blocksize][i/blocksize] = 0;
				else
					WM[i/blocksize][i/blocksize] = 255;
			}
		}
		return WM;
	}

	public double[] haar_dwt(double[] f) {
		int len = f.length;
		double[] M = new double[len];
		len = (int) len / 2;
		double[] L = new double[len];
		double[] H = new double[len];
		for (int i = 0; i < len; i++) {
			L[i] = (f[2 * i] + f[2 * i + 1]) / Math.sqrt(2);
			H[i] = (f[2 * i] - f[2 * i + 1]) / Math.sqrt(2);
		}
		// pr_vals1(L);
		M = merge(L, 0, M, 0, len);
		M = merge(H, 0, M, len, len * 2);
		return M;
	};

	public double[][] haar_dwt2D(double[][] f) {

		int x0 = f.length;
		int y0 = f[0].length;
		double[][] tmpf = new double[x0][y0];
		double[][] transf = new double[y0][x0];
		for (int i = 0; i < x0; i++) {
			tmpf[i] = (double[]) haar_dwt(f[i]);
		}
		for (int i = 0; i < x0; i++) {
			for (int j = 0; j < y0; j++) {
				transf[j][i] = tmpf[i][j];
			}
		}
		for (int i = 0; i < y0; i++) {
			transf[i] = (double[]) haar_dwt(transf[i]);
		}
		for (int i = 0; i < x0; i++) {
			for (int j = 0; j < y0; j++) {
				tmpf[i][j] = transf[j][i];
			}
		}
		return tmpf;

	}

	public double[] haar_idwt(double[] f) {
		int len = f.length;
		len = (int) len / 2;
		double[] L = new double[len];
		double[] H = new double[len];
		for (int i = 0; i < len; i++) {
			L[i] = f[i];
			H[i] = f[len + i];
		}
		for (int i = 0; i < len; i++) {
			f[2 * i] = (L[i] + H[i]) / Math.sqrt(2);
			f[2 * i + 1] = (L[i] - H[i]) / Math.sqrt(2);
		}
		// pr_vals1(L);
		return f;
	};

	public double[][] haar_idwt2D(double[][] f) {

		int x0 = f.length;
		int y0 = f[0].length;
		double[][] tmpf = f;
		double[][] transf = new double[y0][x0];
		for (int i = 0; i < x0; i++) {
			for (int j = 0; j < y0; j++) {
				transf[i][j] = tmpf[j][i];
			}
		}
		for (int i = 0; i < y0; i++) {
			transf[i] = (double[]) haar_idwt(transf[i]);
		}
		for (int i = 0; i < x0; i++) {
			for (int j = 0; j < y0; j++) {
				tmpf[j][i] = transf[i][j];
			}
		}
		for (int i = 0; i < x0; i++) {
			f[i] = (double[]) haar_idwt(tmpf[i]);
		}
		return f;

	}

	private double[] merge(double[] a, int Astart, double[] host, int Hstart, int Hend) {
		// pr_vals1(a);
		for (int i = Hstart; i < Hend; i++) {
			host[i] = a[i - Hstart];
		}
		return host;
	}

	public Bitmap BmDownSample(String picturePath) {
		// 获取Bitmap图像大小与类型属性
		Bitmap srcImage;
		int reqHeight = 256;
		int reqWidth = 256;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);
		int height = options.outHeight;
		int width = options.outWidth;
		// 下采样
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value
			// that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;
		srcImage = BitmapFactory.decodeFile(picturePath, options);
		return srcImage;
	}

	public double[][][] Bm2DoubleYIQ(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		double[][][] YIQ = new double[4][width][height];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int pixel = bitmap.getPixel(col, row);// ARGB
				int red = Color.red(pixel); // same as (pixel >> 16) &0xff
				int green = Color.green(pixel); // same as (pixel >> 8) &0xff
				int blue = Color.blue(pixel); // same as (pixel & 0xff)
				int alpha = Color.alpha(pixel); // same as (pixel >>> 24)
				YIQ[0][row][col] = (double) 0.299 * red + 0.587 * green + 0.114 * blue;
				YIQ[1][row][col] = (double) 0.596 * red - 0.275 * green - 0.321 * blue;
				YIQ[2][row][col] = (double) 0.212 * red - 0.523 * green + 0.311 * blue;
				YIQ[3][row][col] = (double) alpha;
			}
		}
		return YIQ;
	}

	public Bitmap DoubleYIQ2Bm(double[][] Y,double[][] I,double[][] Q,double[][] Alpha) {

		int width = Y.length;
		int height = Y[0].length;
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				double y = Y[row][col];
				double i = I[row][col];
				double q = Q[row][col];
				int alpha = (int) Alpha[row][col];
				int R = (int) (y + 0.956 * i + 0.621 * q);
				int G = (int) (y - 0.272 * i - 0.647 * q);
				int B = (int) (y - 1.105 * i + 1.702 * q);
				if (R < 0)
					R = 0;
				if (G < 0)
					G = 0;
				if (B < 0)
					B = 0;
				if (R > 255)
					R = 255;
				if (G > 255)
					G = 255;
				if (B > 255)
					B = 255;
				bm.setPixel(col, row, Color.argb(alpha, R, G, B));
			}
		}
		return bm;
	}

}