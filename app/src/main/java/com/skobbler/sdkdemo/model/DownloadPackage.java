package com.skobbler.sdkdemo.model;


import java.util.ArrayList;
import java.util.List;


/**
 * Download package class
 * 
 * 
 */
public class DownloadPackage {
    
    /**
     * Package code (e.g. RO - for Romania)
     */
    private String code;
    
    /**
     * Code of the parent package (e.g. EUR)
     */
    private String parentCode;
    
    /**
     * Package name (e.g. Romania, Bucharest, ...)
     */
    private String name;
    
    /**
     * The type of the package (continent, country, state, region, city)
     */
    private String type;
    
    /**
     * Size of the SKM file in tha package
     */
    private long size;
    
    /**
     * Codes of the children pakages (e.g. ROCITY01)
     */
    private List<String> childrenCodes = new ArrayList<String>();
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public List<String> getChildrenCodes() {
        return childrenCodes;
    }
    
    public void setChildrenCodes(List<String> childrenCodes) {
        this.childrenCodes = childrenCodes;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getParentCode() {
        return parentCode;
    }
    
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
    
    @Override
    public String toString() {
        // return "[" + code + "(" + name + "), type=" + type + ", size=" + size
        // + ", parent=" + parentCode
        // + ", children=" + childrenCodes + "]\n";
        return name;
    }
}