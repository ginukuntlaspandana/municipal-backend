package com.municipal.dto.response;

public class LocalityStatDto {
    private String locality;
    private long count;

    public LocalityStatDto() {
    }

    public LocalityStatDto(String locality, long count) {
        this.locality = locality;
        this.count = count;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
