/*
 * @(#) GlobalExceptionHandler.java 2017年3月18日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.dragsun.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常handler
 * 
 * @author wwlong
 * @version 2017年3月18日
 */
//@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


}
