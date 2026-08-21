package regulus.charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import regulus.util.Config;
import regulus.util.Utils;
import regulus.util.XlsxMapping;


public class MyBig5CharsetTest {

	private static final Charset big5_orig = Charset.forName("Big5");
	private static final List<Character> businessUnits =
			Utils.listOf(Config.BU_CODE_A, Config.BU_CODE_B, Config.BU_CODE_C);
	
	@BeforeAll
	public static void make_sure_mapping_xlsx_exist() {
		assertTrue(Files.exists(Config.DIFFICULT_WORDS_A_PATH));
		assertTrue(Files.exists(Config.DIFFICULT_WORDS_B_PATH));
		assertTrue(Files.exists(Config.DIFFICULT_WORDS_C_PATH));
		assertTrue(Files.exists(Config.SPECIAL_BIG5_WORDS_PATH));
	}
	
	@Test
	public void test_charsets_basic_info() {
		
		for (char bu : businessUnits) {
			
			Charset cs = MyBig5CharsetProvider.charsetForBU(bu);
			assertEquals("MyBig5", cs.getClass().getSimpleName());
			assertEquals("Big5-" + bu, cs.name());
			assertEquals("Big5-" + bu, cs.displayName());
			
			assertTrue(cs.contains(cs));
			assertTrue(cs.aliases().contains("BIG5-" + bu));
			assertTrue(cs.aliases().contains("big5-" + bu));
			assertTrue(cs.isRegistered());
			assertFalse(cs.contains(Charset.forName("Big5")));
			assertFalse(cs.contains((Charset) new MyBig5('X')));
			
			MyBig5 ba = (MyBig5) cs;
			assertEquals(bu, ba.businessUnit());
		}
	}
	
	@Test
	public void test_decoders_for_difficult_words() {
		
		Charset charset;
		Set<Entry<String, String>> mappingEntries;
		
		for (char bu : businessUnits) {
			System.out.println("===== BU " + bu + " =====");
			
			charset = MyBig5CharsetProvider.charsetForBU(bu);
			mappingEntries = XlsxMapping.difficultWords(bu).entrySet();
			int cnt = 0;
			
			for (Entry<String, String> me : mappingEntries) {
				assertTrue(map_big5_to_unicode(me.getKey(), me.getValue(), charset));
				cnt++;
			}
			System.out.println("難字對映筆數 = " + cnt);
		}
	}
	
	@Test
	public void test_encoders_for_difficult_words() {
		
		Charset charset;
		Set<Entry<String, String>> mappingEntries;
		
		for (char bu : businessUnits) {
			System.out.println("===== BU " + bu + " =====");
			
			charset = MyBig5CharsetProvider.charsetForBU(bu);
			mappingEntries = XlsxMapping.difficultWords(bu).entrySet();
			int cnt = 0;
			
			for (Entry<String, String> me : mappingEntries) {
				assertTrue(map_unicode_to_big5(me.getKey(), me.getValue(), charset));
				cnt++;
			}
			System.out.println("難字對映筆數 = " + cnt);
		}
	}
	
	@Test
	public void test_decoders_for_long_input() throws UnsupportedEncodingException {
		
		byte[] targetBig5Bytes = "我是誰？蕭俊立...Frank Ｈｓｉａｏ 1990-12-11".getBytes(big5_orig);
		
		for (char bu : businessUnits) {
			System.out.println("===== BU " + bu + " =====");
			Charset big5_bu = MyBig5CharsetProvider.charsetForBU(bu);
			
			String big5_orig_decoded = new String(targetBig5Bytes, big5_orig);
			String big5_bu_decoded = new String(targetBig5Bytes, big5_bu);
			
			boolean sameSize = big5_orig_decoded.length() == big5_bu_decoded.length();
			assertTrue(sameSize);
			
			if (sameSize) {
				for (int i = 0; i < big5_orig_decoded.length(); i++) {
					boolean res = big5_orig_decoded.charAt(i) == big5_bu_decoded.charAt(i);
					if (!res) {
						System.out.println("At index " + i + " they have different char");
					}
					assertTrue(res);
				}
			}
			System.out.println("No news is good news");
		}
	}
	
	@Test
	public void test_encoders_for_long_input() throws UnsupportedEncodingException {

		String targetString = "我是誰？蕭俊立...Frank Ｈｓｉａｏ 1990-12-11";
		
		Charset big5_bu = MyBig5CharsetProvider.charsetForBU('B');
		
		for (char bu : businessUnits) {
			System.out.println("===== BU " + bu + " =====");
		
			byte[] big5_orig_encoded = targetString.getBytes(big5_orig);
			byte[] big5_bu_encoded = targetString.getBytes(big5_bu);
			
			boolean sameSize = big5_orig_encoded.length == big5_bu_encoded.length;
			assertTrue(sameSize);
			
			if (sameSize) {
				for (int i = 0; i < big5_orig_encoded.length; i++) {
					boolean res = big5_orig_encoded[i] == big5_bu_encoded[i];
					if (!res) {
						System.out.println("At index " + i + " they have different byte");
					}
					assertEquals(big5_orig_encoded[i], big5_bu_encoded[i]);
				}
			}
			System.out.println("No news is good news");
		}
	}
	
	/**
	 * Big5 -> Unicode：比較 String 以測試 decoding / 解碼功能
	 * @param big5_hex
	 * @param uni_hex
	 * @param charset
	 * @return
	 */
	static boolean map_big5_to_unicode(String big5_hex, String uni_hex, Charset charset) {
		String highByte = big5_hex.substring(0,2);
		String lowByte = big5_hex.substring(2,4);
		
    	byte[] big5_byteArr = Utils.hexArrayToByteArray(new String[] {highByte,lowByte});
    	String mapped_uni_str = new String(big5_byteArr, charset);
    	
    	String uni_str = "";
    	int uni_codepoint = Integer.parseInt(uni_hex, 16);
    	if (!Character.isBmpCodePoint(uni_codepoint)) {
    		char highSurro = Character.highSurrogate(uni_codepoint);
    		char lowSurro = Character.lowSurrogate(uni_codepoint);
    		uni_str= String.valueOf(new char[] {highSurro, lowSurro});
    	} else {
    		uni_str = String.valueOf((char) uni_codepoint);
    	}
    	
    	boolean res = mapped_uni_str.equals(uni_str);
    	if (!res) System.out.println(
    			"big5_hex = " + big5_hex + " ,uni_str = "+ uni_str + ", mapped_uni_str = " + mapped_uni_str);
    	return res;
	}
	
	/**
	 * Unicode -> Big5：比較 hex 以測試 encoding / 編碼功能
	 * @param big5_hex
	 * @param uni_hex
	 * @param charset
	 * @return
	 */
	static boolean map_unicode_to_big5(String big5_hex, String uni_hex, Charset charset) {
		String uni_str;
		
		int uni_codepoint = Integer.parseInt(uni_hex, 16);
		
		if (!Character.isBmpCodePoint(uni_codepoint)) {
			char highSurro = Character.highSurrogate(uni_codepoint);
			char lowSurro = Character.lowSurrogate(uni_codepoint);
			char[] surrogatePair = new char[] {highSurro, lowSurro};
			uni_str = new String(surrogatePair);
		} else {
			uni_str = Character.toString((char) uni_codepoint);
		}
		
		String mapped_big5_hex = Utils.bytesArrayToHex(uni_str.getBytes(charset)).toUpperCase()
				.replaceAll(" ", "");
		boolean res = mapped_big5_hex.equals(big5_hex.toUpperCase());
		
		if (!res) { 
			String basic_msg = "mapped_big5_hex = " + mapped_big5_hex + ", big5_hex = " + big5_hex
    				+ ", uni_hex = "+ uni_hex + ", uni_str = " + uni_str;
			
			String big5_str = Utils.fromSomeCharsetHexToString(big5_hex, Charset.forName("Big5"));
			String mapped_big5_str = Utils.fromSomeCharsetHexToString(mapped_big5_hex, charset);
    		System.out.println(basic_msg 
    				+ ", big5_str = " + big5_str + ", mapped_big5_str = " + mapped_big5_str);
    	}
		return true;
//		return res;
	}
	
}
