package regulus.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UtilTest {
	
	@Test
	public void test_IsHexadecimal_and_only_4_digits() {
		// Expect returning TRUE
		String input_good_1 = "FA4C";
		String input_good_2 = "683e";
		
		// Expect returning FALSE
		String input_bad_1 = "#A123";
		String input_bad_2 = "C67W";
		String input_bad_3 = "";
		String input_bad_4 = null;
		String input_bad_5 = "2683e";
		
		assertAll("Utils.isHexadecimal_4digit",
			() -> assertEquals(true, Utils.isHexadecimal_4digit(input_good_1), "input_good_1"),
			() -> assertEquals(true, Utils.isHexadecimal_4digit(input_good_2), "input_good_2"),
			() -> assertEquals(false, Utils.isHexadecimal_4digit(input_bad_1), "input_bad_1"),
			() -> assertEquals(false, Utils.isHexadecimal_4digit(input_bad_2), "input_bad_2"),
			() -> assertEquals(false, Utils.isHexadecimal_4digit(input_bad_3), "input_bad_3"),
			() -> assertEquals(false, Utils.isHexadecimal_4digit(input_bad_4), "input_bad_4"),
			() -> assertEquals(false, Utils.isHexadecimal_4digit(input_bad_5), "input_bad_5")
		);
	}

}
