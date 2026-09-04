package regulus.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxMapping {
	
	private static Map<String, String> difficultWords_buA;
	private static Map<String, String> difficultWords_buB;
	private static Map<String, String> difficultWords_buC;
	
	private static int[][] b2cMapping_buA;
	private static int[][] b2cMapping_buB;
	private static int[][] b2cMapping_buC;
	
	private static final Map<String, Character[]> b2ncMap_buA = new HashMap<>();
	private static final Map<String, Character[]> b2ncMap_buB = new HashMap<>();
	private static final Map<String, Character[]> b2ncMap_buC = new HashMap<>();
	
	private static Map<Character, Character> c2bMap_buA;
	private static Map<Character, Character> c2bMap_buB;
	private static Map<Character, Character> c2bMap_buC;
	
	private static final Map<String, Byte[]> nc2bMap_buA = new HashMap<>();
	private static final Map<String, Byte[]> nc2bMap_buB = new HashMap<>();
	private static final Map<String, Byte[]> nc2bMap_buC = new HashMap<>();
	
	static {
		
		try {
			// 載入各 BU 難字(自造字)
			difficultWords_buA = processXlsxForMapping(Config.DIFFICULT_WORDS_A_PATH, true);
			difficultWords_buB = processXlsxForMapping(Config.DIFFICULT_WORDS_B_PATH, true);
			difficultWords_buC = processXlsxForMapping(Config.DIFFICULT_WORDS_C_PATH, true);
			
			// 載入 Big5 特殊字, 各 BU 共用
			Map<String, String> specialBig5Words = processXlsxForMapping(Config.SPECIAL_BIG5_WORDS_PATH, false);
			
			// 特殊字補充至各 BU 難字
			for (Entry<String, String> e : specialBig5Words.entrySet()) {
				String big5Code = e.getKey();
				String utf16Code = e.getValue();
				
				difficultWords_buA.put(big5Code, utf16Code);
				difficultWords_buB.put(big5Code, utf16Code);
				difficultWords_buC.put(big5Code, utf16Code);
			}
			
			// 針對 Decoder 預處理難字對映
			b2cMapping_buA = processMappingFor_b2c(difficultWords_buA, b2ncMap_buA);
			b2cMapping_buB = processMappingFor_b2c(difficultWords_buB, b2ncMap_buB);
			b2cMapping_buC = processMappingFor_b2c(difficultWords_buC, b2ncMap_buC);
			
			// 針對 Encoder 預處理難字對映
			c2bMap_buA = processMappingFor_c2b(difficultWords_buA, nc2bMap_buA);
			c2bMap_buB = processMappingFor_c2b(difficultWords_buB, nc2bMap_buB);
			c2bMap_buC = processMappingFor_c2b(difficultWords_buC, nc2bMap_buC);
			
		} catch (IOException e) {
			System.err.println("難字表初始化失敗, 請確認難字表存在及內容正確性");
			e.printStackTrace();
		}
	}
	
	public static Map<String, String> difficultWords(char bu) {
		switch (bu) {
            case Config.BU_CODE_B: return difficultWords_buB;
			case Config.BU_CODE_C: return difficultWords_buC;
            case Config.BU_CODE_A:
            default: return difficultWords_buA;
		}
	}
	
	public static int[][] b2cMapping(char bu) {
		switch (bu) {
            case Config.BU_CODE_B: return b2cMapping_buB;
			case Config.BU_CODE_C: return b2cMapping_buC;
            case Config.BU_CODE_A:
            default: return b2cMapping_buA;
		}
	}
	
	public static Map<String, Character[]> b2ncMap(char bu) {
		switch (bu) {
            case Config.BU_CODE_B: return b2ncMap_buB;
			case Config.BU_CODE_C: return b2ncMap_buC;
            case Config.BU_CODE_A:
            default: return b2ncMap_buA;
		}
	}
	
	public static Map<Character, Character> c2bMap(char bu) {
		switch (bu) {
            case Config.BU_CODE_B: return c2bMap_buB;
			case Config.BU_CODE_C: return c2bMap_buC;
            case Config.BU_CODE_A:
            default: return c2bMap_buA;
		}
	}
	
	public static Map<String, Byte[]> nc2bMap(char bu) {
		switch (bu) {
            case Config.BU_CODE_B: return nc2bMap_buB;
			case Config.BU_CODE_C: return nc2bMap_buC;
            case Config.BU_CODE_A:
            default: return nc2bMap_buA;
		}
	}
	
	private static Map<String, String> processXlsxForMapping(Path xlsxPath,
			boolean difficultWords) throws IOException {
				
		try(InputStream is = Files.newInputStream(xlsxPath.toFile().toPath());
            Workbook wb = new XSSFWorkbook(is)) {
			
			if (difficultWords) {
				return processSheetForMapping(wb, 1, 3);
			} else { // SpecialBig5Words/SpecialBig5Words.xlsx
				return processSheetForMapping(wb, 1, 2);
			}
		}
	}
	
	private static Map<String, String> processSheetForMapping(Workbook wb, 
			int colNo_Big5, int colNo_Utf16) {
		
		Map<String, String> mapping = new HashMap<>();
		
		Sheet sheet = wb.getSheetAt(0);
		// RowNum is 0-based, but skipping 0 to skip header 
		for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
			Row row = sheet.getRow(i);
			String big5Code = "";
			String utf16Code = "";
			try {
				big5Code = Utils.allTrim(row.getCell(colNo_Big5).getStringCellValue());
				utf16Code = Utils.allTrim(row.getCell(colNo_Utf16).getStringCellValue());
			} catch (RuntimeException e) {
				// 儲存格內容非字串或為 null;
			}
			
			// 確認為十六進位字串, 亦排除空字串和 null; 此處需確保正確性, 後續不再有任何檢核!
			if (Utils.isHexadecimal_4digit(big5Code) && Utils.isHexadecimal_4digitOr5(utf16Code)) {
				mapping.put(big5Code, utf16Code);	
			}
		}
		
		return mapping;
	}

	private static int[][] processMappingFor_b2c(Map<String, String> buDifficultWords,
			Map<String, Character[]> b2ncMap_bu) {
		
		int[][] b2cMapping = new int [buDifficultWords.size()][3];
		int mappingIndex = 0;

    	for (Entry<String, String> entry : buDifficultWords.entrySet()) {
    		// Big5 -> Unicode
    		String big5_hex = entry.getKey();
    		String uni_hex = entry.getValue();
    		
    		if (uni_hex.length() == 5) {
    			// Out of UTF-16 range
    			String big5_hex_lowercase = big5_hex.toLowerCase();
    			int uni_codepoint = Integer.parseInt(uni_hex, 16);
    			char highSurrogate = Character.highSurrogate(uni_codepoint);
    			char lowSurrogate = Character.lowSurrogate(uni_codepoint);
    			
    			b2ncMap_bu.put(big5_hex_lowercase, new Character[] {highSurrogate, lowSurrogate});
    		} else {
    			// Within UTF-16 range
    			// b1 for big5_hex
        		b2cMapping[mappingIndex][0] = Integer.parseInt(big5_hex.substring(0,2), 16);
        		// b2 for big5_hex
        		b2cMapping[mappingIndex][1] = Integer.parseInt(big5_hex.substring(2,4), 16);
        		// ch for utf16_hex
        		b2cMapping[mappingIndex][2] = Integer.parseInt(uni_hex, 16);
        		
        		mappingIndex++;
    		}
    	}
    	
    	return b2cMapping;
	}
	
	private static Map<Character, Character> processMappingFor_c2b(
			Map<String, String> buDifficultWords,
			Map<String, Byte[]> nc2bMap_bu) {
		
		Map<Character, Character> c2bMap = new HashMap<>(
				buDifficultWords.size() * 2, 0.55f);
    	
    	for (Entry<String, String> entry : buDifficultWords.entrySet()) {
    		// Unicode -> Big5
    		String big5_hex = entry.getKey();
    		String uni_hex = entry.getValue();
    		
			if (uni_hex.length() == 5) {
    			// Out of Unicode BMP range
    			String uni_hex_lowerCase = Utils.fromHexToBytesHexDB(uni_hex);
    			byte highByte = Utils.hexToSignedByte(big5_hex.substring(0, 2));
    			byte lowByte = Utils.hexToSignedByte(big5_hex.substring(2, 4));
    			Byte[] big5_bytes = new Byte[] {highByte, lowByte};
    			
    			nc2bMap_bu.put(uni_hex_lowerCase, big5_bytes);
    		} else {
    			// Within Unicode BMP range
    			Character uni_ch = Utils.fromHexToCharSB(uni_hex);
    			Character big5_ch = Utils.fromHexToCharSB(big5_hex);
    			
    			c2bMap.put(uni_ch, big5_ch);
    		}
    	}
    	
    	return c2bMap;
	}
	
}
