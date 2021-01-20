package com.benjaminfaal.ps4remotepkginstaller.controller;

import com.benjaminfaal.ps4remotepkginstaller.service.RemotePKGInstallerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Base64;

@RequestMapping("/pkg")
@RestController
public class PKGController {

    @Autowired
    private RemotePKGInstallerService remotePKGInstallerService;

    @GetMapping("/{fileBase64}")
    public ResponseEntity<Resource> getPkg(@PathVariable String fileBase64, @RequestParam(required = false) String downloadId) {
        if (downloadId != null) {
            Integer taskId = Integer.parseInt(downloadId, 16);
            if (remotePKGInstallerService.getInstallRequest(taskId) == null) {
                return ResponseEntity.notFound().build();
            }
        }

        File file = new File(new String(Base64.getUrlDecoder().decode(fileBase64)));
        if (file.exists() && file.isFile()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

}
