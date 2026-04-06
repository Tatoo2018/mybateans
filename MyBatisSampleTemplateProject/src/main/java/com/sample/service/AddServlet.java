package com.sample.service;

import com.sample.dbmodel.Todo;
import com.sample.mapper.TodoMapper;
import com.sample.service.util.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.ibatis.session.SqlSession;

@WebServlet("/add")
public class AddServlet extends BaseServlet {

    @Override
    protected void executeGet(HttpServletRequest req, HttpServletResponse resp, SqlSession session) throws ServletException, IOException {

        TodoMapper mapper = session.getMapper(TodoMapper.class);

        Todo todo = new Todo();
        todo.setId(mapper.getMaxId());

        req.getRequestDispatcher("/add.jsp").forward(req, resp);
    }
}
