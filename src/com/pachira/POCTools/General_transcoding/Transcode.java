package com.pachira.POCTools.General_transcoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transcode {
	
	private StringBuffer message = new StringBuffer();
	
	public static void main(String[] args) {
		Transcode t = new Transcode();
		t.General_trans("a.mp3",	"a.wav");
	}
	
	public HashMap<String, String> General_Detect(String filePath) {
		message.setLength(0);
		
		HashMap<String, String> map = new HashMap<String, String>();
		String cmd = "ffmpeg -i "+filePath;
		java_os(cmd);
//System.out.println(message);
		String mesageString = message.toString();
		Pattern p = Pattern.compile("Audio:.*");
		Matcher m = p.matcher(mesageString);
		if (m.find()) {
			String typeString = m.group(0).trim();
			System.out.println(typeString+"\t"+filePath);
			map.put(filePath, typeString);
		}else {
			//System.out.println("unknow\t"+filePath);
			map.put(filePath, "unknow");
		}
		
		//System.out.println(mesageString);
		
		return map;
	}
	
	public boolean General_trans(String input,String output) {
		message.setLength(0);
		
		
		if (!new File(input).exists()) {
			Utils.write(input+"\n","can_not_find.list");
			return false;
		}
		String cmd = "ffmpeg -i "+input+" -ar 8000 -ac 1 -ab 128k -y "+output;
		int i = java_os(cmd);
//System.out.println(message);
		if (i != 0) {
			/*
			 * 不能转码的文件加入失败列表
			 */
			Utils.write(input+"\n","can_not_tran.list");
			/*
			 * 删除转码失败的文件
			 */
			File f = new File(output);
			if (f.exists()) {
				if (f.isFile()) {
					f.delete();
				}
			}
			return false;
			
		}
//		System.out.println(i);
		return true;
	}
	
	/*
	 * 执行系统命令，如果失败检查是否需要指定操作系统系统
	 */
	public int java_os(String cmdStr,String os) {
		int exitVal = 0;
		cmdStr = cmdStr.trim();
		String[] comands = null;
		if (os.equals("linux")) {
			comands = new String[] { "/bin/sh", "-c", cmdStr };
		}else if (os.equals("win")) {
			comands = new String[] {"cmd.exe","/c",cmdStr};
		}
		
//System.out.println("--"+cmdStr+"--");
		InputStream stderr = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
        try {
        	Runtime rt = Runtime.getRuntime();
        	Process p ;
        	if (comands!=null) {
    			p = rt.exec(comands);
			}else {
    			p = rt.exec(cmdStr);
			}

//            System.out.println("-------------");
			exitVal += doWaitFor(p);
//			System.out.println("Process exitValue: " + exitVal);
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			
			try {
				if (stderr!=null) {
					stderr.close();
				}
				if (isr != null) {
					isr.close();
				}
				if (br !=null) {
					br.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			};
		}
		return exitVal;
	}

	public int java_os(String cmdStr){
		int i = java_os(cmdStr,"");
		return i;
	} 
	
	private int doWaitFor(Process p) {
		int exitValue = -1; // returned to caller when p is finished
		try {

			InputStream in = p.getInputStream();
			InputStream err = p.getErrorStream();
			boolean finished = false; // Set to true when p is finished

			while (!finished)
			{
				try {
					while (in.available() > 0)
					{
						// Print the output of our system call
						Character c = new Character((char) in.read());
						this.message.append(c);
						//System.out.print(c);
					}
					while (err.available() > 0)
					{
						// Print the output of our system call
						Character c = new Character((char) err.read());
						this.message.append(c);
						//System.out.print(c);
					}

					// Ask the process for its exitValue. If the process
					// is not finished, an IllegalThreadStateException
					// is thrown. If it is finished, we fall through and
					// the variable finished is set to true.
					exitValue = p.exitValue();
					finished = true;
				} catch (IllegalThreadStateException e) {
					Thread.currentThread();
					// Process is not finished yet;
					// Sleep a little to save on CPU cycles
					Thread.sleep(500);
				}
			}
		} catch (Exception e) {
			// unexpected exception! print it out for debugging...
			System.err.println("doWaitFor(): unexpected exception - "
					+ e.getMessage());
		}

		// return completion status to caller
		return exitValue;
	}
	 
}
