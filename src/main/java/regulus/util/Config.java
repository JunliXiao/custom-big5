package regulus.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

	private static final String root_dir = "F:/mapping_data";

	/** 存放 BU_A 專屬的難字對映 **/
	public static final Path DIFFICULT_WORDS_A_PATH =
			Paths.get(root_dir, "DifficultWords","BU_A.xlsx");

	/** 存放 BU_B 專屬的難字對映 **/
	public static final Path DIFFICULT_WORDS_B_PATH =
			Paths.get(root_dir, "DifficultWords","BU_B.xlsx");

	/** 存放 BU_C 專屬的難字對映 **/
	public static final Path DIFFICULT_WORDS_C_PATH =
			Paths.get(root_dir, "DifficultWords","BU_C.xlsx");

	/** 存放各 BU 共用的特殊字對映 **/
	public static final Path SPECIAL_BIG5_WORDS_PATH = 
			Paths.get(root_dir, "SpecialBig5Words","SpecialBig5Words.xlsx");

	public static final char BU_CODE_A = 'A';

	public static final char BU_CODE_B = 'B';

	public static final char BU_CODE_C = 'C';
	
}
