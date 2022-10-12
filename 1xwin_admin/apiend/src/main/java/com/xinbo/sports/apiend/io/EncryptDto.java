package com.xinbo.sports.apiend.io;

import com.xinbo.sports.apiend.aop.annotation.FieldAnnotation;
import lombok.Data;

/**
 * @author: wells
 * @date: 2020/7/8
 * @description:
 */
@Data
@FieldAnnotation(createFlag = true)
public class EncryptDto {
    String name = "encrypt name!";
}
