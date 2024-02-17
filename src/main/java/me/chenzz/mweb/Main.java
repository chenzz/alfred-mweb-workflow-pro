package me.chenzz.mweb;

import me.chenzz.java.script.util.JsonUtil;
import me.chenzz.mweb.model.AlfredFileListBO;
import me.chenzz.mweb.service.FileFindService;
import me.chenzz.mweb.service.FormatService;
import org.apache.commons.lang3.StringUtils;

import static me.chenzz.mweb.constant.CommonConstant.CMD_FORMAT;
import static me.chenzz.mweb.constant.CommonConstant.CMD_SEARCH;

/**
 * @author chenzhongzheng
 * @since 2024/02/17
 */
public class Main {



    public static void main(String[] args) {
        String cmd = null;
        if (null != args && args.length > 0) {
            cmd = args[0];
        }
        if (StringUtils.isEmpty(cmd)) {
            cmd = CMD_SEARCH;
        }

        if (CMD_SEARCH.equals(cmd)) {
            String keyword = null;
            if (null != args && args.length > 1) {
                keyword = args[1];
            }

            AlfredFileListBO file = FileFindService.findFile(keyword);
            System.out.println(JsonUtil.toString(file));
        } else if (CMD_FORMAT.equals(cmd)) {
            String filePath = null;
            if (args.length > 1) {
                filePath = args[1];
            }

            boolean increase = true;
            if (args.length >= 3) {
                try {
                    increase = Boolean.parseBoolean(args[2]);
                } catch (Throwable throwable) {
                    System.out.println("increase 解析失败！args[2]=" + args[2]);
                }
            }

            FormatService.format(filePath, increase);
        }

    }
}
