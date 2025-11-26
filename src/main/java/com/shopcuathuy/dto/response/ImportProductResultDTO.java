package com.shopcuathuy.dto.response;

import java.util.List;

public class ImportProductResultDTO {
    private int success;
    private int failed;
    private List<String> errors;

    public ImportProductResultDTO(int success, int failed, List<String> errors) {
        this.success = success;
        this.failed = failed;
        this.errors = errors;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}


