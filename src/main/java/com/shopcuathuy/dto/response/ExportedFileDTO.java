package com.shopcuathuy.dto.response;

import org.springframework.core.io.Resource;

public class ExportedFileDTO {
    private final Resource resource;
    private final String filename;

    public ExportedFileDTO(Resource resource, String filename) {
        this.resource = resource;
        this.filename = filename;
    }

    public Resource getResource() {
        return resource;
    }

    public String getFilename() {
        return filename;
    }
}

