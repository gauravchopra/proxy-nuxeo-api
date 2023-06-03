package com.myorg.nuxeo.proxylayer.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {
    private String uid;
    private String path;
    private String documentTitle;
    private String url;
    private String error;
}
