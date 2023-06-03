package com.myorg.nuxeo.proxylayer.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetResponse {

    private String uid;
    private String url;
    private String title;
    private String path;
    private String type;
    private String error;

    private String lastModified;
    //private DocumentProperties PropertiesObject;




}

