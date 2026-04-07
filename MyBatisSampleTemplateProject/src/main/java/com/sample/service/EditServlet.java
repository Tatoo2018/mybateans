package com.sample.service;

import com.sample.dbmodel.Todo;
import com.sample.mapper.TodoMapper;
import com.sample.service.util.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

@WebServlet("/edit")
public class EditServlet extends BaseServlet {

    @Override
    protected void executeGet(HttpServletRequest req, HttpServletResponse resp, SqlSession session) throws ServletException, IOException {

        String idStr = req.getParameter("id");

        TodoMapper mapper = session.getMapper(TodoMapper.class);

        List<Todo> list = mapper.findById(Integer.valueOf(idStr));

        if (!list.isEmpty()) {

            req.setAttribute("todo", list.get(0));

        }
        req.getRequestDispatcher("/edit.jsp").forward(req, resp);
    }
}
