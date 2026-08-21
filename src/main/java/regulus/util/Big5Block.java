package regulus.util;

/**
 *    BIG5 區段，參考 <a href="https://www.cns11643.gov.tw/pageView.jsp?ID=9">...</a>。<br>
 *    <br>
 *    Range	        Name                   <br>
 * -------------------------------------   <br>
 *    8140 - 8DFE	使用者造字區：第三段   <br>
 *    8E40 - A0FE	使用者造字區：第二段   <br>
 *    A140 - A2CE	符號區                 <br>
 *    A2CF - A343	全形英文字母           <br>
 *    A344 - A373	全形希臘字母           <br>
 *    A374 - A3BF	注音符號               <br>
 *    A3C0 - A3E0	控制符號               <br>
 *    A3E1 - A3E1	歐元符號               <br>
 *    A3E2 - A3FE	保留                   <br>
 *    A440 - C67E	常用字                 <br>
 *    C6A1 - C6BE	數字符號               <br>
 *    C6BF - C6D7	部首                   <br>
 *    C6D8 - C6E6	罕用符號               <br>
 *    C6E7 - C77A	日文平假名             <br>
 *    C77B - C7F2	日文片假名             <br>
 *    C7F3 - C8FE	保留                   <br>
 *    C940 - F9D5	次常用字               <br>
 *    F9D6 - F9DC	七個倚天外字集的擴充字 <br>
 *    F9DD - F9FE	表格符號               <br>
 *    FA40 - FEFE	使用者造字區：第一段   <br>
 */
public class Big5Block {

    private final String name;
    private final String lowerBound_Hex;
    private final String upperBound_Hex;
    private final int lowerBound_Decimal;
    private final int upperBound_Decimal;
    private final int size;
    private final byte[][] byteArrays;

    public Big5Block(String lowerBound_Hex, String upperBound_Hex, String name) {
        this.name = name;
        this.lowerBound_Hex = lowerBound_Hex;
        this.upperBound_Hex = upperBound_Hex;
        this.lowerBound_Decimal = Integer.parseInt(lowerBound_Hex, 16);
        this.upperBound_Decimal = Integer.parseInt(upperBound_Hex, 16);
        if (upperBound_Decimal - lowerBound_Decimal < 0) {
            throw new RuntimeException("區段上限應大於等於區段下限");
        } else if (upperBound_Decimal - lowerBound_Decimal == 0) {
            this.size = 1;
        } else {
            this.size = upperBound_Decimal - lowerBound_Decimal;
        }
        this.byteArrays = new byte[this.size][2];

        int currentBig5CodePoint = lowerBound_Decimal;
        for (int i = 0; i < byteArrays.length; i++) {
            byteArrays[i] = big5CodePointAsBytes(currentBig5CodePoint);
            currentBig5CodePoint++;
        }
    }

    private static byte[] big5CodePointAsBytes(int cpDecimal) {
        String cpHex = Integer.toHexString(cpDecimal);
        if (cpHex.length() != 4) throw new RuntimeException("Big5 碼位格式不對");
        String[] hexPair = new String[] {cpHex.substring(0, 2), cpHex.substring(2, 4)};
        return Utils.hexArrayToByteArray(hexPair);
    }

    public String name() { return name; }

    public int size() { return size; }

    public int lowerBound_Decimal() { return lowerBound_Decimal; }

    public int upperBound_Decimal() { return upperBound_Decimal; }

    public String upperBound_Hex() { return upperBound_Hex; }

    public String lowerBound_Hex() { return lowerBound_Hex; }

    public byte[] bytesPerIndex(int i) {
        if (byteArrays[i] == null) return null;
        return byteArrays[i].clone();
    }
}
