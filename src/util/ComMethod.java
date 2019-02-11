package util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 坐标处理通用工具类
 */
public class ComMethod {
	/**
	 * 判断地理坐标是否合法
	 * @param longitude 经度
	 * @param latitude 纬度
	 * @return bool
	 */
	public static boolean isValidPoint(double longitude, double latitude) {
		return (longitude > 70.0 && longitude < 140.0) && (latitude > 3.0 && latitude < 60.0);
	}

	/**
	 * 获取Bean实体类属性名,统一转大写
	 * @param originClass 类名
	 * @return 属性名数组
	 */
	@SuppressWarnings("rawtypes")
	public static String[] getBeanFields(Class originClass) {
		if(null == originClass) return null;
		Field[] fields = originClass.getDeclaredFields();
		String[] results = new String[fields.length];
		int i = 0;
		for(Field field : fields) {
			results[i++] = field.getName().toUpperCase();
		}
		return  results;
	}

	/**
	 * 根据经纬度获取距离
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return
	 */
	public static double getSphericalDistance(double startX, double startY, double endX, double endY) {
		Coordinate startPt = new Coordinate(startX, startY);
		Coordinate endPt = new Coordinate(endX, endY);

		double EarthRadius = 6378.137;
		startPt = webMercatorToGeographic(startPt);
		endPt = webMercatorToGeographic(endPt);
		double lon1 = startPt.x / 180 * Math.PI;
		double lon2 = endPt.x / 180 * Math.PI;
		double lat1 = startPt.y / 180 * Math.PI;
		double lat2 = endPt.y / 180 * Math.PI;
		double distance = 2 * Math.asin(Math.sqrt(Math.pow((Math.sin((lat1 - lat2) / 2)), 2) +
				Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon1 - lon2) / 2), 2))) * EarthRadius * 1000;

		return (double)(Math.round(distance*1)/1.0);
	}

	/**
	 * web墨卡托转地理坐标
	 */
	public static Coordinate webMercatorToGeographic(Coordinate coord) {
		if (coord == null) {
			return null;
		}
		if(isGeoCoordinate(coord)) {
			return coord;
		}
		double x = coord.x;
		double y = coord.x;
		double num3 = x / 6378137.0;
		double num4 = num3 * 57.295779513082323;
		double num5 = Math.floor((double) ((num4 + 180.0) / 360.0));
		double num6 = num4 - (num5 * 360.0);
		double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));
		return new Coordinate(num6 + (num5 * 360.0), num7 * 57.295779513082323);
	}

	/**
	 * 判断是否是地理坐标系
	 */
	private static boolean isGeoCoordinate(Coordinate coord) {
		if(coord == null) {
			return false;
		}

		if(coord.x >= -180 && coord.x <= 180 && coord.y >= -90 && coord.y <= 90) {
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = 0 ;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					count = count + 1;
				}
				chLength++;
			}
		}
		float result = count / chLength ;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 字符串转码，处理中文输入
	 * @param str
	 * @return
	 */
	public static String encodeStr(String str) {
		try {
			if(isMessyCode(str)) {
				return new String(str.getBytes("ISO-8859-1"), "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}


	/**
	 * 字符串转码，处理中文输入
	 * @param str
	 * @return
	 */
	public static String[] encodeStrs(String[] str) {
		String[] res = new String[str.length];
		for(int i = 0; i < str.length; i++) {
			res[i] = encodeStr(res[i]);
		}
		return res;
	}

	/**
	 * 判断输入字符串是否为指定时间格式
	 * @param str
	 * @param format
	 * @return
	 */
	public static boolean isValidDate(String str, SimpleDateFormat format) {
		boolean convertSuccess = true;

		try {
			// 设置lenient为false.
			// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// e.printStackTrace();
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}

	/**
	 * 判断输入字符串是否为yyyy-MM-dd HH:mm:ss格式
	 * @param str
	 * @return
	 */
	public static boolean isValidDate(String str) {
		return isValidDate(str, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 * 获取uuid
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-","");
	}

	/**
	 * 将json串key转换为大写
	 */
	public static String convertUpperCaseJson(JSONObject jsonObject, boolean removeEmptyString) {
		HashMap<String, Object> result = new HashMap<>();
		for(String key : jsonObject.keySet()) {
			if(null != jsonObject.get(key)) {
				if(removeEmptyString && jsonObject.get(key) instanceof String
						&& ((String) jsonObject.get(key)).isEmpty()) {
					continue;
				}
				result.put(key.toUpperCase(), jsonObject.get(key));
			}
		}
		return JSON.toJSONString(result);
	}
}
