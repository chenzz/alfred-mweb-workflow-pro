package me.chenzz.mweb.service;

import me.chenzz.java.script.util.FileUtil;
import me.chenzz.java.script.util.LsUtil;
import me.chenzz.java.script.util.MdFindUtil;
import me.chenzz.java.script.util.ShellUtil;
import me.chenzz.mweb.model.AlfredFileBO;
import me.chenzz.mweb.model.AlfredFileListBO;
import me.chenzz.mweb.model.NoteBO;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenzhongzheng
 * @since 2024/02/17
 */
public class FileFindService {

    public static AlfredFileListBO findFile(String keyword) {

        // 1. 找到文档所在的目录
        String docHome = ShellUtil.getEnv("MDOC_HOME");
        if (StringUtils.isEmpty(docHome)) {
            docHome = "~/Library/Containers/com.coderforart.MWeb3/Data/Library/Application Support/MWebLibrary";
        }

        docHome = docHome.replaceFirst("^~", System.getProperty("user.home"));
        String mwebDocRootPath = docHome + "/docs/";


        // 2. 找文件
        List<NoteBO> noteBOList;
        if (StringUtils.isEmpty(keyword)) {
            noteBOList = findRecentModifiedNoteList(mwebDocRootPath);
        } else {
            // 2. 找到所有包含关键字的文档
            noteBOList = findNoteListByKeyword(mwebDocRootPath, keyword);
        }

        // 3. 结果转换
        List<AlfredFileBO> fileList = new ArrayList<>();
        for (NoteBO noteBO : noteBOList) {
            AlfredFileBO alfredFileBO = new AlfredFileBO();
            alfredFileBO.setType("file");
            alfredFileBO.setTitle(noteBO.getTitle());
            alfredFileBO.setArg(noteBO.getTitle());

            fileList.add(alfredFileBO);
        }

        AlfredFileListBO alfredFileListBO = new AlfredFileListBO();
        alfredFileListBO.setItems(fileList);
        return alfredFileListBO;
    }

    private static List<NoteBO> findRecentModifiedNoteList(String mwebDocRootPath) {
        List<String> fileList = LsUtil.listFilesByModificationTime(mwebDocRootPath);

        fileList = fileList.stream()
                .filter(filePath -> filePath.endsWith(".md"))
                .collect(Collectors.toList());
        fileList = fileList.subList(0, 7);

        List<NoteBO> noteBOList = new ArrayList<>();
        for (String filePath : fileList) {
            NoteBO noteBO = new NoteBO();
            noteBO.setTitle(FileUtil.readFirstLine(filePath));
            noteBO.setFilePath(filePath);

            noteBOList.add(noteBO);
        }

        return noteBOList;
    }

    @NotNull
    private static List<NoteBO> findNoteListByKeyword(String mwebDocRootPath, String keyword) {
        List<String> filePathList = MdFindUtil.findFileListContainsKeyword(mwebDocRootPath, keyword);

        List<NoteBO> noteBOList = new ArrayList<>();
        for (String filePath : filePathList) {
            String firstLine = FileUtil.readFirstLine(filePath);

            NoteBO noteBO = new NoteBO();
            noteBO.setTitle(firstLine);
            noteBO.setFilePath(filePath);

            noteBOList.add(noteBO);
        }

        // 3. 根据标题进行排序
        noteBOList.sort((a, b) -> {
            String title = a.getTitle();
            if (null != title && title.contains(keyword)) {
                return -1;
            }

            String bTitle = b.getTitle();
            if (null != bTitle && bTitle.contains(keyword)) {
                return 1;
            }

            return 0;
        });
        return noteBOList;
    }
}
