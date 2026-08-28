package regulus.charset;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import regulus.util.Big5Block;

@Disabled("For manual testing")
public class Big5CoverageTest {

    private static final List<Big5Block> blocks = new ArrayList<>();

    @BeforeAll
    public static void init() {
//        blocks.add(new Big5Block("8140", "8DFE", "使用者造字區：第三段"));
//        blocks.add(new Big5Block("8E40", "A0FE", "使用者造字區：第二段"));
        blocks.add(new Big5Block("A140", "A2CE", "符號區"));
//        blocks.add(new Big5Block("A2CF", "A343", "全形英文字母"));
//        blocks.add(new Big5Block("A344", "A373", "全形希臘字母"));
//        blocks.add(new Big5Block("A374", "A3BF", "注音符號"));
//        blocks.add(new Big5Block("A3C0", "A3E0", "控制符號"));
//        blocks.add(new Big5Block("A3E1", "A3E1", "歐元符號"));
//        blocks.add(new Big5Block("A3E2", "A3FE", "保留 - 1"));
//        blocks.add(new Big5Block("A440", "C67E", "常用字"));
//        blocks.add(new Big5Block("C6A1", "C6BE", "數字符號"));
//        blocks.add(new Big5Block("C6BF", "C6D7", "部首"));
//        blocks.add(new Big5Block("C6D8", "C6E6", "罕用符號"));
        blocks.add(new Big5Block("C6E7", "C77A", "日文平假名"));
        blocks.add(new Big5Block("C77B", "C7F2", "日文片假名"));
//        blocks.add(new Big5Block("C7F3", "C8FE", "保留 - 2"));
//        blocks.add(new Big5Block("C940", "F9D5", "次常用字"));
//        blocks.add(new Big5Block("F9D6", "F9DC", "七個倚天外字集的擴充字"));
//        blocks.add(new Big5Block("F9DD", "F9FE", "表格符號"));
//        blocks.add(new Big5Block("FA40", "FEFE", "使用者造字區：第一段"));
    }

    @Test
    public void runAll() {
        List<Charset> big5Charsets = new ArrayList<>();
        big5Charsets.add(Charset.forName("BIG5"));
//        big5Charsets.add(Charset.forName("MS950"));
//        big5Charsets.add(MyBig5CharsetProvider.charsetForBU('A'));
//        big5Charsets.add(MyBig5CharsetProvider.charsetForBU('B'));
//        big5Charsets.add(MyBig5CharsetProvider.charsetForBU('C'));

        for (Charset bigCharset : big5Charsets) {
//            coveragePerBlock(bigCharset);
            checkUniCodeMapping(bigCharset);
        }
    }


    private static void coveragePerBlock(Charset targetBig5) {
        System.out.println("=====  " + targetBig5.name() + "  =====");
        int allSum = 0;
        int failCountSum = 0;

        for (Big5Block block : blocks) {
            allSum += block.size();
            int failCount = checkCoverage(targetBig5, block);
            if (failCount > 0) {
                failCountSum += failCount;
            }
        }

        double ratio = ((double) allSum - failCountSum) / (double) allSum;
        System.out.println("總涵蓋率：" + asPercent(ratio));
    }

    private static String asPercent(double ratio) {
        return Math.round(ratio * 1000000) / (double) 10000 + "%";
    }

    private static int checkCoverage(Charset targetBig5, Big5Block block) {
        int failCount = 0;

        for (int i = 0; i < block.size(); i++) {
            byte[] bytes = block.bytesPerIndex(i);
            String decoded = new String(bytes, targetBig5);
            if (decoded.contains("�")) {
                failCount++;
            }
        }
        double ratio = ((double) block.size() - failCount) / (double) block.size();
        System.out.println(block.name() + "：" + asPercent(ratio));
        return failCount;
    }

    private static void checkUniCodeMapping(Charset targetBig5) {
        System.out.println("=====  " + targetBig5.name() + "  =====");

        for (Big5Block block : blocks) {
            Map<Character.UnicodeBlock, Integer> mappingStats = new HashMap<>();
            for (int i = 0; i < block.size(); i++) {
                byte[] bytes = block.bytesPerIndex(i);
                if (bytes == null) continue;
                String decoded = new String(bytes, targetBig5);
                char[] decodedChars = decoded.toCharArray();
                Character.UnicodeBlock unicodeBlock;
                if (decodedChars.length == 1) {
                    unicodeBlock = Character.UnicodeBlock.of(decodedChars[0]);
                } else if (decodedChars.length == 2
                    && Character.isSurrogatePair(decodedChars[0], decodedChars[1])) {
                    int cp = Character.toCodePoint(decodedChars[0], decodedChars[1]);
                    unicodeBlock = Character.UnicodeBlock.of(cp);
                } else {
                    continue;
                }

                if (mappingStats.containsKey(unicodeBlock)) {
                    mappingStats.put(unicodeBlock, mappingStats.get(unicodeBlock) + 1);
                } else {
                    mappingStats.put(unicodeBlock, 1);
                }
            }
            if (mappingStats.isEmpty()) continue;
            System.out.println("Big5Block - " + block.name() + ":");
            for (Map.Entry<Character.UnicodeBlock, Integer> entry : mappingStats.entrySet()) {
                System.out.println("-- " + entry.getKey() + ", " + entry.getValue());
            }
        }

//        System.out.println("總涵蓋率：" + asPercent(ratio));
    }

}

