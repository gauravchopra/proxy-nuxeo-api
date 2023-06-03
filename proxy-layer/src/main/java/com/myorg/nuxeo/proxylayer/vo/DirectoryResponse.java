package com.myorg.nuxeo.proxylayer.vo;

import lombok.Data;

import java.util.ArrayList;

@Data
public class DirectoryResponse {
    private String type;
    private boolean isPaginable;
    private float resultsCount;
    private float pageSize;
    private float maxPageSize;
    private float resultsCountLimit;
    private float currentPageSize;
    private float currentPageIndex;
    private float currentPageOffset;
    private float numberOfPages;
    private boolean isPreviousPageAvailable;
    private boolean isNextPageAvailable;
    private boolean isLastPageAvailable;
    private boolean isSortable;
    private boolean hasError;
    private String errorMessage = null;
    private float pageIndex;
    private float pageCount;
    ArrayList< Object > entries = new ArrayList < Object > ();

}
