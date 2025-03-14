package com.book.book.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)

public class TbBookStoreDto {
    @XmlElement(name = "offName", namespace = "http://www.aladin.co.kr/ttb/apiguide.aspx")
    private String offName;

    @XmlElement(name = "link", namespace = "http://www.aladin.co.kr/ttb/apiguide.aspx")
    private String link;
}

//public class TbBookStoreDto {
//
//    private String offCode;
//    private String offName;
//    private String link;
//
//    @XmlElement
//    public String getOffCode() {
//        return offCode;
//    }
//
//    public void setOffCode(String offCode) {
//        this.offCode = offCode;
//    }
//
//    @XmlElement
//    public String getOffName() {
//        return offName;
//    }
//
//    public void setOffName(String offName) {
//        this.offName = offName;
//    }
//
//    @XmlElement
//    public String getLink() {
//        return link;
//    }
//
//    public void setLink(String link) {
//        this.link = link;
//    }
//}
