package com.rodbate.httpserver.upload;




public interface FileItemFactory {



    /**
     *
     * 创建FileItem
     *
     * @param fieldName     参数名
     * @param contentType   Content-Type
     * @param filename      上传的文件名
     * @param isFormField   是否是参数
     * @return FileItem
     */
    FileItem createItem(
            String fieldName,
            String contentType,
            String filename,
            boolean isFormField
    );


}
