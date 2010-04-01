package org.opennms.web.map.config;

public class Icon {
   
    private String m_fileName;
    private String m_width;
    private String m_height;
    private String m_semaphoreRadius;
    private String m_semaphoreX;
    private String m_semaphoreY;
    private String m_textX;
    private String m_textY;
    private String m_textSize;
    private String m_textAlign;

    
    public String getFileName() {
        return m_fileName;
    }
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }
    public String getWidth() {
        return m_width;
    }
    public void setWidth(String width) {
        m_width = width;
    }
    public String getHeight() {
        return m_height;
    }
    public void setHeight(String height) {
        m_height = height;
    }
    public String getSemaphoreRadius() {
        return m_semaphoreRadius;
    }
    public void setSemaphoreRadius(String semaphoreRadius) {
        m_semaphoreRadius = semaphoreRadius;
    }
    public String getSemaphoreX() {
        return m_semaphoreX;
    }
    public void setSemaphoreX(String semaphoreX) {
        m_semaphoreX = semaphoreX;
    }
    public String getSemaphoreY() {
        return m_semaphoreY;
    }
    public void setSemaphoreY(String semaphoreY) {
        m_semaphoreY = semaphoreY;
    }
    public String getTextX() {
        return m_textX;
    }
    public void setTextX(String textX) {
        m_textX = textX;
    }
    public String getTextY() {
        return m_textY;
    }
    public void setTextY(String textY) {
        m_textY = textY;
    }
    public String getTextSize() {
        return m_textSize;
    }
    public void setTextSize(String textSize) {
        m_textSize = textSize;
    }
    public String getTextAllign() {
        return m_textAlign;
    }
    public void setTextAllign(String textAllign) {
        m_textAlign = textAllign;
    }
   
    public Icon(String fileName, String width, String height, String semaphoreRadius,
            String semaphoreX, String semaphoreY, String textX, String textY,
            String textSize, String textAllign) {

        m_fileName = fileName;
        m_width = width;
        m_height = height;
        m_semaphoreRadius = semaphoreRadius;
        m_semaphoreX = semaphoreX;
        m_semaphoreY = semaphoreY;
        m_textX = textX;
        m_textY = textY;
        m_textSize = textSize;
        m_textAlign = textAllign;
    }   
}
