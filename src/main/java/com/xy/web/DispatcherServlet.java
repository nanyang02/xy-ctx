//package com.xy.web;
//
//import com.xy.beans.BeansException;
//import com.xy.context.ApplicationContext;
//import com.xy.factory.ApplicationContextAware;
//
//import javax.servlet.ServletException;
//import javax.servlet.ServletInputStream;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * Class <code>DispatcherServlet</code>
// *
// * @author yangnan 2022/11/21 11:09
// * @since 1.8
// */
//public class DispatcherServlet extends HttpServlet {
//
//
//    private void setResponseJsonContentType(HttpServletResponse resp) {
//        resp.setContentType("text/html;charset=utf-8");
//    }
//
//    private void setNoCacheCtrl(HttpServletResponse resp) {
//        resp.setDateHeader("Expires", -1);
//        resp.setHeader("Cache-control", "no-cache");
//        resp.setHeader("Pragma", "no-cache");
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String queryString = req.getQueryString();
//        if (null == queryString) queryString = "";
//        // url请求参数的值进行编码，解决乱码
//        queryString = new String(queryString.getBytes("iso-8859-1"), "utf-8");
//        resp.setHeader("qs", queryString);
//        resp.getWriter().println(queryString);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // 请求体中的数据设置编码
//        req.setCharacterEncoding("utf-8");
//        ServletInputStream inputStream = req.getInputStream();
//        StringBuilder result = new StringBuilder();
//        int respInt = inputStream.read();
//        while (respInt != -1) {
//            result.append((char) respInt);
//            respInt = inputStream.read();
//        }
//
//        resp.getWriter().println(result.toString());
//    }
//}
