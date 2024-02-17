package me.chenzz.mweb.service;

import lombok.SneakyThrows;
import me.chenzz.java.script.util.FileUtil;
import me.chenzz.mweb.model.TitleInfoBO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.Objects;
import java.util.Stack;


public class FormatService {

    /**
     * 用来标识当前是否在code区域
     */
    private static boolean codeArea = false;

    @SneakyThrows
    public static void format(String filePath, boolean topLeveTitleIncrease) {

        System.out.println("filePath=" + filePath);
        String content = FileUtil.readContent(filePath);

        // 先备份一下
        String backupFileName = DateFormatUtils.format(System.currentTimeMillis(), "yyyy年MM月dd日 HH时mm分ss秒");
        String path = "/tmp/" + backupFileName + ".md";
        FileUtils.writeStringToFile(new File(path), content, "UTF-8");


        String[] lineArr = content.split("\n");
        Integer currentTitleLevel = null;



        Stack<TitleInfoBO> titleNoStack = new Stack<>();

        Integer topLevelWellNum = getTopLevelTitleWellNum(lineArr);
        Integer topLevelNum = getTopLevelTitleNum(lineArr, topLevelWellNum);

        int currentNo = 0;
        if (!topLeveTitleIncrease) {
            currentNo = topLevelNum + 1;
        }

        for (int i = 0; i < lineArr.length; i++) {
            String line = lineArr[i];

            if (!isTitle(line)) {
                continue;
            }

            int titleLevel = getTitleLevel(line);
            if (null == currentTitleLevel) {
                currentTitleLevel = titleLevel;
            }

            if (titleLevel == currentTitleLevel) {
                // 如果本行层级 和 当前的层级一致

                if (needDecrease(topLeveTitleIncrease, currentTitleLevel, topLevelWellNum)) {
                    currentNo--;
                } else {
                    currentNo++;
                }

                String newLine = generateNewLine(currentNo, titleNoStack, line);
                lineArr[i] = newLine;
            } else if (titleLevel > currentTitleLevel) {
                // 如果本行层级 小于 当前的层级 (2级标题 小于 1级标题)

                TitleInfoBO titleInfo = new TitleInfoBO(currentNo, currentTitleLevel);

                titleNoStack.push(titleInfo);
                currentTitleLevel = titleLevel;
                currentNo = 0;

                currentNo++;
                String newLine = generateNewLine(currentNo, titleNoStack, line);
                lineArr[i] = newLine;
            } else {
                // 如果本行层级 大于 当前的层级

                // 一直出栈到 本行层级相等的元素
                // （来应对那种跨级的情况，比如当前处在 4级标题下，下一行是2级标题，如果只出栈一次，则当前的层级还是3级）

                TitleInfoBO titleInfo = findEqualLevelEle(titleNoStack, titleLevel);
                if (null == titleInfo) {
                    throw new RuntimeException("无法找到和当前行层级一致的历史行，解析终止！"
                            + "问题原因是，文章某个标题出现了比前面标题等级高的情况；解决方案，在文章前面加一个和问题行等级一样的标记。line=" + line);
                }

                currentNo = titleInfo.getTitleNo();
                currentTitleLevel = titleInfo.getTitleLevel();

                if (needDecrease(topLeveTitleIncrease, currentTitleLevel, topLevelWellNum)) {
                    currentNo--;
                } else {
                    currentNo++;
                }

                String newLine = generateNewLine(currentNo, titleNoStack, line);
                lineArr[i] = newLine;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lineArr) {
            stringBuilder.append(line).append("\n");
        }

        content = stringBuilder.toString();

        FileUtils.writeStringToFile(new File(filePath), content, "UTF-8");
    }

    private static boolean needDecrease(boolean topLeveTitleIncrease, Integer currentTitleLevel,
            Integer topLevelWellNum) {
        return !topLeveTitleIncrease && topLevelWellNum != null && Objects.equals(currentTitleLevel, topLevelWellNum);
    }

    private static Integer getTopLevelTitleNum(String[] lineArr, Integer topLevelWellNum) {
        // 顶级标题的数量
        int topLevelTitleNum = 0;

        for (String line : lineArr) {
            if (!isTitle(line)) {
                continue;
            }

            int wellNum = calWellNum(line);
            if (topLevelWellNum != null && wellNum == topLevelWellNum) {
                topLevelTitleNum++;
            }
        }

        return topLevelTitleNum;
    }

    private static Integer getTopLevelTitleWellNum(String[] lineArr) {
        // 顶级标题的井号数量
        Integer topLevelWellNum = null;
        for (String line : lineArr) {
            if (!isTitle(line)) {
                continue;
            }

            int wellNum = calWellNum(line);
            if (topLevelWellNum == null || wellNum < topLevelWellNum) {
                topLevelWellNum = wellNum;
            }
        }

        return topLevelWellNum;
    }

    private static int calWellNum(String line) {
        int num = 0;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '#') {
                num++;
            }
        }

        return num;
    }

    @SuppressWarnings("UnnecessaryContinue")
    private static TitleInfoBO findEqualLevelEle(Stack<TitleInfoBO> titleNoStack, int titleLevel) {
        TitleInfoBO titleInfo = null;

        while (!titleNoStack.isEmpty()) {
            TitleInfoBO tempTitleInfo= titleNoStack.pop();
            if (!tempTitleInfo.getTitleLevel().equals(titleLevel)) {
                continue;
            } else {
                titleInfo = tempTitleInfo;
                break;
            }
        }
        return titleInfo;
    }

    private static String generateNewLine(int currentNo, Stack<TitleInfoBO> titleNoStack, String line) {
        String currentTitlePrefix = generateTitlePrefix(line, titleNoStack, currentNo);
        String newLine = currentTitlePrefix + extractTitleContent(line);

        System.out.println("newLine=" + newLine);

        return newLine;
    }

    @SuppressWarnings({"SingleStatementInBlock", "UnnecessaryContinue"})
    private static String generateTitlePrefix(String line, Stack<TitleInfoBO> titleNoStack, int currentNo) {
        if (!line.startsWith("#")) {
            throw new RuntimeException("当前行不是#开头，无法解析出#前缀！line=" + line);
        }

        int i;
        for (i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '#') {
                continue;
            } else {
                break;
            }
        }
        String hashSignPrefix = line.substring(0, i);

        StringBuilder prefixStringBuilder = new StringBuilder();
        prefixStringBuilder.append(hashSignPrefix);
        prefixStringBuilder.append(" ");

        for (TitleInfoBO titleInfo : titleNoStack) {
            prefixStringBuilder.append(titleInfo.getTitleNo()).append(".");
        }
        prefixStringBuilder.append(currentNo).append(".");
        prefixStringBuilder.append(" ");

        return prefixStringBuilder.toString();
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantIfStatement"})
    private static boolean isTitle(String line) {
        if (StringUtils.isEmpty(line)) {
            return false;
        }

        // 进出code区域
        if (line.contains("```")) {
            codeArea = !codeArea;
        }

        if (codeArea) {
            return false;
        }

        if (line.startsWith("# ")
                || line.startsWith("## ")
                || line.startsWith("### ")
                || line.startsWith("#### ")
                || line.startsWith("##### ")
                || line.startsWith("###### ")) {
            return true;
        } else {
            return false;
        }
    }

    private static int getTitleLevel(String line) {
        if (StringUtils.isEmpty(line)) {
            throw new RuntimeException("当前行不是title，无法获取标题层级！line=" + line);
        }
        if (line.startsWith("# ")) {
            return 1;
        } else if (line.startsWith("## ")) {
            return 2;
        } else if (line.startsWith("### ")) {
            return 3;
        } else if (line.startsWith("#### ")) {
            return 4;
        } else if (line.startsWith("##### ")) {
            return 5;
        } else if (line.startsWith("###### ")) {
            return 6;
        } else {
            throw new RuntimeException("当前行无法获取标题层级！line=" + line);
        }
    }

    /**
     * 抽取标题内容
     * <p>
     * 例如，
     * <p>
     * ### 1.2.3. 旁路缓存
     * 对应的标题内容是 旁路缓存
     *
     * @param line 行内容
     * @return 标题
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    private static String extractTitleContent(String line) {
        if (StringUtils.isEmpty(line)) {
            throw new RuntimeException("当前行不是title，无法获取标题内容！line=" + line);
        }

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if ('#'!= currentChar
                    && ' ' != currentChar
                    && (currentChar < '0' || currentChar > '9')
                    && '.' != currentChar
                    && '、' != currentChar
                    && '）' != currentChar
                    && ')' != currentChar) {
                String lineContent = line.substring(i);
                return lineContent;
            }
        }
        return "";
    }

}
