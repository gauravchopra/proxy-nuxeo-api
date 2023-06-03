package com.myorg.nuxeo.proxylayer.service;

import com.myorg.nuxeo.proxylayer.vo.GetResponse;
import com.myorg.nuxeo.proxylayer.vo.UploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    ResponseEntity<byte[]> downloadDocumentGivenUid(String uid) throws Exception;
    ResponseEntity<Map<String, List<GetResponse>>> getFileByFolderPath(String folderPath, String nameToSearch);
    ResponseEntity<Map<String, List<UploadResponse>>> uploadDocumentGivenPath(MultipartFile multipartFile, String parentPath, Map<String, String> properties) throws Exception;
    ResponseEntity<Map<String, List<UploadResponse>>> updateDocumentGivenUid(MultipartFile multipartFile, String uid, Map<String, String> properties) throws Exception;
    ResponseEntity<Map<String, String>> deleteDocumentGivenUid(String uid) throws Exception;
    public ResponseEntity<Map<String,List<String>>> getAllFilesInTheFolder(String folderPath);

}
