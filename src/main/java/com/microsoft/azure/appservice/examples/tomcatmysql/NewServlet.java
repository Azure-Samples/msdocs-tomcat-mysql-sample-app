package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.Student;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/create")
public class NewServlet extends HttpServlet  {

    private static Logger logger = LogManager.getLogger(NewServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("GET /create");
    
        req.getRequestDispatcher("/WEB-INF/views/newStudent.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /create");

        String name = req.getParameter("name");
        String stdStr = req.getParameter("std");

        if (name == null || stdStr == null)
            throw new ServletException("Error: parameters missing.");
        int std = Integer.parseInt(stdStr);

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Student student = new Student();
            student.setName(name);
            student.setStd(std);
            em.persist(student);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        } finally {
            em.close();
        }

        String path = req.getContextPath();
        if(path != "") {
            resp.sendRedirect(path);
        } else {
            resp.sendRedirect("/");
        }
    }
}
