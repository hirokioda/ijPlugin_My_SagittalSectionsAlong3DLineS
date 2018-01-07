ImageJ plugin "My_SagittalSectionsAlong3DlineS"

This plugin was developed by Hiroki Oda and Natsuki Hemmi and used for data collection from fluorescently stained spider embryos (Hemmi et al., submitted).
This plugin may be used in combination with an attached imageJ Macro "Treat All MeasurementPoints.ijm."
A workspace directory "C:/nsworkspace/imageJ/", including "_MPoints.txt", "_MSetting.txt", and "_MDRange.txt", must be prepared in advance.
The "_MPoints.txt" file will be automatically generated from Imaris Measurement Points .csv files when using the Macro.

Procedure (in case of Windows 7 or 10)
0) Install Fiji.app (ImageJ), which is placed in "home/Program Files/" in my case.
0) Place My_SagittalSectionsAlong3DLineS.jar in the ImageJ plugin directory (e.g., home/Program Files/Fiji.app/plugins/MyPlugins/).
1) Prepare Measurement points .csv files using Imaris and put them together with the image stack (.ids/.ics) in a directory.
2) Prepare _MSetting.txt and _MDRange.txt and put them in "C:/nsworkspace/imageJ/".
3) Prepare an output directroy with subfolders (named "1", "2", ..) where you like.
4) Open the image stack using the ImageJ with the plugin "My_SagittalSectionsAlong3DlineS" installed.
5) Run the macro "Treat All MeasurementPoints.ijm".
6) Select the directory containing the .csv files.
7) Select the directory for output.
8) Check the output directory for results.


The formats of the setting files

_MPoitns.txt
---------------------------------------------------
Position
 ====================
Position X,Position Y,Position Z,Unit,Collection,Name,Time,ID,
478.361,854.174,68.2137,um,Position,A,1,0,
480.633,838.042,53.632,um,Position,B,1,1,
483.35,818.816,43.935,um,Position,C,1,2,
485.868,800.993,37.558,um,Position,D,1,3,
488.13,784.979,34.345,um,Position,E,1,4,
491.194,763.293,30.494,um,Position,F,1,5,
494.134,742.482,25.672,um,Position,G,1,6,
497.691,717.406,19.1225,um,Position,H,1,7,
503.204,678.374,14.531,um,Position,I,1,8,
---------------------------------------------------

_MDRange.txt
---------------------------------------------------
#MDRange
#
thLineRng,10
startDist,0
endDist,500
divStep,50
divWidth,5
---------------------------------------------------
thLineRng: Half width of the scale frame (unit)
startDist: Start position of the frame (unit)
endDist: End position of the frame (unit)
divStep: Interval of scale ticks (unit)
divWidth: Size of scale ticks (unit)
---------------------------------------------------

_MSetting.txt
---------------------------------------------------
#MSetting
#
pixinfo,1.0742,1.0742,4.9934
unitVL,1.0
thLine,10
avStep,5
thSlice,10
lineType,0
avThick,0
comment,lineType=0(sheet-average)
drawType,1
---------------------------------------------------
pixinfo: the sizes (x, y, z) of each pixel
unitVL: unit length (usually 1 micro meter)
thLine: half width along the measurement line for data collection (unit) Max.800
avStep: width step size for averaging (unit)
thSlice: half height along the measurement line for data collection (unit) Max.200
lineType: method for averaging (0, linear; 1, square; 2, rectangular)
avThick: (When lineType is 2) half height for averaging (unit)
comment: Stamp comments on the graphs
drawType: 0, Only display the measurement line used; 1, Display scale frames with image stacks for Imaris
---------------------------------------------------

last updated by Hiroki Oda Jan 5, 2018
