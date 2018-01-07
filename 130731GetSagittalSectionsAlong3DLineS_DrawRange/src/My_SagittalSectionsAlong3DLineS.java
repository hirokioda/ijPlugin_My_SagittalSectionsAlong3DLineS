import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

// This class was developed by Hiroki Oda (oda_bigtree@me.com) and Natsuki Hemmi.
// 平均化を水平線(0)、正方形面(1)、長方形面(2)で行えるようにした。lineType, avThickの導入。6/8/13
// 描画したグラフを保存できるようにした。6/8/13
// グラフにグラフ作成日時を記録、コメント行の導入。6/8/13
// Macroと連携して複数の_MPointsファイルに対して自動処理をできるようにした。Treat_All_MeasurementPoints.txtを参照。6/8/13
// pixinfoを_MPoints.txtではなく、MSetting.txtから取得。
// Imarisで出力したMPTsファイルをそのまま取り込めるようにMpts3クラスのReadFromFileメソッドとReadMSettingメソッドを修正した。6/10/13
// データ取得領域の描画を選択で可能にした。drawType初期設定; データ取得領域枠表示別ファイルなし、直接書き込みのみ（0)、imarisへの取り込み用画像の作成も同時に行う(1)   8/2/13に追加
// 130424 My_SagittalSectionsAlong3DLineS_DrawRnage.javeの変更を反映させた。130802
// 170103 Mpts3.javaに変更を加えた。ラインに沿った長さ (Max 1601 dots)、横幅、深さ (Max 401 dots) を大きく取れるように。

public class My_SagittalSectionsAlong3DLineS implements PlugInFilter {    //   このプログラムの完成度は高い。マルチチャネルに対応。
	ImagePlus imp;
	static Color[] col={Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.GRAY, };

	public int setup(String arg, ImagePlus imp) {
		if (imp == null) {
			IJ.noImage();
			return DONE;
		}
		this.imp = imp;
		return DOES_ALL;
	}

	public void run (ImageProcessor ip) {
		if(imp.getType()!=0)
		{IJ.error("File type should be 8 bit!!"); return;} // 8 bit でなく、複数チャネルに対応

		int w = imp.getWidth();        // 幅
		int h = imp.getHeight();       // 高さ
		int nCh = imp.getNChannels();  // カラーチャネル
		int nSl = imp.getNSlices();    // zスライス
		int nFr = imp.getNFrames();    // タイムポイント

		ImageStack[] stack = getEachStack(imp); //stackをchannelごとに分割（このクラスのメソッド、下を見よ）

		Mpts3 mp= new Mpts3(stack, w, h, nCh, nSl, nFr); // Measurement points analysis (Mpts2)クラスのコンストラクタ作成。

// ファイルから測定点の位置に関する数値を取り込む----------------------------------
		// open a file and read data from it
		String rfilename =  "c:/nsworkspace/imageJ/_MPoints.txt";
		int elnumb =3; // 一行あたりに取得するデータ要素の数 (x,y,z)（行始から、指定の要素数以降は無視される）
		String separator=",";


		mp.ReadFromFile(separator, elnumb, rfilename);


		// ファイルから読み込んだ位置情報（計測位置）の数、x配列の数を取得。
		System.out.println("要素数 "+mp.mptNumb);

		// 描画フレームの設定
		JFrame frame=new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle("Graph1");
		frame.setSize(900,800);

// Measurement pointsを繋ぐラインを含むようにsagittal sectionsを取得-----------------
		mp.nDots=0;  // ドット数のリセット
		mp.deltaStart=0;  // 繰り越し距離のリセット
		mp.unitVL=1.0;  // 単位距離の設定（micro meter）
		mp.thLine=20;  // Sagittal sectionsの数の設定（整数、片側分のユニット数）
		mp.thSlice=20;  // z方向に取得するデータ量（上下片側分のユニット数）
		mp.lineType=0;  // lineType初期設定; データを平均化するための範囲を水平線（0)にするか正四角形面(1)にするか長方形面(2)   6/8/13に追加
		mp.avThick=0;   // lineType=2の場合の平均化するための厚み  6/8/13に追加
		mp.drawType=0;  // drawType初期設定; データ取得領域枠表示別ファイルなし、直接書き込みのみ（0)、imarisへの取り込み用画像の作成も同時に行う(1)   8/2/13に追加

		int smoothenWindowSize=0;  // スムージング

		System.out.println("総距離 "+mp.getTotalLength());

		String msfilename = "c:/nsworkspace/imageJ/_MSetting.txt";
		mp.ReadMSetting(separator, msfilename);

		if(mp.drawType==1){                                         // _MSetting.txtの情報の中でdrawType=1の時のみ_MDRange.txtの読み込みを行う
			msfilename = "c:/nsworkspace/imageJ/_MDRange.txt";       // 130802 130424 My_SagittalSectionsAlong3DLineS_DrawRnage.javeの変更を反映
			System.out.println("ファイル名 "+msfilename);
			mp.ReadMDRange(separator, msfilename);
		}

		mp.getCh3DVolumeProfile();

		ImagePlus impCh;            // 新しいImageStackを作って抜き出した画像を表示
		ImageStack stackCh[]=new ImageStack[nCh];
		for(int ch=0; ch<nCh; ch++){
			impCh=NewImage.createByteImage("Ch"+ch, mp.nDots, mp.thSlice*2+1, mp.thLine*2+1, NewImage.FILL_BLACK);
			//chValue3D=[nCh][thLine*2+1][tLengthUnits][thSlice*2+1]
			stackCh[ch]=impCh.getStack();
			for(int k=0; k<mp.thLine*2+1; k++){
				ImageProcessor iFrame=stackCh[ch].getProcessor(k+1);
				for(int j=0; j<mp.thSlice*2+1; j++){
					for(int i=0; i<mp.nDots; i++){
						iFrame.putPixel(i, j, (int)mp.chValue3D[ch][i][k][j]);
					}
				}
			}
			impCh.show();
		}

		ImagePlus impCh2;            // 新しいImageStackを作って抜き出した画像を表示
		ImageStack stackCh2[]=new ImageStack[nCh];;
		for(int ch=0; ch<nCh; ch++){
			impCh2=NewImage.createByteImage("Ch2-"+ch, mp.nDots, mp.thLine*2+1, mp.thSlice*2+1, NewImage.FILL_BLACK);
			//chValue3D=[nCh][thLine*2+1][tLengthUnits][thSlice*2+1]
			stackCh2[ch]=impCh2.getStack();
			for(int k=0; k<mp.thSlice*2+1; k++){
				ImageProcessor iFrame=stackCh2[ch].getProcessor(k+1);
				for(int j=0; j<mp.thLine*2+1; j++){
					for(int i=0; i<mp.nDots; i++){
						iFrame.putPixel(i, j, (int)mp.chValue3D[ch][i][j][k]);
					}
				}
			}
			impCh2.show();
		}

		Calendar today=Calendar.getInstance();                                 // グラフ作成日時の取得  6/8/13
		int y=today.get(Calendar.YEAR);
		int m=today.get(Calendar.MONTH)+1;
		int d=today.get(Calendar.DATE);
		String stampDATE=String.valueOf(y)+"-"+String.valueOf(m)+"-"+String.valueOf(d);

//		--------------------------------------------------------------------
// 新しいウインドウをを作ってグラフを表示
		for(int av=0;av<=mp.thLine;av=av+mp.avStep){
			BufferedImage image=new BufferedImage(400,200,BufferedImage.TYPE_INT_ARGB);  // TYPE_INT_ARGB,背景透明;TYPE_INT_BGR,背景黒
			Graphics2D g2=(Graphics2D)image.createGraphics();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			GraphMaker gt1=new GraphMaker(image.getWidth(), image.getHeight(), g2);

			gt1.clearImage();
			double[] range={0, 800, 0, 250}; //  表示範囲{xmin, xmax, ymin, ymax}
			gt1.setGraphFrame(40,20,false, range); // 座標変換係数の計算
			gt1.drawAxis("x", 8, "y", 5); // 座標軸の描画
			gt1.drawStr(11,    //  パラメータの値の表示(フォントサイズ指定)
					"My_SagittalSectionsAlong3DLineS (130608)"+" "+stampDATE,            // グラフ作成日時のスタンプ  6/8/13
					mp.comment,                                                          // コメント行の導入。設定ファイルで設定。 6/8/13
					"UnitLength= "+mp.unitVL+" um",
					"LineThickness= "+av*2+" um"+ "  drawType="+mp.drawType);
			for(int i=0; i<nCh; i++){
				gt1.plotData(mp.ipDist, mp.getChLineProfileAv(i, av, mp.lineType, mp.avThick), mp.nDots, col[i], null, 0.5F );
			}


			// 描画内容
			g2.dispose();
			JLabel label1=new JLabel(new ImageIcon(image));
			frame.add(label1);

			// GraphToolsで描画したグラフ画像の保存     6/8/13追加
			String gfilename =  "c:/nsworkspace/imageJ/graph";
			if (image != null){
				//
				try {
					boolean result = ImageIO.write(image, "png", new File(gfilename+av+".png"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				gt1.clearImage();
			}
		}

//		---------------------------------------------------------------------
		// 描画フレームの表示
		frame.setVisible(true);
//		---------------------------------------------------------------------


// ------------------------------------------------------------------------------------------
		// データ取得領域の表示：選択１
// ------------------------------------------------------------------------------------------
		ImageStack stackdr=mp.getStack(2);

// RGB intensity line profileを取得した位置の確認のためにdotsを描画
		for(int i=0; i<mp.nDots; i++){
			ImageProcessor ipm= stackdr.getProcessor((int)Math.round(mp.getPZ(mp.ipz[i])));  // 描画するスライスのImagaProcessorを取得
			ipm.putPixel((int)Math.round(mp.getPX(mp.ipx[i])), (int)Math.round(mp.getPY(mp.ipy[i])), 200);
		}

// Measurement pointsをimageJ画像に印して確認
		for(int i=0; i<mp.mptNumb; i++){
			ImageProcessor ipm= stackdr.getProcessor((int)Math.round(mp.getPZ(mp.mpt_xyz[2][i])));  // 描画するスライスのImagaProcessorを取得
			ipm.putPixel((int)Math.round(mp.getPX(mp.mpt_xyz[0][i])), (int)Math.round(mp.getPY(mp.mpt_xyz[1][i])), 255);
		}
// ------------------------------------------------------------------------------------------
		// データ取得領域の表示：選択２
// ------------------------------------------------------------------------------------------
		if(mp.drawType==1){                             //  drawType=1の時のみimaris取り込み用画像の作成を行う
			ImagePlus impDR;            // 新しいImageStackを作って抜き出した画像を表示
			ImageStack stackDR[]=new ImageStack[4];

			for(int ch=0; ch<stackDR.length; ch++){
				impDR=NewImage.createByteImage("DrawRange-"+ch, w, h, nSl, NewImage.FILL_BLACK);
				stackDR[ch]=impDR.getStack();
				switch(ch){
					case 0: for(int i=0; i<mp.mptNumb; i++){   // RGB intensity line profileを取得した位置の確認のためにdotsを描画
						ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.mpt_xyz[2][i])));  // 描画するスライスのImagaProcessorを取得
						ipm.putPixel((int)Math.round(mp.getPX(mp.mpt_xyz[0][i])), (int)Math.round(mp.getPY(mp.mpt_xyz[1][i])), 255);
					}
						break;
					case 1: for(int i=0; i<mp.nDots; i++){     // Measurement pointsをimageJ画像に印して確認
						ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.ipz[i])));  // 描画するスライスのImagaProcessorを取得
						ipm.putPixel((int)Math.round(mp.getPX(mp.ipx[i])), (int)Math.round(mp.getPY(mp.ipy[i])), 255);
					}
						break;
					case 2: for(int i=mp.startDist; i<mp.endDist; i++){     // Measurement pointsをimageJ画像に印して確認
						for(int k=1;k<3;k++){
							ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.ipzRNG[k][i])));  // 描画するスライスのImagaProcessorを取得
							ipm.putPixel((int)Math.round(mp.getPX(mp.ipxRNG[k][i])), (int)Math.round(mp.getPY(mp.ipyRNG[k][i])), 255);
						}
					}
					for(int k=0; k<2;k++){
						for(int i=0; i<=mp.thLine*2; i++){
							ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.ipzEND[k][i])));  // 描画するスライスのImagaProcessorを取得
							ipm.putPixel((int)Math.round(mp.getPX(mp.ipxEND[k][i])), (int)Math.round(mp.getPY(mp.ipyEND[k][i])), 255);
						}
					}
					for(int l=1; l<=mp.divNumb; l++){
						for(int k=0;k<2;k++){
							for(int i=0; i<mp.divWidth; i++){
								ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.ipzDIV[k][l][i])));  // 描画するスライスのImagaProcessorを取得
								ipm.putPixel((int)Math.round(mp.getPX(mp.ipxDIV[k][l][i])), (int)Math.round(mp.getPY(mp.ipyDIV[k][l][i])), 255);
							}
						}
					}
						break;
					case 3:  for(int i=mp.startDist; i<mp.endDist; i++){     // Measurement pointsをimageJ画像に印して確認
						for(int k=0;k<1;k++){
							ImageProcessor ipm= stackDR[ch].getProcessor((int)Math.round(mp.getPZ(mp.ipzRNG[k][i])));  // 描画するスライスのImagaProcessorを取得
							ipm.putPixel((int)Math.round(mp.getPX(mp.ipxRNG[k][i])), (int)Math.round(mp.getPY(mp.ipyRNG[k][i])), 255);
						}
					}
						break;
				}
				impDR.show();
			}
		}
// ------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------


// データをファイルへ書き出し------------------------------------------------------
		String wfilename =  "c:/nsworkspace/imageJ/MResult";
		mp.WriteProfileToFile("comment", wfilename);
// --------------------------------------------------------------------------------

		System.out.println(w+" "+h+" "+" "+nSl);
		System.out.println("file-type= "+imp.getType());
		System.out.println("nchannel= "+nCh);
	}

// -----------------------------------------------------------------------------
	//stackをchannelごとに分割し配列へ by 逸見さん
	ImageStack[] getEachStack(ImagePlus imp) {
		int nCh = imp.getNChannels();
		ImageStack[] stack = new ImageStack[nCh];
		for (int i = 0; i < nCh; i++) {
			stack[i] = getMultiChannel(imp, i + 1);
		}
		return stack;
	}

	//指定したchannelのstackを取得(1 <= c) by 逸見さん
	ImageStack getMultiChannel(ImagePlus imp, int ch) {
		ImageStack stackWhole = imp.getStack();
		ImageStack stackEach = new ImageStack(imp.getWidth(), imp.getHeight());
		int index;
		for (int nf = 1; nf <= imp.getNFrames(); nf++) {   // time points
			for (int ns = 1; ns <= imp.getNSlices(); ns++) {  // z slices
				index = imp.getStackIndex(ch, ns, nf);
				stackEach.addSlice(stackWhole.getProcessor(index));
			}
		}
		return stackEach;
	}
}