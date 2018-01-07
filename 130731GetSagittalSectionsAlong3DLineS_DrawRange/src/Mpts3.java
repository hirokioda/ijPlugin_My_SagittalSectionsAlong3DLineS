import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.ImageStack;

// This class was developed by Hiroki Oda (oda_bigtree@me.com) and Natsuki Hemmi.

public class Mpts3 {
	ImageStack[] stack;
	double [][] mpt_xyz=new double[3][100]; // x, y, z real pos：Measurement pointsの数は最大100まで。

	int width;
	int height;
	int nCh=3; // チャネル数
	int nSlices; // zスライスの数
	int nFrames;  // time points
	int mptNumb=0;
	double puLx =1.0;  // 1 pixel当たりの長さ、デフォルトで1.0を設定
	double puLy =1.0;
	double puLz =1.0;

	String comment="";

	Mpts3(ImageStack[] stack, int w, int h,int ch, int nsl, int nfr){
		this.stack=stack;
		this.width=w;
		this.height=h;
		this.nCh=ch;
		this.nSlices=nsl;
		this.nFrames=nfr;
	}

	public void ReadFromFile(String cut, int n, String infile) { // cutは区切り文字をセット、nは１行当たりのデータ数
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File(infile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			System.out.println(infile);
			String rstr="";
			mptNumb=0;  // データ数を０にリセット

			String regex="^\\d";                                          //  正規表現でマッチングするため。6/10/13
			Pattern p=Pattern.compile(regex);

			while((rstr=br.readLine()) != null){
				System.out.println(rstr);
				System.out.println(mptNumb);

				Matcher m=p.matcher(rstr);                                // 正規表現でマッチングするため。6/10/13
				if(m.find()){                                             // 数字で始まる行から数値を取得
					String[] suji = rstr.split(cut, 0);
					System.out.println(suji[0]+" "+suji[1]+" "+suji[2]+"ed");
					for (int i=0; i<n; i++){
						mpt_xyz[i][mptNumb]=Double.parseDouble(suji[i]);
					}
					mptNumb++;
				 }
				/*
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視。それ以外の行からデータを取得。
					if(rstr.startsWith("//")==true || rstr.startsWith("\n")==true || rstr.startsWith(cut))break;  // 130324加えた。
					String[] suji = rstr.split(cut, 0);
					System.out.println(suji[0]+" "+suji[1]+" "+suji[2]+"ed");
					for (int i=0; i<n; i++){
						mpt_xyz[i][mptNumb]=Double.parseDouble(suji[i]);
					}
					mptNumb++;
				}
				*/
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}

	public void ReadMSetting(String cut, String infile) { // cutは区切り文字をセット、nは１行当たりのデータ数
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File(infile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String rstr="";
			while((rstr=br.readLine()) != null){
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視。それ以外の行からデータを取得。
					if(rstr.startsWith("//")==true || rstr.startsWith("\n")==true || rstr.startsWith(cut))break;  // 130324加えた。
					if(rstr.startsWith("pixinfo")==true){  // pixinfoで始まる行に1pixelあたりの長さをx,y,zの順に入れておく。   _MPointsから移動　6/10/13
						String[] suji = rstr.split(cut, 0);
						puLx=Double.parseDouble(suji[1]);
						puLy=Double.parseDouble(suji[2]);
						puLz=Double.parseDouble(suji[3]);
					}
					if(rstr.startsWith("unitVL")==true){  // unitVLの初期設定
						String[] suji = rstr.split(cut, 0);
						unitVL=Double.parseDouble(suji[1]);
					}
					if(rstr.startsWith("thLine")==true){  // thLineの初期設定
						String[] suji = rstr.split(cut, 0);
						thLine=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("avStep")==true){  // avStepの初期設定
						String[] suji = rstr.split(cut, 0);
						avStep=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("thSlice")==true){  // thSlice初期設定
						String[] suji = rstr.split(cut, 0);
						thSlice=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("lineType")==true){  // lineType初期設定; データを平均化するための範囲を水平線（0)にするか正四角形面(1)にするか長方形面(2)   6/8/13に追加
						String[] suji = rstr.split(cut, 0);
						lineType=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("avThick")==true){  // avThick初期設定; lineType=2の場合の平均化するための厚み                                               6/8/13に追加
						String[] suji = rstr.split(cut, 0);
						avThick=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("comment")==true){  // グラフへ書き込むコメント                               6/8/13に追加
						String[] suji = rstr.split(cut, 0);
						comment=suji[1];
					}
					if(rstr.startsWith("drawType")==true){  // drawType初期設定; データ取得領域枠表示別ファイルなし、直接書き込み（0)、imaris用ファイル(1)にするか長方形面(2)   8/2/13に追加
						String[] suji = rstr.split(cut, 0);
						drawType=Integer.parseInt(suji[1]);
					}
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}
	//---------------------------------130801 130424のMpts5.javaから
	public void ReadMDRange(String cut, String infile) { // cutは区切り文字をセット、nは１行当たりのデータ数
		System.out.println("infile"+infile);
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File(infile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String rstr="";
			while((rstr=br.readLine()) != null){
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視。それ以外の行からデータを取得。
					if(rstr.startsWith("//")==true || rstr.startsWith("\n")==true || rstr.startsWith(cut))break;  // 130324加えた。
					if(rstr.startsWith("thLineRng")==true){  // thLineRngの初期設定
						String[] suji = rstr.split(cut, 0);
						thLineRng=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("startDist")==true){  // startDistの初期設定
						String[] suji = rstr.split(cut, 0);
						startDist=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("endDist")==true){  // endDistの初期設定
						String[] suji = rstr.split(cut, 0);
						endDist=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("divStep")==true){  // thSlice初期設定
						String[] suji = rstr.split(cut, 0);
						divStep=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("divWidth")==true){  // thSlice初期設定
						String[] suji = rstr.split(cut, 0);
						divWidth=Integer.parseInt(suji[1]);
					}
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}
	// -----------------------------------------------------------------

	public void WriteProfileToFile(String comment, String outfile) {  // ３つの変数の書き出す（コメントあり）
		try {					// フォルダがなかったり、ファイルへの書き込みができない場合には例外処理をする必要がある
			for(int av=0; av<=thLine; av=av+avStep){
				double [][] chProfile=new double[nCh][dotNMax];
				File file = new File(outfile+av+".txt");
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("MResult Average Line "+av+"\n");
				for(int ch=0; ch<nCh; ch++){
					chProfile[ch]=getChLineProfileAv(ch, av, lineType, avThick);                                      //  6/8/13 修正
				}
				for (int i = 0; i < nDots; i++) {
					bw.write(i+"\t"+ ipDist[i]+"\t"+ipx[i]+"\t"+ipy[i]+"\t"+ipz[i]);
					for(int ch=0; ch<nCh;ch++){
						bw.write("\t"+chProfile[ch][i]);
					}
					bw.write("\n");
				}
				bw.write("\n");
				bw.close();
			}
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}

	public double[] getIntpValues(double x, double y, double z){  // 任意の位置におけるRGB強度を取得。// ImageStack[]を使用
		double[] iValue = new double[nCh];
		double px=getPX(x); double py=getPY(y); double pz=getPZ(z); // pixel値の取得、下に変換メソッドあり。
		/*
		if(px<0||py<0||pz<1||(int)px>width||(int)py>height||(int)pz>nSlices){
			IJ.error("Measurement points are out of range! \n"+"px="+px+"\npy="+py+"\npz="+pz); System.exit(-1);
			return iValue;
		}
		*/

		for(int i=0; i<nCh;i++){
			iValue[i] = getInterpolatedValue(stack[i], px, py, pz);
		}
		return iValue;
	}

	// 2点間のintensity profileを取得
	double unitVL=1.0; // unit vetorのサイズ デフォルト値は1.0；　 getRGBIntensityProfileメソッドを呼び出す前にあらかじめセットする必要あり
	double deltaStart = 0;  // 前の測定からの繰り越し分；　getRGBIntensityProfileメソッドを呼び出す前にあらかじめセットする必要あり
	int nDots = 0;  // 端から何点目を調べているか？　getRGBIntensityProfileメソッドを呼び出す前にあらかじめセット(リセット) する必要あり

//	int tLengthUnits=getTotalLengthUnits();   // Lineに沿った長さのユニット数（終点と始点両者を含める）// 3/24/13に加えた。
//	int tLengthUnits=10000;
	static final int dotNMax = 1601; // dotの数の最大数、along the measurement line, 180103 changed
	static final int dotYZMax = 401; // dotの数の最大数、yz-plane (related to Max of thLine and thSlice), 180103 added
	int thLine=0;  // 平均（またはSagittal sections) をとるための線の幅 unit数片側分
	int avStep=10; // テキストへの書き出しのためのライン幅のステップ量
	int thSlice=20;  // 平均 （またはSagittal sections) をとるためのスライスの枚数　上下片側分
	int lineType=0;  // データを平均化するための範囲を水平線（0)にするか正四角形面(1)にするか長方形面(2)   6/8/13に追加
	int avThick=0; // lineType=2の場合の平均化するための厚み                                               6/8/13に追加
	int drawType=0;  // drawType初期設定; データ取得領域枠表示別ファイルなし、直接書き込み（0)、imaris用ファイルの作成を行う(1)   8/2/13に追加
	double[] ipx = new double[dotNMax];                          // dotNMaxをtLengthUnitsに変更
	double[] ipy = new double[dotNMax];
	double[] ipz = new double[dotNMax];
	double[][] chValueProfile = new double[nCh][dotNMax];
	double[] ipDist = new double[dotNMax];


	// ----------------------------------130801 130424のMpts5.javaから  DrawRangeに関わる配列の宣言
	double[][] ipxRNG = new double[3][dotNMax];  // lineに沿って平均を取得する範囲の両サイドの位置座標を入れる
	double[][] ipyRNG = new double[3][dotNMax];
	double[][] ipzRNG = new double[3][dotNMax];

	int thLineRng;   // 枠の幅、始め、終わり
	int startDist;
	int endDist;  // unit数で表す
	double[][] ipxEND = new double[2][dotYZMax];
	double[][] ipyEND = new double[2][dotYZMax];
	double[][] ipzEND = new double[2][dotYZMax];
	int divStep;
	int divNumb=0;
	int divWidth;
	double[][][] ipxDIV = new double[2][100][dotYZMax];   // 目盛用の線の座標 [左右][手前からの順番][サイドからの位置]
	double[][][] ipyDIV = new double[2][100][dotYZMax];
	double[][][] ipzDIV = new double[2][100][dotYZMax];
	// -----------------------------------------------------130801


	//----------------------------------------------------------------------------------------------
	double[][][][] chValue3D= new double[5][dotNMax][dotYZMax][dotYZMax]; // チャネル、ラインに沿った長さ、横、高さ

	public boolean getChIntensityProfile3D(double x1, double y1, double z1, double x2, double y2, double z2){
		// 単位ベクトルの計算
		double L=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));
		double Lxy=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));      //  130801 130424のMpts5.javaでの修正を反映
		double chL=L;
		double ex=(x2-x1)*unitVL/L;
		double ey=(y2-y1)*unitVL/L;
		double ez=(z2-z1)*unitVL/L;

//		double[] iValue=new double[nCh];
		if(nDots==0){   // 一番最初の測定点の場合
			ipx[nDots]=x1;
			ipy[nDots]=y1;
			ipz[nDots]=z1;
		}
		// 各線分の最初の点の場合（２番目の測定点以降について）
		else {
			ipx[nDots]=x1+ex*(1-deltaStart/unitVL);
			ipy[nDots]=y1+ey*(1-deltaStart/unitVL);
			ipz[nDots]=z1+ez*(1-deltaStart/unitVL);
		}

		getValueInPlane(ipx[nDots], ipy[nDots], ipz[nDots],ex, ey, ez);

		ipDist[nDots]=unitVL*nDots;
		nDots++;

		// 各線分の最初の点の次からの強度取得
		while(chL>=unitVL){
			// 測定位置の算出
			ipx[nDots]=ipx[nDots-1]+ex;
			ipy[nDots]=ipy[nDots-1]+ey;
			ipz[nDots]=ipz[nDots-1]+ez;
			// RGBシグナル強度の取得   130801 130424のMpts5.javaでの修正を反映
			getValueInPlane(ipx[nDots], ipy[nDots], ipz[nDots],ex*L/Lxy, ey*L/Lxy, ez);  // vectorのサイズを補正（z方向に傾いていると横方向に線幅が狭くなってしまう）

			ipDist[nDots]=unitVL*nDots;
			// 次のMeasurement pointまでの距離を算出
			chL=Math.sqrt((x2-ipx[nDots])*(x2-ipx[nDots])+(y2-ipy[nDots])*(y2-ipy[nDots])+(z2-ipz[nDots])*(z2-ipz[nDots]));
			System.out.println("distanceW="+ipDist[nDots]+"chL="+chL+"L="+L);
			nDots++;
			if(nDots>=dotNMax) return false;                               //     3/24/13にdotNMaxからtLengthUnitsに変更
		}
		deltaStart=chL;
		return true;
	}

	// 面内の値を取得      130801 130424のMpts5,javaのDrawRange核心部分を反映
	public void getValueInPlane(double x, double y, double z, double ex, double ey, double ez){ // ImageStack[]を使用
		double wx, wy, wz;
		double[] iValue=new double[nCh];
		System.out.println("nDots="+nDots+"  startDist"+startDist+"  endDist"+endDist+ "  thLineRng"+thLineRng);
		for(int i=-thLine; i<=thLine; i++){       //  間違いあり。3/24/13 i=0を加える必要あり。
			wx=x+ey*i; wy=y-ex*i;
			if(i==0){
				ipxRNG[0][nDots]=wx;
				ipyRNG[0][nDots]=wy;
				ipzRNG[0][nDots]=z;
			}else if(i==-thLineRng){                 //  枠のサイドの線のための位置座標
				ipxRNG[1][nDots]=wx;
				ipyRNG[1][nDots]=wy;
				ipzRNG[1][nDots]=z;
			} else if(i==thLineRng){           //  枠のサイドの線のための位置座標
				ipxRNG[2][nDots]=wx;
				ipyRNG[2][nDots]=wy;
				ipzRNG[2][nDots]=z;
			}
			if(nDots==startDist){           //  枠の手前の線のための位置座標
				if(i>=-thLineRng && i<=thLineRng){
					ipxEND[0][thLineRng+i]=wx;
					ipyEND[0][thLineRng+i]=wy;
					ipzEND[0][thLineRng+i]=z;
				}
			}
			if(nDots==endDist){             //  枠の奥の線のための位置座標
				if(i>=-thLineRng && i<=thLineRng){
					ipxEND[1][thLineRng+i]=wx;
					ipyEND[1][thLineRng+i]=wy;
					ipzEND[1][thLineRng+i]=z;
				}
			}
			for(int k=0; startDist+divStep*k<endDist; k++){   // 目盛のため
				if(nDots==startDist+divStep*k){
					if(i>=-thLineRng && i<-thLineRng+divWidth){
						ipxDIV[0][k][thLineRng+i]=wx;
						ipyDIV[0][k][thLineRng+i]=wy;
						ipzDIV[0][k][thLineRng+i]=z;
					}else if(i<=thLineRng && i>thLineRng-divWidth){
						ipxDIV[1][k][thLineRng-i]=wx;
						ipyDIV[1][k][thLineRng-i]=wy;
						ipzDIV[1][k][thLineRng-i]=z;
					}
				}
				divNumb=k;
			}
			for(int j=-thSlice;j<=thSlice;j++){
				wz=z+unitVL*j;
				iValue=getIntpValues(wx, wy, wz);
				for(int ch=0; ch<nCh;ch++){
					chValue3D[ch][nDots][i+thLine][j+thSlice]=iValue[ch];
				}
			}
		}
	}

	public boolean getCh3DVolumeProfile(){
		if(mptNumb<2)return false;
		nDots=0;
		deltaStart=0;
		for(int i=1; i<mptNumb; i++){
			getChIntensityProfile3D(mpt_xyz[0][i-1], mpt_xyz[1][i-1], mpt_xyz[2][i-1], mpt_xyz[0][i], mpt_xyz[1][i], mpt_xyz[2][i]);
		}
		return true;
	}

	public double[] getChLineProfile(int ch){
		double[] result=new double[dotNMax];
		for(int i=0; i<nDots; i++){
			result[i]=chValue3D[ch][i][thLine][thSlice];
		}
		return result;
	}

	public double[] getChLineProfileAv(int ch, int lineNumb){
		double[] result=new double[dotNMax];
		if(lineNumb>thLine)return result;
		for(int i=0; i<nDots; i++){
			double sum=0;
			for(int j=-lineNumb; j<=lineNumb; j++){
				sum=sum+chValue3D[ch][i][j+thLine][thSlice];
			}
			result[i]=sum/(lineNumb*2+1);
		}
		return result;
	}
	                                                                                       // 06/08/13に追加:いろいろな平均の取り方に対応できるようにするため
	public double[] getChLineProfileAv(int ch, int lineNumb, int lineType, int avThick){   // データを平均化するための範囲を水平線（0)にするか正四角形面(1)にするか長方形面にするか(2)
		double[] result=new double[dotNMax];
//		if(lineNumb>thLine || lineNumb>thSlice || avThick>thSlice)return result;
		for(int i=0; i<nDots; i++){
			double sum=0;
			if(lineType==0){                                            // 水平線上での平均化 (水平線の長さunit数 lineNumb)
				for(int j=-lineNumb; j<=lineNumb; j++){
					sum=sum+chValue3D[ch][i][j+thLine][thSlice];
				}
				result[i]=sum/(lineNumb*2+1);
			}
			else if(lineType==1){                                                           // 正四角形面での平均化 (各辺の長さunit数 lineNumb)
				for(int k=-lineNumb; k<=lineNumb; k++){
					for(int j=-lineNumb; j<=lineNumb; j++){
						sum=sum+chValue3D[ch][i][j+thLine][k+thSlice];
					}
				}
				result[i]=sum/((lineNumb*2+1)*(lineNumb*2+1));
			}
			else if(lineType==2){                                                           // 長方形面での平均化 (各辺の長さunit数 lineNumb)
				for(int k=-avThick; k<=avThick; k++){
					for(int j=-lineNumb; j<=lineNumb; j++){
						sum=sum+chValue3D[ch][i][j+thLine][k+thSlice];
					}
				}
				result[i]=sum/((lineNumb*2+1)*(avThick*2+1));
			}
		}
		return result;
	}

// ----------------------------------------------------------------------------------------------------

	public ImageStack getStack(int ch){
		return stack[ch];
	}

	public double getPX(double x){  //  実際の位置からスクリーン上の位置に変換
		double px = x/puLx;
		return px;
	}
	public double getPY(double y){
		double py = y/puLy;
		return py;
	}
	public double getPZ(double z){
		double pz = z/puLz+1.0;
//		if(pz>=(double)nSlices+0.5 && pz<(double)nSlices+0.5+0.01) return pz=(double)nSlices;
//		return Math.round(pz);
		return pz;
	}

	public double getTotalLength(){  // measurement pointsを繋げたラインの長さ
		double L, tL=0;
		double x1,y1,z1,x2,y2,z2;
		for(int i=1; i<mptNumb; i++){
			x1=mpt_xyz[0][i-1]; x2=mpt_xyz[0][i];
			y1=mpt_xyz[1][i-1]; y2=mpt_xyz[1][i];
			z1=mpt_xyz[2][i-1]; z2=mpt_xyz[2][i];
			L=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));
			tL=tL+L;
		}
		return tL;
	}

	public int getTotalLengthUnits(){ // measurement pointsを繋げたラインの長さのユニット数をtLengthUnitsに入れる。
		return (int)(getTotalLength()/unitVL)+1;
	}

	/*
	public double getInterpolatedPixel(ImageStack is, double px, double py, double pz){
		double A, B, C, D, E, F, G;

		int u=(int) Math.floor(px);
		int v=(int) Math.floor(py);
		double a=px - u;
		double b=py - v;

		A=is.getVoxel(u,v,(int)pz);
		B=is.getVoxel(u+1,v,(int)pz);
		C=is.getVoxel(u,v+1,(int)pz);
		D=is.getVoxel(u+1,v+1,(int)pz);
		E=A+a*(B-A);
		F=C+a*(D-C);
		G=E+b*(F-E);
		return G;
	}
	*/
	//座標(x, y, z)のinterpolation後の値をdoubleで取得                                         from 逸見さん
	public double getInterpolatedValue(ImageStack is, double x, double y, double z) {
		int width = is.getWidth();
		int height = is.getHeight();
		int depth = is.getSize();
		if (x < 0.0 || x >= width - 1.0 || y < 0.0 || y >= height - 1.0 || z < 0.0 || z >= depth - 1.0) {
			if (x < -1.0 || x >= width || y < -1.0 || y >= height || z < -1.0 || z >= depth)
				return 0.0;
			else
				return getInterpolatedEdgeValue(is, x, y, z);
		}
		int xbase = (int) x;
		int ybase = (int) y;
		int zbase = (int) z;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		double zFraction = z - zbase;
		if (xFraction < 0.0)
			xFraction = 0.0;
		if (yFraction < 0.0)
			yFraction = 0.0;
		if (zFraction < 0.0)
			zFraction = 0.0;
		double inLowerLeft = is.getVoxel(xbase, ybase, zbase);
		double inLowerRight = is.getVoxel(xbase + 1, ybase, zbase);
		double inUpperRight = is.getVoxel(xbase + 1, ybase + 1, zbase);
		double inUpperLeft = is.getVoxel(xbase, ybase + 1, zbase);
		double exLowerLeft = is.getVoxel(xbase, ybase, zbase + 1);
		double exLowerRight = is.getVoxel(xbase + 1, ybase, zbase + 1);
		double exUpperRight = is.getVoxel(xbase + 1, ybase + 1, zbase + 1);
		double exUpperLeft = is.getVoxel(xbase, ybase + 1, zbase + 1);
		double inUpper = inUpperLeft + xFraction * (inUpperRight - inUpperLeft);
		double inLower = inLowerLeft + xFraction * (inLowerRight - inLowerLeft);
		double exUpper = exUpperLeft + xFraction * (exUpperRight - exUpperLeft);
		double exLower = exLowerLeft + xFraction * (exLowerRight - exLowerLeft);
		double inAverage = inLower + yFraction * (inUpper - inLower);
		double exAverage = exLower + yFraction * (exUpper - exLower);
		return inAverage + zFraction * (exAverage - inAverage);
	}

	//座標が画像の端の時に使用
	private double getInterpolatedEdgeValue(ImageStack is, double x, double y, double z) {
		int xbase = (int) x;
		int ybase = (int) y;
		int zbase = (int) z;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		double zFraction = z - zbase;
		if (xFraction < 0.0)
			xFraction = 0.0;
		if (yFraction < 0.0)
			yFraction = 0.0;
		if (zFraction < 0.0)
			zFraction = 0.0;
		double inLowerLeft = getEdgeValue(is, xbase, ybase, zbase);
		double inLowerRight = getEdgeValue(is, xbase + 1, ybase, zbase);
		double inUpperRight = getEdgeValue(is, xbase + 1, ybase + 1, zbase);
		double inUpperLeft = getEdgeValue(is, xbase, ybase + 1, zbase);
		double exLowerLeft = getEdgeValue(is, xbase, ybase, zbase + 1);
		double exLowerRight = getEdgeValue(is, xbase + 1, ybase, zbase + 1);
		double exUpperRight = getEdgeValue(is, xbase + 1, ybase + 1, zbase + 1);
		double exUpperLeft = getEdgeValue(is, xbase, ybase + 1, zbase + 1);
		double inUpper = inUpperLeft + xFraction * (inUpperRight - inUpperLeft);
		double inLower = inLowerLeft + xFraction * (inLowerRight - inLowerLeft);
		double exUpper = exUpperLeft + xFraction * (exUpperRight - exUpperLeft);
		double exLower = exLowerLeft + xFraction * (exLowerRight - exLowerLeft);
		double inAverage = inLower + yFraction * (inUpper - inLower);
		double exAverage = exLower + yFraction * (exUpper - exLower);
		return inAverage + zFraction * (exAverage - inAverage);
	}

	//画像の端のintensity
	private double getEdgeValue(ImageStack is, int x, int y, int z) {
		int width = is.getWidth();
		int height = is.getHeight();
		int depth = is.getSize();
		if (x <= 0)
			x = 0;
		if (x >= width)
			x = width - 1;
		if (y <= 0)
			y = 0;
		if (y >= height)
			y = height - 1;
		if (z <= 0)
			z = 0;
		if (z >= depth)
			z = depth - 1;
		return is.getVoxel(x, y, z);
	}
}
