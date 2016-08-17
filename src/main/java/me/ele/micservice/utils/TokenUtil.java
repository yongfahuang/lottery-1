/**
 * 
 */
package me.ele.micservice.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chengjianle
 *
 */
public class TokenUtil {

	private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);

	public static final String PREFIX = "c6ded811-6e1e-4c11-bc5d-a383d0672cc8";

	/**
	 * 构造token
	 * 
	 * @return
	 */
	public static String getToken() {
		Date currentDate = new Date();

		String src = PREFIX + "|" + dateToString(currentDate);

		return DESUtil.encrypt(src);
	}

	/**
	 * 时间转换
	 * 
	 * @param value
	 * @return
	 */
	private static String dateToString(Date value) {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = null;
		try {
			date = simpledateformat.format(value);
		} catch (Exception ce) {
			logger.error("时间转换出错了", ce);
		}
		return date;
	}

	/**
	 * 获取日期
	 * 
	 * @return
	 */
	public static Date getDate(String token) {

		Date date = null;

		String[] args = DESUtil.decrypt(token).split("\\|");
		if (args.length == 2) {
			date = stringToDate(args[1]);
		}

		return date;
	}

	/**
	 * 获取前缀
	 * 
	 * @return
	 */
	public static String getPrefix(String token) {

		String prefix = null;

		String[] args = DESUtil.decrypt(token).split("\\|");
		if (args.length == 2) {
			prefix = args[0];

		}
		return prefix;
	}

	/**
	 * 时间转换
	 * 
	 * @param value
	 * @return
	 */
	private static Date stringToDate(String value) {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = simpledateformat.parse(value);
		} catch (Exception ce) {
			logger.error("时间转换出错了", ce);
		}
		return date;
	}
	
	/**
	 * 获取privilege token, 具体逻辑为，token+当前日期 md5加密
	 * 
	 * @param token
	 * @return
	 */
	public static String getPrivilegeToken(String token){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String date=sdf.format(new Date());
		String tokenStr=token+date;
		String mdtToken=EncrypUtil.string2MD5(tokenStr);
		return mdtToken;
	}


	public static boolean checkToken(String token) {
		logger.info("EHR请求的TOKEN:" + token);
		String prefix = TokenUtil.getPrefix(token);
		logger.info("prefix:" + prefix);
		if (!TokenUtil.PREFIX.equals(prefix)) {
			return false;
		}
		Date currentDate = new Date();
		logger.info("当前时间:" + currentDate);
		Date date = TokenUtil.getDate(token);

		// 当前时间后退5小时
		Date after5H = buildDate(date, -5);
		logger.info("后退五小时:" + after5H);

		// 当前时间提前5小时
		Date before5H = buildDate(date, 5);
		logger.info("提前五小时:" + before5H);
		boolean isOk = currentDate.after(after5H) && currentDate.before(before5H);
		logger.info("Token校验的结果:" + isOk);
		return isOk;
	}


	private static Date buildDate(Date date, int hours) {
		Date time = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, hours);
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			time = simpledateformat.parse(simpledateformat.format(cal.getTime()));
		} catch (ParseException e) {
			logger.error("时间转换出错了...", e);
		}
		return time;
	}


	public static void main(String[] args) {
		System.out.println(checkToken(getToken()));
	}


}
