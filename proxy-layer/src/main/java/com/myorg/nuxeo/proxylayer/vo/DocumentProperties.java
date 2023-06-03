package com.myorg.nuxeo.proxylayer.vo;

import lombok.Data;

import java.util.ArrayList;

@Data
public class DocumentProperties {
    private String dc_creator;
    private String dc_source;
    private String dc_nature;
    ArrayList< Object > dc_contributors = new ArrayList < Object > ();
    private String dc_created;
    private String dc_description;
    private String dc_rights;
    ArrayList < Object > dc_subjects = new ArrayList < Object > ();
    private String dc_publisher = null;
    private String dc_valid = null;
    private String dc_format;
    private String dc_issued = null;
    private String dc_modified;
    private String dc_expired;
    private String dc_coverage;
    private String dc_language;
    private String dc_title;
    private String dc_lastContributor;
    private String common_icon;
    private String domain_display_type;


}


