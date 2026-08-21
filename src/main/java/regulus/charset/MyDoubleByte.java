/*
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package regulus.charset;

import static sun.nio.cs.CharsetMapping.UNMAPPABLE_DECODING;
import static sun.nio.cs.CharsetMapping.UNMAPPABLE_ENCODING;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.Map;

import sun.nio.cs.ArrayDecoder;
import sun.nio.cs.ArrayEncoder;
import sun.nio.cs.Surrogate;

/**
 * A copy of sun.nio.cs.ext.DoubleByte, except the changed class name and the size of B2C_UNMAPPABLE.
 */
public class MyDoubleByte {
	
	/**
     * b2c 無法對映陣列之大小，原為 0x100(256)，現為 0xbf(191) 與其他正常對映陣列大小一致；initC2B() 部分的與此無關  
     */
    private final static int b2c_unmappable_size = 0xbf;
	
    public final static char[] B2C_UNMAPPABLE = new_B2C_UNMAPPABLE();
    
    public static char[] new_B2C_UNMAPPABLE() {
    	char[] newArr = new char[MyDoubleByte.b2c_unmappable_size];
		Arrays.fill(newArr, UNMAPPABLE_DECODING);
		return newArr;
    }

    public static class Decoder extends CharsetDecoder
                                implements ArrayDecoder
    {
        final char[][] b2c;
        final char[] b2cSB;
        final int b2Min;
        final int b2Max;

        // Map<big5_hex_lowerCase, surrogatePair> dw_b2ncMap
        Map<String, Character[]> dw_b2ncMap;
        
        // for SimpleEUC override
        protected CoderResult crMalformedOrUnderFlow(int b) {
            return CoderResult.UNDERFLOW;
        }

        protected CoderResult crMalformedOrUnmappable(int b1, int b2) {
            if (b2c[b1] == B2C_UNMAPPABLE ||                // isNotLeadingByte(b1)
                b2c[b2] != B2C_UNMAPPABLE ||                // isLeadingByte(b2)
                decodeSingle(b2) != UNMAPPABLE_DECODING) {  // isSingle(b2)
                return CoderResult.malformedForLength(1);
            }
            return CoderResult.unmappableForLength(2);
        }

        Decoder(Charset cs, float avgcpb, float maxcpb,
                char[][] b2c, char[] b2cSB,
                int b2Min, int b2Max, Map<String, Character[]> dw_b2ncMap) {
            super(cs, avgcpb, maxcpb);
            this.b2c = b2c;
            this.b2cSB = b2cSB;
            this.b2Min = b2Min;
            this.b2Max = b2Max;
            this.dw_b2ncMap = dw_b2ncMap;
        }

        Decoder(Charset cs, char[][] b2c, char[] b2cSB, int b2Min, int b2Max, 
        		Map<String, Character[]> dw_b2ncMap) {
            this(cs, 1.0f, 1.0f, b2c, b2cSB, b2Min, b2Max, dw_b2ncMap);
        }

        protected CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();

            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();

            try {
                while (sp < sl && dp < dl) {
                    // inline the decodeSingle/Double() for better performance
                    int inSize = 1;
                    int b1 = sa[sp] & 0xff;
                    char c = b2cSB[b1];
                    if (c == UNMAPPABLE_DECODING) {
                        if (sl - sp < 2)
                            return crMalformedOrUnderFlow(b1);
                        int b2 = sa[sp + 1] & 0xff;
                        if (b2 < b2Min || b2 > b2Max ||
                            (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                        	// By Frank starts
                        	String big5_hex = Integer.toHexString(b1) + Integer.toHexString(b2);
                        	if (dw_b2ncMap.get(big5_hex) != null) {
                        		Character[] sgpair = dw_b2ncMap.get(big5_hex);
                        		da[dp++] = sgpair[0];
                        		c = sgpair[1];
                        	} else {          
                        	// By Frank ends
                        		return crMalformedOrUnmappable(b1, b2);
                        	}
                        }
                        inSize++;
                    }
                    da[dp++] = c;
                    sp += inSize;
                }
                return (sp >= sl) ? CoderResult.UNDERFLOW
                                  : CoderResult.OVERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }

        protected CoderResult decodeBufferLoop(ByteBuffer src, CharBuffer dst) {
            int mark = src.position();
            try {

                while (src.hasRemaining() && dst.hasRemaining()) {
                    int b1 = src.get() & 0xff;
                    char c = b2cSB[b1];
                    int inSize = 1;
                    if (c == UNMAPPABLE_DECODING) {
                        if (src.remaining() < 1)
                            return crMalformedOrUnderFlow(b1);
                        int b2 = src.get() & 0xff;
                        if (b2 < b2Min || b2 > b2Max ||
                            (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                        	// By Frank starts
                        	String big5_hex = Integer.toHexString(b1) + Integer.toHexString(b2);
                        	if (dw_b2ncMap.get(big5_hex) != null) {
                        		Character[] sgpair = dw_b2ncMap.get(big5_hex);
                        		dst.put(sgpair[0]);
                        		c = sgpair[1];
                        	} else {                        		
                        	// By Frank ends	
                        		return crMalformedOrUnmappable(b1, b2);
                        	}
                        }                        	
                        inSize++;
                    }
                    dst.put(c);
                    mark += inSize;
                }
                return src.hasRemaining()? CoderResult.OVERFLOW
                                         : CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }

        // Make some protected methods public for use by JISAutoDetect
        public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }

        public int decode(byte[] src, int sp, int len, char[] dst) {
            int dp = 0;
            int sl = sp + len;
            char repl = replacement().charAt(0);
            while (sp < sl) {
                int b1 = src[sp++] & 0xff;
                char c = b2cSB[b1];
                if (c == UNMAPPABLE_DECODING) {
                    if (sp < sl) {
                        int b2 = src[sp++] & 0xff;
                        if (b2 < b2Min || b2 > b2Max ||
                            (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                        	// By Frank starts
                        	String big5_hex = Integer.toHexString(b1) + Integer.toHexString(b2);
                        	if (dw_b2ncMap.get(big5_hex) != null) {
                        		Character[] sgpair = dw_b2ncMap.get(big5_hex);
                        		dst[dp++] = sgpair[0];
                        		c = sgpair[1];
                        	// By Frank ends
                        	} else if (b2c[b1] == B2C_UNMAPPABLE ||  // isNotLeadingByte
                                b2c[b2] != B2C_UNMAPPABLE ||  // isLeadingByte
                                decodeSingle(b2) != UNMAPPABLE_DECODING) {
                                sp--;
                            }
                        }
                    }
                    if (c == UNMAPPABLE_DECODING) {
                        c = repl;
                    }
                }
                dst[dp++] = c;
            }
            return dp;
        }

        public void implReset() {
            super.implReset();
        }

        public CoderResult implFlush(CharBuffer out) {
            return super.implFlush(out);
        }

        // decode loops are not using decodeSingle/Double() for performance
        // reason.
        public char decodeSingle(int b) {
            return b2cSB[b];
        }

        public char decodeDouble(int b1, int b2) {
            if (b1 < 0 || b1 > b2c.length ||
                b2 < b2Min || b2 > b2Max)
                return UNMAPPABLE_DECODING;
            return  b2c[b1][b2 - b2Min];
        }
       
    }

    public static class Encoder extends CharsetEncoder
                                implements ArrayEncoder
    {
        final int MAX_SINGLEBYTE = 0xff;
        private final char[] c2b;
        private final char[] c2bIndex;
        Surrogate.Parser sgp;
        
        // Map<uni_ch, big5_ch> dw_c2bMap
        private final Map<Character, Character> dw_c2bMap;
        // Map<uni_hex_lowerCase, big5_bytes> dw_nc2bMap
        private final Map<String, Byte[]> dw_nc2bMap;

        protected Encoder(Charset cs, char[] c2b, char[] c2bIndex, 
        		Map<Character, Character> dw_c2bMap,Map<String, Byte[]> dw_nc2bMap) {
            super(cs, 2.0f, 2.0f);
            this.c2b = c2b;
            this.c2bIndex = c2bIndex;
            this.dw_c2bMap = dw_c2bMap;
            this.dw_nc2bMap = dw_nc2bMap;
        }

        Encoder(Charset cs, float avg, float max, byte[] repl, char[] c2b, char[] c2bIndex, 
        		Map<Character, Character> dw_c2bMap,Map<String, Byte[]> dw_nc2bMap) {
            super(cs, avg, max, repl);
            this.c2b = c2b;
            this.c2bIndex = c2bIndex;
            this.dw_c2bMap = dw_c2bMap;
            this.dw_nc2bMap = dw_nc2bMap;
        }

        public boolean canEncode(char c) {
            return encodeChar(c) != UNMAPPABLE_ENCODING;
        }

        Surrogate.Parser sgp() {
            if (sgp == null)
                sgp = new Surrogate.Parser();
            return sgp;
        }

        protected CoderResult encodeArrayLoop(CharBuffer src, ByteBuffer dst) {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();

            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();

            try {
                while (sp < sl) {
                    char c = sa[sp];
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                    	// By Frank starts
                    	if (sp + 1 < sl 
                    			&& Character.isHighSurrogate(c)
                    			&& Character.isLowSurrogate(sa[sp + 1])) {
                    		String uni_hex = Integer.toHexString(c) + Integer.toHexString(sa[sp + 1]);
                    		if (dw_nc2bMap.get(uni_hex) != null) {
                        		Byte[] resBytes = dw_nc2bMap.get(uni_hex);
                        		da[dp++] = resBytes[0];
                        		da[dp++] = resBytes[1];
                        		sp = sp + 2;
                        		continue;
                        	} else { return CoderResult.unmappableForLength(2); }
                    	// By Frank ends
                    	} else {                    		
                    		if (Character.isSurrogate(c)) {
                    			if (sgp().parse(c, sa, sp, sl) < 0)
                    				return sgp.error();
                    			return sgp.unmappableResult();
                    		}
                    		return CoderResult.unmappableForLength(1);
                    	}
                    }

                    if (bb > MAX_SINGLEBYTE) {    // DoubleByte
                        if (dl - dp < 2)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)(bb >> 8);
                        da[dp++] = (byte)bb;
                    } else {                      // SingleByte
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)bb;
                    }

                    sp++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }

        protected CoderResult encodeBufferLoop(CharBuffer src, ByteBuffer dst) {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    char c = src.get();
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                    	// By Frank starts
                    	if (src.hasRemaining() 
                    			&& Character.isHighSurrogate(c) 
                    			&& Character.isLowSurrogate(src.get(src.position()))) {
                    		String uni_hex = Integer.toHexString(c) + Integer.toHexString(src.get());
                        	if (dw_nc2bMap.get(uni_hex) != null) {
                        		Byte[] resBytes = dw_nc2bMap.get(uni_hex);
                        		dst.put(resBytes[0]);
                        		dst.put(resBytes[1]);
                        		mark = mark + 2;
                        		continue;
                        	} else { return CoderResult.unmappableForLength(2); }
                    	// By Frank ends
                    	} else {
                    		if (Character.isSurrogate(c)) {
                                if (sgp().parse(c, src) < 0)
                                    return sgp.error();
                                return sgp.unmappableResult();
                            }
                            return CoderResult.unmappableForLength(1);
                    	}
                    }
                    if (bb > MAX_SINGLEBYTE) {  // DoubleByte
                        if (dst.remaining() < 2)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)(bb >> 8));
                        dst.put((byte)(bb));
                    } else {
                        if (dst.remaining() < 1)
                        return CoderResult.OVERFLOW;
                        dst.put((byte)bb);
                    }
                    mark++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }

        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }

        protected byte[] repl = replacement();
        protected void implReplaceWith(byte[] newReplacement) {
            repl = newReplacement;
        }

        public int encode(char[] src, int sp, int len, byte[] dst) {
            int dp = 0;
            int sl = sp + len;
//            int dl = dst.length;
            while (sp < sl) {
                char c = src[sp++];
                int bb = encodeChar(c);
                if (bb == UNMAPPABLE_ENCODING) {
                    if (Character.isHighSurrogate(c) && sp < sl &&
                        Character.isLowSurrogate(src[sp])) {
                    	// By Frank starts
                    	String uni_hex = Integer.toHexString(c) + Integer.toHexString(src[sp]);
                    	if (dw_nc2bMap.get(uni_hex) != null) {
                    		Byte[] resBytes = dw_nc2bMap.get(uni_hex);
                    		dst[dp++] = resBytes[0];
                    		dst[dp++] = resBytes[1];
                    		sp++;
                    		continue;
                    	}
                    	// By Frank ends
                        sp++;
                    }
                    dst[dp++] = repl[0];
                    if (repl.length > 1)
                        dst[dp++] = repl[1];
                    continue;
                } //else
                if (bb > MAX_SINGLEBYTE) { // DoubleByte
                    dst[dp++] = (byte)(bb >> 8);
                    dst[dp++] = (byte)bb;
                } else {                          // SingleByte
                    dst[dp++] = (byte)bb;
                }

            }
            return dp;
        }

        public int encodeChar(char ch) {
        	char res = c2b[c2bIndex[ch >> 8] + (ch & 0xff)]; // 原始
        	// By Frank starts
        	if (res == UNMAPPABLE_ENCODING) {
        		if (dw_c2bMap.get(ch) != null) res = dw_c2bMap.get(ch); // 根據 BU 難字表擴充
        	}
        	// By Frank ends
            return res;
        }

        // init the c2b and c2bIndex tables from b2c.
        static void initC2B(String[] b2c, String b2cSB, String b2cNR,  String c2bNR,
                            int b2Min, int b2Max,
                            char[] c2b, char[] c2bIndex)
        {
            Arrays.fill(c2b, (char)UNMAPPABLE_ENCODING);
            int off = 0x100;

            char[][] b2c_ca = new char[b2c.length][];
            char[] b2cSB_ca = null;
            if (b2cSB != null)
                b2cSB_ca = b2cSB.toCharArray();

            for (int i = 0; i < b2c.length; i++) {
                if (b2c[i] == null)
                    continue;
                b2c_ca[i] = b2c[i].toCharArray();
            }

            if (b2cNR != null) {
                int j = 0;
                while (j < b2cNR.length()) {
                    char b  = b2cNR.charAt(j++);
                    char c  = b2cNR.charAt(j++);
                    if (b < 0x100 && b2cSB_ca != null) {
                        if (b2cSB_ca[b] == c)
                            b2cSB_ca[b] = UNMAPPABLE_DECODING;
                    } else {
                        if (b2c_ca[b >> 8][(b & 0xff) - b2Min] == c)
                            b2c_ca[b >> 8][(b & 0xff) - b2Min] = UNMAPPABLE_DECODING;
                    }
                }
            }

            if (b2cSB_ca != null) {      // SingleByte
                for (int b = 0; b < b2cSB_ca.length; b++) {
                    char c = b2cSB_ca[b];
                    if (c == UNMAPPABLE_DECODING)
                        continue;
                    int index = c2bIndex[c >> 8];
                    if (index == 0) {
                        index = off;
                        off += 0x100;
                        c2bIndex[c >> 8] = (char)index;
                    }
                    c2b[index + (c & 0xff)] = (char)b;
                }
            }
            
            for (int b1 = 0; b1 < b2c.length; b1++) {  // DoubleByte
                char[] db = b2c_ca[b1];
                if (db == null)
                    continue;
                for (int b2 = b2Min; b2 <= b2Max; b2++) {
                    char c = db[b2 - b2Min];
                    if (c == UNMAPPABLE_DECODING)
                        continue;
                    int index = c2bIndex[c >> 8];
                    if (index == 0) {
                        index = off;
                        off += 0x100;
                        c2bIndex[c >> 8] = (char)index;
                    }

                    c2b[index + (c & 0xff)] = (char)((b1 << 8) | b2);
                }
            }
     
            if (c2bNR != null) {
                // add c->b only nr entries
                for (int i = 0; i < c2bNR.length(); i += 2) {
                    char b = c2bNR.charAt(i);
                    char c = c2bNR.charAt(i + 1);
                    int index = (c >> 8);
                    if (c2bIndex[index] == 0) {
                        c2bIndex[index] = (char)off;
                        off += 0x100;
                    }
                    index = c2bIndex[index] + (c & 0xff);
                    c2b[index] = b;
                }
            }
        }
        
    }

}
