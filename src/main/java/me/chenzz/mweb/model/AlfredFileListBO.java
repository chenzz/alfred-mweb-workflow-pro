package me.chenzz.mweb.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author chenzhongzheng
 * @since 2024/02/17
 */
@Data
public class AlfredFileListBO {
    private List<AlfredFileBO> items;

    @SuppressWarnings("unused")
    public static AlfredFileListBO buildByError(String errorMsg) {

        AlfredFileListBO alfredFileListBO = new AlfredFileListBO();

        AlfredFileBO alfredFileBO = new AlfredFileBO();
        alfredFileBO.setType("error");
        alfredFileBO.setTitle(errorMsg);

        alfredFileListBO.setItems(Collections.singletonList(alfredFileBO));
        return alfredFileListBO;
    }
}
