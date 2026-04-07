package com.sample.service;

import com.sample.mapper.TodoMapper;
import com.sample.service.util.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.ibatis.session.SqlSession;

@WebServlet("/delete")
public class DeleteServlet extends BaseServlet {

    @Override
    protected void executePost(HttpServletRequest req, HttpServletResponse resp, SqlSession session) throws ServletException, IOException {

        String idStr = req.getParameter("id");

        TodoMapper mapper = session.getMapper(TodoMapper.class);

        if (idStr != null && !idStr.trim().isEmpty()) {
            mapper.delete(Integer.valueOf(idStr));
        }

        resp.sendRedirect(req.getContextPath() + "/");

    }
}
