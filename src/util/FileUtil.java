package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
/**
 * 保存每一个新增的UUID主键
 * 随机获取一个UUID主键
 * @author wsz
 * @date 2018年8月17日14:50:24
 */
public class FileUtil {

	/**
	 * 新增保持UUID的文件
	 */
	private static final String TXT_URL = "uuid.txt";
	private static final String TXT_TEMP_URL = "uuid_temp.txt";
	/**
	 * 保持被删除的UUID的文件
	 */
	private static final String DELETE_URL = "delete.txt";
	/**
	 * 随机获取一个uuid
	 * @return
	 */
	public static String getUUID() {
		String uuid = "";
		File file = new File(TXT_URL);
		BufferedReader br = null;
		try {
			List<String> uuids = new ArrayList<String>();
			br = new BufferedReader(new FileReader(file));
			String readLine = null;
			while((readLine = br.readLine()) != null) {
				uuids.add(readLine);
			}
			if(uuids.isEmpty())	return "empty";
			//随机获取
			Random random = new Random();
			uuid = uuids.get(random.nextInt(uuids.size()));
			uuid = uuid.split("----")[1];
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return uuid;
	}
	
	/**
	 * 保存uuid
	 * @param uuid
	 * @return
	 */
	public static String insertUUID(String uuid) {
		String returnStr = "";
		File file = new File(TXT_URL);
		BufferedWriter bw = null;
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			bw = new BufferedWriter (new FileWriter(file, true));
			returnStr =sdf.format(new Date())+"----"+ uuid; 
			bw.write(returnStr);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnStr;
	}
	
	/**
	 * 删除uuid
	 * @param uuid
	 * @return
	 */
	public static boolean deleteUUID(String uuid) {
		File file = new File(TXT_URL);
		BufferedReader br = null;
		String uuidStr = "";
		try {
			List<String> uuids = new ArrayList<String>();
			br = new BufferedReader(new FileReader(file));
			String readLine = null;
			while((readLine = br.readLine()) != null) {
				if(!readLine.split("----")[1].equals(uuid)) {
					uuids.add(readLine);
				}else {
					uuidStr = readLine;
				}
			}
			//先关闭流,否则无法删除
			br.close();
			file.delete();
			//临时文件存储;重新命名
			File temp = batchInsertUUID(uuids);
			if(temp != null) {
				temp.renameTo(new File(TXT_URL));
				saveDeletedUUID(uuidStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * 批量添加uuid:
	 * 1.使用中间临时文件
	 * @param uuids
	 * @return
	 */
	public static File batchInsertUUID(List<String> uuids) {
		File file = null;
		BufferedWriter bw = null;
		try {
			file = new File(TXT_TEMP_URL);
			file.delete();
			file.createNewFile();
			bw = new BufferedWriter (new FileWriter(file, true));
			for(String uuid : uuids) {
				bw.write(uuid);
				bw.newLine();
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	/**
	 * 保存被删除后的UUID
	 * @param uuidStr
	 * @return
	 * @ date 2018年8月20日
	 */
	public static boolean saveDeletedUUID(String uuidStr) {
		if("".equals(uuidStr))	return false;
		File file = new File(DELETE_URL);
		BufferedWriter bw = null;
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS");
			bw = new BufferedWriter (new FileWriter(file, true));
			bw.write("("+sdf.format(new Date())+")"+uuidStr);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
//		System.out.println(insertUUID("哈哈"));
//		System.out.println(getUUID());
//		List<String> uuids = new ArrayList<String>();
//		uuids.add("a");
//		uuids.add("b");
//		uuids.add("c");
//		System.out.println(batchInsertUUID(uuids));
		System.out.println(deleteUUID("d33615ea739b4f50b6569cb9effa867e"));
	}
}
