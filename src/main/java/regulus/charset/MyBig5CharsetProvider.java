package regulus.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.List;

import regulus.util.Utils;

import static regulus.util.Config.*;

/**
 * 提供各 BU 依各自難字表擴充的 Big5 字符集：A、B、C。
 */
public class MyBig5CharsetProvider extends CharsetProvider {
	
	private static final Charset CHARSET_A = new MyBig5(BU_CODE_A);
	private static final Charset CHARSET_B = new MyBig5(BU_CODE_B);
	private static final Charset CHARSET_C = new MyBig5(BU_CODE_C);

	private static final List<Charset> BU_CHARSETS = Utils.listOf(CHARSET_A, CHARSET_B, CHARSET_C);
	
    @Override public Iterator<Charset> charsets() {
        return BU_CHARSETS.iterator();
    }
    
    @Override public Charset charsetForName(String charsetName) {
    	for (Charset buCharset : BU_CHARSETS) {
    		if (buCharset.name().equals(charsetName)) return buCharset;
            if (buCharset.aliases().contains(charsetName)) return buCharset;
    	}
        return null;
    }
    
    public static Charset charsetForBU(char bu) {
    	switch (bu) {
            case BU_CODE_B: return CHARSET_B;
			case BU_CODE_C: return CHARSET_C;
            case BU_CODE_A:
            default   : return CHARSET_A;
		}
    }
    
}