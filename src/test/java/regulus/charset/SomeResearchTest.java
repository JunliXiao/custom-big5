package regulus.charset;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import regulus.util.Utils;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Disabled("For the purpose of research")
public class SomeResearchTest {

    @Test
    public void charArray() {
        String uni_28CDD = "𨳝";
		String uni_2F82C = "卉";

		char[] uni_28CDD_charArr = uni_28CDD.toCharArray();
		char[] uni_2F82C_charArr = uni_2F82C.toCharArray();

		System.out.println(uni_28CDD_charArr.length);
		System.out.println(uni_2F82C_charArr.length);
    }

    @Test
    public void charBuffer() {
        CharBuffer charBuffer = CharBuffer.wrap("A".toCharArray());

		System.out.println(charBuffer.position());
		System.out.println(charBuffer.get());
		System.out.println(charBuffer.hasRemaining());

		int i = 0;
		char[] letters = new char[] {'a','b','c'};
		System.out.println(letters[++i]);
    }

    @Test
    public void deque() {
        Deque<String> deque = new ArrayDeque<String>();

        deque.add("1xxxx");
        deque.add("2ABCD");
        deque.add("2EFGH");
        deque.add("2IJKL");
        deque.add("3xxxx");

        System.out.println("deque size = " + deque.size());
        System.out.println("first element = " + deque.removeFirst());
        System.out.println("last element = " + deque.removeLast());
        System.out.println("deque size = " + deque.size());
        System.out.println(deque.remove());
        System.out.println(deque.remove());
        System.out.println(deque.remove());
        System.out.println("deque size = " + deque.size());
    }

    @Test
    public void simple() {
        Charset cs_A = MyBig5CharsetProvider.charsetForBU('A');
//		Charset cs_A = Charset.forName("MS950");
        byte[] dw_1_buA_byteArr = Utils.hexArrayToByteArray(new String[] {"A3","E1"});
        String dw_1 = new String(dw_1_buA_byteArr, cs_A);
        System.out.println(dw_1 + "<- BIG5 自造字對映到 UTF8 自造字");

        String dw_1_hexStr = Utils.fromCharToHex('').toUpperCase();
        System.out.println("dw_1_hexStr: " + dw_1_hexStr);

        byte[] dw_2_buA_byteArr = Utils.hexArrayToByteArray(new String[] {"81","46"});
        String dw_2 = new String(dw_2_buA_byteArr, cs_A);
        System.out.println(dw_2 + "<- BIG5: 8146");
        char[] dw_2_charArr = dw_2.toCharArray();
        String dw_2_hexStr = Utils.fromCharToHex(dw_2_charArr[0]).toUpperCase();
        System.out.println("dw_2_hexStr: " + dw_2_hexStr);
    }

    @Test
    public void surrogate() {
        // Big5: 8EC0 -> Unicode: E36F
		Map<String, Character[]> dw_b2ncMap = new HashMap<>();

		String big5_hex = "FAD9";
		String uni_hex = "28CDD";

		int uni_codepoint = Integer.parseInt(uni_hex, 16);
		char highSurro = Character.highSurrogate(uni_codepoint);
		char lowSurro = Character.lowSurrogate(uni_codepoint);

		char[] surrogatePair = new char[] {highSurro, lowSurro};

		dw_b2ncMap.put(big5_hex.toLowerCase(), new Character[] {surrogatePair[0], surrogatePair[1]});

		int b1 = 250;
		int b2 = 217;
		Character[] surroCharacters = dw_b2ncMap.get(Integer.toHexString(b1) + Integer.toHexString(b2));
		if (surroCharacters == null ) {
			System.out.println("NULL");
		}
		System.out.println(new char[] {surroCharacters[0], surroCharacters[1]});
    }

}
