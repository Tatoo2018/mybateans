package com.sample.service;

import com.sample.dbmodel.Todo;
import com.sample.mapper.TodoMapper;
import com.sample.service.util.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import org.apache.ibatis.exceptions.PersistenceException;

import org.apache.ibatis.session.*;

@WebServlet(urlPatterns = {"/search", ""})
public class SearchServlet extends BaseServlet {

    @Override
    protected void executeGet(HttpServletRequest req, HttpServletResponse resp, SqlSession session)
            throws ServletException, IOException {

        String title = req.getParameter("title");
        if(title!=null){
            title = title.trim();
            if(title.isEmpty()){
                title = null;
            }
        }

        TodoMapper mapper = session.getMapper(TodoMapper.class);

        int count = 0;
        try {
            
            count = mapper.checkTableExists();
            
        } catch (PersistenceException e) {
            
            //Database connection failed. Please verify that the DB server is running and the configuration is correct.
            req.setAttribute("isExistDbServer", "false");
            RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);
            return;
        }

        if (count == 0) {
            
            //It looks like the required table is missing. Please ensure the table is created in your database.
            req.setAttribute("isExistTable", "false");
        
        } else {

            List<Todo> list = mapper.findByTitle(title);
            req.setAttribute("todos", list);

        }
        RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void executePost(HttpServletRequest req, HttpServletResponse resp, SqlSession session)
            throws ServletException, IOException {

        Todo todo = new Todo();
        todo.setTitle(req.getParameter("title"));
        todo.setDescription(req.getParameter("description"));
        
        String dueDateStr = req.getParameter("dueDate");
        
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            todo.setDueDate(java.sql.Date.valueOf(dueDateStr));
        }

        String isCompStr = req.getParameter("isCompleted");
        todo.setIsCompleted((isCompStr != null && isCompStr.equals("1")) ? 1 : 0);

        String idStr = req.getParameter("id");

        TodoMapper mapper = session.getMapper(TodoMapper.class);

        if (idStr == null || idStr.trim().isEmpty()) {

            Integer maxId = mapper.getMaxId();
            int nextId = (maxId == null) ? 1 : maxId + 1;
            todo.setId(nextId);
            mapper.insert(todo);
            
        } else {
            
            todo.setId(Integer.valueOf(idStr.trim()));
            mapper.update(todo);
        
        }

        resp.sendRedirect(req.getContextPath() + "/");
    }

}
