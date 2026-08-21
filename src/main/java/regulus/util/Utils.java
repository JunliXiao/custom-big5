package regulus.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	/**
	 * Basic validation for Big5/UTF-16 codes
	 */
	private static final Pattern PATTERN_HEXADECIMAL = Pattern.compile("\\p{XDigit}+");
	
	public static String repeatString(String str, int size) {
		if ( size <= 0 ) return str;
		StringBuilder sb = new StringBuilder(); 
		for ( int i = 0; i < size; i++)  {
			sb.append(str);
		}
		return sb.toString();
	}
	
	public static String fillSpace(int size) {
		return repeatString(" ", size);
	}
	
	public static String leftPadding(String source, String paddingElement, int expectedSize) {
		if ( source.length() >= expectedSize ) {
			return source;
		}
		return repeatString(paddingElement, expectedSize - source.length()) + source;
	}
	
	public static String leftPadZero(String source, int expectedSize) {
		return leftPadding(source, "0", expectedSize);
	}

	public static boolean isHexadecimal_4digit(String input) {
		if (input == null || input.length() != 4) { return false; }
	    final Matcher matcher = PATTERN_HEXADECIMAL.matcher(input);
	    return matcher.matches();
	}
	
	public static boolean isHexadecimal_4digitOr5(String input) {
		if (input == null || input.length() < 4 || input.length() > 5) { return false; }
	    final Matcher matcher = PATTERN_HEXADECIMAL.matcher(input);
	    return matcher.matches();
	}

	public static String signedByteToHex(byte b) {
		return Integer.toHexString(b & 0xFF).toUpperCase();
	}
	
	public static String bytesArrayToHex(byte[] b_arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b_arr.length; i++) {
			String hexCode = signedByteToHex(b_arr[i]).length() == 1 ? "0" + signedByteToHex(b_arr[i]) : signedByteToHex(b_arr[i]); 
			sb.append(hexCode).append(" ");
		}
		return sb.toString();
	}
	
	public static byte hexToSignedByte(String twoDigitHex) {
		// No validation currently
		return (byte) (
				(Character.digit(twoDigitHex.charAt(0), 16) << 4)
				+ Character.digit(twoDigitHex.charAt(1), 16)
		);
	}
	
	public static byte[] hexArrayToByteArray(String[] arrayOfTwoDigitHex) {
		byte[] byteArr = new byte[arrayOfTwoDigitHex.length];
		for (int i = 0; i < arrayOfTwoDigitHex.length; i++) {
			byteArr[i] = hexToSignedByte(arrayOfTwoDigitHex[i]);
		}
		return byteArr;
	}
	
	public static String fromBig5HexToString(String hex) {
		if (hex == null || hex.length() != 4) { return ""; }
		try {
			String[] arr = new String[2];
			arr[0] = hex.substring(0,2);
			arr[1] = hex.substring(2,4);
			return new String(hexArrayToByteArray(arr), "Big5");
		} catch (Exception e) {
			return "charset not found";
		}
	}
	
	public static String fromSomeCharsetHexToString(String hex, Charset charset) {
		if (hex == null || hex.length() != 4) { return ""; }
		String[] arr = new String[2];
		arr[0] = hex.substring(0,2);
		arr[1] = hex.substring(2,4);
		return new String(hexArrayToByteArray(arr), charset);
	}
	
	public static char fromHexToCharSB(String hex) {
		return (char) Integer.parseInt(hex, 16);
	}
	
	public static String fromHexToStringSB(String hex) {
		return Character.toString(fromHexToCharSB(hex));
	}
	
	public static String fromCharToHex(char ch) {
		return String.format("%04x", (int) ch);
	}
	
	/**
	 * 將 double-byte Unicode 字的 codepoint hex 轉換成 bytes[] hex
	 * @param hex
	 * @return
	 */
	public static String fromHexToBytesHexDB(String hex) {
		int codepoint = Integer.parseInt(hex, 16);
		return Integer.toHexString(Character.highSurrogate(codepoint)) 
				+ Integer.toHexString(Character.lowSurrogate(codepoint));
	}
	
	/**
	 * Creates an immutable list of input elements, like List.of since Java 9 
	 * @param <T>
	 * @param elements
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> listOf(T... elements) {
	    List<T> list = new ArrayList<>();
	    for (T e : elements)
	        list.add(e);
	    return Collections.unmodifiableList(list);
	}

	public static String allTrim(String s) {
		return s == null ? null : s.replaceAll("[ |　]", "");
	}
	
}
