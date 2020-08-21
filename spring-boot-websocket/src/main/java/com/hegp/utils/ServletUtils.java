package com.hegp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Optional;

public class ServletUtils {
    private static final Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    public static void printAllHeaders() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            logger.debug(headerName + "  ===>>>  " + request.getHeader(headerName));
        }
    }

    public static String getBasePathWhenRequestIsForwarded() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String xForwardedUri = request.getHeader("x-forwarded-uri");
        Assert.isTrue(StringUtils.hasText(xForwardedUri), "请配置请求头的 x-forwarded-uri 参数");
        String requestURI = request.getRequestURI();
        if ("/".equals(requestURI)) {
            return xForwardedUri.endsWith("/")? xForwardedUri.substring(0, xForwardedUri.length()-1):xForwardedUri;
        } else {
            return xForwardedUri.substring(0, xForwardedUri.lastIndexOf(requestURI));
        }
    }

    /** 获取当前请求对应的ServletRequest */
    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(attributes)) {
            logger.error("\t\t\t当前线程获取不了HttpServletRequest对象, 可能的原因有\n" +
                    "\t\t\t\t\t1) 该项目不是springweb项目" +
                    "\t\t\t\t\t2) 该方法不是request请求线程直接调用, 由异步线程调用, 异步线程获取不到request请求线程的HttpServletRequest对象");
            return null;
        }
        return ((ServletRequestAttributes) attributes).getRequest();
    }

    /** 获取当前请求对应的HttpServletResponse */
    public static HttpServletResponse getCurrentResponse() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(attributes)) {
            logger.error("\t\t\t当前线程获取不了HttpServletResponse对象, 可能的原因有\n" +
                    "\t\t\t\t\t1) 该项目不是springweb项目" +
                    "\t\t\t\t\t2) 该方法不是request请求线程直接调用, 由异步线程调用, 异步线程获取不到request请求线程的HttpServletResponse对象");
            return null;
        }
        return ((ServletRequestAttributes) attributes).getResponse();
    }

    /** 获取当前请求对应的HttpServletResponse */
    public static HttpSession getCurrentSession() {
        HttpServletRequest request = getCurrentRequest();
        if (ObjectUtils.isEmpty(request)) {
            return request.getSession();
        }
        return null;
    }

    // 获取最原始的http,https协议,以及host
    public static String getOriginSchemeHost() {
        String originScheme = Optional.of(getOriginScheme()).orElse("http");
        String originHost = getOriginHost();
        return originScheme+"://"+originHost;
    }

    /** 获取请求入口最开始的schema, 不是请求转发后的schema */
    public static String getOriginScheme() {
        HttpServletRequest request = getCurrentRequest();
        if (request==null) return null;
        String xForwardedProto = request.getHeader("x-forwarded-proto"); // 发现 xForwardedProto 是由多个 https,http组成
        if (StringUtils.hasText(xForwardedProto)) {
            int index = xForwardedProto.indexOf(",");
            return (index==-1)? xForwardedProto:xForwardedProto.substring(0, index);
        } else {
            return request.getScheme();
        }
    }

    /** 获取请求入口最开始的host, 不是请求转发后的host */
    public static String getOriginHost() {
        HttpServletRequest request = getCurrentRequest();
        if (request==null) return null;
        /**
         * zuul网关会自动封装 x-forwarded-host 参数
         * zuul网关不会自动封装 x-forwarded-uri 参数, 要手动写代码补上去
         */
        String xForwardedHost = request.getHeader("x-forwarded-host");
        if (StringUtils.hasText(xForwardedHost)) {
            return xForwardedHost;
        } else {
            return request.getHeader("host");
        }
    }

    public static String getOriginForwardedUrl() {
        HttpServletRequest request = getCurrentRequest();
        if (request==null) return null;
        /**
         * zuul网关会自动封装 x-forwarded-host 参数
         * zuul网关不会自动封装 x-forwarded-uri 参数,要手动写代码补上去
         */
        String xForwardedUri = request.getHeader("X-Forwarded-Uri");
        if (StringUtils.hasText(xForwardedUri)) {
            return xForwardedUri;
        } else {
            return request.getServletPath();
        }
    }

    /**
     * 获取当前请求的原始客户端IP地址
     * @return
     */
    public static String getCurrentRequestOriginIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } else if (ip.contains(",")) { // 经过转发后，会用逗号 , 拼接多个IP
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }
}
