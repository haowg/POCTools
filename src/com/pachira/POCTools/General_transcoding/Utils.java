package com.pachira.POCTools.General_transcoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Utils {


	public static void main(String[] args) throws Exception {

		String filePath = "D:\\Tools\\WordSplit";
		ArrayList<String> filelist = getFiles(filePath);
		for (String string : filelist) {
			System.out.println(string+"asfasdf");
		}
		System.out.println();
		System.out.println(getParentDir(filePath));
		System.out.println();
		System.out.println(getDir(filePath));
	}

	/*
	 * 通过递归得到某一路径下所有的目录及其文件
	 */
	public static ArrayList<String> getFiles(String filePath) {
		ArrayList<String> filelist = new ArrayList<String>();
		File root = new File(filePath);

		File[] files = root.listFiles();

		for (File file : files) {
			/*
			 * 递归调用
			 */
			try {
				if (file.isDirectory()) {
					filelist.addAll(getFiles(file.getCanonicalPath()));
				} else {
					filelist.add(file.getCanonicalPath());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return filelist;
	}
	/*
	 * 获得父路径
	 */
	public static String getParentDir (String filePath){
		
		String dirString;
		File f = new File(filePath);
		dirString = f.getParent();
		return dirString;
		
	}
	/*
	 * 得到当前路径
	 */
	public static String getDir (String filePath){
		
		String dirString;
		File f = new File(filePath);
		dirString = f.getAbsolutePath();
		return dirString;
		
	}
	
	/*
	 * 创建文件夹
	 */
	public static  void creatdir(String lostr) {
		try{
		File file = new File(lostr);
//		System.out.println(lostr);
		if (!file.exists()) {
			file.mkdirs();
		}
		}catch(Exception e){
			System.err.println("创建文件夹失败！");
		}
	}
	
	/*
	 * 读取二进制数据
	 */
	@SuppressWarnings("resource")
	public static byte[] getContent(String filePath) throws IOException {
		File file = new File(filePath);

		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.out.println("file too big...");
			return null;
		}

		FileInputStream fi = new FileInputStream(file);

		byte[] buffer = new byte[(int) fileSize];

		int offset = 0;

		int numRead = 0;

		while (offset < buffer.length

		&& (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {

			offset += numRead;

		}

		// 确保所有数据均被读取

		if (offset != buffer.length) {

			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		fi.close();

		return buffer;
	}

	public static synchronized void write(String inputString,String list) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(list,true);
			writer.write(inputString);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static List<String> readlistFile(String filePath) {
		List<String> s = new ArrayList<String>() ;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine().trim()) != null) {
					s.add(lineTxt);
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件:\t"+filePath);
				System.exit(0);
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return s;
	}
	/*
	 * 复制文件
	 */
	public static boolean fileChannelCopy(File s, File t) {

        FileInputStream fi = null;

        FileOutputStream fo = null;

        FileChannel in = null;

        FileChannel out = null;

        try {

            fi = new FileInputStream(s);

            fo = new FileOutputStream(t);

            in = fi.getChannel();//得到对应的文件通道

            out = fo.getChannel();//得到对应的文件通道

            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {

            try {

                fi.close();

                in.close();

                fo.close();

                out.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
        return true;

    }
/*
 * 删除空文件夹
 */
	public static void deleteEmptyDir(String filePath) {
		File file = new File(filePath);
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			if (subFiles != null && subFiles.length > 0) {
				for (int i = 0; i < subFiles.length; i++) {
					deleteEmptyDir(subFiles[i].getPath());
				}
			} else {
				file.delete();
			}
			if (file.isDirectory()) {
				File[] newsubFiles = file.listFiles();
				if (newsubFiles == null || newsubFiles.length == 0) {
					file.delete();
				}
			}
		}
	}
}
