package com.myorg.nuxeo.proxylayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@RestController
public class DirectoryController {

    @RequestMapping(path = "/api/v1/fetch-dir/", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getListOfFiles(@RequestParam("cabinet") String cabinet,
                                                       @RequestParam("path") String path) throws IOException {
        return ResponseEntity.ok().body(new ArrayList<>());
    }

}
