package com.sample.service.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class BaseServlet extends HttpServlet {

    private SqlSessionFactory factory;

    @Override
    public void init() throws ServletException {
        
        try {
        
            InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
            
            factory = new SqlSessionFactoryBuilder().build(is);
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        try (SqlSession session = factory.openSession()) {

            executeGet(req, resp, session);

            session.commit();
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");

        try (SqlSession session = factory.openSession()) {
            
            executePost(req, resp, session);

            session.commit();
        }

    }

    protected void executePost(HttpServletRequest req, HttpServletResponse resp, SqlSession session)
            throws ServletException, IOException {
    
    }

    protected void executeGet(HttpServletRequest req, HttpServletResponse resp, SqlSession session) throws ServletException, IOException {

        
    }
}
