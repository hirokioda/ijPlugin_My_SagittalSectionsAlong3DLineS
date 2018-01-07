// ImageJ Macro "Treat All MeasurementPoints"
// This macro uses an imageJ plugin "My SagittalSectionsAlong3DLineS."
// A workspace directory "C:/nsworkspace/imageJ/", including "_MSetting.txt" and "_MDRange.txt", must be prepared in advance.
// A directory must be prepared for each image stack (.ids/.ics).
// It should contain Imaris Measurement Points .csv file(s) for data collection.
// An output directory with subfolders named  "1", "2", and so on (depending on the number of the .csv files) must be prepared.
// This macro was developed by Hiroki Oda, 2013.6.7, updated 2013.8.2, 2018.1.3

macro "Treat All MeasurementPoints" {
	source_dir = getDirectory("MPoints files (.txt)");  // select the directory containg Imaris Measurement Points .csv file(s).
	target_dir = getDirectory("Target Directory");  // select the directory for output.
	if (File.exists(target_dir) && File.exists(source_dir)) {
		setBatchMode(false);
		list = getFileList(source_dir);
		j=0;
		for(i=0; i<list.length; i++){
			if(endsWith(list[i], "csv")){
				j=j+1;
				run("Text File... ", "open="+source_dir+"/"+list[i]);
				selectWindow(list[i]);
				saveAs("Text", "C:\\nsworkspace\\imageJ\\_MPoints.txt");
				selectWindow(list[i]);
				run("Close");
				run("My SagittalSectionsAlong3DLineS");
				selectWindow("Ch0");
				saveAs("tiff", target_dir + "/" +j+ "/Ch0");
				close();
				selectWindow("Ch1");
				saveAs("tiff", target_dir + "/" +j+ "/Ch1");
				close();
				selectWindow("Ch2");
				saveAs("tiff", target_dir + "/" +j+ "/Ch2");
				close();
				selectWindow("Ch2-0");
				saveAs("tiff", target_dir + "/" +j+ "/Ch2-0");
				close();
				selectWindow("Ch2-1");
				saveAs("tiff", target_dir + "/" +j+ "/Ch2-1");
				close();
				selectWindow("Ch2-2");
				saveAs("tiff", target_dir + "/" +j+ "/Ch2-2");
				close();
				selectWindow("DrawRange-0");
				saveAs("tiff", target_dir + "/" +j+ "/DrawRange-0");  // added 130802
				close();
				selectWindow("DrawRange-1");
				saveAs("tiff", target_dir + "/" +j+ "/DrawRange-1");
				close();
				selectWindow("DrawRange-2");
				saveAs("tiff", target_dir + "/" +j+ "/DrawRange-2");
				close();
				selectWindow("DrawRange-3");
				saveAs("tiff", target_dir + "/" +j+ "/DrawRange-3");
				close();

				// trasfer the resulting text files to the target directory
				listResults = getFileList("C:\\nsworkspace\\imageJ");
				for(k=0; k<listResults.length; k++){
					if(endsWith(listResults[k], "txt")){
						run("Text File... ", "open=C:/nsworkspace/imageJ/"+listResults[k]);
						selectWindow(listResults[k]);
						saveAs("Text", target_dir + "/" +j+ "/"+listResults[k]);
						selectWindow(listResults[k]);
						run("Close");
					}
					else if(endsWith(listResults[k], "png")){
						run("Open [Image IO]", "image=C:/nsworkspace/imageJ/"+listResults[k]);
						saveAs("PNG", target_dir + "/" +j+ "/"+listResults[k]);
						selectWindow(listResults[k]);
						run("Close");
					}
				}
				showProgress(i, list.length);
			}
		}
	}
}
