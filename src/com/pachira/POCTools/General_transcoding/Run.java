package com.pachira.POCTools.General_transcoding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Run {
	
	private static int i = 0;
	private static ArrayList<String> outputFiles = new ArrayList<String>();
	static HashMap<String, String> map = new HashMap<String, String>();
	
	public static void main(String[] args) {
		String usage = "usage:\tjava -jar xxx.jar order[1\\2\\3] threadnum inputdir(in) [outdir(out)]  ";
		int order = 0;
		String outputPath = ".";
		try {
			order = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.err.println("命令格式错误！请重试");
			System.err.println(usage);
			System.exit(-1);
		}
		if ((order==1&&args.length<3)||(order!=1&&args.length<4)) {
			System.out.println(usage);
			System.exit(0);
		}
		String inputPath = args[2];
		if (order != 1) {
			outputPath = args[3];
		}
		
		
		int threadnum = 1;
		try {
			threadnum = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("threadnum "+threadnum+" is illegle");
			System.exit(-1);
		}
		
		Run runner = new Run();
		ArrayList<String> inputFiles = Utils.getFiles(inputPath);
//System.out.println(inputFiles);
//		if (order == 3) {
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//			String startTime = df.format(new Date());
//			Utils.write(startTime+"\n", "can_not_tran.list");
//		}
		ExecutorService pool = Executors.newFixedThreadPool(threadnum);
		
		for (int i = 0; i < threadnum + 10; i++) {
			pool.execute(runner.new transer(inputFiles, inputPath, outputPath,order));
//System.out.println("join to pool :"+inputFiles);

		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * 统计结果
		 */
		if (order == 3) {
			countOutput(outputPath);
		}else if (order == 2) {
			countDetect(order,inputPath);
			copyFiles(inputPath,outputPath);
		}else{
			countDetect(order,inputPath);
		}
	}
	
	private static void copyFiles(String inputPath, String outputPath) {
		String absInputPath,absOutputPath;
		try {
			absInputPath = new File(inputPath).getCanonicalPath();
			absOutputPath = new File(outputPath).getCanonicalPath();
			Pattern   p   =   Pattern.compile("[\\W]");     
			for (String file : map.keySet()) {
				String typeString = map.get(file);
		        Matcher   m   =   p.matcher(typeString); 
		        String typeDirString = m.replaceAll("_").trim();
		        String classifiedFilePath = file.replace(absInputPath, absOutputPath+"/"+typeDirString);
		        Utils.creatdir(new File(classifiedFilePath).getParent());
		        Utils.fileChannelCopy(new File(file), new File(classifiedFilePath));
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

	private static void countDetect(int order,String inputDir) {
		LinkedHashSet<String> valueset =  new LinkedHashSet<String>();
		Transcode tc = new Transcode();
		String testWav = "pachira_test_-_x.wav";
		StringBuffer sb = new StringBuffer();
		for (String value : map.values()) {
			valueset.add(value);
		}
		for( String value : valueset){
			HashSet<String> fileSet = new HashSet<String>();
			for (String key : map.keySet()) {
				if (map.get(key).equals(value)) {
					fileSet.add(key);
				}
			}
			String wouldTrans = "";
			Iterator<String> it = fileSet.iterator();
//System.out.println(value+value.equals("unknow")+"unknow");
			
			if ((!value.equals("unknow"))&&tc.General_trans(it.next(),testWav )) {
				wouldTrans = "\t可以直接转码";
			}
			String s ="\n"+ value+"\n\t:一共"+fileSet.size()+"个"+wouldTrans+"\n";
			System.out.print(s);
			sb.append(s);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String startTime = df.format(new Date());
		Utils.write("\n###############\t"+startTime+"\t################\n", "DetectCount");
		Utils.write(sb.toString(), "DetectCount");
		File xwav = new File(testWav);
		if (xwav.exists()) {
			xwav.delete();
		}
		File listfile = new File("can_not_tran.list");
		if (listfile.isFile()) {
			listfile.delete();
		}
		
	}

	private static void countOutput(String outputPath) {
		//ArrayList<String> outputFiles = Utils.getFiles(outputPath);
		int num = outputFiles.size();
		long datasize = 0;
		for (String filePath : outputFiles) {
			File f = new File(filePath);
			if (f.exists()&&f.isFile()) {
				datasize += (f.length()-44);
			}
		}
		long time = datasize/16000;
		String timeString = getTime(time);
		System.out.println("文件个数:\t"+num);
		//System.out.println(time);
		System.out.println("总时长:\t"+timeString);
		String countResultString = "统计结果:\n"+
									"文件个数:\t"+num+
									"\n总时长:\t"+timeString;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String startTime = df.format(new Date());
		Utils.write("#############"+startTime+"###############\n", outputPath+"/outputCount");
		Utils.write(countResultString, outputPath+"/outputCount");
		Utils.deleteEmptyDir(outputPath);
	}

	private static String getTime(long time) {
		long day = time / (24 * 3600);
		long hour = time % (24 * 3600) / 3600;
		long minute = time % 3600 / 60;
		long second = time % 60;
		return "" + day + "天" + hour + "小时" + minute + "分" + second + "秒";
	}

	public synchronized String getwavloc(List<String> inputFiles) {
		if (i >= inputFiles.size())// 读完的话就返回null，暂时决定
			return null;
		String str = inputFiles.get(i);
		i++;
		return str;
	}
	
	class transer implements Runnable {
		
		Transcode t = new Transcode();
		private List<String> inputFiles;
		String outDir = "";
		String inDir = "";
		String acodec = "";
		int order = 0;
		
		transer(List<String> inputFiles,String inDir,String outDir,int order) {
			this(inputFiles,inDir,outDir,order,"");
		}
		
		transer(List<String> inputFiles,String inDir,String outDir,int order,String acodec) {
//System.out.println(inputFiles);
			this.inputFiles = inputFiles;
//System.out.println(this.toString());
//System.out.println(this.inputFiles);
			this.outDir = outDir;
			this.inDir = inDir;
			this.acodec = acodec;
			this.order = order;
		}

		public void run() {
//System.out.println(this.inDir);			
//System.out.println(this.toString());
//System.out.println(this.inputFiles);
			String filePath;
			while ((filePath = getwavloc(inputFiles))!=null) {
				try {
					if (order == 3) {
						String outputFile = getOutputFile(filePath);
						if(t.General_trans(filePath, outputFile)){
							
//							System.out.println(outputFile);
							outputFiles.add(outputFile);
						}
					}else {
						//System.out.println("detect.................");
						map.putAll(t.General_Detect(filePath));
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private String getOutputFile(String filePath) {
			String absInputPath;
			String absOutputPath = null;
			try {
				absInputPath = new File(filePath).getCanonicalPath();
				//File absOutFile = new File(outDir).getCanonicalFile();
				String absOutdir = null,absIndir = null;

				absOutdir = new File(outDir).getCanonicalPath();
				absIndir = new File(inDir).getCanonicalPath();
				
				//System.out.println(absIndir+"\n"+absOutdir+"\n"+absInputPath);
				/*
				 * 获得输出文件名
				 */
				//absOutputPath = absInputPath.replace(absIndir, absOutdir)+".wav"; 
				File absOutFile = new File(absInputPath.replace(absIndir, absOutdir)); //替换输入路径和输出路径
				String outFileParentDir = absOutFile.getParent();
				String outFileParentName = absOutFile.getName();
				Pattern pattern = Pattern.compile(".*\\.[^\\.]*$");
				Matcher matcher = pattern.matcher(outFileParentName);
				String newname = "";
				if (matcher.matches()) {
					//newname = matcher.replaceAll(".wav");
					newname = outFileParentName.replaceAll("\\.[^\\.]*$", ".wav");
				}else {
					newname = outFileParentName+".wav";
				}
				absOutputPath = outFileParentDir+"/"+newname;
//System.out.println(newname);
				Utils.creatdir(outFileParentDir);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return absOutputPath;
		}
	}
}
